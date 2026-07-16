package com.easytickets.application.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecurityProperties securityProperties;
    private final CustomJwtAuthenticationConverter customJwtAuthenticationConverter;
    private final JsonSecurityErrorHandler jsonSecurityErrorHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    for (SecurityProperties.ApiPath apiPath : securityProperties.getPermit()) {
                        if (apiPath.getMethods() == null || apiPath.getMethods().isEmpty()) {
                            auth.requestMatchers(apiPath.getPath()).permitAll();
                        } else {
                            for (String method : apiPath.getMethods()) {
                                auth.requestMatchers(HttpMethod.valueOf(method), apiPath.getPath()).permitAll();
                            }
                        }
                    }
                    auth.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(customJwtAuthenticationConverter)))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(jsonSecurityErrorHandler)
                        .accessDeniedHandler(jsonSecurityErrorHandler));

        return http.build();
    }
}
