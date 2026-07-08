package com.hexaware.main.careassist.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hexaware.main.careassist.entity.TokenBlacklist;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Integer> {
    boolean existsByToken(String token);
    List<TokenBlacklist> findByInvalidatedAtBefore(LocalDateTime dateTime);
}