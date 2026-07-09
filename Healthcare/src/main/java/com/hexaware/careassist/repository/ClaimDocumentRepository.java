package com.hexaware.careassist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hexaware.careassist.entity.ClaimDocument;

public interface ClaimDocumentRepository extends JpaRepository<ClaimDocument, Integer> {
    List<ClaimDocument> findByClaimClaimIdOrderByUploadedAtAsc(Integer claimId);
    long countByClaimClaimId(Integer claimId);
    void deleteByClaimClaimId(Integer claimId);
}
