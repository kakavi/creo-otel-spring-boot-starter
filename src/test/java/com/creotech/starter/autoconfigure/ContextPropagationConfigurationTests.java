package com.creotech.starter.autoconfigure;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;

import static org.assertj.core.api.Assertions.assertThat;

class ContextPropagationConfigurationTests {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ContextPropagationConfiguration.class));

    @Test
    void decoratorIsRegisteredByDefault() {
        this.runner.run(context -> assertThat(context).hasSingleBean(ContextPropagatingTaskDecorator.class));
    }

    @Test
    void decoratorBacksOffWhenDisabled() {
        this.runner.withPropertyValues("creo.otel.context-propagation.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(ContextPropagatingTaskDecorator.class));
    }

    @Test
    void applicationDefinedDecoratorWins() {
        ContextPropagatingTaskDecorator custom = new ContextPropagatingTaskDecorator();
        this.runner.withBean("customDecorator", ContextPropagatingTaskDecorator.class, () -> custom)
                .run(context -> assertThat(context).getBean(ContextPropagatingTaskDecorator.class).isSameAs(custom));
    }
}
