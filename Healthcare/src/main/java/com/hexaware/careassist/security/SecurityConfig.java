package com.hexaware.careassist.security;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final String allowedOrigins;
    private final String frontendUrl;

    public SecurityConfig(
            JwtFilter jwtFilter,
            JwtAuthenticationEntryPoint authenticationEntryPoint,
            JwtAccessDeniedHandler accessDeniedHandler,
            CustomOAuth2UserService customOAuth2UserService,
            OAuth2SuccessHandler oAuth2SuccessHandler,
            @Value("${app.cors.allowed-origins}") String allowedOrigins,
            @Value("${app.frontend-url}") String frontendUrl) {

        this.jwtFilter = jwtFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.allowedOrigins = allowedOrigins;
        this.frontendUrl = removeTrailingSlash(frontendUrl);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/users/*/account"
                        ).hasAnyRole(
                                "PATIENT",
                                "PROVIDER",
                                "INSURANCE",
                                "ADMIN"
                        )

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/users/*/change-password"
                        ).hasAnyRole(
                                "PATIENT",
                                "PROVIDER",
                                "INSURANCE",
                                "ADMIN"
                        )

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/users/*/account"
                        ).hasAnyRole(
                                "PATIENT",
                                "PROVIDER",
                                "INSURANCE",
                                "ADMIN"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/users/*/profile-picture"
                        ).hasAnyRole(
                                "PATIENT",
                                "PROVIDER",
                                "INSURANCE",
                                "ADMIN"
                        )

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/users/*/profile-picture"
                        ).hasAnyRole(
                                "PATIENT",
                                "PROVIDER",
                                "INSURANCE",
                                "ADMIN"
                        )

                        .requestMatchers("/api/v1/admin/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/v1/users/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/v1/tokens/**")
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/providers/all",
                                "/api/v1/providers/*"
                        ).hasAnyRole(
                                "PATIENT",
                                "PROVIDER",
                                "INSURANCE",
                                "ADMIN"
                        )

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/patients/all",
                                "/api/v1/patients/*"
                        ).hasAnyRole(
                                "PROVIDER",
                                "INSURANCE",
                                "ADMIN"
                        )

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/insurance-companies/all",
                                "/api/v1/insurance-companies/*"
                        ).hasAnyRole(
                                "PATIENT",
                                "PROVIDER",
                                "INSURANCE",
                                "ADMIN"
                        )

                        .requestMatchers("/api/v1/patients/**")
                        .hasAnyRole("PATIENT", "ADMIN")

                        .requestMatchers("/api/v1/providers/**")
                        .hasAnyRole("PROVIDER", "ADMIN")

                        .requestMatchers("/api/v1/insurance-companies/**")
                        .hasAnyRole("INSURANCE", "ADMIN")

                        .requestMatchers("/api/v1/insurance-plans/**")
                        .hasAnyRole(
                                "INSURANCE",
                                "PATIENT",
                                "PROVIDER",
                                "ADMIN"
                        )

                        .requestMatchers("/api/v1/patient-insurance/**")
                        .hasAnyRole("PATIENT", "ADMIN")

                        .requestMatchers("/api/v1/claims/**")
                        .hasAnyRole(
                                "PATIENT",
                                "PROVIDER",
                                "INSURANCE",
                                "ADMIN"
                        )

                        .requestMatchers("/api/v1/invoices/**")
                        .hasAnyRole(
                                "PATIENT",
                                "PROVIDER",
                                "ADMIN"
                        )

                        .requestMatchers("/api/v1/claim-payments/**")
                        .hasAnyRole(
                                "INSURANCE",
                                "ADMIN"
                        )

                        .requestMatchers("/api/v1/invoice-payments/**")
                        .hasAnyRole(
                                "PATIENT",
                                "ADMIN"
                        )

                        .requestMatchers("/api/v1/email-notifications/**")
                        .hasAnyRole(
                                "PATIENT",
                                "PROVIDER",
                                "INSURANCE",
                                "ADMIN"
                        )

                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            String error = URLEncoder.encode(
                                    exception.getMessage() == null
                                            ? "GitHub login failed"
                                            : exception.getMessage(),
                                    StandardCharsets.UTF_8
                            );

                            response.sendRedirect(
                                    frontendUrl + "/login?oauthError=" + error
                            );
                        })
                );

        http.addFilterBefore(
                jwtFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();

        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH",
                        "OPTIONS"
                )
        );
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(
                List.of(
                        "Authorization",
                        "Content-Disposition"
                )
        );
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
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
