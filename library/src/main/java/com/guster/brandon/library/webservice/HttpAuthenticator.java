package com.guster.brandon.library.webservice;

import android.util.Base64;

/**
 * Created by Gusterwoei on 10/18/13.
 */
public class HttpAuthenticator {
    private String username;
    private String password;

    public HttpAuthenticator() {}
    public HttpAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getPasswordAuthentication() {
        return "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAuthentication(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public boolean noAuthentication() {
        return (username==null && password==null);
    }
}
