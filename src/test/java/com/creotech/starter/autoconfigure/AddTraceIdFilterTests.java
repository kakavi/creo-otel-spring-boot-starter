package com.creotech.starter.autoconfigure;

import io.micrometer.tracing.CurrentTraceContext;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AddTraceIdFilterTests {

    @Test
    void writesTraceIdToConfiguredHeader() throws Exception {
        Tracer tracer = tracerReturning("0dbe0809731e35081d6db16c2ca0ef91");
        AddTraceIdFilter filter = new AddTraceIdFilter(tracer, "X-Trace-Id");

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest(), response, new MockFilterChain());

        assertThat(response.getHeader("X-Trace-Id")).isEqualTo("0dbe0809731e35081d6db16c2ca0ef91");
    }

    @Test
    void honoursCustomHeaderName() throws Exception {
        Tracer tracer = tracerReturning("abc123");
        AddTraceIdFilter filter = new AddTraceIdFilter(tracer, "Trace-Id");

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest(), response, new MockFilterChain());

        assertThat(response.getHeader("Trace-Id")).isEqualTo("abc123");
        assertThat(response.getHeader("X-Trace-Id")).isNull();
    }

    @Test
    void omitsHeaderWhenNoTraceContext() throws Exception {
        Tracer tracer = mock(Tracer.class);
        CurrentTraceContext currentTraceContext = mock(CurrentTraceContext.class);
        when(tracer.currentTraceContext()).thenReturn(currentTraceContext);
        when(currentTraceContext.context()).thenReturn(null);

        AddTraceIdFilter filter = new AddTraceIdFilter(tracer, "X-Trace-Id");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest(), response, new MockFilterChain());

        assertThat(response.getHeader("X-Trace-Id")).isNull();
    }

    private static Tracer tracerReturning(String traceId) {
        Tracer tracer = mock(Tracer.class);
        CurrentTraceContext currentTraceContext = mock(CurrentTraceContext.class);
        TraceContext traceContext = mock(TraceContext.class);
        when(tracer.currentTraceContext()).thenReturn(currentTraceContext);
        when(currentTraceContext.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn(traceId);
        return tracer;
    }
}
