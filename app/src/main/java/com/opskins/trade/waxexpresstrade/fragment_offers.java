package com.opskins.trade.waxexpresstrade;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.ref.WeakReference;
import cz.msebera.android.httpclient.Header;

public class fragment_offers extends Fragment {
    // ** DEFINITIONS

    private static class Constant {
        // ** GENERAL

        private static final int OFFERS_PER_PAGE = 100;

        // ** OFFER TYPES

        private static final int OFFER_TYPE_RECEIVED = 0;
        private static final int OFFER_TYPE_SENT = 1;

        // ** OFFER STATES

        private static final int OFFER_STATE_ACTIVE = 2;
        private static final int OFFER_STATE_ACCEPTED = 3;
        private static final int OFFER_STATE_EXPIRED = 5;
        private static final int OFFER_STATE_CANCELED = 6;
        private static final int OFFER_STATE_DECLINED = 7;
    }

    // ** VARIABLES

    // *** GENERAL

    private int selected_offer_type = Constant.OFFER_TYPE_RECEIVED;
    private int selected_offer_state_filter = Constant.OFFER_STATE_ACTIVE;
    private View selected_offer_type_view;
    private View selected_offer_state_filter_view;

    // *** STATES

    private Boolean perform_action = false;

    // ** CALLBACKS

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        new main().set_perform_action(false);

        // -----

        final View fragment = inflater.inflate(R.layout.fragment_offers, container, false);
        final Resources resources = getResources();
        final DisplayMetrics display_metrics = resources.getDisplayMetrics();
        final int unit_conversion_1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 19, display_metrics);
        final int unit_conversion_2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, display_metrics);

        final Drawable vcase_image = resources.getDrawable(R.drawable.vcase);
        vcase_image.setBounds(0, 0, unit_conversion_1, unit_conversion_2);

        final Drawable verified_image = resources.getDrawable(R.drawable.verified);
        verified_image.setBounds(0, 0, unit_conversion_2, unit_conversion_2);

        final SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append("Please note: Trade Offers that are sent from verified sites will display *");
        builder.setSpan(new ImageSpan(vcase_image, ImageSpan.ALIGN_BOTTOM), builder.length() - 1, builder.length(), 0);
        builder.append(" or *");
        builder.setSpan(new ImageSpan(verified_image, ImageSpan.ALIGN_BOTTOM), builder.length() - 1, builder.length(), 0);
        builder.append(" next to the account's name. Happy trading!");

        ((TextView) fragment.findViewById(R.id.fragment_offers_note)).setText(builder);

        // -----

        selected_offer_type_view = fragment.findViewById(R.id.fragment_offers_tab_received);

        selected_offer_type_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedOfferType(fragment, Constant.OFFER_TYPE_RECEIVED);
            }
        });

        fragment.findViewById(R.id.fragment_offers_tab_sent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedOfferType(fragment, Constant.OFFER_TYPE_SENT);
            }
        });

        selected_offer_state_filter_view = fragment.findViewById(R.id.fragment_offers_state_tab_pending);

        selected_offer_state_filter_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedOfferStateFilter(fragment, Constant.OFFER_STATE_ACTIVE, false);
            }
        });

        fragment.findViewById(R.id.fragment_offers_state_tab_accepted).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedOfferStateFilter(fragment, Constant.OFFER_STATE_ACCEPTED, false);
            }
        });

        fragment.findViewById(R.id.fragment_offers_state_tab_expired).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedOfferStateFilter(fragment, Constant.OFFER_STATE_EXPIRED, false);
            }
        });

        fragment.findViewById(R.id.fragment_offers_state_tab_declined).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedOfferStateFilter(fragment, Constant.OFFER_STATE_DECLINED, false);
            }
        });

        fragment.findViewById(R.id.fragment_offers_state_tab_canceled).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedOfferStateFilter(fragment, Constant.OFFER_STATE_CANCELED, false);
            }
        });

        loadOffers(fragment, 1, true);
        return fragment;
    }

    // ** FUNCTIONS

    private void clearView_LinearLayout(View view) {
        if(((LinearLayout) view).getChildCount() > 0) {
            ((LinearLayout) view).removeAllViews();
        }
    }

    private void loadOffers(final View fragment, int page, Boolean unrestricted_call) {
        final main main = new main();

        if(perform_action || unrestricted_call) {
            perform_action = false;
            main.set_perform_action(false);

            // -----

            final Context context = fragment_offers.this.getContext();
            final Resources resources = getResources();
            final DisplayMetrics display_metrics = resources.getDisplayMetrics();
            final opskins_trade_api opskins_trade_api = new opskins_trade_api(new WeakReference<>(context));
            final LinearLayout offers_container = fragment.findViewById(R.id.fragment_offers_offers_container);

            clearView_LinearLayout(offers_container);

            TextView loading_view = new TextView(context);
            loading_view.setText(resources.getString(R.string.fragment_offers_loading));
            loading_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            loading_view.setTextColor(resources.getColor(R.color.white));
            offers_container.addView(loading_view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            // -----

            final RequestParams params = new RequestParams();
            params.put("state", String.valueOf(selected_offer_state_filter));
            params.put("page", page);
            params.put("per_page", Constant.OFFERS_PER_PAGE);

            if(selected_offer_type == Constant.OFFER_TYPE_RECEIVED) {
                params.put("type", "received");
            }
            else {
                params.put("type", "sent");
            }

            opskins_trade_api.get_SetBearerAuth("ITrade/GetOffers/v1", params, false, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        final JSONObject data = response.getJSONObject("response");
                        final JSONArray offers = data.getJSONArray("offers");
                        final int offer_count = offers.length();

                        if(offer_count >= 1) {
                            clearView_LinearLayout(offers_container);

                            // -----

                            JSONObject offer;
                            JSONObject sender;
                            JSONObject recipient;
                            JSONArray sender_items;
                            JSONArray recipient_items;
                            RelativeLayout layout;
                            TextView user_username_view;
                            String user_username;
                            final int layout_background = resources.getColor(R.color.background);
                            final int color_white = resources.getColor(R.color.white);

                            for(int i = 0; i < offer_count; i ++) {
                                offer = offers.getJSONObject(i);
                                sender = offer.getJSONObject("sender");
                                recipient = offer.getJSONObject("recipient");
                                sender_items = sender.getJSONArray("items");
                                recipient_items = recipient.getJSONArray("items");

                                if(selected_offer_type == Constant.OFFER_TYPE_RECEIVED) {
                                    user_username = sender.getString("display_name");
                                }
                                else {
                                    user_username = recipient.getString("display_name");
                                }

                                layout = new RelativeLayout(context);
                                layout.setBackgroundColor(layout_background);

                                user_username_view = new TextView(context);
                                user_username_view.setPadding(0, 0, 0, 0);
                                user_username_view.setText(user_username);
                                user_username_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 7);
                                user_username_view.setTextColor(color_white);
                                layout.addView(user_username_view, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                                offers_container.addView(layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            }
                        }
                        else {
                            clearView_LinearLayout(offers_container);

                            // -----

                            TextView error_view = new TextView(context);
                            error_view.setText(resources.getString(R.string.fragment_offers_empty_tab));
                            error_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                            error_view.setTextColor(resources.getColor(R.color.white));
                            offers_container.addView(error_view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        }
                    }
                    catch (JSONException e) {
                        clearView_LinearLayout(offers_container);

                        // -----

                        TextView error_view = new TextView(context);
                        error_view.setText(resources.getString(R.string.fragment_offers_error_expected_return_data_not_found));
                        error_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                        error_view.setTextColor(resources.getColor(R.color.white));
                        offers_container.addView(error_view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    }

                    // -----

                    perform_action = true;
                    main.set_perform_action(true);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    if(statusCode == 0) {
                        if(!main.isNetworkAvailable(context)) {
                            main.showDialog(context, "No internet connection", "Check your mobile data or Wi-Fi.");
                        }
                        else {
                            main.showDialog(context, "An error occurred", "Request failed.");
                        }
                    }
                    else {
                        try {
                            if(!errorResponse.has("message")) {
                                clearView_LinearLayout(offers_container);

                                // -----

                                TextView error_view = new TextView(context);
                                error_view.setText(new StringBuilder(resources.getString(R.string.fragment_offers_error_tag) + errorResponse.getString("error_description")));
                                error_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                error_view.setTextColor(resources.getColor(R.color.white));
                                offers_container.addView(error_view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            }
                            else {
                                clearView_LinearLayout(offers_container);

                                // -----

                                TextView error_view = new TextView(context);
                                error_view.setText(new StringBuilder(resources.getString(R.string.fragment_offers_error_tag) + errorResponse.getString("message")));
                                error_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                error_view.setTextColor(resources.getColor(R.color.white));
                                offers_container.addView(error_view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            }
                        }
                        catch (JSONException e) {
                            clearView_LinearLayout(offers_container);

                            // -----

                            TextView error_view = new TextView(context);
                            error_view.setText(resources.getString(R.string.fragment_offers_error_expected_return_data_not_found));
                            error_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                            error_view.setTextColor(resources.getColor(R.color.white));
                            offers_container.addView(error_view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        }
                    }

                    // -----

                    perform_action = true;
                    main.set_perform_action(true);
                }
            });
        }
    }

    private void updateSelectedOfferStateFilter(View fragment, int state) {
        View view;

        switch(state) {
            default: case Constant.OFFER_STATE_ACTIVE:
                view = fragment.findViewById(R.id.fragment_offers_state_tab_pending);
                break;
            case Constant.OFFER_STATE_ACCEPTED:
                view = fragment.findViewById(R.id.fragment_offers_state_tab_accepted);
                break;
            case Constant.OFFER_STATE_EXPIRED:
                view = fragment.findViewById(R.id.fragment_offers_state_tab_expired);
                break;
            case Constant.OFFER_STATE_DECLINED:
                view = fragment.findViewById(R.id.fragment_offers_state_tab_declined);
                break;
            case Constant.OFFER_STATE_CANCELED:
                view = fragment.findViewById(R.id.fragment_offers_state_tab_canceled);
                break;
        }

        selected_offer_state_filter_view.setBackgroundDrawable(null);
        view.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_fragment_offers_tab));

        selected_offer_state_filter_view = view;
    }

    private void updateSelectedOfferType(View fragment, int type) {
        View view;

        if(type == Constant.OFFER_TYPE_RECEIVED) {
            view = fragment.findViewById(R.id.fragment_offers_tab_received);
        }
        else {
            view = fragment.findViewById(R.id.fragment_offers_tab_sent);
        }

        selected_offer_type_view.setBackgroundDrawable(null);
        view.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_fragment_offers_tab));

        selected_offer_type_view = view;
    }

    private void setSelectedOfferStateFilter(View fragment, int state, Boolean unrestricted_call) {
        if((perform_action && state != selected_offer_state_filter) || unrestricted_call) {
            perform_action = false;
            new main().set_perform_action(false);
            selected_offer_state_filter = state;

            // -----

            updateSelectedOfferStateFilter(fragment, state);
            loadOffers(fragment, 1, true);
        }
    }

    private void setSelectedOfferType(View fragment, int type) {
        final main main = new main();

        if(perform_action) {
            perform_action = false;
            main.set_perform_action(false);

            // -----

            if(type != selected_offer_type) {
                selected_offer_type = type;

                updateSelectedOfferType(fragment, type);
                setSelectedOfferStateFilter(fragment, Constant.OFFER_STATE_ACTIVE, true);
            }
            else if(selected_offer_state_filter != Constant.OFFER_STATE_ACTIVE) {
                updateSelectedOfferType(fragment, type);
                setSelectedOfferStateFilter(fragment, Constant.OFFER_STATE_ACTIVE, true);
            }
            else {
                perform_action = true;
                main.set_perform_action(true);
            }
        }
    }
}