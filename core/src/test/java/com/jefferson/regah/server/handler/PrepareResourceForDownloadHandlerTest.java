package com.jefferson.regah.server.handler;

import com.google.gson.Gson;
import com.jefferson.regah.SharedResources;
import com.jefferson.regah.handler.ErrorWrappingHandler;
import com.jefferson.regah.transport.FailureToPrepareForDownload;
import com.jefferson.regah.transport.Transporter;
import com.jefferson.regah.transport.serialization.TransportDataSerializer;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PrepareResourceForDownloadHandlerTest {
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

        new ErrorWrappingHandler(
                new PrepareResourceForDownloadHandler(sharedResources, transporter))
                .handle(exchange);

        verify(exchange).sendResponseHeaders(ArgumentMatchers.eq(400), AdditionalMatchers.gt(0L));
    }

    @Test
    void requestFileIsNotShared() throws IOException {
        // prepare an HTTP request for a file
        final String path = this.getClass().getResource("/noshare/NotForShare.txt").getPath();
        final Map<String, String> parameters = Map.of(PrepareResourceForDownloadHandler.FILE_PATH_PARAMETER, path);
        // use a real InputStream implementation
        final InputStream inputStream = new ByteArrayInputStream(gson.toJson(parameters).getBytes(StandardCharsets.UTF_8));

        when(requestHeaders.getFirst(HttpConstants.CONTENT_TYPE)).thenReturn(HttpConstants.APPLICATION_JSON);
        when(exchange.getRequestHeaders()).thenReturn(requestHeaders);
        when(exchange.getRequestBody()).thenReturn(inputStream);

        // call with the given request
        new ErrorWrappingHandler(
                new PrepareResourceForDownloadHandler(sharedResources, transporter))
                .handle(exchange);

        // verify behaviour
        verify(exchange).sendResponseHeaders(ArgumentMatchers.eq(400), AdditionalMatchers.gt(0L));
    }

    @Test
    void transportGotTheRequestFile() throws FailureToPrepareForDownload {
        // prepare an HTTP request for a file
        final String path = this.getClass().getResource("/share/ForSharing.txt").getPath();
        final Map<String, String> parameters = Map.of(PrepareResourceForDownloadHandler.FILE_PATH_PARAMETER, path);
        final File expectedFile = new File(path);
        // use a real InputStream implementation
        final InputStream inputStream = new ByteArrayInputStream(gson.toJson(parameters).getBytes(StandardCharsets.UTF_8));
        when(exchange.getRequestBody()).thenReturn(inputStream);
        when(sharedResources.isShared(expectedFile)).thenReturn(true);

        // call with the given request
        final PrepareResourceForDownloadHandler prepareResourceForDownloadHandler = new PrepareResourceForDownloadHandler(sharedResources, transporter);
        final TransportDataSerializer transportDataSerializer = mock(TransportDataSerializer.class);
        when(transportDataSerializer.toJson(any())).thenReturn("{}");

        prepareResourceForDownloadHandler.setTransportDataConverterCreator(() -> transportDataSerializer);

        new ErrorWrappingHandler(prepareResourceForDownloadHandler).handle(exchange);

        // verify behaviour
        verify(transporter).dataForDownloading(expectedFile);
    }

    @Test
    void transportDataIsReturned() throws IOException {
        when(sharedResources.isShared(ArgumentMatchers.any())).thenReturn(true);
        final Map<String, String> parameters = Map.of(PrepareResourceForDownloadHandler.FILE_PATH_PARAMETER, "");

        final InputStream inputStream = new ByteArrayInputStream(gson.toJson(parameters).getBytes(StandardCharsets.UTF_8));
        when(exchange.getRequestBody()).thenReturn(inputStream);
        final String expectedJson = "{\"mocked\":\"1\"}";

        // call with the given request
        final PrepareResourceForDownloadHandler prepareResourceForDownloadHandler = new PrepareResourceForDownloadHandler(sharedResources, transporter);
        final TransportDataSerializer transportDataSerializer = mock(TransportDataSerializer.class);
        when(transportDataSerializer.toJson(any())).thenReturn(expectedJson);
        prepareResourceForDownloadHandler.setTransportDataConverterCreator(() -> transportDataSerializer);
        new ErrorWrappingHandler(prepareResourceForDownloadHandler)
                .handle(exchange);

        verify(exchange).sendResponseHeaders(200, expectedJson.length());
        verify(responseOutpuStream).write(expectedJson.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void errorWhenFailedToPrepareForDownload() throws FailureToPrepareForDownload, IOException {
        when(sharedResources.isShared(ArgumentMatchers.any())).thenReturn(true);
        final Map<String, String> parameters = Map.of(PrepareResourceForDownloadHandler.FILE_PATH_PARAMETER, "");

        final InputStream inputStream = new ByteArrayInputStream(gson.toJson(parameters).getBytes(StandardCharsets.UTF_8));
        when(exchange.getRequestBody()).thenReturn(inputStream);
        when(transporter.dataForDownloading(ArgumentMatchers.any())).thenThrow(new FailureToPrepareForDownload("Thrown exception for test - should be converted to HTTP error response", null));

        // call with the given request
        new ErrorWrappingHandler(
                new PrepareResourceForDownloadHandler(sharedResources, transporter))
                .handle(exchange);

        verify(exchange).sendResponseHeaders(ArgumentMatchers.eq(503), AdditionalMatchers.gt(0L));
    }
}