WebService
==========

WebService is an Android client-end web service library that aims for handling any HTTP requests.
Currently WebService supports 4 standard types of RESTful actions - GET, POST, PUT, DELETE.

Example
=======
```java
public class MainActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebService webService = new WebService(getActivity());
        RequestHandler rh = webService.init()
                .addHeader("AppVersion", "1.0") // set request header
                .addHeader("Content-Type", "application/json") // add request header, default: "application/json"
                .setSocketTimeout(60000) // set socket timeout, default: 30000
                .setConnectionTimeout(60000) // set connection timeout, default: 30000
                .setListener(webServiceListener); // set a WebService callback listener

         rh.get({YOUR_URL}); // send as GET request

         JSONObject payload = new JSONObject();
         payload.put("name", "Jeffrey");
         payload.put("score", 5);
         rh.post({YOUR_URL}, payload.toString()); // send as POST request
    }

    private RequestHandler.WebServiceListener webServiceListener = new RequestHandler.WebServiceListener() {
        @Override
        public void onPrepare(RequestHandler requestHandler) {
            String url = requestHandler.getRequest().getURI().toString();
            Toast.makeText(getActivity(), "Sending request to: " + url, Toast.LENGTH_LONG).show();
            showProgressbar(true);
        }

        @Override
        public void onReceive(Response response) {
            // do something
        }

        @Override
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
        }
    };
}
```
