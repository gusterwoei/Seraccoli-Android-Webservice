package com.guster.brandon.webservice;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import com.guster.brandon.library.webservice.RequestHandler;
import com.guster.brandon.library.webservice.Response;
import com.guster.brandon.library.webservice.WebService;

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
    private TextView txtContent;
    private View lytContainer;
    private View lytRetry;
    private Button btnRetry;
    private ProgressBar progressBar;
    private ImageView imgImage;

    private CustomWebService customWebService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        urlSpinner = (Spinner) rootView.findViewById(R.id.spinner_url);
        btnSend = (Button) rootView.findViewById(R.id.btn_send);
        txtContent = (TextView) rootView.findViewById(R.id.txt_content);
        lytContainer = rootView.findViewById(R.id.lyt_container);
        lytRetry = rootView.findViewById(R.id.lyt_retry);
        btnRetry = (Button) rootView.findViewById(R.id.btn_retry);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        imgImage = (ImageView) rootView.findViewById(R.id.img_image);
        btnSend.setOnClickListener(this);
        btnRetry.setOnClickListener(this);
        customWebService = new CustomWebService(getActivity());

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
        String url = (String) urlSpinner.getSelectedItem();
        try {
            sendRequest(url);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(view == btnRetry)
            lytRetry.setVisibility(View.GONE);
    }


    /**
     * Send Request
     * @param url
     */
    private void sendRequest(String url) throws JSONException {
        WebService webService = new WebService(getActivity());
        RequestHandler rh = webService.init()
                .addHeader("AppVersion", "1.0")
                .addHeader("Content-Type", "application/json")
                .setSocketTimeout(60000)
                .setConnectionTimeout(60000)
                .setListener(webServiceListener);

        // send HTTP requests to server
        if(urlSpinner.getSelectedItemPosition() == 1) {
            JSONObject payload = new JSONObject();
            payload.put("key1", "value1");
            payload.put("key2", "value2");
            rh.post(url, payload.toString()); // send as POST request
        } else {
            rh.get(url); // send as GET request
        }
    }


    private RequestHandler.WebServiceListener webServiceListener = new RequestHandler.WebServiceListener() {
        @Override
        public void onPrepare(RequestHandler requestHandler) {
            String url = requestHandler.getRequest().getURI().toString();
            Toast.makeText(getActivity(), "Sending request to: " + url, Toast.LENGTH_LONG).show();
            showProgressbar(true);
        }

        @Override
        public void onReceive(Response response, boolean success) {
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
            }
        }

        /*@Override
        public void onSuccess(Response response) {
            String contentType = response.getContentType().getValue();

            // Note: for image, video or binary content, calling getResponse() may cause OutOfMemoryException
            // when trying to convert to string, use with care
            if(contentType.contains("image/")) {
                imgImage.setVisibility(View.VISIBLE);
                txtContent.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                imgImage.setImageBitmap(BitmapFactory.decodeStream(response.getRawResponse()));
            } else {
                String content = response.getResponse();
                txtContent.setText(content);
                showProgressbar(false);
            }
        }

        @Override
        public void onFailed(Response response) {
            showProgressbar(false);

            // no response, either server has no response or no internet available
            if(response == null) {
                txtContent.setVisibility(View.GONE);
                lytRetry.setVisibility(View.VISIBLE);
            }
        }*/
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
