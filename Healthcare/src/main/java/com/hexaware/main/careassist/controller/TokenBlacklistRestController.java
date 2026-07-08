package com.hexaware.main.careassist.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hexaware.main.careassist.dto.TokenBlacklistDTO;
import com.hexaware.main.careassist.service.ITokenBlacklistService;

import jakarta.validation.Valid;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/v1/tokens")
public class TokenBlacklistRestController {

	@Autowired
	private ITokenBlacklistService tokenBlacklistService;

	@PostMapping("/blacklist")
	public TokenBlacklistDTO blacklistToken(@Valid @RequestBody TokenBlacklistDTO dto) {
		return tokenBlacklistService.blacklistToken(dto);
	}

	@PostMapping("/blacklist-token")
	public TokenBlacklistDTO blacklistTokenValue(@RequestParam String token) {
		return tokenBlacklistService.blacklistToken(token);
	}

	@GetMapping("/check")
	public boolean isTokenBlacklisted(@RequestParam String token) {
		return tokenBlacklistService.isTokenBlacklisted(token);
	}

	@GetMapping("/all")
	public List<TokenBlacklistDTO> getAllBlacklistedTokens() {
		return tokenBlacklistService.getAllBlacklistedTokens();
	}

	@DeleteMapping("/expired")
	public String deleteExpiredTokens(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beforeDateTime) {

		tokenBlacklistService.deleteExpiredTokens(beforeDateTime);
		return "Expired tokens deleted successfully";
	}
}
