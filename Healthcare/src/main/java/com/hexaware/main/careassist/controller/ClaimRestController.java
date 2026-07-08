package com.hexaware.main.careassist.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hexaware.main.careassist.dto.ClaimDTO;
import com.hexaware.main.careassist.dto.ClaimDocumentDTO;
import com.hexaware.main.careassist.service.IClaimDocumentService;
import com.hexaware.main.careassist.service.IClaimService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/claims")
@RequiredArgsConstructor
public class ClaimRestController {

    private final IClaimService claimService;
    private final IClaimDocumentService claimDocumentService;

    @PostMapping("/submit")
    public ClaimDTO submitClaim(@Valid @RequestBody ClaimDTO dto) {
        return claimService.submitClaim(dto);
    }

    @PostMapping(value = "/submit-with-documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ClaimDTO submitClaimWithDocuments(
            @Valid @RequestPart("claim") ClaimDTO dto,
            @RequestPart("documents") List<MultipartFile> documents) {
        return claimService.submitClaimWithDocuments(dto, documents);
    }

    @PostMapping(value = "/{claimId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<ClaimDocumentDTO> uploadDocuments(
            @PathVariable Integer claimId,
            @RequestPart("documents") List<MultipartFile> documents) {
        return claimDocumentService.storeDocuments(claimId, documents);
    }

    @GetMapping("/{claimId}/documents")
    public List<ClaimDocumentDTO> getClaimDocuments(@PathVariable Integer claimId) {
        return claimDocumentService.getDocuments(claimId);
    }

    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Integer documentId) {
        ClaimDocumentDTO document = claimDocumentService.getDocument(documentId);
        Resource resource = claimDocumentService.loadDocument(documentId);
        String contentType = document.getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : document.getContentType();

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(document.getOriginalFileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }

    @DeleteMapping("/documents/{documentId}")
    public String deleteDocument(@PathVariable Integer documentId) {
        claimDocumentService.deleteDocument(documentId);
        return "Medical document deleted successfully";
    }

    @GetMapping("/{claimId}")
    public ClaimDTO getClaimById(@PathVariable Integer claimId) {
        return claimService.getClaimById(claimId);
    }

    @GetMapping("/patient/{patientId}")
    public List<ClaimDTO> getClaimsByPatientId(@PathVariable Integer patientId) {
        return claimService.getClaimsByPatientId(patientId);
    }

    @GetMapping("/company/{companyId}")
    public List<ClaimDTO> getClaimsByInsuranceCompanyId(@PathVariable Integer companyId) {
        return claimService.getClaimsByInsuranceCompanyId(companyId);
    }

    @GetMapping("/all")
    public List<ClaimDTO> getAllClaims() {
        return claimService.getAllClaims();
    }

    @PatchMapping("/approve/{claimId}")
    public ClaimDTO approveClaim(@PathVariable Integer claimId) {
        return claimService.approveClaim(claimId);
    }

    @PatchMapping("/reject/{claimId}")
    public ClaimDTO rejectClaim(
            @PathVariable Integer claimId,
            @RequestParam String rejectionReason) {
        return claimService.rejectClaim(claimId, rejectionReason);
    }

    @DeleteMapping("/delete/{claimId}")
    public String deleteClaim(@PathVariable Integer claimId) {
        claimService.deleteClaim(claimId);
        return "Claim deleted successfully";
    }
}
