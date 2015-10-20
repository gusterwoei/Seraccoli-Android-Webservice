package com.guster.skywebservice.library.webservice;

import android.graphics.Bitmap;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by Gusterwoei on 10/19/15.
 */
public class FormContent {
    private HashMap<String, Object> map = new HashMap<>();
    private HashMap<String, String> fileNameMap = new HashMap<>();

    public static FormContent create() {
        return new FormContent();
    }

    private FormContent() {

    }

    public FormContent addContent(String name, String value) {
        map.put(name, value);
        return this;
    }

    public FormContent addContent(String name, Bitmap bitmap, String fileName) {
        map.put(name, bitmap);
        fileNameMap.put(name, fileName);
        return this;
    }

    public FormContent addContent(String name, File file) {
        map.put(name, file);
        return this;
    }

    public FormContent addContent(String name, InputStream inputStream, String fileName) {
        map.put(name, inputStream);
        fileNameMap.put(name, fileName);
        return this;
    }

    public FormContent addContent(String name, byte[] bytes, String fileName) {
        map.put(name, bytes);
        fileNameMap.put(name, fileName);
        return this;
    }

    public HashMap<String, Object> getContent() {
        return map;
    }

    public HashMap<String, String> getMap() {
        return fileNameMap;
    }
}
