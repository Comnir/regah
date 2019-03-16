package com.jefferson.regah.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.jefferson.regah.SharedResources;
import com.jefferson.regah.client.handler.AddHandler;
import com.jefferson.regah.client.handler.DownloadHandler;
import com.jefferson.regah.handler.ErrorWrappingHandler;
import com.jefferson.regah.server.handler.ListResourcesHandler;
import com.jefferson.regah.server.handler.PrepareResourceForDownloadHandler;
import com.jefferson.regah.transport.Transporter;
import com.jefferson.regah.transport.torrent.TorrentTransporter;
import com.sun.net.httpserver.HttpHandler;

import javax.inject.Named;

public class HttpHandlersModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Transporter.class).to(TorrentTransporter.class);
    }

    @Provides
    @Named("prepareResourceForDownload")
    HttpHandler prepareResourceForDownloadHandler(final SharedResources sharedResources, final Transporter transporter) {
        return new ErrorWrappingHandler<>(new PrepareResourceForDownloadHandler(sharedResources, transporter));
    }

    @Provides
    @Named("addResource")
    HttpHandler listResourcesHandler(final SharedResources sharedResources) {
        return new ErrorWrappingHandler<>(new ListResourcesHandler(sharedResources));
    }

    @Provides
    @Named("addHandler")
    HttpHandler addHandler(final SharedResources sharedResources, final Transporter transporter) {
        return new ErrorWrappingHandler<>(new AddHandler(sharedResources));
    }

    @Provides
    @Named("downloadHandler")
    HttpHandler downloadHandler(final SharedResources sharedResources) {
        return new ErrorWrappingHandler<>(new DownloadHandler());
    }
}
