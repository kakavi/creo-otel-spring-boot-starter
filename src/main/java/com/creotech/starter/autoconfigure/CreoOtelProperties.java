package com.creotech.starter.autoconfigure;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Creo OpenTelemetry starter.
 *
 * <p>All features can be toggled individually. Defaults are chosen to be safe in
 * production: anything that could disclose information (request-header logging,
 * exposing trace IDs to clients) is disabled by default and must be opted into.
 */
@ConfigurationProperties(prefix = "creo.otel")
public class CreoOtelProperties {

    private final ContextPropagation contextPropagation = new ContextPropagation();
    private final HeaderLogging headerLogging = new HeaderLogging();
    private final TraceId traceId = new TraceId();
    private final Metrics metrics = new Metrics();

    public ContextPropagation getContextPropagation() {
        return this.contextPropagation;
    }

    public HeaderLogging getHeaderLogging() {
        return this.headerLogging;
    }

    public TraceId getTraceId() {
        return this.traceId;
    }

    public Metrics getMetrics() {
        return this.metrics;
    }

    /** Async context propagation for {@code TaskExecutor}-based work. */
    public static class ContextPropagation {

        /** Whether to register a {@code ContextPropagatingTaskDecorator}. */
        private boolean enabled = true;

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * Incoming request-header logging (DEBUG level).
     *
     * <p>Disabled by default: header values frequently contain credentials, and this
     * starter also installs the OpenTelemetry log appender, which would export those
     * values to the observability backend. When enabled, sensitive headers are
     * redacted using {@link #getRedactedHeaders()}.
     */
    public static class HeaderLogging {

        /** Whether to register the header-logging filter. */
        private boolean enabled = false;

        /**
         * Header names (case-insensitive) whose values are replaced with a redaction
         * marker before logging. Defaults cover the most common credential-bearing
         * headers.
         */
        private List<String> redactedHeaders = List.of(
                "authorization",
                "proxy-authorization",
                "cookie",
                "set-cookie",
                "x-api-key",
                "api-key",
                "x-auth-token",
                "x-csrf-token");

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getRedactedHeaders() {
            return this.redactedHeaders;
        }

        public void setRedactedHeaders(List<String> redactedHeaders) {
            this.redactedHeaders = redactedHeaders;
        }
    }

    /**
     * Exposing the current trace ID to clients via a response header.
     *
     * <p>Disabled by default: surfacing internal trace IDs to arbitrary clients is an
     * information-disclosure decision that should be made deliberately (e.g. only for
     * internal services or behind a trusted gateway).
     */
    public static class TraceId {

        /** Whether to add the trace-id response header. */
        private boolean enabled = false;

        /** Name of the response header carrying the current trace ID. */
        private String headerName = "X-Trace-Id";

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getHeaderName() {
            return this.headerName;
        }

        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }
    }

    /** JVM/system metrics published with OpenTelemetry semantic conventions. */
    public static class Metrics {

        /**
         * Whether to register JVM/system metrics binders that use OpenTelemetry
         * semantic conventions in place of the Micrometer defaults.
         */
        private boolean enabled = true;

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
