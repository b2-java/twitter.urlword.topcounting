package com.reagere.twitter.urlword.topcounting.web;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import sun.net.httpserver.DefaultHttpServerProvider;

import java.io.IOException;
import java.net.InetSocketAddress;

public class WebServer {
    private final HttpServer httpServer;
    public WebServer(int port, HttpHandler topWordsHandler, HttpHandler topUrlsHandler) throws IOException {
        httpServer = DefaultHttpServerProvider.provider().createHttpServer(new InetSocketAddress(port), 0);
        HttpContext topWordsContext = httpServer.createContext("/top-words");
        topWordsContext.setHandler(topWordsHandler);
        HttpContext topUrlsContext = httpServer.createContext("/top-urls");
        topUrlsContext.setHandler(topUrlsHandler);
        httpServer.start();
    }
    public void stop() {
        httpServer.stop(100);
    }
}
