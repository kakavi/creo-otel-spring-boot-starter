package com.creotech.starter.autoconfigure;

import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class HeaderLoggerFilterTests {

    private final Logger logger = (Logger) LoggerFactory.getLogger(HeaderLoggerFilter.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private Level previousLevel;

    @BeforeEach
    void attachAppender() {
        this.previousLevel = this.logger.getLevel();
        this.logger.setLevel(Level.DEBUG);
        this.appender.start();
        this.logger.addAppender(this.appender);
    }

    @AfterEach
    void detachAppender() {
        this.logger.detachAppender(this.appender);
        this.logger.setLevel(this.previousLevel);
    }

    @Test
    void sensitiveHeaderValuesAreRedacted() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer super-secret-token");
        request.addHeader("Cookie", "session=abc123");
        request.addHeader("X-Custom", "visible-value");

        HeaderLoggerFilter filter = new HeaderLoggerFilter(List.of("authorization", "cookie"));
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        List<String> messages = this.appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
        assertThat(messages).noneMatch(message -> message.contains("super-secret-token"));
        assertThat(messages).noneMatch(message -> message.contains("session=abc123"));
        assertThat(messages).anyMatch(message -> message.contains("Authorization: ***"));
        assertThat(messages).anyMatch(message -> message.contains("X-Custom: visible-value"));
    }

    @Test
    void redactionIsCaseInsensitive() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("AUTHORIZATION", "Bearer super-secret-token");

        HeaderLoggerFilter filter = new HeaderLoggerFilter(List.of("Authorization"));
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        List<String> messages = this.appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
        assertThat(messages).noneMatch(message -> message.contains("super-secret-token"));
    }
}
