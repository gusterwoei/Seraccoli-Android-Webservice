/**
 * Copyright 2014 Gusterwoei

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.guster.skywebservice.library.webservice;

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
    private String contentEncoding;
    private String contentType;
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

    /*public Header getContentEncoding() {
        return contentEncoding;
    }*/
    /*public void setContentEncoding(Header contentEncoding) {
        this.contentEncoding = contentEncoding;
    }*/

    public String getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    /*public Header getContentType() {
        return contentType;
    }

    public void setContentType(Header contentType) {
        this.contentType = contentType;
    }*/

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
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

        /*BufferedReader reader = new BufferedReader(new InputStreamReader(rawResponse));
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
            Log.e("ABC", "Response getResponse() Exception: " + e.getMessage());
            e.printStackTrace();
        }*/

        return response;
    }

    public InputStream getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(InputStream rawResponse) {
        this.rawResponse = rawResponse;
    }

    public boolean success() {
        return statusCode > 0 && statusCode < 400;
    }
}
