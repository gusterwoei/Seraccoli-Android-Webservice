# SkyWebService

SkyWebService is an Android HTTP client library that aims for handling any HTTP requests with ease,
a one-line way of usage. Currently supporting 4 standard types of RESTful actions - GET, POST, PUT, DELETE.

## Installation
##### Gradle
Include the following dependency in your build.gradle file of your project.
```xml
repositories {
    jcenter()
}

dependencies {
    compile 'com.guster.android:skywebservice:1.1.0'
}
```

##### Maven
```xml
<dependency>
    <groupId>com.guster.android</groupId>
    <artifactId>skywebservice</artifactId>
    <version>1.1.0</version>
</dependency>
```

## Set global HTTP properties
This will affect all your web service requests throughout the entire app
```java
WebService.setConnectionTimeout(30000);
WebService.setSocketTimeout(60000);
```

## Send Request
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
##### PUT
```java
payload.put("score", 10);
WebService.newRequest().put("http://www.myawesomeapi.com/updateUser", payload.toString()).send();
```
##### DELETE
```java
WebService.newRequest().delete("http://www.myawesomeapi.com/deleteUser?userId=6").send();
```

## Receive Response
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

## Developed by
* Guster Woei - <gusterwoei@gmail.com>

## License
```xml
 Copyright 2015 Gusterwoei

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
```
