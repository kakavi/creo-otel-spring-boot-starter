package com.creotech.starter.autoconfigure;

import java.io.IOException;

import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Adds the current trace ID to the response under a configurable header name.
 *
 * <p>The header is written before the filter chain proceeds (i.e. before the response
 * is committed). This filter must run <em>inside</em> the tracing/observation filter so
 * that a trace context is current; when no context is available the header is omitted.
 */
class AddTraceIdFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddTraceIdFilter.class);

    private final Tracer tracer;
    private final String headerName;

    AddTraceIdFilter(Tracer tracer, String headerName) {
        this.tracer = tracer;
        this.headerName = headerName;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = getTraceId();
        if (traceId != null && !response.isCommitted()) {
            response.setHeader(this.headerName, traceId);
        }
        else if (traceId == null && LOGGER.isTraceEnabled()) {
            // Usually means this filter ran before the tracing/observation filter opened a scope.
            LOGGER.trace("No current trace context for {} {}; '{}' response header not set",
                    request.getMethod(), request.getRequestURI(), this.headerName);
        }
        filterChain.doFilter(request, response);
    }

    private String getTraceId() {
        TraceContext context = this.tracer.currentTraceContext().context();
        return context != null ? context.traceId() : null;
    }
}
