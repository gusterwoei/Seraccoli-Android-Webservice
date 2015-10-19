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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Gusterwoei on 10/30/13.
 */
public class WebService {
    // global variables
    private static int CONNECTION_TIMEOUT = 30000;
    private static int SOCKET_TIMEOUT = 30000;
    private static List<HttpHeader> globalHeaders = new ArrayList<HttpHeader>();

    private Context context;
    //private RequestHandler requestHandler;

    public WebService(Context context) {
        this.context = context.getApplicationContext();
        //requestHandler = new RequestHandler();
    }

    public static RequestBuilder newRequest() {
        RequestBuilder requestBuilder = new RequestBuilder();
        return requestBuilder;
    }

    public Context getContext() {
        return context;
    }

    /*protected RequestHandler getRequestHandler() {
        return requestHandler;
    }*/

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
    public static final class RequestBuilder implements WebServiceInterface {
        private static final String TAG = "SkyWebService";
        // HTTP socket parameters
        private int connectionTimeout = CONNECTION_TIMEOUT;
        private int socketTimeout = SOCKET_TIMEOUT;
        private HttpClient httpClient;
        private HttpAuthenticator httpAuthenticator;
        private HttpRequestBase request;
        private WebServiceListener webServiceListener;
        private List<HttpHeader> headers = new ArrayList<HttpHeader>(); // http headers
        private Executor asyncTaskExecutor = AsyncTask.SERIAL_EXECUTOR;

        // HttpUrlConnection
        private HttpURLConnection urlConnection;
        private String payload;
        private HttpEntity multiformEntity;

        public RequestBuilder() {
            init();
        }

        public RequestBuilder(HttpRequestBase request) {
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

        public RequestBuilder setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public int getSocketTimeout() {
            return socketTimeout;
        }

        public RequestBuilder setSocketTimeout(int socketTimeout) {
            this.socketTimeout = socketTimeout;
            return this;
        }

        public WebServiceListener getWebServiceListener() {
            return webServiceListener;
        }

        public RequestBuilder withResponse(WebServiceListener webServiceListener) {
            this.webServiceListener = webServiceListener;
            return this;
        }

        /*public void setRequest(HttpRequestBase request) {
            this.request = request;
        }*/
        public void setRequest(HttpURLConnection urlConnection, String payload, HttpEntity multiformEntity) {
            this.urlConnection = urlConnection;
            this.payload = payload;
            this.multiformEntity = multiformEntity;
        }

        private RequestBuilder setHeader(String name, String value) {
            return this;
        }

        public RequestBuilder addHeader(String name, String value) {
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
        public RequestBuilder setAuthentication(String usrname, String pwd) {
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
         * @param link
         * @return
         */
        @Override
        public RequestBuilder get(String link) {
            /*HttpGet get = new HttpGet(link);
            setRequest(get);*/

            try {
                URL url = new URL(link);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(false);
                setRequest(urlConnection, null, null);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "WebService: " + e.getMessage());
            }

            return this;
        }


        /**
         * HTTP POST
         *
         * @param link
         * @param payload a content payload, could be string, json, xml, etc
         * @return
         */
        @Override
        public RequestBuilder post(String link, String payload) {
            /*HttpPost post = new HttpPost(link);
            StringEntity entity;
            try {
                entity = new StringEntity(payload);
                post.setEntity(entity);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            setRequest(post);*/


            try {
                URL url = new URL(link);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                setRequest(urlConnection, payload, null);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "WebService: " + e.getMessage());
            }

            return this;
        }

        /*@Override
        public RequestBuilder post(String url, String fileName, File fileToUpload) {
            //InputStreamEntity entity = new InputStreamEntity(new FileInputStream(fileToUpload), -1);
            ContentBody contentBody = new FileBody(fileToUpload);
            uploadFileToServer(url, fileName, contentBody);

            return this;
        }

        @Override
        public RequestBuilder post(String url, String fileName, InputStream stream) {
            ContentBody contentBody = new InputStreamBody(stream, fileName);
            uploadFileToServer(url, fileName, contentBody);

            return this;
        }

        @Override
        public RequestBuilder post(String url, String fileName, byte[] bytes) {
            ContentBody contentBody = new ByteArrayBody(bytes, fileName);
            uploadFileToServer(url, fileName, contentBody);

            return this;
        }*/

        public RequestBuilder post(String url, FormContent formContent) {
            uploadFileToServer(url, formContent);
            return this;
        }

        //private RequestBuilder uploadFileToServer(String link, String fileName, ContentBody contentBody) {
        private RequestBuilder uploadFileToServer(String link, FormContent formContent) {
            String boundary = "-------------" + System.currentTimeMillis();
            String contentType = "multipart/form-data; boundary=" + boundary;

            MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .setBoundary(boundary);

            // get all contents
            HashMap<String, Object> map = formContent.getContent();
            HashMap<String, String> filenameMap = formContent.getMap();
            Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                String key = entry.getKey();
                Object value = entry.getValue();

                if(value instanceof String) {
                    builder.addTextBody(key, (String) value);

                } else if(value instanceof Bitmap) {
                    String filename = filenameMap.get(key);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ((Bitmap) value).compress(Bitmap.CompressFormat.PNG, 0, baos);
                    builder.addPart(key, new ByteArrayBody(baos.toByteArray(), filename));

                } else if(value instanceof File) {
                    builder.addPart(key, new FileBody((File) value));

                } else if(value instanceof InputStream) {
                    String filename = filenameMap.get(key);
                    builder.addPart(key, new InputStreamBody((InputStream) value, filename));

                } else {
                    // byte array
                    String filename = filenameMap.get(key);
                    builder.addPart(key, new ByteArrayBody((byte[]) value, filename));
                }
            }

            HttpEntity entity = builder.build();

            /*HttpEntity entity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .setBoundary(boundary)
                    .addPart(fileName, contentBody)
                    .build();*/

            /*HttpPost post = new HttpPost(url);
            post.setHeader("Content-type", contentType);
            post.setEntity(entity);
            setRequest(post);*/

            try {
                URL url = new URL(link);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Connection", "Keep-Alive");
                urlConnection.setRequestProperty("Content-type", contentType);
                urlConnection.addRequestProperty(entity.getContentType().getName(), entity.getContentType().getValue());

                setRequest(urlConnection, null, entity);

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "WebService: " + e.getMessage());
            }

            return this;
        }


        /**
         * HTTP PUT
         *
         * @param link
         * @param payload a content payload, could be string, json, xml, etc
         * @return
         */
        @Override
        public RequestBuilder put(String link, String payload) {
            /*HttpPut put = new HttpPut(url);
            StringEntity entity;
            try {
                entity = new StringEntity(payload);
                put.setEntity(entity);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            setRequest(put);*/
            try {
                URL url = new URL(link);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("PUT");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                setRequest(urlConnection, payload, null);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "WebService: " + e.getMessage());
            }

            return this;
        }


        /**
         * HTTP DELETE
         *
         * @param link
         * @return
         */
        @Override
        public RequestBuilder delete(String link) {
            /*HttpDelete delete = new HttpDelete(url);
            setRequest(delete);*/

            try {
                URL url = new URL(link);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("DELETE");
                urlConnection.setDoInput(false);
                urlConnection.setDoOutput(true);
                setRequest(urlConnection, null, null);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "WebService: " + e.getMessage());
            }

            return this;
        }


        /**
         * Send Request to the server
         */
        @Override
        public void send() {
            // Initializing parameters
            urlConnection.setConnectTimeout(connectionTimeout);
            urlConnection.setReadTimeout(socketTimeout);

            // check if there is any http authentication
            if (httpAuthenticator != null) {
                urlConnection.setRequestProperty("Authorization", httpAuthenticator.getPasswordAuthentication());
            }

            // add the user defined http headers accordingly
            for (HttpHeader h : headers) {
                urlConnection.setRequestProperty(h.getName(), h.getValue());
            }

            // Initializing parameters
            /*HttpParams httpParams = new BasicHttpParams();
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
            }*/

            // send the request to server
            if(webServiceListener != null)
                webServiceListener.onPrepare(this);
            new AsyncTask<Void, Void, Response>() {
                @Override
                protected Response doInBackground(Void... voids) {
                    Response response = null;
                    try {
                        //response = send(request);
                        response = send(urlConnection);
                        if(webServiceListener != null)
                            webServiceListener.onReceiveInBackground(response, (response != null && response.success()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                }

                @Override
                protected void onPostExecute(Response response) {
                    if(webServiceListener != null) webServiceListener.onReceive(response, (response != null && response.success()));
                }
            }.executeOnExecutor(asyncTaskExecutor);
        }

        private Response validateResponse(Response response) {
            if(response == null) {
                response = new Response();
                response.setStatusCode(HttpStatus.SC_REQUEST_TIMEOUT);
            }

            return response;
        }


        /**
         * Main method of sending HTTP Request to the server
         *
         * @return Response, if no response from the server or no internet connection,
         * this object will return null
         * @throws IOException
         */
        private Response send(HttpURLConnection urlConnection) throws IOException {
            // connect to the server
            urlConnection.connect();

            if(urlConnection.getDoOutput()) {
                DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());

                if(payload != null)
                    dos.writeBytes(URLEncoder.encode(payload, "UTF-8"));

                if(multiformEntity != null) {
                    multiformEntity.writeTo(dos);
                }

                dos.flush();
                dos.close();
            }

            InputStream rawInputStream = urlConnection.getInputStream();

            // variables to be put into Response
            int statusCode = urlConnection.getResponseCode();
            String statusDesc = urlConnection.getResponseMessage();
            long contentLength = urlConnection.getContentLength();
            String response = "";

            if(payload != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(rawInputStream));
                String line;
                // only save as string object if it is a media object
                try {
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    rawInputStream.close();
                    response = stringBuilder.toString();
                } catch (IOException e) {
                    Log.e(TAG, "Response getResponse() Exception: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // onPrepare the response
            Response responseObject = new Response();
            responseObject.setStatusCode(statusCode);
            responseObject.setStatusDesc(statusDesc);
            responseObject.setContentLength(contentLength);
            responseObject.setRawResponse(rawInputStream);
            responseObject.setResponse(response);
            responseObject.setContentEncoding(urlConnection.getContentEncoding());
            responseObject.setContentType(urlConnection.getContentType());
            responseObject.setUrl(urlConnection.getURL().toString());

            return responseObject;
        }
        /*private Response send(HttpRequestBase rq) throws IOException {
            HttpResponse httpResponse = httpClient.execute(rq);
            HttpEntity entity = httpResponse.getEntity();
            BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
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
        }*/
    }
}