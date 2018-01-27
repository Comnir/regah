package com.jefferson.regah.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ErrorWrappingHandlerTest {
    @BeforeAll
    static void setupAll() {
        System.out.println("Start all");
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void callsInputHandlerWithExchangeObject() throws IOException {
        final HttpExchange exchange = mock(HttpExchange.class);
        final HttpHandler handler = mock(HttpHandler.class);
        final ErrorWrappingHandler errorWrappingHandler = new ErrorWrappingHandler(handler);

        errorWrappingHandler.handle(exchange);

        verify(handler).handle(exchange);
    }

    @Test
    void throwsWhenExcahangeParameterIsNull() {
        final HttpHandler handler = mock(HttpHandler.class);
        final ErrorWrappingHandler errorWrappingHandler = new ErrorWrappingHandler(handler);

        assertThrows(IllegalArgumentException.class, () -> errorWrappingHandler.handle(null));
    }

    @Test
    void infoAboutExceptionFromWrappedHandlerSentAsRepsonse() throws IOException {
        final HttpExchange exchange = mock(HttpExchange.class);
        final HttpHandler handler = mock(HttpHandler.class);
        final IOException exception = new IOException("Error encountered during handling what needs to be handled");
        doThrow(exception).when(handler).handle(exchange);
        final ErrorWrappingHandler errorWrappingHandler = new ErrorWrappingHandler(handler);

        final OutputStream responseOutpuStream = mock(OutputStream.class);

        final Headers responseHeaders = mock(Headers.class);
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
        when(exchange.getResponseBody()).thenReturn(responseOutpuStream);

        // execute behaviour
        errorWrappingHandler.handle(exchange);

        // verify behaviour
        verify(exchange).sendResponseHeaders(Mockito.eq(400), AdditionalMatchers.gt(0L));
        final ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(responseOutpuStream).write(captor.capture());

        final String responseString = new String(captor.getValue(), StandardCharsets.UTF_8);

        assertTrue(responseString.contains("Error encountered"));
    }
}