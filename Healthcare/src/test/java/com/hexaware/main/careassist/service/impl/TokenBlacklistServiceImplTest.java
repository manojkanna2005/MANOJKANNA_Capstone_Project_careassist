package com.hexaware.main.careassist.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.main.careassist.dto.TokenBlacklistDTO;
import com.hexaware.main.careassist.service.ITokenBlacklistService;

@SpringBootTest
@Transactional
class TokenBlacklistServiceImplTest {

    @Autowired
    private ITokenBlacklistService tokenBlacklistService;

    @Test
    void blacklistTokenTest() {
        TokenBlacklistDTO dto = new TokenBlacklistDTO();
        dto.setToken("token-" + UUID.randomUUID());
        dto.setInvalidatedAt(LocalDateTime.now());

        TokenBlacklistDTO savedToken = tokenBlacklistService.blacklistToken(dto);

        assertNotNull(savedToken);
        assertTrue(savedToken.getId() > 0);
        assertTrue(tokenBlacklistService.isTokenBlacklisted(savedToken.getToken()));
    }
}
