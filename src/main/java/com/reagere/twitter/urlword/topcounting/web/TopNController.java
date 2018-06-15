package com.reagere.twitter.urlword.topcounting.web;

import com.google.gson.Gson;
import com.reagere.twitter.urlword.topcounting.actors.TopNSinkStreamActor;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.AllArgsConstructor;

import java.io.IOException;

@AllArgsConstructor
public class TopNController implements HttpHandler {

    private final TopNSinkStreamActor topN;
    private final Gson gson;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String content = gson.toJson(topN.getTopN());
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, content.length());
            exchange.getResponseBody().write(content.getBytes("UTF-8"));
        } else {
            exchange.sendResponseHeaders(404, 0);
        }
    }
}
