package com.guster.brandon.library.webservice;

public abstract class WebServiceListener {

    public void onPrepare(WebService.RequestHandler requestHandler) {
        // do something
    }

    public void onReceiveInBackground(Response response, boolean success) {
        // do something
    }

    public abstract void onReceive(Response response, boolean success);
}