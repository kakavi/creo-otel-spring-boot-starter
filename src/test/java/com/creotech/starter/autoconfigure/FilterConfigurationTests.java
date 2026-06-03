package com.creotech.starter.autoconfigure;

import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class FilterConfigurationTests {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FilterConfiguration.class))
            .withBean(Tracer.class, () -> Mockito.mock(Tracer.class));

    @Test
    void filtersAreDisabledByDefault() {
        this.runner.run(context -> assertThat(context)
                .doesNotHaveBean("headerLoggerFilter")
                .doesNotHaveBean("addTraceIdFilter"));
    }

    @Test
    void headerLoggerFilterIsRegisteredWhenEnabled() {
        this.runner.withPropertyValues("creo.otel.header-logging.enabled=true")
                .run(context -> assertThat(context).hasBean("headerLoggerFilter"));
    }

    @Test
    void traceIdFilterIsRegisteredWhenEnabled() {
        this.runner.withPropertyValues("creo.otel.trace-id.enabled=true")
                .run(context -> assertThat(context).hasBean("addTraceIdFilter"));
    }

    @Test
    void traceIdFilterBacksOffWithoutTracer() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(FilterConfiguration.class))
                .withPropertyValues("creo.otel.trace-id.enabled=true")
                .run(context -> assertThat(context).doesNotHaveBean("addTraceIdFilter"));
    }
}
