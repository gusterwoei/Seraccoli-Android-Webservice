package com.guster.skywebservice.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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

import com.guster.skywebservice.library.webservice.FormContent;
import com.guster.skywebservice.library.webservice.Response;
import com.guster.skywebservice.library.webservice.SkyHttp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gusterwoei on 8/24/14.
 */
public class MainFragment extends Fragment implements View.OnClickListener {
    private static final int REQ_CODE_SELECT_FILE = 1;
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
        SkyHttp.addGlobalHeader("AppVersion", "1.0");
        SkyHttp.addGlobalHeader("Content-Type", "application/json");
        SkyHttp.setConnectionTimeout(60000);

        loadUrls();

        return rootView;
    }

    private void loadUrls() {
        List<String> urls = new ArrayList<String>();
        urls.add("https://www.facebook.com");
        urls.add("http://echo.jsontest.com/key/value/one/two");
        urls.add("http://betterexplained.com/examples/compressed/index.htm");
        urls.add("http://192.168.1.138:1234/the_images.zip");
        urls.add("http://simpleicon.com/wp-content/uploads/smile-256x256.png");
        urls.add("https://upload.wikimedia.org/wikipedia/commons/b/b7/Big_smile.png");
        MyAdapter adapter = new MyAdapter(urls);
        urlSpinner.setAdapter(adapter);
    }


    @Override
    public void onClick(View view) {
        if(view == btnSend) {
            testHTTPConnection();
            /*String url = (String) urlSpinner.getSelectedItem();
            try {
                sendRequest(url);
            } catch (JSONException e) {
                Log.e("ABC", "Send error: " + e.getMessage());
                e.printStackTrace();
            }*/

        } else if(view == btnRetry) {
            lytRetry.setVisibility(View.GONE);

        } else if(view == txtUploadFile) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQ_CODE_SELECT_FILE);
        }
    }


    /**
     * Send Request
     * @param url
     */
    private void sendRequest(String url) throws JSONException {
        SkyHttp.RequestBuilder requestBuilder = SkyHttp.newRequest();

        // send HTTP requests to server
        if(urlSpinner.getSelectedItemPosition() == 1) {
            // send as POST request
            JSONObject payload = new JSONObject();
            payload.put("key1", "value1");
            payload.put("key2", "value2");
            requestBuilder.post(url, payload.toString()).send(webServiceListener);
        } else {
            // send as GET request
            requestBuilder.get(url).send(webServiceListener);
        }
    }

    private void testHTTPConnection() {
        CustomWebService webService = new CustomWebService(getActivity());
        webService.testHttpsConnection();
    }


    private SkyHttp.Callback webServiceListener = new SkyHttp.Callback() {
        @Override
        public void onPrepare() {
            //String url = requestBuilder.getRequest().getURI().toString();
            Toast.makeText(getActivity(), "Sending request to: ", Toast.LENGTH_LONG).show();
            showProgressbar(true);
        }

        @Override
        public void onResponse(final Response response, boolean success, Object ... args) {
            Log.d("ABC",(response != null)? response.getResponse() : "no response");
            showProgressbar(false);

            // no response, either request timeout due to server no respond or loss of internet connection
            if(response == null) {
                txtContent.setVisibility(View.GONE);
                lytRetry.setVisibility(View.VISIBLE);
            }

            if(success) {
                // Note: for image, video or binary content, calling getResponse() may cause OutOfMemoryException
                // when trying to convert to string, use with care
                //String contentType = response.getContentType().getValue();
                String contentType = response.getContentType();
                if (contentType != null && contentType.contains("image/")) {
                    imgImage.setVisibility(View.VISIBLE);
                    txtContent.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);

                    Bitmap bitmap = BitmapFactory.decodeStream(response.getRawResponse());
                    imgImage.setImageBitmap(bitmap);

                } else if(contentType != null && !contentType.contains("text/")) {
                    Log.d("ABC", "writing my file");
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            try {
                                File file = new File("sdcard/ME_FILE.zip");
                                OutputStream stream = new BufferedOutputStream(new FileOutputStream(file.getPath()));
                                int bufferSize = 1024;
                                byte[] buffer = new byte[bufferSize];
                                int len;
                                while ((len = response.getRawResponse().read(buffer)) != -1) {
                                    stream.write(buffer, 0, len);
                                }
                                if(stream!=null)
                                    stream.close();
                            } catch (IOException e) {
                                Log.d("ABC", "write file error: " + e.getMessage());
                                e.printStackTrace();
                            }

                            return null;
                        }
                    }.execute();

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("ABC", "activity result: " + requestCode + ", " + resultCode + ", " + data);
        if(resultCode == Activity.RESULT_OK) {
            switch(requestCode) {
                case REQ_CODE_SELECT_FILE: {
                    if(data == null) {
                        Log.e("ABC", "intent no data");
                        return;
                    }

                    try {
                        InputStream stream = getActivity().getContentResolver().openInputStream(data.getData());
                        customWebService.uploadFile(stream, webServiceListener);
                    } catch (FileNotFoundException e) {
                        Log.e("ABC", "file not found");
                        e.printStackTrace();
                    }
                    break;
                }
            }
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
