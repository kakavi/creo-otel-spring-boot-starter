package com.creotech.starter.autoconfigure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Logs incoming HTTP request headers at DEBUG level, with the values of sensitive
 * headers redacted.
 *
 * <p>Redaction is mandatory rather than optional: this starter also installs the
 * OpenTelemetry log appender, so anything logged here may be shipped to a remote
 * observability backend. Header names to redact are supplied at construction time.
 */
class HeaderLoggerFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeaderLoggerFilter.class);

    private static final String REDACTED = "***";

    /** Lower-cased header names whose values must never be logged. */
    private final Set<String> redactedHeaders;

    HeaderLoggerFilter(List<String> redactedHeaders) {
        this.redactedHeaders = redactedHeaders.stream()
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Avoid touching the headers at all unless DEBUG is actually enabled.
        if (LOGGER.isDebugEnabled()) {
            logHeaders(request);
        }
        filterChain.doFilter(request, response);
    }

    private void logHeaders(HttpServletRequest request) {
        request.getHeaderNames().asIterator().forEachRemaining(header -> {
            if (isRedacted(header)) {
                LOGGER.debug("{}: {}", header, REDACTED);
                return;
            }
            List<String> values = new ArrayList<>();
            request.getHeaders(header).asIterator().forEachRemaining(values::add);
            LOGGER.debug("{}: {}", header, values.size() == 1 ? values.getFirst() : values);
        });
    }

    private boolean isRedacted(String headerName) {
        return this.redactedHeaders.contains(headerName.toLowerCase(Locale.ROOT));
    }
}
