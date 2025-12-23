package com.creotech.starter.autoconfigure;

import io.micrometer.tracing.Tracer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass({ Tracer.class, OncePerRequestFilter.class })
public class FilterConfiguration {

    @Bean
    public HeaderLoggerFilter headerLoggerFilter() {
        return new HeaderLoggerFilter();
    }

    @Bean
    public AddTraceIdFilter addTraceIdFilter(Tracer tracer) {
        return new AddTraceIdFilter(tracer);
    }
}
