package com.guster.brandon.library.webservice;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created by Gusterwoei on 8/24/14.
 */
public class RequestHandler {
    // HTTP socket parameters
    public static int CONNECTION_TIMEOUT = 60000;
    public static int SOCKET_TIMEOUT = 60000;

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

    private void init() {}

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

    public RequestHandler setHeader(String name, String value) {
        return this;
    }

    public RequestHandler addHeader(String name, String value) {
        headers.add(new HttpHeader(name, value));
        return this;
    }


    /**
     * Set Authentication credentials
     * @param usrname credential username
     * @param pwd credential password
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
     * Send Request to the server
     */
    public void send() {
        // Initializing parameters
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeout);
        HttpConnectionParams.setSoTimeout(httpParams, socketTimeout);

        httpClient = new DefaultHttpClient(httpParams);

        // check if there is any http authentication
        if(httpAuthenticator != null) {
            request.setHeader("Authorization", httpAuthenticator.getPasswordAuthentication());
        }

        // add the user defined http headers accordingly
        for(HttpHeader h : headers) {
            request.addHeader(h.getName(), h.getValue());
        }

        // send the request to server
        listener.prepare(this);
        new AsyncTask<Void, Void, Response>() {
            @Override
            protected Response doInBackground(Void... voids) {
                Response response = null;
                try {
                    response = send(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }

            @Override
            protected void onPostExecute(Response response) {
                if(response != null && response.success()) {
                    listener.success(response);
                } else {
                    listener.failed(response);
                }
            }
        }.executeOnExecutor(asyncTaskExecutor);
    }


    /**
     * Main method of sending HTTP Request to the server
     * @param rq Apache HTTP Base Request
     * @return Response, if no response from the server or no internet connection,
     *          this object will return null
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

        // prepare the response
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


    /**
     * HttpHeader - Inner class that represents a HTTP Header
     */
    private class HttpHeader {
        private String name;
        private String value;

        private HttpHeader(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }


    /**
     * Web Service Event Listener
     */
    public interface WebServiceListener {
        void prepare(RequestHandler requestHandler);
        void success(Response response);
        void failed(Response response);
    }
}
