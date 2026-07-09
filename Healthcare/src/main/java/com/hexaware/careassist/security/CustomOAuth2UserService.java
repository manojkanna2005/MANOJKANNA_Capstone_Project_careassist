package com.hexaware.careassist.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.hexaware.careassist.entity.AppUser;
import com.hexaware.careassist.repository.AppUserRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final String GITHUB_EMAILS_URL = "https://api.github.com/user/emails";

    private final AppUserRepository appUserRepository;
    private final JwtUtil jwtUtil;
    private final RestClient restClient;

    public CustomOAuth2UserService(AppUserRepository appUserRepository,
                                   JwtUtil jwtUtil) {
        this.appUserRepository = appUserRepository;
        this.jwtUtil = jwtUtil;
        this.restClient = RestClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader(HttpHeaders.USER_AGENT, "CareAssist")
                .build();
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User githubUser = super.loadUser(userRequest);

        String email = githubUser.getAttribute("email");

        if (!StringUtils.hasText(email)) {
            email = loadPrimaryVerifiedEmail(
                    userRequest.getAccessToken().getTokenValue()
            );
        }

        if (!StringUtils.hasText(email)) {
            throw oauthError(
                    "github_email_missing",
                    "GitHub did not provide a verified email address."
            );
        }

        final String resolvedEmail = email;

        AppUser careAssistUser = appUserRepository
                .findByEmailIgnoreCase(resolvedEmail)
                .orElseThrow(() -> oauthError(
                        "careassist_account_missing",
                        "No CareAssist account exists for the GitHub email: " + resolvedEmail
                ));

        if (!careAssistUser.isActive()) {
            throw oauthError(
                    "careassist_account_disabled",
                    "This CareAssist account is disabled."
            );
        }

        String normalizedRole = jwtUtil.normalizeRole(careAssistUser.getRole());

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + normalizedRole));

        Map<String, Object> attributes = new HashMap<>(githubUser.getAttributes());
        attributes.put("careassist_email", careAssistUser.getEmail());
        attributes.put("careassist_user_id", careAssistUser.getUserId());
        attributes.put("careassist_role", normalizedRole);

        return new DefaultOAuth2User(authorities, attributes, "id");
    }

    private String loadPrimaryVerifiedEmail(String accessToken) {
        try {
            List<Map<String, Object>> emails = restClient.get()
                    .uri(GITHUB_EMAILS_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (emails == null || emails.isEmpty()) {
                return null;
            }

            return emails.stream()
                    .filter(item -> Boolean.TRUE.equals(item.get("verified")))
                    .filter(item -> Boolean.TRUE.equals(item.get("primary")))
                    .map(item -> item.get("email"))
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .filter(StringUtils::hasText)
                    .findFirst()
                    .orElseGet(() -> emails.stream()
                            .filter(item -> Boolean.TRUE.equals(item.get("verified")))
                            .map(item -> item.get("email"))
                            .filter(String.class::isInstance)
                            .map(String.class::cast)
                            .filter(StringUtils::hasText)
                            .findFirst()
                            .orElse(null));

        } catch (Exception exception) {
            throw oauthError(
                    "github_email_lookup_failed",
                    "Unable to read the verified email address from GitHub."
            );
        }
    }

    private OAuth2AuthenticationException oauthError(String code,
                                                       String description) {
        return new OAuth2AuthenticationException(
                new OAuth2Error(code),
                description
        );
    }
}
