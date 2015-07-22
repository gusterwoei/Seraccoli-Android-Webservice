package com.guster.skywebservice.sample;

import android.content.Context;
import com.guster.skywebservice.library.webservice.WebService;
import com.guster.skywebservice.library.webservice.WebServiceListener;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by Gusterwoei on 9/10/14.
 * Example of creating a custom WebService
 */
public class CustomWebService extends WebService {

    public CustomWebService(Context context) {
        super(context);
    }

    public void setListener(WebServiceListener listener) {
        newRequest().withResponse(listener);
    }

    public void getFacebookPage(WebServiceListener listener) {
        String url = "http://www.facebook.com";
        newRequest().get(url).withResponse(listener);
    }

    public void sendPostRequest(JSONObject payload, WebServiceListener listener) {
        String url = "http://date.jsontest.com";
        newRequest().post(url, payload.toString()).withResponse(listener);
    }

    public void uploadFile(File file, WebServiceListener listener) throws FileNotFoundException {
        String url = "http://www.g-i.com.my:10000";
        newRequest().post(url, "myFile", file).withResponse(listener);
    }
}
