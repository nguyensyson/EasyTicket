package com.easytickets.application.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "url")
@Getter
@Setter
public class SecurityProperties {

    private List<ApiPath> permit = new ArrayList<>();

    @Getter
    @Setter
    public static class ApiPath {
        private String path;
        private List<String> methods = new ArrayList<>();
    }
}
