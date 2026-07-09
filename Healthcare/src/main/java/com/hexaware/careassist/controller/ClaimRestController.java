package com.hexaware.careassist.controller;

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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import com.hexaware.careassist.dto.ClaimApprovalRequest;
import com.hexaware.careassist.dto.ClaimDTO;
import com.hexaware.careassist.dto.ClaimDocumentDTO;
import com.hexaware.careassist.dto.ClaimRejectionRequest;
import com.hexaware.careassist.service.IClaimDocumentService;
import com.hexaware.careassist.service.IClaimService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@Validated
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
            @PathVariable @Positive Integer claimId,
            @RequestPart("documents") List<MultipartFile> documents) {
        return claimDocumentService.storeDocuments(claimId, documents);
    }

    @GetMapping("/{claimId}/documents")
    public List<ClaimDocumentDTO> getClaimDocuments(@PathVariable @Positive Integer claimId) {
        return claimDocumentService.getDocuments(claimId);
    }

    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable @Positive Integer documentId) {
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
    public String deleteDocument(@PathVariable @Positive Integer documentId) {
        claimDocumentService.deleteDocument(documentId);
        return "Medical document deleted successfully";
    }

    @GetMapping("/{claimId}")
    public ClaimDTO getClaimById(@PathVariable @Positive Integer claimId) {
        return claimService.getClaimById(claimId);
    }

    @GetMapping("/patient/{patientId}")
    public List<ClaimDTO> getClaimsByPatientId(@PathVariable @Positive Integer patientId) {
        return claimService.getClaimsByPatientId(patientId);
    }

    @GetMapping("/company/{companyId}")
    public List<ClaimDTO> getClaimsByInsuranceCompanyId(@PathVariable @Positive Integer companyId) {
        return claimService.getClaimsByInsuranceCompanyId(companyId);
    }


    @GetMapping("/company/{companyId}/actionable")
    public List<ClaimDTO> getActionableClaimsByInsuranceCompanyId(
            @PathVariable @Positive Integer companyId) {
        return claimService.getActionableClaimsByInsuranceCompanyId(companyId);
    }

    @GetMapping("/all")
    public List<ClaimDTO> getAllClaims() {
        return claimService.getAllClaims();
    }

    @PatchMapping("/approve/{claimId}")
    public ClaimDTO approveClaim(
            @PathVariable @Positive Integer claimId,
            @Valid @RequestBody ClaimApprovalRequest request) {
        return claimService.approveClaim(claimId, request.getApprovedAmount());
    }

    @PatchMapping("/reject/{claimId}")
    public ClaimDTO rejectClaim(
            @PathVariable @Positive Integer claimId,
            @Valid @RequestBody ClaimRejectionRequest request) {
        return claimService.rejectClaim(claimId, request.getRejectionReason());
    }

    @DeleteMapping("/delete/{claimId}")
    public String deleteClaim(@PathVariable @Positive Integer claimId) {
        claimService.deleteClaim(claimId);
        return "Claim deleted successfully";
    }
}
