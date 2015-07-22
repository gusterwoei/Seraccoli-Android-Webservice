package com.guster.skywebservice.sample;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.guster.skywebservice.library.webservice.Response;
import com.guster.skywebservice.library.webservice.WebService;
import com.guster.skywebservice.library.webservice.WebServiceListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gusterwoei on 8/24/14.
 */
public class MainFragment extends Fragment implements View.OnClickListener {
    private Spinner urlSpinner;
    private Button btnSend;
    private TextView txtContent, txtUploadFile;
    private View lytContainer;
    private View lytRetry;
    private Button btnRetry, btnUpload;
    private ProgressBar progressBar;
    private ImageView imgImage;

    private CustomWebService customWebService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        urlSpinner = (Spinner) rootView.findViewById(R.id.spinner_url);
        btnSend = (Button) rootView.findViewById(R.id.btn_send);
        btnUpload = (Button) rootView.findViewById(R.id.btn_upload);
        txtContent = (TextView) rootView.findViewById(R.id.txt_content);
        txtUploadFile = (TextView) rootView.findViewById(R.id.txt_upload_file);
        lytContainer = rootView.findViewById(R.id.lyt_container);
        lytRetry = rootView.findViewById(R.id.lyt_retry);
        btnRetry = (Button) rootView.findViewById(R.id.btn_retry);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        imgImage = (ImageView) rootView.findViewById(R.id.img_image);

        btnSend.setOnClickListener(this);
        btnRetry.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
        txtUploadFile.setOnClickListener(this);
        customWebService = new CustomWebService(getActivity());

        // set global web service properties
        WebService.addHeader("AppVersion", "1.0");
        WebService.addHeader("Content-Type", "application/json");
        WebService.setConnectionTimeout(60000);

        loadUrls();

        return rootView;
    }

    private void loadUrls() {
        List<String> urls = new ArrayList<String>();
        urls.add("http://www.facebook.com");
        urls.add("http://echo.jsontest.com/key/value/one/two");
        urls.add("http://date.jsontest.com");
        urls.add("http://upload.wikimedia.org/wikipedia/commons/b/b7/Big_smile.png");
        MyAdapter adapter = new MyAdapter(urls);
        urlSpinner.setAdapter(adapter);
    }


    @Override
    public void onClick(View view) {
        if(view == btnSend) {
            String url = (String) urlSpinner.getSelectedItem();
            try {
                sendRequest(url);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if(view == btnRetry) {
            lytRetry.setVisibility(View.GONE);
        } else if(view == txtUploadFile) {

        }
    }


    /**
     * Send Request
     * @param url
     */
    private void sendRequest(String url) throws JSONException {
        WebService.RequestBuilder requestBuilder = WebService.newRequest()
                .setSocketTimeout(60000)
                .withResponse(webServiceListener);

        // send HTTP requests to server
        if(urlSpinner.getSelectedItemPosition() == 1) {
            // send as POST request
            JSONObject payload = new JSONObject();
            payload.put("key1", "value1");
            payload.put("key2", "value2");
            requestBuilder.post(url, payload.toString());
        } else {
            // send as GET request
            requestBuilder.get(url);
        }
    }


    private WebServiceListener webServiceListener = new WebServiceListener() {

        @Override
        public void onPrepare(WebService.RequestBuilder requestBuilder) {
            String url = requestBuilder.getRequest().getURI().toString();
            Toast.makeText(getActivity(), "Sending request to: " + url, Toast.LENGTH_LONG).show();
            showProgressbar(true);
        }

        @Override
        public void onReceive(Response response, boolean success) {
            Log.d("ABC", (response != null)? response.getResponse() : "no response");
            showProgressbar(false);

            // no response, either request timeout due to server no respond or loss of internet connection
            if(response == null) {
                txtContent.setVisibility(View.GONE);
                lytRetry.setVisibility(View.VISIBLE);
            }

            if(success) {
                // Note: for image, video or binary content, calling getResponse() may cause OutOfMemoryException
                // when trying to convert to string, use with care
                String contentType = response.getContentType().getValue();
                if (contentType.contains("image/")) {
                    imgImage.setVisibility(View.VISIBLE);
                    txtContent.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    imgImage.setImageBitmap(BitmapFactory.decodeStream(response.getRawResponse()));
                } else {
                    String content = response.getResponse();
                    txtContent.setText(content);
                }
            } else {
                if(response != null) {
                    String content = response.getResponse();
                    txtContent.setText(content);
                }
            }
        }
    };


    private void showProgressbar(boolean show) {
        if(show) {
            txtContent.setVisibility(View.GONE);
            imgImage.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            txtContent.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }


    /**
     * Spinner Adapter class
     */
    private class MyAdapter extends BaseAdapter {
        private List data;

        public MyAdapter(List data) {
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int i) {
            return data.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(android.R.layout.simple_list_item_1, null);
            }

            String s = (String) getItem(i);
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(s);

            return view;
        }

        @Override
        public View getDropDownView(int i, View view, ViewGroup parent) {
            if(view == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(android.R.layout.simple_list_item_1, null);
            }

            String s = (String) getItem(i);
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(s);

            return view;
        }
    }
}
