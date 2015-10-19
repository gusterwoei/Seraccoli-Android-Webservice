package com.guster.skywebservice.sample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.guster.skywebservice.library.webservice.FormContent;
import com.guster.skywebservice.library.webservice.WebService;
import com.guster.skywebservice.library.webservice.WebServiceListener;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

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
        newRequest().get(url).withResponse(listener).send();
    }

    public void sendPostRequest(JSONObject payload, WebServiceListener listener) {
        String url = "http://date.jsontest.com";
        newRequest().post(url, payload.toString()).withResponse(listener).send();
    }

    public void uploadFile(InputStream stream, WebServiceListener listener) throws FileNotFoundException {

        String url = "http://192.168.1.138:10000";
        FormContent formContent = FormContent.create()
                .addContent("file_name", "Steve")
                .addContent("format", "png")
                .addContent("file", stream, "okFile");

        newRequest().post(url, formContent)
                .withResponse(listener)
                .send();
    }
}
