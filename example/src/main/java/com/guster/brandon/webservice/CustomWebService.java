package com.guster.brandon.webservice;

import android.content.Context;
import com.guster.brandon.library.webservice.WebService;
import com.guster.brandon.library.webservice.WebServiceListener;

import org.json.JSONObject;

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
}
