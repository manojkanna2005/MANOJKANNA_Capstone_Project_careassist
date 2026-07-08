package com.hexaware.main.careassist.service;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.hexaware.main.careassist.dto.ClaimDocumentDTO;

public interface IClaimDocumentService {
    List<ClaimDocumentDTO> storeDocuments(Integer claimId, List<MultipartFile> files);
    List<ClaimDocumentDTO> getDocuments(Integer claimId);
    ClaimDocumentDTO getDocument(Integer documentId);
    Resource loadDocument(Integer documentId);
    void deleteDocument(Integer documentId);
}
