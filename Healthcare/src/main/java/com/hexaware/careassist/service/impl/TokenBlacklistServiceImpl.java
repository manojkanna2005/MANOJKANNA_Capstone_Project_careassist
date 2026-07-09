package com.hexaware.careassist.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.careassist.dto.TokenBlacklistDTO;
import com.hexaware.careassist.entity.TokenBlacklist;
import com.hexaware.careassist.repository.TokenBlacklistRepository;
import com.hexaware.careassist.service.ITokenBlacklistService;

@Service
@Transactional
public class TokenBlacklistServiceImpl implements ITokenBlacklistService {

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Override
    public TokenBlacklistDTO blacklistToken(TokenBlacklistDTO dto) {
        if (isTokenBlacklisted(dto.getToken())) {
            return dto;
        }

        TokenBlacklist token = toEntity(dto);

        if (token.getInvalidatedAt() == null) {
            token.setInvalidatedAt(LocalDateTime.now());
        }

        return toDTO(tokenBlacklistRepository.save(token));
    }

    @Override
    public TokenBlacklistDTO blacklistToken(String token) {
        TokenBlacklistDTO dto = new TokenBlacklistDTO();
        dto.setToken(token);
        dto.setInvalidatedAt(LocalDateTime.now());
        return blacklistToken(dto);
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }

    @Override
    public List<TokenBlacklistDTO> getAllBlacklistedTokens() {
        return tokenBlacklistRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteExpiredTokens(LocalDateTime beforeDateTime) {
        List<TokenBlacklist> expiredTokens = tokenBlacklistRepository.findByInvalidatedAtBefore(beforeDateTime);
        tokenBlacklistRepository.deleteAll(expiredTokens);
    }

    private TokenBlacklistDTO toDTO(TokenBlacklist token) {
        TokenBlacklistDTO dto = new TokenBlacklistDTO();
        dto.setId(token.getId());
        dto.setToken(token.getToken());
        dto.setInvalidatedAt(token.getInvalidatedAt());
        return dto;
    }

    private TokenBlacklist toEntity(TokenBlacklistDTO dto) {
        TokenBlacklist token = new TokenBlacklist();
        token.setId(dto.getId());
        token.setToken(dto.getToken());
        token.setInvalidatedAt(dto.getInvalidatedAt());
        return token;
    }
}