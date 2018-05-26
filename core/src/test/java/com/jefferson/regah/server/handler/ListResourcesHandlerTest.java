package com.jefferson.regah.server.handler;

import com.google.gson.Gson;
import com.jefferson.regah.SharedResources;
import com.jefferson.regah.handler.ErrorWrappingHandler;
import com.jefferson.regah.handler.Responder;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.jefferson.regah.server.handler.HttpConstants.APPLICATION_JSON;
import static com.jefferson.regah.server.handler.HttpConstants.CONTENT_TYPE;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ListResourcesHandlerTest {
    private static final Gson gson = new Gson();

    @Mock
    private Responder responder;
    @Mock
    private SharedResources shareResource;
    @Mock
    private HttpExchange exchange;
    @Mock
    private OutputStream responseBody;
    @Mock
    private Headers responseHeadrs;
    @Mock
    private Headers requestHeaders;

    private ListResourcesHandler listResourcesHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(exchange.getRequestHeaders()).thenReturn(requestHeaders);
        when(exchange.getResponseHeaders()).thenReturn(responseHeadrs);
        when(exchange.getResponseBody()).thenReturn(responseBody);

        listResourcesHandler = new ListResourcesHandler(shareResource);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void emptyListWhenNoSharedResources() {
        when(requestHeaders.getFirst(CONTENT_TYPE)).thenReturn(APPLICATION_JSON);
        when(shareResource.getResources()).thenReturn(Collections.emptySet());

        ErrorWrappingHandler.builder(listResourcesHandler)
                .setResponder(responder)
                .build()
                .handle(exchange);

        final String jsonResult = asJson(Map.of("results", Collections.emptySet()));
        verify(responder).respondeWithJson(ArgumentMatchers.eq(exchange), ArgumentMatchers.eq(jsonResult), ArgumentMatchers.eq(200));
    }

    @Test
    void allSharedResourcesReturned() {
        when(requestHeaders.getFirst(CONTENT_TYPE)).thenReturn(APPLICATION_JSON);
        final Set<File> files = Set.of("file1", "file2", "dir1/file3", "dir2/file4")
                .stream()
                .map(File::new)
                .collect(Collectors.toSet());
        when(shareResource.getResources()).thenReturn(files);

        ErrorWrappingHandler.builder(listResourcesHandler)
                .setResponder(responder)
                .build()
                .handle(exchange);

        verify(responder).respondeWithJson(exchange, gson.toJson(Map.of("results", files)), 200);
    }

    private String asJson(Map<String, Object> map) {
        return gson.toJson(map);
    }
}