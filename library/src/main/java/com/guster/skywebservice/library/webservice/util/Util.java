package com.guster.skywebservice.library.webservice.util;

import android.util.Log;

/**
 * Created by Gusterwoei on 12/19/15.
 */
class Util {
    public static void logd(String msg) {
        Log.d("ABC", msg != null ? msg : "null");
    }

    public static void loge(String msg) {
        Log.e("ABC", msg != null ? msg : "null");
    }
}
