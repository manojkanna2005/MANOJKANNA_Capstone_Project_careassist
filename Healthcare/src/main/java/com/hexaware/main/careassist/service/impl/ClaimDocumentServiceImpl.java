package com.hexaware.main.careassist.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.hexaware.main.careassist.dto.ClaimDocumentDTO;
import com.hexaware.main.careassist.entity.Claim;
import com.hexaware.main.careassist.entity.ClaimDocument;
import com.hexaware.main.careassist.exception.BusinessValidationException;
import com.hexaware.main.careassist.exception.ResourceNotFoundException;
import com.hexaware.main.careassist.repository.ClaimDocumentRepository;
import com.hexaware.main.careassist.repository.ClaimRepository;
import com.hexaware.main.careassist.service.IClaimDocumentService;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class ClaimDocumentServiceImpl implements IClaimDocumentService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png");
    private static final int MAX_FILES_PER_CLAIM = 5;
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L;

    private final ClaimDocumentRepository documentRepository;
    private final ClaimRepository claimRepository;
    private final Path storageRoot;

    public ClaimDocumentServiceImpl(
            ClaimDocumentRepository documentRepository,
            ClaimRepository claimRepository,
            @Value("${app.claim-documents.storage-dir:uploads/claim-documents}") String storageDirectory) {
        this.documentRepository = documentRepository;
        this.claimRepository = claimRepository;
        this.storageRoot = Paths.get(storageDirectory).toAbsolutePath().normalize();
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to initialize claim document storage.", exception);
        }
    }

    @Override
    public List<ClaimDocumentDTO> storeDocuments(Integer claimId, List<MultipartFile> files) {
        Claim claim = getClaim(claimId);
        assertCanAccess(claim, true);

        if (files == null || files.isEmpty()) {
            throw new BusinessValidationException("documents", "At least one medical document is required.");
        }

        long existingCount = documentRepository.countByClaimClaimId(claimId);
        long nonEmptyCount = files.stream().filter(file -> file != null && !file.isEmpty()).count();
        if (nonEmptyCount == 0) {
            throw new BusinessValidationException("documents", "At least one medical document is required.");
        }
        if (existingCount + nonEmptyCount > MAX_FILES_PER_CLAIM) {
            throw new BusinessValidationException("documents", "A claim can contain at most 5 medical documents.");
        }

        List<Path> writtenFiles = new ArrayList<>();
        List<ClaimDocumentDTO> savedDocuments = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }

                validateFile(file);
                String originalName = StringUtils.cleanPath(
                        file.getOriginalFilename() == null ? "document" : file.getOriginalFilename());
                String extension = getExtension(originalName);
                String storedName = claimId + "-" + UUID.randomUUID() + "." + extension;
                Path target = storageRoot.resolve(storedName).normalize();

                if (!target.startsWith(storageRoot)) {
                    throw new BusinessValidationException("documents", "Invalid document file name.");
                }

                try (InputStream inputStream = file.getInputStream()) {
                    Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
                }
                writtenFiles.add(target);

                ClaimDocument document = new ClaimDocument();
                document.setClaim(claim);
                document.setOriginalFileName(originalName);
                document.setStoredFileName(storedName);
                document.setContentType(file.getContentType());
                document.setFileSize(file.getSize());
                document.setUploadedAt(LocalDateTime.now());

                savedDocuments.add(toDTO(documentRepository.save(document)));
            }
        } catch (IOException | RuntimeException exception) {
            writtenFiles.forEach(this::deleteFileQuietly);
            if (exception instanceof BusinessValidationException) {
                throw (BusinessValidationException) exception;
            }
            log.error("Unable to store medical documents for claimId={}", claimId, exception);
            throw new IllegalStateException("Unable to store medical documents. Please try again.", exception);
        }

        log.info("Stored {} medical document(s) for claimId={}", savedDocuments.size(), claimId);
        return savedDocuments;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimDocumentDTO> getDocuments(Integer claimId) {
        Claim claim = getClaim(claimId);
        assertCanAccess(claim, false);
        return documentRepository.findByClaimClaimIdOrderByUploadedAtAsc(claimId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClaimDocumentDTO getDocument(Integer documentId) {
        ClaimDocument document = getDocumentEntity(documentId);
        assertCanAccess(document.getClaim(), false);
        return toDTO(document);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource loadDocument(Integer documentId) {
        ClaimDocument document = getDocumentEntity(documentId);
        assertCanAccess(document.getClaim(), false);

        try {
            Path filePath = storageRoot.resolve(document.getStoredFileName()).normalize();
            if (!filePath.startsWith(storageRoot)) {
                throw new ResourceNotFoundException("Medical document was not found.");
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Medical document was not found.");
            }
            return resource;
        } catch (IOException exception) {
            throw new ResourceNotFoundException("Medical document was not found.");
        }
    }

    @Override
    public void deleteDocument(Integer documentId) {
        ClaimDocument document = getDocumentEntity(documentId);
        assertCanAccess(document.getClaim(), true);
        Path filePath = storageRoot.resolve(document.getStoredFileName()).normalize();
        documentRepository.delete(document);
        deleteFileQuietly(filePath);
        log.info("Deleted medical document documentId={} claimId={}", documentId, document.getClaim().getClaimId());
    }

    private void validateFile(MultipartFile file) {
        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String extension = getExtension(originalName);
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);

        if (originalName.isBlank() || originalName.length() > 255) {
            throw new BusinessValidationException(
                    "documents",
                    "Document file name must be between 1 and 255 characters.");
        }
        if (file.getSize() <= 0 || file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BusinessValidationException(
                    "documents",
                    "Each medical document must be larger than 0 bytes and no more than 5 MB.");
        }
        if (!ALLOWED_EXTENSIONS.contains(extension) || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessValidationException(
                    "documents",
                    "Only PDF, JPG, JPEG, and PNG medical documents are allowed.");
        }
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private Claim getClaim(Integer claimId) {
        return claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));
    }

    private ClaimDocument getDocumentEntity(Integer documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical document not found with id: " + documentId));
    }

    private void assertCanAccess(Claim claim, boolean writeAccess) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return;
        }

        String email = authentication.getName();
        Set<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(java.util.stream.Collectors.toSet());

        if (roles.contains("ROLE_ADMIN")) {
            return;
        }

        boolean patientOwner = claim.getPatient() != null
                && claim.getPatient().getAppUser() != null
                && email.equalsIgnoreCase(claim.getPatient().getAppUser().getEmail());
        boolean providerOwner = claim.getInvoice() != null
                && claim.getInvoice().getHealthcareProvider() != null
                && claim.getInvoice().getHealthcareProvider().getAppUser() != null
                && email.equalsIgnoreCase(claim.getInvoice().getHealthcareProvider().getAppUser().getEmail());
        boolean insuranceOwner = claim.getInsuranceCompany() != null
                && claim.getInsuranceCompany().getAppUser() != null
                && email.equalsIgnoreCase(claim.getInsuranceCompany().getAppUser().getEmail());

        boolean allowed = patientOwner || providerOwner || (!writeAccess && insuranceOwner);
        if (!allowed) {
            throw new AccessDeniedException("You are not allowed to access this claim document.");
        }
        if (writeAccess && !"PENDING".equalsIgnoreCase(claim.getStatus())) {
            throw new BusinessValidationException(
                    "documents",
                    "Medical documents cannot be added or removed after the claim decision.");
        }
    }

    private ClaimDocumentDTO toDTO(ClaimDocument document) {
        ClaimDocumentDTO dto = new ClaimDocumentDTO();
        dto.setDocumentId(document.getDocumentId());
        dto.setClaimId(document.getClaim().getClaimId());
        dto.setOriginalFileName(document.getOriginalFileName());
        dto.setContentType(document.getContentType());
        dto.setFileSize(document.getFileSize());
        dto.setUploadedAt(document.getUploadedAt());
        return dto;
    }

    private void deleteFileQuietly(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            log.warn("Unable to remove stored file path={}", filePath, exception);
        }
    }
}
