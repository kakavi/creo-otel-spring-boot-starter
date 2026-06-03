package com.creotech.starter.autoconfigure;

import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Registers the servlet observability filters in servlet web applications.
 *
 * <p>Both filters are disabled by default and registered through
 * {@link FilterRegistrationBean} so their ordering relative to Spring's tracing /
 * observation filter is deterministic.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass({ Tracer.class, OncePerRequestFilter.class })
@EnableConfigurationProperties(CreoOtelProperties.class)
public class FilterConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterConfiguration.class);

    /**
     * Runs late so the tracing/observation filter has already established a trace
     * context by the time the trace ID is read.
     */
    static final int ADD_TRACE_ID_FILTER_ORDER = Ordered.LOWEST_PRECEDENCE - 10;

    /** Runs early so request headers are logged before downstream processing. */
    static final int HEADER_LOGGER_FILTER_ORDER = Ordered.HIGHEST_PRECEDENCE + 100;

    @Bean
    @ConditionalOnProperty(prefix = "creo.otel.header-logging", name = "enabled", havingValue = "true")
    public FilterRegistrationBean<HeaderLoggerFilter> headerLoggerFilter(CreoOtelProperties properties) {
        FilterRegistrationBean<HeaderLoggerFilter> registration =
                new FilterRegistrationBean<>(new HeaderLoggerFilter(properties.getHeaderLogging().getRedactedHeaders()));
        registration.setOrder(HEADER_LOGGER_FILTER_ORDER);
        registration.addUrlPatterns("/*");
        LOGGER.debug("Registered HeaderLoggerFilter (order={}, redacting {} header name(s)); "
                        + "request headers are logged at DEBUG", HEADER_LOGGER_FILTER_ORDER,
                properties.getHeaderLogging().getRedactedHeaders().size());
        return registration;
    }

    @Bean
    @ConditionalOnBean(Tracer.class)
    @ConditionalOnProperty(prefix = "creo.otel.trace-id", name = "enabled", havingValue = "true")
    public FilterRegistrationBean<AddTraceIdFilter> addTraceIdFilter(Tracer tracer, CreoOtelProperties properties) {
        String headerName = properties.getTraceId().getHeaderName();
        FilterRegistrationBean<AddTraceIdFilter> registration = new FilterRegistrationBean<>(
                new AddTraceIdFilter(tracer, headerName));
        registration.setOrder(ADD_TRACE_ID_FILTER_ORDER);
        registration.addUrlPatterns("/*");
        LOGGER.debug("Registered AddTraceIdFilter (order={}); responses will carry the '{}' header",
                ADD_TRACE_ID_FILTER_ORDER, headerName);
        return registration;
    }
}
