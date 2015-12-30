package com.guster.skywebservice.library.webservice;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLSocketFactory;

/**
 * Created by Gusterwoei on 10/19/15.
 */
interface RequestBuilderInterface {
    int getConnectionTimeout();
    SkyHttp.RequestBuilder setConnectionTimeout(int connectionTimeout);
    int getSocketTimeout();
    SkyHttp.RequestBuilder setSocketTimeout(int socketTimeout);
    SkyHttp.Callback getCallback();
    SkyHttp.RequestBuilder addHeader(String name, String value);
    void removeHeader(String name);
    HttpHeader findHeader(String name);
    void removeAllHeaders();
    List<HttpHeader> getHeaders();
    SkyHttp.RequestBuilder setAuthentication(String usrname, String pwd);
    SkyHttp.RequestBuilder setAsyncTaskExecutor(Executor asyncTaskExecutor);
    SkyHttp.RequestBuilder setSSLSocketFactory(SSLSocketFactory sslSocketFactory);
    SkyHttp.RequestBuilder setSSLCertificate(InputStream certificateFile) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException;

    SkyHttp.RequestBuilder get(String url);
    SkyHttp.RequestBuilder post(String url, String payload);
    SkyHttp.RequestBuilder post(String url, FormContent formContent);
    SkyHttp.RequestBuilder put(String url, String payload);
    SkyHttp.RequestBuilder delete(String url);
    SkyHttp.RequestBuilder head(String url);
    SkyHttp.RequestBuilder encode();
    SkyHttp.RequestBuilder encode(String charset);
    void send();
    void send(SkyHttp.Callback callback);
}
