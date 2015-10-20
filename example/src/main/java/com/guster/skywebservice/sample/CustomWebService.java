package com.guster.skywebservice.sample;

import android.content.Context;

import com.guster.skywebservice.library.webservice.FormContent;
import com.guster.skywebservice.library.webservice.SkyHttp;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by Gusterwoei on 9/10/14.
 * Example of creating a custom WebService
 */
public class CustomWebService extends SkyHttp {

    public CustomWebService(Context context) {
        super(context);
    }

    public void getFacebookPage(Callback listener) {
        String url = "https://www.facebook.com";
        newRequest().get(url).send(listener);
    }

    public void sendPostRequest(JSONObject payload, Callback listener) {
        String url = "http://date.jsontest.com";
        newRequest().post(url, payload.toString()).send(listener);
    }

    public void uploadFile(InputStream stream, Callback listener) throws FileNotFoundException {

        String url = "http://192.168.1.138:10000";
        FormContent formContent = FormContent.create()
                .addContent("file_name", "Steve")
                .addContent("format", "png")
                .addContent("file", stream, "okFile");

        newRequest().post(url, formContent).send(listener);
    }
}
