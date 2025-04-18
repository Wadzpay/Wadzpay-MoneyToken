package com.wadzpay.ddflibrary.apihttp;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class HttpRequestConstants {

    public interface Handler {
        void response(HttpResponse response);
    }

    public final static String OPTIONS = "OPTIONS";
    public final static String GET = "GET";
    public final static String HEAD = "HEAD";
    public final static String POST = "POST";
    public final static String PUT = "PUT";
    public final static String DELETE = "DELETE";
    public final static String TRACE = "TRACE";

    private final String url;
    private final String method;
    private final String json;
    private final String authorization;
    private final Map<String, String> requestProperties;

    public HttpRequestConstants(String url) {
        this(url, GET, null, null);
    }

    public HttpRequestConstants(String url, String method) {
        this(url, method, null, null);
    }

    public HttpRequestConstants(String url, String method, String json) {
        this(url, method, json, null);
    }

    public HttpRequestConstants(String url, String method, String json, String authorization) {
        this(url, method, json, authorization, null);
    }

     public HttpRequestConstants(String url, String method, String json, String authorization, Map<String, String> requestProperties) {
         this.url = url;
         this.method = method;
         this.json = json;
         this.authorization = authorization;
         this.requestProperties = requestProperties;
     }

     public HttpResponse request()  {

        HttpResponse response = new HttpResponse();

        HttpURLConnection con;
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
        } catch (IOException e) {
            return response;
        }

        try {

            if(method != null)
                con.setRequestMethod(method);

            if(authorization != null)
                con.setRequestProperty("Authorization", this.authorization);

            if(requestProperties != null)
                for (Map.Entry<String, String> entry : requestProperties.entrySet())
                    con.setRequestProperty(entry.getKey(), entry.getValue());

            if(json != null) {
                con.setDoOutput(true);
                final byte[] bytes = json.getBytes("UTF-8");
                con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                con.setRequestProperty("Content-Length", "" + bytes.length);
                IO.write(con.getOutputStream(), bytes);
            }

            response.code = con.getResponseCode();
            response.body = valid(response.code) ?
                    IO.read(con.getInputStream()) :
                    IO.read(con.getErrorStream());

        } catch (IOException ignored) {
        } finally {
            con.disconnect();
        }
        return response;
    }

    private boolean valid(int code) {
        return code < 400;
    }
}
