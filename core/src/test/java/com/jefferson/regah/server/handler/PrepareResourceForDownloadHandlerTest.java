package com.jefferson.regah.server.handler;

import com.google.gson.Gson;
import com.jefferson.regah.SharedResources;
import com.jefferson.regah.handler.ErrorWrappingHandler;
import com.jefferson.regah.handler.Responder;
import com.jefferson.regah.transport.FailureToPrepareForDownload;
import com.jefferson.regah.transport.Transporter;
import com.jefferson.regah.transport.serialization.TransportDataSerializer;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
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
    void errorWhenContentNotJson() {
        when(requestHeaders.getFirst(HttpConstants.CONTENT_TYPE)).thenReturn(null);
        when(exchange.getRequestHeaders()).thenReturn(requestHeaders);
        final PrepareResourceForDownloadHandler handler = new PrepareResourceForDownloadHandler(sharedResources, transporter);
        final Responder responder = mock(Responder.class);

        new ErrorWrappingHandler(handler, responder).handle(exchange);

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(responder).respondWithJson(eq(exchange), captor.capture(), eq(400));
        assertThat(captor.getValue(), StringContains.containsString("Invalid request format"));
    }

    @Test
    void requestFileIsNotShared() {
        // prepare an HTTP request for a file
        final String path = this.getClass().getResource("/noshare/NotForShare.txt").getPath();
        final Map<String, String> parameters = Map.of(PrepareResourceForDownloadHandler.FILE_PATH_PARAMETER, path);
        // use a real InputStream implementation
        final InputStream inputStream = new ByteArrayInputStream(gson.toJson(parameters).getBytes(StandardCharsets.UTF_8));

        when(requestHeaders.getFirst(HttpConstants.CONTENT_TYPE)).thenReturn(HttpConstants.APPLICATION_JSON);
        when(exchange.getRequestHeaders()).thenReturn(requestHeaders);
        when(exchange.getRequestBody()).thenReturn(inputStream);
        final PrepareResourceForDownloadHandler handler = new PrepareResourceForDownloadHandler(sharedResources, transporter);
        final Responder responder = mock(Responder.class);

        new ErrorWrappingHandler(handler, responder).handle(exchange);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(responder).respondWithJson(eq(exchange), captor.capture(), ArgumentMatchers.eq(400));
        assertThat(captor.getValue(), StringContains.containsString("Requested file is not shared"));
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
    void transportDataIsReturned() {
        when(sharedResources.isShared(ArgumentMatchers.any())).thenReturn(true);
        final Map<String, String> parameters = Map.of(PrepareResourceForDownloadHandler.FILE_PATH_PARAMETER, "");
        final InputStream inputStream = new ByteArrayInputStream(gson.toJson(parameters).getBytes(StandardCharsets.UTF_8));
        when(exchange.getRequestBody()).thenReturn(inputStream);
        final String expectedJson = "{\"mocked\":\"1\"}";
        final Responder responder = mock(Responder.class);

        // call with the given request
        final PrepareResourceForDownloadHandler prepareResourceForDownloadHandler = new PrepareResourceForDownloadHandler(sharedResources, transporter);
        final TransportDataSerializer transportDataSerializer = mock(TransportDataSerializer.class);
        when(transportDataSerializer.toJson(any())).thenReturn(expectedJson);
        prepareResourceForDownloadHandler.setTransportDataConverterCreator(() -> transportDataSerializer);
        new ErrorWrappingHandler(prepareResourceForDownloadHandler, responder).handle(exchange);

        verify(responder).respondWithJson(exchange, expectedJson, 200);
    }

    @Test
    void errorWhenFailedToPrepareForDownload() throws FailureToPrepareForDownload {
        when(sharedResources.isShared(ArgumentMatchers.any())).thenReturn(true);
        final Map<String, String> parameters = Map.of(PrepareResourceForDownloadHandler.FILE_PATH_PARAMETER, "");

        final InputStream inputStream = new ByteArrayInputStream(gson.toJson(parameters).getBytes(StandardCharsets.UTF_8));
        when(exchange.getRequestBody()).thenReturn(inputStream);
        final String exceptionMessage = "Thrown exception for test - should be converted to HTTP error response";
        when(transporter.dataForDownloading(ArgumentMatchers.any())).thenThrow(new FailureToPrepareForDownload(exceptionMessage, null));
        final Responder responder = mock(Responder.class);

        // call with the given request
        final PrepareResourceForDownloadHandler handler = new PrepareResourceForDownloadHandler(sharedResources, transporter);
        new ErrorWrappingHandler(handler, responder).handle(exchange);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(responder).respondWithJson(eq(exchange), captor.capture(), ArgumentMatchers.eq(503));

        assertThat(captor.getValue(), StringContains.containsString(exceptionMessage));
    }
}