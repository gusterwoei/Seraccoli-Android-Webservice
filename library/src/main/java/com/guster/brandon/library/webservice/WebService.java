/**
 * Copyright 2014 Gusterwoei

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.guster.brandon.library.webservice;

import android.content.Context;

/**
 * Created by Gusterwoei on 10/30/13.
 */
public class WebService {
    private Context context;
    private RequestHandler requestHandler;

    public WebService(Context context) {
        this.context = context;
    }

    public RequestHandler init() {
        requestHandler = new RequestHandler();
        return requestHandler;
    }

    public Context getContext() {
        return context;
    }

    public RequestHandler getRequestHandler() {
        return requestHandler;
    }
}