package com.guster.skywebservice.sample;

import android.content.Context;
import android.util.Log;

import com.guster.skywebservice.library.webservice.FormContent;
import com.guster.skywebservice.library.webservice.Response;
import com.guster.skywebservice.library.webservice.SkyHttp;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Created by Gusterwoei on 9/10/14.
 * Example of creating a custom WebService
 */
public class CustomWebService extends SkyHttp {

    public CustomWebService(Context context) {
        super(context);

        initSSLCertificate();
    }

    private void initSSLCertificate() {
        logd("initializing certificate...");

        // get CA from raw folder
        InputStream is = getContext().getResources().openRawResource(R.raw.mycertificate2);

        try {
            setSSLCertificate(is);

        } catch (CertificateException e) {
            e.printStackTrace();
            loge("CERT: " + e.getMessage());
        } catch (KeyStoreException e) {
            e.printStackTrace();
            loge("KEYSTORE: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            loge("ALGO: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            loge("IO: " + e.getMessage());
        } catch (KeyManagementException e) {
            e.printStackTrace();
            loge("KEY_MGNT: " + e.getMessage());
        }
    }

    public void getFacebookPage(Callback listener) {
        String url = "https://www.facebook.com";
        newRequest().get(url).send(listener);
    }

    public void sendPostRequest(JSONObject payload, Callback listener) {
        String url = "http://date.jsontest.com";
        newRequest().post(url, payload.toString()).send(listener);
    }

    public void testHttpsConnection() {
        String url = "https://certs.cac.washington.edu/CAtest/";
        newRequest().get(url).send(new Callback() {
            @Override
            public void onResponse(Response response, boolean success, Object... args) {
                logd("Test HTTPS response... success(" + success + ")");
                logd(response.getResponse());
            }
        });
    }

    public void uploadFile(InputStream stream, Callback listener) throws FileNotFoundException {
        String url = "http://192.168.1.138:10000";
        FormContent formContent = FormContent.create()
                .addContent("file_name", "Steve")
                .addContent("format", "png")
                .addContent("file", stream, "okFile");

        newRequest().post(url, formContent).send(listener);
    }

    private static void logd(String msg) {
        Log.d("ABC", msg != null ? msg : "null");
    }

    private static void loge(String msg) {
        Log.e("ABC", msg != null ? msg : "null");
    }
}
