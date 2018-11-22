package com.opskins.trade.waxexpresstrade;

import android.content.Context;
import com.loopj.android.http.*;
import java.lang.ref.WeakReference;

class opskins_trade_api {
    // ** DEFINITIONS

    private static class Constant {
        // ** GENERAL

        private static final String BASE_URL = "https://api-trade.opskins.com/";

        // ** REQUEST TYPES

        private static final int REQUEST_POST_SET_BEARER_AUTH = 0;
        private static final int REQUEST_GET_SET_BEARER_AUTH = 1;
    }

    // ** VARIABLES

    private static WeakReference<Context> weak_context;
    private static final AsyncHttpClient client = new AsyncHttpClient();

    // ** MAIN

    opskins_trade_api(WeakReference<Context> reference) {
        weak_context = reference;
    }

    // ** CALLBACKS

    void onAuthorizationValidated(int request_type, String request_url, RequestParams request_params, AsyncHttpResponseHandler request_response_handler) {
        switch(request_type) {
            case Constant.REQUEST_POST_SET_BEARER_AUTH:
                post_SetBearerAuth(request_url, request_params, true, request_response_handler);
                break;
            case Constant.REQUEST_GET_SET_BEARER_AUTH:
                get_SetBearerAuth(request_url, request_params, true, request_response_handler);
                break;
        }
    }

    // ** FUNCTIONS

    void post_SetBearerAuth(String url, RequestParams params, Boolean bearer_token_validated, AsyncHttpResponseHandler response_handler) {
        if(bearer_token_validated) {
            client.removeAllHeaders();

            setBearerAuth();

            client.post(getAbsoluteURL(url), params, response_handler);
        }
        else {
            new opskins_oauth(weak_context).validateAuthorization(Constant.REQUEST_POST_SET_BEARER_AUTH, url, params, response_handler);
        }
    }

    void get_SetBearerAuth(String url, RequestParams params, Boolean bearer_token_validated, AsyncHttpResponseHandler response_handler) {
        if(bearer_token_validated) {
            client.removeAllHeaders();

            setBearerAuth();

            client.get(getAbsoluteURL(url), params, response_handler);
        }
        else {
            new opskins_oauth(weak_context).validateAuthorization(Constant.REQUEST_GET_SET_BEARER_AUTH, url, params, response_handler);
        }
    }

    void get(String url, RequestParams params, AsyncHttpResponseHandler response_handler) {
        client.removeAllHeaders();
        client.get(getAbsoluteURL(url), params, response_handler);
    }

    private static void setBearerAuth() {
        client.addHeader("Authorization", "Bearer " + new main().getUserBearerToken());
    }

    private static String getAbsoluteURL(String relative_url) {
        return Constant.BASE_URL + relative_url;
    }
}