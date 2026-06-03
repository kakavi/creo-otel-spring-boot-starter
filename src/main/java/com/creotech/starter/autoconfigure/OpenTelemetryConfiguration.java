package com.creotech.starter.autoconfigure;

import java.util.List;

import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmClassLoadingMeterConventions;
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmCpuMeterConventions;
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmMemoryMeterConventions;
import io.micrometer.core.instrument.binder.jvm.convention.otel.OpenTelemetryJvmThreadMeterConventions;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.opentelemetry.api.OpenTelemetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.server.observation.OpenTelemetryServerRequestObservationConvention;

/**
 * Wires OpenTelemetry conveniences: installs the Logback OTel appender, registers the
 * OpenTelemetry server-request observation convention, and replaces the default JVM /
 * system metrics binders with ones that use OpenTelemetry semantic conventions.
 *
 * <p>The metrics binders are declared {@link ConditionalOnMissingBean} and this
 * configuration is ordered before Actuator's own JVM/system metrics auto-configurations
 * so that ours win deterministically while still yielding to any beans the application
 * defines itself.
 */
@AutoConfiguration(beforeName = {
        "org.springframework.boot.actuate.autoconfigure.metrics.JvmMetricsAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.metrics.SystemMetricsAutoConfiguration" })
@ConditionalOnClass({ OpenTelemetry.class, ProcessorMetrics.class, JvmMemoryMetrics.class, JvmThreadMetrics.class,
        ClassLoaderMetrics.class, OpenTelemetryServerRequestObservationConvention.class })
@EnableConfigurationProperties(CreoOtelProperties.class)
public class OpenTelemetryConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenTelemetryConfiguration.class);

    @Bean
    @ConditionalOnBean(OpenTelemetry.class)
    @ConditionalOnMissingBean
    InstallOpenTelemetryAppender installOpenTelemetryAppender(OpenTelemetry openTelemetry) {
        return new InstallOpenTelemetryAppender(openTelemetry);
    }

    @Bean
    @ConditionalOnMissingBean
    OpenTelemetryServerRequestObservationConvention openTelemetryServerRequestObservationConvention() {
        LOGGER.debug("Registered OpenTelemetryServerRequestObservationConvention");
        return new OpenTelemetryServerRequestObservationConvention();
    }

    @Bean
    @ConditionalOnProperty(prefix = "creo.otel.metrics", name = "enabled", havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean
    ProcessorMetrics processorMetrics() {
        LOGGER.debug("Registered ProcessorMetrics with OpenTelemetry semantic conventions");
        return new ProcessorMetrics(List.of(), new OpenTelemetryJvmCpuMeterConventions(Tags.empty()));
    }

    @Bean
    @ConditionalOnProperty(prefix = "creo.otel.metrics", name = "enabled", havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean
    JvmMemoryMetrics jvmMemoryMetrics() {
        LOGGER.debug("Registered JvmMemoryMetrics with OpenTelemetry semantic conventions");
        return new JvmMemoryMetrics(List.of(), new OpenTelemetryJvmMemoryMeterConventions(Tags.empty()));
    }

    @Bean
    @ConditionalOnProperty(prefix = "creo.otel.metrics", name = "enabled", havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean
    JvmThreadMetrics jvmThreadMetrics() {
        LOGGER.debug("Registered JvmThreadMetrics with OpenTelemetry semantic conventions");
        return new JvmThreadMetrics(List.of(), new OpenTelemetryJvmThreadMeterConventions(Tags.empty()));
    }

    @Bean
    @ConditionalOnProperty(prefix = "creo.otel.metrics", name = "enabled", havingValue = "true",
            matchIfMissing = true)
    @ConditionalOnMissingBean
    ClassLoaderMetrics classLoaderMetrics() {
        LOGGER.debug("Registered ClassLoaderMetrics with OpenTelemetry semantic conventions");
        return new ClassLoaderMetrics(new OpenTelemetryJvmClassLoadingMeterConventions());
    }
}
