# SkyHttp

SkyHttp is a lightweight Android HTTP client library that aims for handling any HTTP requests with ease,
a one-line way of usage. Currently supporting 4 standard types of RESTful actions - GET, POST, PUT, DELETE, HEAD.

## Installation
##### Gradle
Include the following dependency in your build.gradle file of your project.

```xml
repositories {
    jcenter()
}

dependencies {
    compile 'com.guster.android:skywebservice:2.1.3'
}
```

##### Maven

```xml
<dependency>
    <groupId>com.guster.android</groupId>
    <artifactId>skywebservice</artifactId>
    <version>2.1.3</version>
</dependency>
```

You might also need to include the following in your build.gradle inside android {...} section

```
packagingOptions {
    exclude 'META-INF/DEPENDENCIES'
    exclude 'META-INF/LICENSE'
    exclude 'META-INF/NOTICE'
}
```

## Set global HTTP properties
This will affect all your web service requests throughout the entire app

```java
SkyHttp.setConnectionTimeout(30000);
SkyHttp.setSocketTimeout(60000);
SkyHttp.setSSLSocketFactory(customSSLSocketFactory);
```

## Send Request
Normally, this is how you send a simple GET, POST, PUT, DELETE request
##### GET

```java
SkyHttp.newRequest().get("http://www.myawesomeapi.com/users").send();
```

##### POST

```java
JSONObject payload = new JSONObject();
payload.put("name", "Jeffrey");
payload.put("score", 5);
SkyHttp.newRequest().post("http://www.myawesomeapi.com/addUser", payload.toString()).send(/* callback */);
```

##### PUT

```java
payload.put("score", 10);
SkyHttp.newRequest().put("http://www.myawesomeapi.com/updateUser", payload.toString()).send(/* callback */);
```

##### DELETE

```java
SkyHttp.newRequest().delete("http://www.myawesomeapi.com/deleteUser?userId=6").send(/* callback */);
```

##### POST WITH MULTIPART FORM

```java
FormContent formContent = FormContent.create()
    .addContent("username", "Steve")
    .addContent("gender", "M")
    .addContent("photo", bitmap)
    .addContent("file", file, "fileName");
SkyHttp.newRequest().post(url, formContent).send(/* callback */);
```

##### HEAD
```java
SkyHttp.newRequest().head("http://www.myawesomeapi.com/deleteUser?userId=6").send();
```

## URL Encoding

Default encoding charset is UTF-8
```java
SkyHttp.newRequest().encode().post(url, payload);
```

Or you can specify any encoding charset
```java
SkyHttp.newRequest().encode("UTF-16").post(url, payload);
```


## Handle Response

```java
SkyHttp.newRequest()
        .post("http://www.myawesomeapi.com/addUser", payload.toString())
        .send(new SkyHttp.Callback() {
            @Override
            public void onPrepare() {
                // before network request
                /* do something */
            }

            @Override
            public Object[] onReceiveInBackground(Response response, boolean success) {
                // similar to onResponse(), but this method is invoked in the background thread
                // right before onResponse(). Especially useful when you need to perform
                // additional background tasks after receiving response from the server
                return null;
            }

            @Override
            public void onResponse(Response response, boolean success, Object ... args) {
                if(success) {
                    // RESTful response
                    JSONObject json = new JSONObject(response.getResponse());

                    // if the response is binary data (eg. image), use getRawResponse() instead
                    Bitmap bitmap = BitmapFactory.decodeStream(response.getRawResponse());
                    imageView.setImageBitmap(bitmap);

                    // process response
                }
            }
        })
```

## SSL Certificate
### Trust all certificates
Set this global property to true will ignore all SSL certificate checking

```java
SkyHttp.trustAllCertificates(true)
```

### Adding a certificate
Importing a custom certificate, for example a self signed certificate, to a connection.
```java
InputStream cert = context.getResources().openRawResource(R.raw.my_certificate_file);
requestBuilder().setSSLCertificate(cert);
```
or globally
```java
SkyHttp.setSSLCertificate(cert);
```

## Example

```java
public class MainActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RequestBuilder rh = SkyHttp.newRequest()
                .addHeader("AppVersion", "1.0") // set request header
                .addHeader("Content-Type", "application/json") // add request header, default: "application/json"
                .setSocketTimeout(60000) // set socket timeout, default: 30000
                .setConnectionTimeout(60000) // set connection timeout, default: 30000

        // send as GET request
        rh.get("http://24.media.tumblr.com/tumblr_ma0jzoNfhr1recw5vo1_500.jpg").send(callback);

        JSONObject payload = new JSONObject();
        payload.put("name", "Jeffrey");
        payload.put("score", 5);

        // send as POST request
        rh.post("http://www.myawesomeapi.com/addUser", payload.toString()).send(callback);

        // send PUT request
        payload.put("score", 10);
        rh.post("http://www.myawesomeapi.com/updateUser", payload.toString()).send(callback);

        // send DELETE request
        rh.delete("http://www.myawesomeapi.com/deleteUser/123").send(callback);
    }

    private SkyHttp.Callback callback = new SkyHttp.Callback() {
        @Override
        public void onPrepare() {
            showProgressbar(true);
        }

        @Override
        public void onResponse(Response response, boolean success, Object .. args) {
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
        public Object[] onReceiveInBackground(Response response, boolean success) {
            // similar to onResponse(), but this method is invoked in the background thread
            // right before onResponse(). Especially useful when you need to perform
            // additional background tasks after receiving response from the server
            return null;
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
