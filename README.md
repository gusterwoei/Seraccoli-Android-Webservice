# WebService

WebService is an Android client-end web service library that aims for handling any HTTP requests.
Currently WebService supports 4 standard types of RESTful actions - GET, POST, PUT, DELETE.

## Set global HTTP properties
---
This will affect all your web service requests throughout the entire app
```java
WebService.setConnectionTimeout(30000);
WebService.setSocketTimeout(60000);
```

## Send Request
---
Normally, this is how you send a simple GET, POST, PUT, DELETE request
##### GET
```java
WebService.newRequest().get("http://www.myawesomeapi.com/users").send();
```
##### POST
```java
JSONObject payload = new JSONObject();
payload.put("name", "Jeffrey");
payload.put("score", 5);
WebService.newRequest().post("http://www.myawesomeapi.com/addUser", payload.toString()).send();
```
#### PUT
```java
payload.put("score", 10);
WebService.newRequest().put("http://www.myawesomeapi.com/updateUser", payload.toString()).send();
```
#### DELETE
```java
WebService.newRequest().delete("http://www.myawesomeapi.com/deleteUser?userId=6").send();
```

## Receive Response
---
```java
WebService.newRequest().post("http://www.myawesomeapi.com/addUser", payload.toString())
        .withResponse(new WebServiceListener() {
            @Override
            public void onReceive(Response response, boolean success) {
                if(success) {
                    // RESTful response
                    JSONObject json = new JSONObject(response.getResponse());

                    // if the response is binary data (eg. image), use getRawResponse() instead
                    Bitmap bitmap = BitmapFactory.decodeStream(response.getRawResponse());
                    imageView.setImageBitmap(bitmap);

                    // process response
                }
            }
        }).send()
```
###### Optional callback functions:

onPrepare() - invoked before request sent
onReceiveInBackground() - before onReceived(), invoked in background thread



## Example

```java
public class MainActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebService webService = new WebService(this);
        RequestBuilder rh = webService.newRequest()
                .addHeader("AppVersion", "1.0") // set request header
                .addHeader("Content-Type", "application/json") // add request header, default: "application/json"
                .setSocketTimeout(60000) // set socket timeout, default: 30000
                .setConnectionTimeout(60000) // set connection timeout, default: 30000
                .withResponse(webServiceListener); // set a WebService callback listener

        // send as GET request
        rh.get("http://24.media.tumblr.com/tumblr_ma0jzoNfhr1recw5vo1_500.jpg");

        JSONObject payload = new JSONObject();
        payload.put("name", "Jeffrey");
        payload.put("score", 5);

        // send as POST request
        rh.post("http://www.myawesomeapi.com/addUser", payload.toString());

        // send PUT request
        payload.put("score", 10);
        rh.post("http://www.myawesomeapi.com/updateUser", payload.toString());

        // send DELETE request
        rh.delete("http://www.myawesomeapi.com/deleteUser/123");
    }

    private WebServiceListener webServiceListener = new WebServiceListener() {
        @Override
        public void onPrepare(WebService.RequestBuilder RequestBuilder) {
            String url = RequestBuilder.getRequest().getURI().toString();
            Toast.makeText(getActivity(), "Sending request to: " + url, Toast.LENGTH_LONG).show();
            showProgressbar(true);
        }

        @Override
        public void onReceive(Response response, boolean success) {
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
        public void onReceiveInBackground(Response response, boolean success) {
            // similar to onReceive(), but this method is invoked in the background thread
            // right before onReceive(). Especially useful when you need to perform
            // additional background tasks after receiving response from the server
        }
    };
}
```
