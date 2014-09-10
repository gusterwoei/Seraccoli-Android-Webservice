package com.guster.brandon.webservice;

import android.content.Context;
import com.guster.brandon.library.webservice.RequestHandler;
import com.guster.brandon.library.webservice.WebService;
import org.json.JSONObject;

/**
 * Created by Gusterwoei on 9/10/14.
 * Simple Custom WebService
 */
public class CustomWebService extends WebService {

    private RequestHandler rh;

    public CustomWebService(Context context) {
        super(context);
        rh = init().setConnectionTimeout(60000)
                .setSocketTimeout(60000);
    }

    public void setListener(RequestHandler.WebServiceListener listener) {
        rh.setListener(listener);
    }

    public void getFacebookPage() {
        String url = "http://www.facebook.com";
        rh.get(url);
    }

    public void sendPostRequest(JSONObject payload) {
        String url = "http://date.jsontest.com";
        rh.post(url, payload.toString());
    }
}
