package com.guster.brandon.library.webservice;

import android.util.Log;

import org.apache.http.Header;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Gusterwoei on 8/24/14.
 */
public class Response {
    private int statusCode;
    private String statusDesc;
    private long contentLength;
    private Header contentEncoding;
    private Header contentType;
    private String response;
    private InputStream rawResponse; // for media, binary files
    private String url;

    public Response() {}

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public Header getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(Header contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public Header getContentType() {
        return contentType;
    }

    public void setContentType(Header contentType) {
        this.contentType = contentType;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getResponse() {
        // if the response is already stringified before, return directly
        if(response != null) return response;

        BufferedReader reader = new BufferedReader(new InputStreamReader(rawResponse));
        String line;

        // only save as string object if it is a media object
        try {
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            rawResponse.close();
            response = stringBuilder.toString();
        } catch (IOException e) {
            Log.d("NISSAN", "SOMETHING IS WRONG: " + e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

    public InputStream getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(InputStream rawResponse) {
        this.rawResponse = rawResponse;
    }

    public boolean success() {
        return statusCode == 200;
    }
}
