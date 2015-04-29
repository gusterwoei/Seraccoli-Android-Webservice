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

package com.guster.brandon.library.webservice;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created by Gusterwoei on 10/30/13.
 */
public class WebService {
    // global variables
    private static int CONNECTION_TIMEOUT = 30000;
    private static int SOCKET_TIMEOUT = 30000;
    private static List<HttpHeader> globalHeaders = new ArrayList<HttpHeader>();

    private Context context;
    private RequestHandler requestHandler;

    public WebService(Context context) {
        this.context = context.getApplicationContext();
        requestHandler = new RequestHandler();
    }

    public static RequestHandler newRequest() {
        RequestHandler requestHandler = new RequestHandler();
        return requestHandler;
    }

    public Context getContext() {
        return context;
    }

    protected RequestHandler getRequestHandler() {
        return requestHandler;
    }

    public static void addHeader(String header, String value) {
        globalHeaders.add(new HttpHeader(header, value));
    }

    public static void setSocketTimeout(int duration) {
        CONNECTION_TIMEOUT = duration;
    }

    public static void setConnectionTimeout(int duration) {
        SOCKET_TIMEOUT = duration;
    }


    /**
     * HTTP request object handler
     *
     */
    public static class RequestHandler {
        // HTTP socket parameters

        private HttpClient httpClient;
        private HttpAuthenticator httpAuthenticator;
        private int connectionTimeout = CONNECTION_TIMEOUT;
        private int socketTimeout = SOCKET_TIMEOUT;
        private HttpRequestBase request;
        private WebServiceListener listener;
        private List<HttpHeader> headers = new ArrayList<HttpHeader>(); // http headers
        private Executor asyncTaskExecutor = AsyncTask.SERIAL_EXECUTOR;

        public RequestHandler() {
            init();
        }

        public RequestHandler(HttpRequestBase request) {
            this.request = request;
            init();
        }

        private void init() {
            for(HttpHeader header : globalHeaders) {
                addHeader(header.getName(), header.getValue());
            }
        }

        public HttpRequestBase getRequest() {
            return request;
        }

        public int getConnectionTimeout() {
            return connectionTimeout;
        }

        public RequestHandler setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public int getSocketTimeout() {
            return socketTimeout;
        }

        public RequestHandler setSocketTimeout(int socketTimeout) {
            this.socketTimeout = socketTimeout;
            return this;
        }

        public WebServiceListener getListener() {
            return listener;
        }

        public RequestHandler setListener(WebServiceListener listener) {
            this.listener = listener;
            return this;
        }

        public void setRequest(HttpRequestBase request) {
            this.request = request;
        }

        private RequestHandler setHeader(String name, String value) {
            return this;
        }

        public RequestHandler addHeader(String name, String value) {
            HttpHeader h = findHeader(name);
            if (h != null && h.getName().equals(name) && h.getValue().equals(value))
                return this;
            else {
                removeHeader(name);
                headers.add(new HttpHeader(name, value));
            }
            return this;
        }

        public void removeHeader(String name) {
            int x = findHeaderIndex(name);
            if (x != -1) {
                headers.remove(x);
            }
        }

        public HttpHeader findHeader(String name) {
            int x = findHeaderIndex(name);
            return (x != -1) ? headers.get(x) : null;
        }

        private int findHeaderIndex(String name) {
            int x = -1;
            for (int i = 0; i < headers.size(); i++) {
                HttpHeader header = headers.get(i);
                if (header.getName().equals(name)) {
                    x = i;
                    break;
                }
            }
            return x;
        }

        public void removeAllHeaders() {
            headers.clear();
        }

        public List<HttpHeader> getHeaders() {
            return headers;
        }

        /**
         * Set Authentication credentials
         *
         * @param usrname credential username
         * @param pwd     credential password
         * @return
         */
        public RequestHandler setAuthentication(String usrname, String pwd) {
            httpAuthenticator = new HttpAuthenticator();
            httpAuthenticator.setUsername(usrname);
            httpAuthenticator.setPassword(pwd);
            return this;
        }

        public Executor getAsyncTaskExecutor() {
            return asyncTaskExecutor;
        }

        public void setAsyncTaskExecutor(Executor asyncTaskExecutor) {
            this.asyncTaskExecutor = asyncTaskExecutor;
        }


        /**
         * HTTP GET
         *
         * @param url
         * @return
         */
        public void get(String url) {
            HttpGet get = new HttpGet(url);
            get.setHeader("Content-Type", "application/json");

            setRequest(get);
            send();
        }


        /**
         * HTTP POST
         *
         * @param url
         * @param payload a content payload, could be string, json, xml, etc
         * @return
         */
        public void post(String url, String payload) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            StringEntity entity;
            try {
                entity = new StringEntity(payload);
                post.setEntity(entity);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            setRequest(post);
            send();
        }


        /**
         * HTTP PUT
         *
         * @param url
         * @param payload a content payload, could be string, json, xml, etc
         * @return
         */
        public void put(String url, String payload) {
            HttpPut put = new HttpPut(url);
            put.setHeader("Content-Type", "application/json");
            StringEntity entity;
            try {
                entity = new StringEntity(payload);
                put.setEntity(entity);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            setRequest(put);
            send();
        }


        /**
         * HTTP DELETE
         *
         * @param url
         * @return
         */
        public void delete(String url) {
            HttpDelete delete = new HttpDelete(url);
            delete.setHeader("Content-Type", "application/json");

            setRequest(delete);
            send();
        }


        /**
         * Send Request to the server
         */
        public void send() {
            // Initializing parameters
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeout);
            HttpConnectionParams.setSoTimeout(httpParams, socketTimeout);

            httpClient = new DefaultHttpClient(httpParams);

            // check if there is any http authentication
            if (httpAuthenticator != null) {
                request.setHeader("Authorization", httpAuthenticator.getPasswordAuthentication());
            }

            // add the user defined http headers accordingly
            for (HttpHeader h : headers) {
                request.addHeader(h.getName(), h.getValue());
            }

            // send the request to server
            listener.onPrepare(this);
            new AsyncTask<Void, Void, Response>() {
                @Override
                protected Response doInBackground(Void... voids) {
                    Response response = null;
                    try {
                        response = send(request);
                        listener.onReceiveInBackground(response, (response != null && response.success()));
                        //listener.onReceive(response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                }

                @Override
                protected void onPostExecute(Response response) {
                    listener.onReceive(response, (response != null && response.success()));
                }
            }.executeOnExecutor(asyncTaskExecutor);
        }


        /**
         * Main method of sending HTTP Request to the server
         *
         * @param rq Apache HTTP Base Request
         * @return Response, if no response from the server or no internet connection,
         * this object will return null
         * @throws IOException
         */
        private Response send(HttpRequestBase rq) throws IOException {
            //Log.d("NISSAN", "WebService: Connecting to url... " + rq.getURI());
            HttpResponse httpResponse = httpClient.execute(rq);
            HttpEntity entity = httpResponse.getEntity();
            BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
            InputStream inputStream = bufHttpEntity.getContent();
            InputStream rawInputStream = bufHttpEntity.getContent();

            // variables to be put into Response
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            String statusDesc = httpResponse.getStatusLine().getReasonPhrase();
            long contentLength = entity.getContentLength();

            // onPrepare the response
            Response responseObject = new Response();
            responseObject.setStatusCode(statusCode);
            responseObject.setStatusDesc(statusDesc);
            responseObject.setContentLength(contentLength);
            responseObject.setRawResponse(rawInputStream);
            responseObject.setContentEncoding(entity.getContentEncoding());
            responseObject.setContentType(entity.getContentType());
            responseObject.setUrl(rq.getURI().toString());

            return responseObject;
        }
    }
}