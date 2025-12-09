package ru.alta.thirdproj.config;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Only wrap and log potentially problematic endpoints to avoid noise.
        boolean shouldLogBody = "/margin/userAct/update".equalsIgnoreCase(request.getRequestURI());

        HttpServletRequest requestToUse = request;
        if (shouldLogBody && !(request instanceof ContentCachingRequestWrapper)) {
            requestToUse = new ContentCachingRequestWrapper(request);
        }

        log.info("[RequestLog] {} {}", request.getMethod(), request.getRequestURI());

        try {
            filterChain.doFilter(requestToUse, response);
        } finally {
            if (shouldLogBody && requestToUse instanceof ContentCachingRequestWrapper) {
                ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) requestToUse;
                byte[] buf = wrapper.getContentAsByteArray();
                if (buf.length > 0) {
                    String payload = new String(buf, 0, buf.length, StandardCharsets.UTF_8);
                    log.info("[RequestLog] Body: {}", payload);
                } else {
                    log.info("[RequestLog] Body: <empty>");
                }
            }

            log.info("[RequestLog] Response status {} for {}", response.getStatus(), request.getRequestURI());
        }
    }
}
