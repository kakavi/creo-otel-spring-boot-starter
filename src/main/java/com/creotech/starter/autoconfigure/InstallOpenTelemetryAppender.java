package com.creotech.starter.autoconfigure;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Installs the OpenTelemetry Logback appender once the {@link OpenTelemetry} instance is
 * available, so that application logs are exported and correlated with traces.
 *
 * <p>Spring Boot's OpenTelemetry logging auto-configuration builds the
 * {@code SdkLoggerProvider} but does <em>not</em> install the Logback appender — that is
 * this starter's responsibility, and without it log records are silently dropped.
 *
 * <p>The {@link OpenTelemetry} instance is resolved lazily through an
 * {@link ObjectProvider}: resolving at bean-instantiation time (rather than via a
 * condition or a hard constructor dependency) means this works regardless of
 * auto-configuration ordering, and degrades gracefully when no {@code OpenTelemetry}
 * bean is present. A failure to install is logged as a warning and swallowed rather than
 * failing application startup.
 */
class InstallOpenTelemetryAppender implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstallOpenTelemetryAppender.class);

    private final ObjectProvider<OpenTelemetry> openTelemetry;

    InstallOpenTelemetryAppender(ObjectProvider<OpenTelemetry> openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @Override
    public void afterPropertiesSet() {
        OpenTelemetry instance = this.openTelemetry.getIfAvailable();
        if (instance == null) {
            LOGGER.debug("No OpenTelemetry bean available; OpenTelemetry Logback appender not installed, "
                    + "so application logs will not be exported via OpenTelemetry");
            return;
        }
        try {
            OpenTelemetryAppender.install(instance);
            LOGGER.info("Installed OpenTelemetry Logback appender; logs will be exported and correlated with traces");
        }
        catch (RuntimeException | LinkageError ex) {
            LOGGER.warn("Failed to install the OpenTelemetry Logback appender; "
                    + "application logs will not be exported via OpenTelemetry", ex);
        }
    }
}
