package com.creotech.starter.autoconfigure;

import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.server.observation.OpenTelemetryServerRequestObservationConvention;

import static org.assertj.core.api.Assertions.assertThat;

class OpenTelemetryConfigurationTests {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpenTelemetryConfiguration.class));

    @Test
    void metricsBindersAndConventionArePresentByDefault() {
        this.runner.run(context -> assertThat(context)
                .hasSingleBean(ProcessorMetrics.class)
                .hasSingleBean(JvmMemoryMetrics.class)
                .hasSingleBean(JvmThreadMetrics.class)
                .hasSingleBean(ClassLoaderMetrics.class)
                .hasSingleBean(OpenTelemetryServerRequestObservationConvention.class));
    }

    @Test
    void metricsBindersBackOffWhenDisabled() {
        this.runner.withPropertyValues("creo.otel.metrics.enabled=false")
                .run(context -> assertThat(context)
                        .doesNotHaveBean(ProcessorMetrics.class)
                        .doesNotHaveBean(JvmMemoryMetrics.class)
                        .doesNotHaveBean(JvmThreadMetrics.class)
                        .doesNotHaveBean(ClassLoaderMetrics.class));
    }

    @Test
    void appenderInstallerRequiresAnOpenTelemetryBean() {
        this.runner.run(context -> assertThat(context).doesNotHaveBean(InstallOpenTelemetryAppender.class));
    }

    @Test
    void appenderInstallerIsRegisteredWhenOpenTelemetryBeanPresent() {
        this.runner.withBean(OpenTelemetry.class, OpenTelemetry::noop)
                .run(context -> assertThat(context).hasSingleBean(InstallOpenTelemetryAppender.class));
    }

    @Test
    void applicationDefinedBinderWins() {
        this.runner.withBean("processorMetrics", ProcessorMetrics.class, ProcessorMetrics::new)
                .run(context -> assertThat(context).getBean(ProcessorMetrics.class)
                        .isSameAs(context.getBean("processorMetrics")));
    }
}
