package com.guster.skywebservice.library.webservice;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Gusterwoei on 10/19/15.
 */
public interface WebServiceInterface {
    WebService.RequestBuilder get(String url);
    WebService.RequestBuilder post(String url, String payload);
    /*WebService.RequestBuilder post(String url, String fileName, File fileToUpload);
    WebService.RequestBuilder post(String url, String fileName, InputStream stream);
    WebService.RequestBuilder post(String url, String fileName, byte[] bytes);*/
    WebService.RequestBuilder put(String url, String payload);
    WebService.RequestBuilder delete(String url);
    void send();
}
