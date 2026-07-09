package com.hexaware.main.careassist.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import com.hexaware.careassist.dto.ClaimDocumentDTO;
import com.hexaware.careassist.entity.Claim;
import com.hexaware.careassist.entity.ClaimDocument;
import com.hexaware.careassist.exception.BusinessValidationException;
import com.hexaware.careassist.repository.ClaimDocumentRepository;
import com.hexaware.careassist.repository.ClaimRepository;
import com.hexaware.careassist.service.impl.ClaimDocumentServiceImpl;

class ClaimDocumentServiceImplMockitoTest {

    @TempDir
    Path tempDirectory;

    @Test
    void storesValidMedicalDocument() throws Exception {
        ClaimRepository claimRepository = mock(ClaimRepository.class);
        ClaimDocumentRepository documentRepository = mock(ClaimDocumentRepository.class);
        Claim claim = new Claim();
        claim.setClaimId(10);

        when(claimRepository.findById(10)).thenReturn(Optional.of(claim));
        when(documentRepository.countByClaimClaimId(10)).thenReturn(0L);
        when(documentRepository.save(any(ClaimDocument.class))).thenAnswer(invocation -> {
            ClaimDocument document = invocation.getArgument(0);
            document.setDocumentId(20);
            return document;
        });

        ClaimDocumentServiceImpl service = new ClaimDocumentServiceImpl(
                documentRepository,
                claimRepository,
                tempDirectory.toString());

        MockMultipartFile file = new MockMultipartFile(
                "documents",
                "report.pdf",
                "application/pdf",
                "%PDF-test".getBytes());

        List<ClaimDocumentDTO> saved = service.storeDocuments(10, List.of(file));

        assertEquals(1, saved.size());
        assertEquals("report.pdf", saved.get(0).getOriginalFileName());
        assertEquals(1L, Files.list(tempDirectory).count());
    }

    @Test
    void rejectsUnsupportedDocumentType() {
        ClaimRepository claimRepository = mock(ClaimRepository.class);
        ClaimDocumentRepository documentRepository = mock(ClaimDocumentRepository.class);
        Claim claim = new Claim();
        claim.setClaimId(10);

        when(claimRepository.findById(10)).thenReturn(Optional.of(claim));
        when(documentRepository.countByClaimClaimId(10)).thenReturn(0L);

        ClaimDocumentServiceImpl service = new ClaimDocumentServiceImpl(
                documentRepository,
                claimRepository,
                tempDirectory.toString());

        MockMultipartFile file = new MockMultipartFile(
                "documents",
                "notes.txt",
                "text/plain",
                "not allowed".getBytes());

        assertThrows(
                BusinessValidationException.class,
                () -> service.storeDocuments(10, List.of(file)));
    }
}
