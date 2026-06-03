package com.creotech.starter.autoconfigure;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.InitializingBean;

/**
 * Installs the OpenTelemetry Logback appender once the {@link OpenTelemetry} instance is
 * available, so that application logs are exported and correlated with traces.
 *
 * <p>A failure to install is logged as a warning and swallowed rather than failing
 * application startup: losing log export is a degraded-observability condition, not a
 * reason to take the application down.
 */
class InstallOpenTelemetryAppender implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstallOpenTelemetryAppender.class);

    private final OpenTelemetry openTelemetry;

    InstallOpenTelemetryAppender(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @Override
    public void afterPropertiesSet() {
        try {
            OpenTelemetryAppender.install(this.openTelemetry);
            LOGGER.info("Installed OpenTelemetry Logback appender; logs will be exported and correlated with traces");
        }
        catch (RuntimeException | LinkageError ex) {
            LOGGER.warn("Failed to install the OpenTelemetry Logback appender; "
                    + "application logs will not be exported via OpenTelemetry", ex);
        }
    }
}
