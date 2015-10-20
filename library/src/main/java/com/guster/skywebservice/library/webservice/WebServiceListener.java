package com.guster.skywebservice.library.webservice;

public abstract class WebServiceListener {

    //public void onPrepare(WebService.RequestBuilder requestBuilder) {}

    public void onReceiveInBackground(Response response, boolean success) {}

    public abstract void onReceive(Response response, boolean success);
}