package org.paidaki.pogofinder.web;

import org.paidaki.pogofinder.web.enums.RequestMethod;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpHandler {

    private static final String USER_AGENT = "Mozilla/5.0";

    public HttpResponse sendRequest(URL url, RequestMethod requestMethod, int timeout) {
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod(String.valueOf(requestMethod));
            conn.setRequestProperty("User-Agent", USER_AGENT);

            return new HttpResponse(conn, timeout);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
