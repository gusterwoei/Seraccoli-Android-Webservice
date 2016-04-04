package com.guster.skywebservice.library.webservice.util;

/**
 * Created by Gusterwoei on 4/4/16.
 */
public class SkyHttpException extends RuntimeException {
    public SkyHttpException() {
    }

    public SkyHttpException(String detailMessage) {
        super(detailMessage);
    }

    public SkyHttpException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public SkyHttpException(Throwable throwable) {
        super(throwable);
    }
}
