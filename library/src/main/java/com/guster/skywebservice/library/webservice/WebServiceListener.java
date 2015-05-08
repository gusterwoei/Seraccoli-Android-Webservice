package com.guster.skywebservice.library.webservice;

public abstract class WebServiceListener {

    public void onPrepare(WebService.RequestBuilder requestBuilder) {
        // do something
    }

    public void onReceiveInBackground(Response response, boolean success) {
        // do something
    }

    public abstract void onReceive(Response response, boolean success);
}