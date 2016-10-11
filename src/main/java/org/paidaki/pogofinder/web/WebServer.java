package org.paidaki.pogofinder.web;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Observable;

public class WebServer extends Observable {

    private static final int PORT = 8066;
    private static final String URL = "/";

    private HttpServer httpServer = null;

    protected class ResponseHandler implements HttpHandler {

        private static final int STATUS_OK = 200;
        private final Charset CHARSET = StandardCharsets.UTF_8;
        private static final String HEADER_CONTENT_TYPE = "Content-Type";
        private final String CONTENT_TYPE_JSON = "application/json; charset=" + CHARSET.name();

        private HttpExchange httpExchange = null;

        @Override
        public void handle(HttpExchange t) throws IOException {
            httpExchange = t;

            setChanged();
            notifyObservers(this);
        }
    }

    public WebServer() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
            httpServer.createContext(URL, new ResponseHandler());
            httpServer.setExecutor(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void sendResponse(ResponseHandler rh, JSONObject jsonData) {
        try {
            Headers headers = rh.httpExchange.getResponseHeaders();
            String responseBody = String.valueOf(jsonData);

            headers.set(ResponseHandler.HEADER_CONTENT_TYPE, rh.CONTENT_TYPE_JSON);
            byte[] rawResponseBody = responseBody.getBytes(rh.CHARSET);

            rh.httpExchange.sendResponseHeaders(ResponseHandler.STATUS_OK, rawResponseBody.length);
            rh.httpExchange.getResponseBody().write(rawResponseBody);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            rh.httpExchange.close();
        }
    }

    protected void start() {
        httpServer.start();
    }

    protected void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }
}
