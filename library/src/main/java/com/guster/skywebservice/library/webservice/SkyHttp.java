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
import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Gusterwoei on 10/30/13.
 */
public class SkyHttp implements SkyHttpInterface {
    // global variables
    private static int CONNECTION_TIMEOUT = 30000;
    private static int SOCKET_TIMEOUT = 30000;
    private static List<HttpHeader> globalHeaders = new ArrayList<HttpHeader>();
    private static SSLSocketFactory sslSocketFactory;
    private static boolean trustAllCertificates = false;

    private Context context;

    public SkyHttp(Context context) {
        this.context = context.getApplicationContext();
    }

    protected Context getContext() {
        return context;
    }

    public static RequestBuilder newRequest() {
        RequestBuilder requestBuilder = new RequestBuilder();
        return requestBuilder;
    }

    public static void addGlobalHeader(String header, String value) {
        globalHeaders.add(new HttpHeader(header, value));
    }

    public static void setSocketTimeout(int duration) {
        CONNECTION_TIMEOUT = duration;
    }

    public static void setConnectionTimeout(int duration) {
        SOCKET_TIMEOUT = duration;
    }

    public static void setSSLSocketFactory(SSLSocketFactory factory) {
        sslSocketFactory = factory;
    }

    public static void setSSLCertificate(InputStream certificateFile)
            throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate cert = cf.generateCertificate(certificateFile);

        certificateFile.close();

        // create a keystore containing the certificate
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", cert);

        // create a trust manager for our certificate
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        // create a SSLContext that uses our trust manager
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);

        // set socket factory
        setSSLSocketFactory(context.getSocketFactory());
    }

    public static void trustAllCertificates(boolean yes) {
        trustAllCertificates = yes;
        if(yes) {
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    // Not implemented
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    // Not implemented
                }
            } };

            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                sslSocketFactory = sc.getSocketFactory();

            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }

    /*****************************************************************************
     *
     * INNER CLASS --- RequestBuilder ---
     *
     *****************************************************************************/
    public static final class RequestBuilder implements RequestBuilderInterface {

        private static final String TAG = "SkyWebService";

        // HTTP socket parameters
        private int connectionTimeout = CONNECTION_TIMEOUT;
        private int socketTimeout = SOCKET_TIMEOUT;
        private HttpAuthenticator httpAuthenticator;
        private SSLSocketFactory sslSocketFactory;
        private Callback callback;
        private List<HttpHeader> headers = new ArrayList<HttpHeader>(); // http headers
        private Executor asyncTaskExecutor = AsyncTask.SERIAL_EXECUTOR;
        private HttpURLConnection urlConnection;
        private String payload;
        private HttpEntity multiformEntity;
        private String urlEncodeCharset;

        public RequestBuilder() {
            init();
        }

        private void init() {
            for(HttpHeader header : globalHeaders) {
                addHeader(header.getName(), header.getValue());
            }
        }

        @Override
        public int getConnectionTimeout() {
            return connectionTimeout;
        }

        @Override
        public RequestBuilder setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        @Override
        public int getSocketTimeout() {
            return socketTimeout;
        }

        @Override
        public RequestBuilder setSocketTimeout(int socketTimeout) {
            this.socketTimeout = socketTimeout;
            return this;
        }

        @Override
        public Callback getCallback() {
            return callback;
        }

        private void setRequest(HttpURLConnection urlConnection, String payload, HttpEntity multiformEntity) {
            this.urlConnection = urlConnection;
            this.payload = payload;
            this.multiformEntity = multiformEntity;

            // set ssl certificate, if any
            if(urlConnection != null && (urlConnection instanceof HttpsURLConnection)) {
                if(sslSocketFactory != null && !trustAllCertificates) {
                    ((HttpsURLConnection) this.urlConnection).setSSLSocketFactory(sslSocketFactory);
                } else if(SkyHttp.sslSocketFactory != null) {
                    ((HttpsURLConnection) this.urlConnection).setSSLSocketFactory(SkyHttp.sslSocketFactory);
                }
            }
        }

        @Override
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

        @Override
        public void removeHeader(String name) {
            int x = findHeaderIndex(name);
            if (x != -1) {
                headers.remove(x);
            }
        }

        @Override
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

        @Override
        public void removeAllHeaders() {
            headers.clear();
        }

        @Override
        public List<HttpHeader> getHeaders() {
            return headers;
        }

        /**
         * Set Authentication credentials
         *
         * @param username credential username
         * @param pwd     credential password
         * @return
         */
        @Override
        public RequestBuilder setAuthentication(String username, String pwd) {
            httpAuthenticator = new HttpAuthenticator();
            httpAuthenticator.setUsername(username);
            httpAuthenticator.setPassword(pwd);
            return this;
        }

        @Override
        public RequestBuilder setAsyncTaskExecutor(Executor asyncTaskExecutor) {
            this.asyncTaskExecutor = asyncTaskExecutor;
            return this;
        }

        @Override
        public RequestBuilder setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
            this.sslSocketFactory = sslSocketFactory;
            return this;
        }

        @Override
        public RequestBuilder setSSLCertificate(InputStream certificateFile)
                throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

            SkyHttp.setSSLCertificate(certificateFile);

            return this;
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

        @Override
        public RequestBuilder post(String url, FormContent formContent) {
            uploadFileToServer(url, formContent);
            return this;
        }

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
                //urlConnection.setRequestProperty("Connection", "Keep-Alive");
                //urlConnection.setRequestProperty("Content-type", contentType);
                addHeader("Connection", "Keep-Alive");
                addHeader("Content-type", contentType);
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
            try {
                URL url = new URL(link);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("PUT");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                setRequest(urlConnection, payload, null);
                return this;
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
            try {
                URL url = new URL(link);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("DELETE");
                urlConnection.setDoInput(true);
                setRequest(urlConnection, null, null);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "WebService: " + e.getMessage());
            }

            return this;
        }


        /**
         * HTTP HEAD
         *
         * @param link
         * @return
         */
        @Override
        public RequestBuilder head(String link) {
            try {
                URL url = new URL(link);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("HEAD");
                urlConnection.setDoInput(true);
                setRequest(urlConnection, null, null);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "WebService: " + e.getMessage());
            }

            return this;
        }

        @Override
        public RequestBuilder encode() {
            urlEncodeCharset = "UTF-8";
            return this;
        }

        @Override
        public RequestBuilder encode(String charset) {
            urlEncodeCharset = charset;
            return this;
        }


        /**
         * Send Request to the server with callback
         */
        @Override
        public void send(Callback callback) {
            this.callback = callback;
            send();
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

            // send the request to server
            new AsyncTask<Void, Void, Response>() {
                @Override
                protected void onPreExecute() {
                    if(callback != null)
                        callback.onPrepare();
                }

                @Override
                protected Response doInBackground(Void... voids) {
                    Response response = null;
                    try {
                        response = send(urlConnection);
                        if(callback != null)
                            callback.onReceiveInBackground(response, (response != null && response.success()));
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "do in background: " + e.getMessage());

                        // validate if the response is due to timeout
                        response = validateResponse(response);

                        // read error stream from remote server
                        InputStream is = new BufferedInputStream(urlConnection.getErrorStream());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        String line;
                        StringBuffer sb = new StringBuffer();
                        try {
                            while((line = reader.readLine()) != null) {
                                sb.append(line);
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }

                        response.setResponse(sb.toString());
                    }
                    return response;
                }

                @Override
                protected void onPostExecute(Response response) {
                    if(callback != null)
                        callback.onResponse(response, (response != null && response.success()));
                }
            }.executeOnExecutor(asyncTaskExecutor);

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
                request.addGlobalHeader(h.getName(), h.getValue());
            }*/
        }

        private Response send(HttpURLConnection urlConnection) throws IOException {
            // connect to the server
            urlConnection.connect();

            if(urlConnection.getDoOutput()) {
                DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());

                if(payload != null) {
                    if(urlEncodeCharset == null)
                        dos.writeBytes(payload);
                    else
                        dos.writeBytes(URLEncoder.encode(payload, urlEncodeCharset));
                }

                if(multiformEntity != null) {
                    multiformEntity.writeTo(dos);
                }

                dos.flush();
                dos.close();
            }

            // variables to be put into Response
            InputStream rawInputStream = new BufferedInputStream(urlConnection.getInputStream());
            String contentEncoding = urlConnection.getContentEncoding();
            String contentType = urlConnection.getContentType();
            String response = "";

            // store as string if the response is not binary file
            if (contentType != null && isResponseStringParsable(contentType)) {
                InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                stream.close();
                response = stringBuilder.toString();
                /*try {
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    stream.close();
                    response = stringBuilder.toString();
                } catch (IOException e) {
                    Log.e(TAG, "Response getResponse() Exception: " + e.getMessage());
                    e.printStackTrace();
                }*/
            }

            // onPrepare the response
            Response responseObject = new Response();
            responseObject.setStatusCode(urlConnection.getResponseCode());
            responseObject.setStatusDesc(urlConnection.getResponseMessage());
            responseObject.setContentLength(urlConnection.getContentLength());
            responseObject.setRawResponse(rawInputStream);
            responseObject.setResponse(response);
            responseObject.setContentEncoding(contentEncoding);
            responseObject.setContentType(contentType);
            responseObject.setUrl(urlConnection.getURL().toString());
            responseObject.setHeaders(urlConnection.getHeaderFields());

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

        private boolean isResponseStringParsable(String contentType) {
            return !contentType.matches("image/.*")
                    && !contentType.matches("audio/.*")
                    && !contentType.matches("application/octet-stream")
                    && !contentType.matches("application/pdf")
                    && !contentType.matches("application/ogg")
                    && !contentType.matches("application/zip");
        }

        private Response validateResponse(Response response) {
            if(response == null) {
                response = new Response();
                response.setStatusCode(HttpStatus.SC_REQUEST_TIMEOUT);
            }

            return response;
        }
    }





    /*****************************************************************************
     *
     * INNER CLASS --- Callback ---
     *  callback function for the Http Client
     *
     * *****************************************************************************/
    public static abstract class Callback {
        public void onPrepare() {}
        public Object[] onReceiveInBackground(Response response, boolean success) {
            return null;
        }
        public abstract void onResponse(Response response, boolean success, Object ... args);
    }
}