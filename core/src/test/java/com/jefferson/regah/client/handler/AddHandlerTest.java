package com.jefferson.regah.client.handler;

import com.google.gson.Gson;
import com.jefferson.regah.SharedResources;
import com.jefferson.regah.handler.ErrorWrappingHandler;
import com.jefferson.regah.handler.Responder;
import com.jefferson.regah.server.handler.HttpConstants;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AddHandlerTest {
    private final static Gson gson = new Gson();

    private AddHandler addHandler;

    @Mock
    private SharedResources sharedResources;
    @Mock
    private HttpExchange exchange;
    @Mock
    private Headers responseHeaders;
    @Mock
    private Headers requestHeaders;
    @Mock
    private OutputStream responseBody;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        when(exchange.getRequestHeaders()).thenReturn(requestHeaders);
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
        when(exchange.getResponseBody()).thenReturn(responseBody);

        addHandler = new AddHandler(sharedResources);
    }

    @Test
    void errorWhenContentNotJson() {
        when(requestHeaders.getFirst(HttpConstants.CONTENT_TYPE)).thenReturn(null);
        final Responder responder = mock(Responder.class);

        new ErrorWrappingHandler(addHandler, responder).handle(exchange);

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(responder).respondWithJson(eq(exchange), captor.capture(), eq(400));
        assertThat(captor.getValue(), StringContains.containsString("Expected request with JSON content"));
    }

    @Test
    void errorWhenMissingPathsFromJson() {
        when(requestHeaders.getFirst(HttpConstants.CONTENT_TYPE)).thenReturn(HttpConstants.APPLICATION_JSON);
        final InputStream requestBody = new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8));
        when(exchange.getRequestBody()).thenReturn(requestBody);
        final Responder responder = mock(Responder.class);

        new ErrorWrappingHandler(addHandler, responder).handle(exchange);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(responder).respondWithJson(any(), captor.capture(), eq(400));

        assertThat(captor.getValue(), StringContains.containsString("Missing"));
    }

    @Test
    void errorWhenPathsIsNotAList() {
        when(requestHeaders.getFirst(HttpConstants.CONTENT_TYPE)).thenReturn(HttpConstants.APPLICATION_JSON);
        final Map<String, String> parameters = Map.of("paths", "not_list");
        final InputStream requestBody = new ByteArrayInputStream(gson.toJson(parameters).getBytes(StandardCharsets.UTF_8));
        when(exchange.getRequestBody()).thenReturn(requestBody);

        final Responder responder = mock(Responder.class);

        new ErrorWrappingHandler(addHandler, responder).handle(exchange);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(responder).respondWithJson(any(), captor.capture(), eq(400));

        assertThat(captor.getValue(), StringContains.containsString("Failed to parse JSON"));
    }

    @Test
    void successWhenRequestHasEmptyPaths() {
        when(requestHeaders.getFirst(HttpConstants.CONTENT_TYPE)).thenReturn(HttpConstants.APPLICATION_JSON);
        final Map<String, List> parameters = Map.of("paths", Collections.emptyList());
        final InputStream requestBody = new ByteArrayInputStream(gson.toJson(parameters).getBytes(StandardCharsets.UTF_8));
        when(exchange.getRequestBody()).thenReturn(requestBody);
        final Responder responder = mock(Responder.class);
        new ErrorWrappingHandler(addHandler, responder).handle(exchange);

        verify(responder).respondWithJson(any(), eq(""), eq(200));
    }

    @Test
    void shareIsCalledWithInputPath() {
        when(requestHeaders.getFirst(HttpConstants.CONTENT_TYPE)).thenReturn(HttpConstants.APPLICATION_JSON);

        final String path = "/";
        final Map<String, List> parameters = Map.of("paths", Collections.singletonList(path));
        // use a real InputStream implementation
        final InputStream inputStream = new ByteArrayInputStream(gson.toJson(parameters).getBytes(StandardCharsets.UTF_8));
        when(exchange.getRequestBody()).thenReturn(inputStream);

        new ErrorWrappingHandler(addHandler).handle(exchange);

        verify(sharedResources).share(new File(path));
    }

    @Test
    void shareIsCalledWithAllPassedPaths() {
        when(requestHeaders.getFirst(HttpConstants.CONTENT_TYPE)).thenReturn(HttpConstants.APPLICATION_JSON);

        final List<String> paths = List.of("/share/ForSharing.txt", "/share/subFolder");
        final Map<String, List> parameters = Map.of("paths", paths);
        // use a real InputStream implementation
        final InputStream inputStream = new ByteArrayInputStream(gson.toJson(parameters).getBytes(StandardCharsets.UTF_8));
        when(exchange.getRequestBody()).thenReturn(inputStream);

        new ErrorWrappingHandler(addHandler).handle(exchange);

        for (final String s : paths) {
            verify(sharedResources).share(new File(s));
        }
    }


}