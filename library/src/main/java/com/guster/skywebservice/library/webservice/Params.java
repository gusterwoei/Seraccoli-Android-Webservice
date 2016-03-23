package com.guster.skywebservice.library.webservice;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gusterwoei on 3/23/16.
 */
public class Params {
    private HashMap<String, String> params = new HashMap<>();

    public Params add(String paramName, String paramValue) {
        params.put(paramName, paramValue);
        return this;
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public String getEncodedParams() throws UnsupportedEncodingException {
        return getEncodedParams("UTF-8");
    }

    public String getEncodedParams(String urlEncodeCharset) throws UnsupportedEncodingException {
        String str = "";
        if(params != null && !params.isEmpty()) {
            //str += "?";
            int i = 0;
            for(Map.Entry<String, String> entry : params.entrySet()) {
                str += entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), urlEncodeCharset != null ? urlEncodeCharset : "UTF-8");
                if(i < params.size() - 1) {
                    str += "&";
                }

                i++;
            }
        }

        return str;
    }

    public void clear() {
        params.clear();
    }
}
