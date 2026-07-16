package com.easytickets.business.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Order Service's {@code /my-tickets} and Event Service's {@code /organizer-history}
 * both scope their result to the caller identified by JWT, so the caller's own JWT is
 * forwarded rather than bypassing authentication with a service account.
 */
public class FeignClientConfig implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public void apply(RequestTemplate template) {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization != null) {
            template.header(AUTHORIZATION_HEADER, authorization);
        }
    }
}
