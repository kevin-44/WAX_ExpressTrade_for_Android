package com.opskins.trade.waxexpresstrade;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.ref.WeakReference;
import cz.msebera.android.httpclient.Header;

public class background_service extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(new main().isUserLoggedIn()) {
                    final opskins_trade_api opskins_trade_api = new opskins_trade_api(new WeakReference<>(getApplicationContext()));

                    final RequestParams params = new RequestParams();
                    params.put("state", "2");
                    params.put("type", "received");

                    opskins_trade_api.get_SetBearerAuth("ITrade/GetOffers/v1", params, false, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {
                                final JSONObject data = response.getJSONObject("response");
                                final JSONArray offers = data.getJSONArray("offers");
                                final int offer_count = offers.length();

                                if(offer_count >= 1) {
                                    JSONObject offer;

                                    for(int i = 0; i < offer_count; i ++) {
                                        offer = offers.getJSONObject(i);

                                        if(offer.getBoolean("is_case_opening")) {
                                            final RequestParams params = new RequestParams();
                                            params.put("offer_id", offer.getInt("id"));

                                            opskins_trade_api.post_SetBearerAuth("ITrade/AcceptOffer/v1", params, false, new JsonHttpResponseHandler());
                                        }
                                    }
                                }

                                onStartCommand(intent, flags, startId);
                            }
                            catch (JSONException e) {
                                onStartCommand(intent, flags, startId);
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            onStartCommand(intent, flags, startId);
                        }
                    });
                }
            }
        }, 5000);
        return super.onStartCommand(intent, flags, startId);
    }
}