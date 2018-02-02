package com.jefferson.regah.client.handler;

import com.google.gson.Gson;
import com.jefferson.regah.SharedResources;
import com.jefferson.regah.server.handler.HttpConstants;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @BeforeAll
    static void setupAll() {
        System.out.println("Start all");
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        when(exchange.getRequestHeaders()).thenReturn(requestHeaders);
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
        when(exchange.getResponseBody()).thenReturn(responseBody);

        addHandler = new AddHandler(sharedResources);
    }

    @Test
    void errorWhenContentNotJson() throws IOException {
        when(requestHeaders.getFirst(HttpConstants.CONTENT_TYPE)).thenReturn(null);

        addHandler.handle(exchange);

        verify(exchange).sendResponseHeaders(Mockito.eq(400), AdditionalMatchers.gt(0L));
    }

    @Test
    void errorWhenMissingPathsFromJson() throws IOException {
        when(requestHeaders.getFirst(HttpConstants.CONTENT_TYPE)).thenReturn(HttpConstants.APPLICATION_JSON);
        final InputStream requestBody = new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8));
        when(exchange.getRequestBody()).thenReturn(requestBody);

        addHandler.handle(exchange);

        verify(exchange).sendResponseHeaders(Mockito.eq(400), AdditionalMatchers.gt(0L));
    }

    @Test
    void errorWhenPathsIsNotAList() throws IOException {
        when(requestHeaders.getFirst(HttpConstants.CONTENT_TYPE)).thenReturn(HttpConstants.APPLICATION_JSON);
        final Map<String, String> parameters = Map.of("paths", "not_list");
        final InputStream requestBody = new ByteArrayInputStream(gson.toJson(parameters).getBytes(StandardCharsets.UTF_8));
        when(exchange.getRequestBody()).thenReturn(requestBody);

        addHandler.handle(exchange);

        verify(exchange).sendResponseHeaders(Mockito.eq(400), AdditionalMatchers.gt(0L));
    }

    @Test
    void successWhenRequestHasEmptyPaths() throws IOException {
        when(requestHeaders.getFirst(HttpConstants.CONTENT_TYPE)).thenReturn(HttpConstants.APPLICATION_JSON);
        final Map<String, List> parameters = Map.of("paths", Collections.emptyList());
        final InputStream requestBody = new ByteArrayInputStream(gson.toJson(parameters).getBytes(StandardCharsets.UTF_8));
        when(exchange.getRequestBody()).thenReturn(requestBody);
        addHandler.handle(exchange);

        verify(exchange).sendResponseHeaders(Mockito.eq(200), Mockito.eq(0L));
    }

    @Test
    void shareIsCalledWithInputPath() throws IOException {
        when(requestHeaders.getFirst(HttpConstants.CONTENT_TYPE)).thenReturn(HttpConstants.APPLICATION_JSON);

        final String path = "/";
        final Map<String, List> parameters = Map.of("paths", Collections.singletonList(path));
        // use a real InputStream implementation
        final InputStream inputStream = new ByteArrayInputStream(gson.toJson(parameters).getBytes(StandardCharsets.UTF_8));
        when(exchange.getRequestBody()).thenReturn(inputStream);

        addHandler.handle(exchange);

        verify(sharedResources).share(new File(path));
    }

    @Test
    void shareIsCalledWithAllPassedPaths() throws IOException {
        when(requestHeaders.getFirst(HttpConstants.CONTENT_TYPE)).thenReturn(HttpConstants.APPLICATION_JSON);

        final List<String> paths = List.of("/share/ForSharing.txt", "/share/subFolder");
        final Map<String, List> parameters = Map.of("paths", paths);
        // use a real InputStream implementation
        final InputStream inputStream = new ByteArrayInputStream(gson.toJson(parameters).getBytes(StandardCharsets.UTF_8));
        when(exchange.getRequestBody()).thenReturn(inputStream);

        addHandler.handle(exchange);

        for (final String s : paths) {
            verify(sharedResources).share(new File(s));
        }
    }


}