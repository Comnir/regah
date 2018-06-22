package com.jefferson.regah.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

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
        final Handler handler = mock(Handler.class);
        final Responder responder = mock(Responder.class);
        final ErrorWrappingHandler errorWrappingHandler = new ErrorWrappingHandler(handler, responder);

        errorWrappingHandler.handle(exchange);

        verify(handler).handleHttpRequest(exchange);
    }

    @Test
    void throwsWhenExcahangeParameterIsNull() {
        final Handler handler = mock(Handler.class);
        final ErrorWrappingHandler errorWrappingHandler = new ErrorWrappingHandler(handler);

        assertThrows(IllegalArgumentException.class, () -> errorWrappingHandler.handle(null));
    }

    @Test
    void infoAboutExceptionFromWrappedHandlerSentAsRepsonse() throws IOException {
        final HttpExchange exchange = mock(HttpExchange.class);
        final Handler handler = mock(Handler.class);
        final IOException exception = new IOException("Expected exception as part of the test.");
        doThrow(exception).when(handler).handleHttpRequest(exchange);
        final ErrorWrappingHandler errorWrappingHandler = new ErrorWrappingHandler(handler);

        final OutputStream responseOutpuStream = mock(OutputStream.class);

        final Headers responseHeaders = mock(Headers.class);
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
        when(exchange.getResponseBody()).thenReturn(responseOutpuStream);

        // execute behaviour
        errorWrappingHandler.handle(exchange);

        // verify behaviour
        verify(exchange).sendResponseHeaders(ArgumentMatchers.eq(400), AdditionalMatchers.gt(0L));
        final ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(responseOutpuStream).write(captor.capture());

        final String responseString = new String(captor.getValue(), StandardCharsets.UTF_8);

        assertTrue(responseString.contains("Error encountered"), "The respnse didn't containt the expected message");
    }
}