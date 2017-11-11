package com.jefferson.regah.server.handler;

import com.google.gson.Gson;
import com.jefferson.regah.SharedResources;
import com.jefferson.regah.server.transport.TransportData;
import com.jefferson.regah.server.transport.Transporter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.*;
import org.mockito.AdditionalMatchers;
import org.mockito.Mockito;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

class FetchResourceHandlerTest {
    private static Gson gson;

    private SharedResources sharedResources;
    private Transporter transporter;
    private HttpExchange exchange;
    private Headers requestHeaders;
    private Headers responseHeaders;
    private OutputStream responseOutpuStream;

    @BeforeAll
    static void setupClass() {
        System.out.println("Start test class");
        gson = new Gson();
    }

    @AfterAll
    static void teardownClass() {
        System.out.println("Done with test class");
    }

    @BeforeEach
    void setUp() {
        sharedResources = Mockito.mock(SharedResources.class);
        transporter = Mockito.mock(Transporter.class);
        exchange = Mockito.mock(HttpExchange.class);


        requestHeaders = Mockito.mock(Headers.class);
        Mockito.when(requestHeaders.getFirst(HttpConstants.CONTENT_TYPE)).thenReturn(HttpConstants.APPLICATION_JSON);
        Mockito.when(exchange.getRequestHeaders()).thenReturn(requestHeaders);
        responseHeaders = Mockito.mock(Headers.class);
        Mockito.when(exchange.getResponseHeaders()).thenReturn(responseHeaders);
        responseOutpuStream = Mockito.mock(OutputStream.class);
        Mockito.when(exchange.getResponseBody()).thenReturn(responseOutpuStream);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void errorWhenContentNotJson() throws IOException {
        final Headers headers = Mockito.mock(Headers.class);
        Mockito.when(headers.getFirst(HttpConstants.CONTENT_TYPE)).thenReturn(null);
        Mockito.when(exchange.getRequestHeaders()).thenReturn(headers);

        new FetchResourceHandler(sharedResources, transporter).handle(exchange);

        Mockito.verify(exchange).sendResponseHeaders(Mockito.eq(400), AdditionalMatchers.gt(0L));
    }

    @Test
    void requestFileIsNotShared() throws IOException {
        System.out.println("Start");
        // prepare an HTTP request for a file
        final String path = this.getClass().getResource("/noshare/NotForShare.txt").getPath();
        final Map<String, String> parameters = Map.of(FetchResourceHandler.FILE_PATH_PARAMETER, path);
        // use a real InputStream implementation
        final InputStream inputStream = new ByteArrayInputStream(gson.toJson(parameters).getBytes(StandardCharsets.UTF_8));

        final Headers headers = Mockito.mock(Headers.class);
        Mockito.when(headers.getFirst(HttpConstants.CONTENT_TYPE)).thenReturn(HttpConstants.APPLICATION_JSON);
        Mockito.when(exchange.getRequestHeaders()).thenReturn(headers);
        Mockito.when(exchange.getRequestBody()).thenReturn(inputStream);

        // call with the given request
        new FetchResourceHandler(sharedResources, transporter).handle(exchange);

        // verify behaviour
        Mockito.verify(exchange).sendResponseHeaders(Mockito.eq(400), AdditionalMatchers.gt(0L));
        System.out.println("End");
    }

    @Test
    void transportGotTheRequestFile() throws IOException {
        // prepare an HTTP request for a file
        final String path = this.getClass().getResource("/share/ForSharing.txt").getPath();
        final Map<String, String> parameters = Map.of(FetchResourceHandler.FILE_PATH_PARAMETER, path);
        final File expectedFile = new File(path);
        // use a real InputStream implementation
        final InputStream inputStream = new ByteArrayInputStream(gson.toJson(parameters).getBytes(StandardCharsets.UTF_8));
        Mockito.when(exchange.getRequestBody()).thenReturn(inputStream);
        final TransportData transportData = Mockito.mock(TransportData.class);
        Mockito.when(transportData.asJson()).thenReturn("{}");
        Mockito.when(transporter.getCommunicationInfoFor(expectedFile)).thenReturn(transportData);
        Mockito.when(sharedResources.isShared(expectedFile)).thenReturn(true);

        // call with the given request
        new FetchResourceHandler(sharedResources, transporter).handle(exchange);

        // verify behaviour
        Mockito.verify(transporter).getCommunicationInfoFor(expectedFile);
    }

    @Test
    void transportDataIsReturned() throws IOException {
        Mockito.when(sharedResources.isShared(Mockito.any())).thenReturn(true);
        final Map<String, String> parameters = Map.of(FetchResourceHandler.FILE_PATH_PARAMETER, "");

        final InputStream inputStream = new ByteArrayInputStream(gson.toJson(parameters).getBytes(StandardCharsets.UTF_8));
        Mockito.when(exchange.getRequestBody()).thenReturn(inputStream);
        final TransportData transportData = Mockito.mock(TransportData.class);
        final String expectedJson = "{\"mocked\":\"1\"}";
        Mockito.when(transportData.asJson()).thenReturn(expectedJson);
        Mockito.when(transporter.getCommunicationInfoFor(Mockito.any())).thenReturn(transportData);

        // call with the given request
        new FetchResourceHandler(sharedResources, transporter).handle(exchange);

        Mockito.verify(exchange).sendResponseHeaders(200, expectedJson.length());
        Mockito.verify(responseOutpuStream).write(expectedJson.getBytes(StandardCharsets.UTF_8));
    }
}