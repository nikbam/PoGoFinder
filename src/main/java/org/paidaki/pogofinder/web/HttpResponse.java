package org.paidaki.pogofinder.web;

import org.paidaki.pogofinder.web.enums.ResponseCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class HttpResponse {

    private HttpURLConnection connection;
    private ResponseCode responseCode;
    private String contentType;
    private String responseBody;

    public HttpResponse(HttpURLConnection connection, int timeout) {
        try {
            this.connection = connection;
            connection.setConnectTimeout(timeout);
            responseCode = ResponseCode.getResponseCode(connection.getResponseCode());
            contentType = connection.getContentType();
            responseBody = parseResponseBody(connection);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HttpURLConnection getConnection() {
        return connection;
    }

    public ResponseCode getResponseCode() {
        return responseCode;
    }

    public String getContentType() {
        return contentType;
    }

    public String getResponseBody() {
        return responseBody;
    }

    private String parseResponseBody(HttpURLConnection conn) {
        try {
            StringBuilder responseBody = new StringBuilder();
            BufferedReader in;
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                responseBody.append(inputLine);
            }
            in.close();

            return String.valueOf(responseBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
