package com.hexaware.careassist.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.hexaware.careassist.entity.AppUser;
import com.hexaware.careassist.repository.AppUserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AppUserRepository appUserRepository;
    private final JwtUtil jwtUtil;
    private final String frontendUrl;

    public OAuth2SuccessHandler(AppUserRepository appUserRepository,
                                JwtUtil jwtUtil,
                                @Value("${app.frontend-url}") String frontendUrl) {
        this.appUserRepository = appUserRepository;
        this.jwtUtil = jwtUtil;
        this.frontendUrl = removeTrailingSlash(frontendUrl);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("careassist_email");

        AppUser user = appUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated CareAssist user was not found"
                ));

        String token = jwtUtil.generateToken(user);
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);

        clearAuthenticationAttributes(request);

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect(
                frontendUrl + "/oauth-success#token=" + encodedToken
        );
    }

    private static String removeTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "http://localhost:5173";
        }

        return value.endsWith("/")
                ? value.substring(0, value.length() - 1)
                : value;
    }
}
