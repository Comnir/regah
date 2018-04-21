package com.jefferson.regah.server.handler;

import com.google.gson.Gson;
import com.jefferson.regah.SharedResources;
import com.jefferson.regah.handler.Responder;
import com.jefferson.regah.transport.FailureToPrepareForDownload;
import com.jefferson.regah.transport.TransportData;
import com.jefferson.regah.transport.Transporter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FetchResourceHandlerTest {
    private final static Gson gson = new Gson();

    @Mock
    private SharedResources sharedResources;
    @Mock
    private Transporter transporter;
    @Mock
    private HttpExchange exchange;
    @Mock
    private Headers requestHeaders;
    @Mock
    private Headers responseHeaders;
    @Mock
    private OutputStream responseOutpuStream;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(requestHeaders.getFirst(HttpConstants.CONTENT_TYPE)).thenReturn(HttpConstants.APPLICATION_JSON);
        when(exchange.getRequestHeaders()).thenReturn(requestHeaders);
        when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
        when(exchange.getResponseBody()).thenReturn(responseOutpuStream);
    }

    @Test
    void errorWhenContentNotJson() throws IOException {
        when(requestHeaders.getFirst(HttpConstants.CONTENT_TYPE)).thenReturn(null);
        when(exchange.getRequestHeaders()).thenReturn(requestHeaders);

        new FetchResourceHandler(sharedResources, transporter, new Responder()).handle(exchange);

        verify(exchange).sendResponseHeaders(Mockito.eq(400), AdditionalMatchers.gt(0L));
    }

    @Test
    void requestFileIsNotShared() throws IOException {
        // prepare an HTTP request for a file
        final String path = this.getClass().getResource("/noshare/NotForShare.txt").getPath();
        final Map<String, String> parameters = Map.of(FetchResourceHandler.FILE_PATH_PARAMETER, path);
        // use a real InputStream implementation
        final InputStream inputStream = new ByteArrayInputStream(gson.toJson(parameters).getBytes(StandardCharsets.UTF_8));

        when(requestHeaders.getFirst(HttpConstants.CONTENT_TYPE)).thenReturn(HttpConstants.APPLICATION_JSON);
        when(exchange.getRequestHeaders()).thenReturn(requestHeaders);
        when(exchange.getRequestBody()).thenReturn(inputStream);

        // call with the given request
        new FetchResourceHandler(sharedResources, transporter, new Responder()).handle(exchange);

        // verify behaviour
        verify(exchange).sendResponseHeaders(Mockito.eq(400), AdditionalMatchers.gt(0L));
    }

    @Test
    void transportGotTheRequestFile() throws IOException, FailureToPrepareForDownload {
        // prepare an HTTP request for a file
        final String path = this.getClass().getResource("/share/ForSharing.txt").getPath();
        final Map<String, String> parameters = Map.of(FetchResourceHandler.FILE_PATH_PARAMETER, path);
        final File expectedFile = new File(path);
        // use a real InputStream implementation
        final InputStream inputStream = new ByteArrayInputStream(gson.toJson(parameters).getBytes(StandardCharsets.UTF_8));
        when(exchange.getRequestBody()).thenReturn(inputStream);
        final TransportData transportData = Mockito.mock(TransportData.class);
        when(transportData.asJson()).thenReturn("{}");
        when(transporter.dataForDownloading(expectedFile)).thenReturn(transportData);
        when(sharedResources.isShared(expectedFile)).thenReturn(true);

        // call with the given request
        new FetchResourceHandler(sharedResources, transporter, new Responder()).handle(exchange);

        // verify behaviour
        verify(transporter).dataForDownloading(expectedFile);
    }

    @Test
    void transportDataIsReturned() throws IOException, FailureToPrepareForDownload {
        when(sharedResources.isShared(Mockito.any())).thenReturn(true);
        final Map<String, String> parameters = Map.of(FetchResourceHandler.FILE_PATH_PARAMETER, "");

        final InputStream inputStream = new ByteArrayInputStream(gson.toJson(parameters).getBytes(StandardCharsets.UTF_8));
        when(exchange.getRequestBody()).thenReturn(inputStream);
        final TransportData transportData = Mockito.mock(TransportData.class);
        final String expectedJson = "{\"mocked\":\"1\"}";
        when(transportData.asJson()).thenReturn(expectedJson);
        when(transporter.dataForDownloading(Mockito.any())).thenReturn(transportData);

        // call with the given request
        new FetchResourceHandler(sharedResources, transporter, new Responder()).handle(exchange);

        verify(exchange).sendResponseHeaders(200, expectedJson.length());
        verify(responseOutpuStream).write(expectedJson.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void errorWhenFailedToPrepareForDownload() throws FailureToPrepareForDownload, IOException {
        when(sharedResources.isShared(Mockito.any())).thenReturn(true);
        final Map<String, String> parameters = Map.of(FetchResourceHandler.FILE_PATH_PARAMETER, "");

        final InputStream inputStream = new ByteArrayInputStream(gson.toJson(parameters).getBytes(StandardCharsets.UTF_8));
        when(exchange.getRequestBody()).thenReturn(inputStream);
        when(transporter.dataForDownloading(Mockito.any())).thenThrow(new FailureToPrepareForDownload("Thrown exception for test - should be converted to HTTP error response", null));

        // call with the given request
        new FetchResourceHandler(sharedResources, transporter, new Responder()).handle(exchange);

        verify(exchange).sendResponseHeaders(Mockito.eq(503), AdditionalMatchers.gt(0L));
    }
}