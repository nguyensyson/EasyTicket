package com.easytickets.infratructures.config;

import org.hibernate.Interceptor;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableJpaRepositories(basePackages = {"com.easytickets"}, repositoryImplementationPostfix = "JpaFragment")
@EntityScan(basePackages = {"com.easytickets"})
public class JpaConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null
                    || !authentication.isAuthenticated()
                    || authentication instanceof AnonymousAuthenticationToken) {
                return Optional.empty();
            }
            return Optional.ofNullable(authentication.getName());
        };
    }

    // Entities with a manually-assigned id (e.g. UserProfile, keyed off the Keycloak user id)
    // inherit BaseEntity's @GeneratedValue(UUID) id mapping, which makes Hibernate's own
    // transient/detached check assume any non-null id means "already persisted" and reject
    // persist() with a "Detached entity passed to persist" error. Bridging Persistable.isNew()
    // into the session Interceptor lets Hibernate trust that decision instead.
    @Bean
    public HibernatePropertiesCustomizer persistableAwareInterceptorCustomizer() {
        Interceptor interceptor = new Interceptor() {
            @Override
            public Boolean isTransient(Object entity) {
                return entity instanceof Persistable<?> persistable ? persistable.isNew() : null;
            }
        };
        return properties -> properties.put("hibernate.session_factory.interceptor", interceptor);
    }
}
