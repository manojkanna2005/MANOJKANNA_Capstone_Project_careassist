package com.hexaware.main.careassist.service;

import java.time.LocalDateTime;
import java.util.List;

import com.hexaware.main.careassist.dto.TokenBlacklistDTO;

public interface ITokenBlacklistService {

	TokenBlacklistDTO blacklistToken(TokenBlacklistDTO dto);

	TokenBlacklistDTO blacklistToken(String token);

	boolean isTokenBlacklisted(String token);

	List<TokenBlacklistDTO> getAllBlacklistedTokens();

	void deleteExpiredTokens(LocalDateTime beforeDateTime);
}