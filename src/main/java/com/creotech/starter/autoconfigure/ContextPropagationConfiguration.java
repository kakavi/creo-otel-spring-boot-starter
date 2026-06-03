package com.creotech.starter.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;

/**
 * Registers a {@link ContextPropagatingTaskDecorator} so observation/trace context is
 * carried across {@code TaskExecutor} boundaries. Enabled by default; backs off if the
 * application already defines its own decorator.
 */
@AutoConfiguration
@ConditionalOnClass(ContextPropagatingTaskDecorator.class)
@ConditionalOnProperty(prefix = "creo.otel.context-propagation", name = "enabled", havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(CreoOtelProperties.class)
public class ContextPropagationConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextPropagationConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public ContextPropagatingTaskDecorator contextPropagatingTaskDecorator() {
        LOGGER.debug("Registered ContextPropagatingTaskDecorator for async context propagation");
        return new ContextPropagatingTaskDecorator();
    }

}
