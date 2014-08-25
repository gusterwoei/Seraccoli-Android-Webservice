package com.guster.brandon.library.webservice;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import java.io.UnsupportedEncodingException;

/**
 * Created by Gusterwoei on 10/30/13.
 */
public class WebService {

    public WebService() {}


    /**
     * HTTP GET
     * @param url
     * @return
     */
    public RequestHandler get(String url) {
        HttpGet get = new HttpGet(url);
        get.setHeader("Content-Type", "application/json");

        RequestHandler requestHandler = new RequestHandler(get);
        return requestHandler;
    }


    /**
     * HTTP POST
     * @param url
     * @param payload a content payload, could be string, json, xml, etc
     * @return
     */
    public RequestHandler post(String url, String payload) {
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/json");
        StringEntity entity;
        try {
            entity = new StringEntity(payload);
            post.setEntity(entity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        RequestHandler requestHandler = new RequestHandler(post);
        return requestHandler;
    }


    /**
     * HTTP PUT
     * @param url
     * @param payload a content payload, could be string, json, xml, etc
     * @return
     */
    public RequestHandler put(String url, String payload) {
        HttpPut put = new HttpPut(url);
        put.setHeader("Content-Type", "application/json");
        StringEntity entity;
        try {
            entity = new StringEntity(payload);
            put.setEntity(entity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        RequestHandler requestHandler = new RequestHandler(put);
        return requestHandler;
    }


    /**
     * HTTP DELETE
     * @param url
     * @return
     */
    public RequestHandler delete(String url) {
        HttpDelete delete = new HttpDelete(url);
        delete.setHeader("Content-Type", "application/json");

        RequestHandler requestHandler = new RequestHandler(delete);
        return requestHandler;
    }
}