package com.opskins.trade.waxexpresstrade;

import android.content.Context;
import android.content.res.Resources;
import com.loopj.android.http.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;
import cz.msebera.android.httpclient.Header;

class opskins_oauth {
    // ** DEFINITIONS

    private static class Constant {
        private static final String BASE_URL = "https://oauth.opskins.com/";
        private static final String SCOPE = "identity_basic+trades+items+open_cases";
    }

    // ** VARIABLES

    private static WeakReference<Context> weak_context;
    private static final AsyncHttpClient client = new AsyncHttpClient();

    // ** MAIN

    opskins_oauth(WeakReference<Context> reference) {
        weak_context = reference;
    }

    // ** FUNCTIONS

    void validateAuthorization(final int request_type, final String request_url, final RequestParams request_params, final AsyncHttpResponseHandler request_response_handler) {
        final main main = new main();
        final opskins_trade_api opskins_trade_api = new opskins_trade_api(weak_context);

        if(((int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())) > (main.getUserBearerTokenTimestampIssued() + main.getUserBearerTokenExpiresIn())) {
            final RequestParams params = new RequestParams();
            params.put("grant_type", "refresh_token");
            params.put("refresh_token", main.getUserRefreshToken());

            post_SetBasicAuth("v1/access_token", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        main.setUserBearerToken(response.getString("access_token"));
                        main.setUserBearerTokenExpiresIn(response.getInt("expires_in"));
                        main.setUserBearerTokenTimestampIssued((int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));

                        if(response.has("refresh_token")) {
                            main.setUserRefreshToken(response.getString("refresh_token"));
                        }

                        opskins_trade_api.onAuthorizationValidated(request_type, request_url, request_params, request_response_handler);
                    }
                    catch (JSONException e) {
                        opskins_trade_api.onAuthorizationValidated(request_type, request_url, request_params, request_response_handler);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    opskins_trade_api.onAuthorizationValidated(request_type, request_url, request_params, request_response_handler);
                }
            });
        }
        else {
            opskins_trade_api.onAuthorizationValidated(request_type, request_url, request_params, request_response_handler);
        }
    }

    void requestAuthorization(final String authorization_code) {
        final Context context = weak_context.get();
        final main main = new main();

        final RequestParams params = new RequestParams();
        params.put("grant_type", "authorization_code");
        params.put("code", authorization_code);

        post_SetBasicAuth("v1/access_token", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    final String bearer_token = response.getString("access_token");
                    final int bearer_token_expires_in = (response.getInt("expires_in") - 10);
                    final int bearer_token_timestamp_issued = (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
                    final String refresh_token = response.getString("refresh_token");

                    get_SetBearerAuth(bearer_token, "https://api.opskins.com/IUser/GetProfile/v1", null, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            main.onLogIn(context, bearer_token, bearer_token_expires_in, bearer_token_timestamp_issued, refresh_token, response);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            main.onLogInFailed(context, "Unable to get profile.");
                        }
                    });
                }
                catch (JSONException e) {
                    main.onLogInFailed(context, "Unable to request profile.");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                main.onLogInFailed(context, "Unable to get bearer token.");
            }
        });
    }

    int authenticate() {
        try {
            final Context context = weak_context.get();
            final Resources resources = context.getResources();

            new custom_tabs().openTab(context, getAbsoluteURL("v1/authorize?client_id=" + URLEncoder.encode(resources.getString(R.string.OPSKINS_OAUTH_CLIENT_ID), "UTF-8") + "&response_type=code&state=" + URLEncoder.encode(resources.getString(R.string.OPSKINS_OAUTH_CLIENT_SECRET), "UTF-8") + "&duration=permanent&mobile=1&scope=" + Constant.SCOPE));
        }
        catch (IOException e) {
            return 0;
        }
        return 1;
    }

    void post_SetBasicAuth(String url, RequestParams params, AsyncHttpResponseHandler response_handler) {
        client.removeAllHeaders();

        setBasicAuth();

        client.post(getAbsoluteURL(url), params, response_handler);
    }

    private static void get_SetBearerAuth(String bearer_token, String url, RequestParams params, AsyncHttpResponseHandler response_handler) {
        client.removeAllHeaders();

        setBearerAuth(bearer_token);

        client.get(url, params, response_handler);
    }

    private static void setBearerAuth(String bearer_token) {
        client.addHeader("Authorization", "Bearer " + bearer_token);
    }

    private static void setBasicAuth() {
        final Resources resources = weak_context.get().getResources();

        client.setBasicAuth(resources.getString(R.string.OPSKINS_OAUTH_CLIENT_ID), resources.getString(R.string.OPSKINS_OAUTH_CLIENT_SECRET));
    }

    private static String getAbsoluteURL(String relative_url) {
        return Constant.BASE_URL + relative_url;
    }
}