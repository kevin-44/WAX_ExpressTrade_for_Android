package com.opskins.trade.waxexpresstrade;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

    private List<Object> offer_containers = new ArrayList<>();
    private int selected_offer_type = Constant.OFFER_TYPE_RECEIVED;
    private int selected_offer_state_filter = Constant.OFFER_STATE_ACTIVE;
    private View selected_offer_type_view;
    private View selected_offer_state_filter_view;
    private int image_load_count = 0;

    // *** STATES

    private Boolean offers_loaded = false;
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

    @Override
    public void onResume() {
        super.onResume();

        refreshContainers();
    }

    private void on2FAEntered(final View fragment, EditText edit_text, int offer_id) {
        final main main = new main();
        final String twofactor = edit_text.getText().toString();

        if(perform_action && !twofactor.isEmpty()) {
            perform_action = false;
            main.set_perform_action(false);

            // -----

            try {
                Integer.parseInt(twofactor);

                // -----

                final Context context = fragment_offers.this.getContext();

                final RequestParams params = new RequestParams();
                params.put("twofactor_code", twofactor);
                params.put("offer_id", offer_id);

                new opskins_trade_api(new WeakReference<>(context)).post_SetBearerAuth("ITrade/AcceptOffer/v1", params, false, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            if(!response.has("message")) {
                                loadOffers(fragment, 1, true);
                            }
                            else {
                                main.showDialog(context, response.getString("message"), null);

                                // -----

                                perform_action = true;
                                main.set_perform_action(true);
                            }
                        }
                        catch (JSONException e) {
                            main.showDialog(context, "An error occurred", "Expected return data not found.");

                            // -----

                            perform_action = true;
                            main.set_perform_action(true);
                        }
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
                                    main.showDialog(context, "An error occurred", errorResponse.getString("error_description"));
                                }
                                else {
                                    main.showDialog(context, "An error occurred", errorResponse.getString("message"));
                                }
                            }
                            catch (JSONException e) {
                                main.showDialog(context, "An error occurred", "Expected return data not found.");
                            }
                        }

                        // -----

                        perform_action = true;
                        main.set_perform_action(true);
                    }
                });
            }
            catch(NumberFormatException nfe) {
                perform_action = true;
                main.set_perform_action(true);
            }
        }
    }

    // ** FUNCTIONS

    public void refreshContainers() {
        final Resources resources = getResources();
        final Drawable container_outline_drawable = resources.getDrawable(R.drawable.shape_fragment_offers_container_outline);
        final Drawable offer_header_container_drawable = resources.getDrawable(R.drawable.shape_fragment_offers_offer_header_container);
        final Drawable offer_content_container_drawable = resources.getDrawable(R.drawable.shape_fragment_offers_offer_content_container);

        for(int i = 0, j = offer_containers.size(); i < j; i += 3) {
            ((LinearLayout) offer_containers.get(i)).setBackgroundDrawable(container_outline_drawable);
            ((LinearLayout) offer_containers.get(i + 1)).setBackgroundDrawable(offer_header_container_drawable);
            ((LinearLayout) offer_containers.get(i + 2)).setBackgroundDrawable(offer_content_container_drawable);
        }
    }

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

            offer_containers.clear();
            image_load_count = 0;
            offers_loaded = false;

            // -----

            final Context context = fragment_offers.this.getContext();
            final Resources resources = getResources();
            final DisplayMetrics display_metrics = resources.getDisplayMetrics();
            final opskins_trade_api opskins_trade_api = new opskins_trade_api(new WeakReference<>(context));
            final LinearLayout offers_container = fragment.findViewById(R.id.fragment_offers_offer_list_container);

            if(context != null) {
                clearView_LinearLayout(offers_container);

                TextView loading_view = new TextView(context);
                loading_view.setText(resources.getString(R.string.fragment_offers_loading));
                loading_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                loading_view.setTextColor(resources.getColor(R.color.white));
                loading_view.setGravity(Gravity.CENTER_HORIZONTAL);
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
                                JSONObject offer;
                                JSONObject sender;
                                JSONObject recipient;
                                JSONArray sender_items;
                                JSONArray recipient_items;
                                JSONArray their_items;
                                JSONArray your_items;
                                JSONObject item;

                                final LinearLayout offer_inner_container_layout = new LinearLayout(context);
                                offer_inner_container_layout.setOrientation(LinearLayout.VERTICAL);

                                LinearLayout offer_align_right_container_layout;
                                LinearLayout.LayoutParams offer_align_right_container_layout_params;
                                LinearLayout offer_outline_container_layout;
                                LinearLayout.LayoutParams offer_outline_container_layout_params;
                                LinearLayout offer_header_layout;
                                LinearLayout offer_header_inner_layout;
                                LinearLayout offer_detail_layout;
                                LinearLayout offer_detail_inner_layout;
                                LinearLayout offer_content_layout;
                                LinearLayout offer_content_inner_layout;
                                ImageView user_avatar_view_1;
                                de.hdodenhof.circleimageview.CircleImageView user_avatar_view_2;
                                TextView user_name_view;
                                ImageView user_status_view;
                                LinearLayout.LayoutParams user_status_view_layout_params;
                                TextView trade_summary_view;
                                TextView message_view;
                                TextView offer_detail_view;
                                TextView item_count_view;
                                ImageView item_image_view;
                                TextView item_name_view;
                                TextView item_price_view;
                                TextView button_view_1;
                                TextView button_view_2;
                                LinearLayout.LayoutParams button_view_layout_params;
                                RelativeLayout separator_view;
                                String item_image;
                                long item_suggested_price;
                                Double item_wear;
                                JSONObject item_attributes;
                                long item_serial_number;
                                String user_avatar;
                                String user_name;
                                Boolean user_verified;
                                Boolean user_case_opening;
                                String trade_summary;
                                final int unit_conversion_1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, display_metrics);
                                final int unit_conversion_2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, display_metrics);
                                final int unit_conversion_3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, display_metrics);
                                final int unit_conversion_4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, display_metrics);
                                final int unit_conversion_5 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, display_metrics);
                                final int unit_conversion_6 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, display_metrics);
                                final int unit_conversion_7 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, display_metrics);
                                final int unit_conversion_8 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, display_metrics);
                                final int unit_conversion_9 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 17, display_metrics);
                                final int unit_conversion_10 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, display_metrics);
                                final int unit_conversion_11 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, display_metrics);
                                final int unit_conversion_12 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, display_metrics);
                                final int unit_conversion_13 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, display_metrics);
                                final Drawable container_outline_drawable = resources.getDrawable(R.drawable.shape_fragment_offers_container_outline);
                                final Drawable offer_header_container_drawable = resources.getDrawable(R.drawable.shape_fragment_offers_offer_header_container);
                                final Drawable offer_detail_container_drawable = resources.getDrawable(R.drawable.shape_fragment_offers_offer_detail_container);
                                final Drawable offer_content_container_drawable = resources.getDrawable(R.drawable.shape_fragment_offers_offer_content_container);
                                final Drawable opskins_logo_drawable = resources.getDrawable(R.drawable.ic_opskins_logo);
                                final Drawable verified_drawable = resources.getDrawable(R.drawable.verified);
                                final Drawable vcase_drawable = resources.getDrawable(R.drawable.vcase);
                                final String decline_button_text = resources.getString(R.string.fragment_offers_decline_button);
                                final String accept_button_text = resources.getString(R.string.fragment_offers_accept_button);
                                final String cancel_button_text = resources.getString(R.string.fragment_offers_cancel_button);
                                final String offer_detail_text = resources.getString(R.string.fragment_offers_offer_detail);
                                final int color_white = resources.getColor(R.color.white);
                                final int color_pickled_bluewood = resources.getColor(R.color.pickled_bluewood);
                                final int color_red_berry = resources.getColor(R.color.red_berry);
                                final int color_denim = resources.getColor(R.color.denim);
                                final int color_kashmir_blue = resources.getColor(R.color.kashmir_blue);
                                final int color_downy = resources.getColor(R.color.downy);
                                String message;
                                String state_name;
                                int sender_item_count;
                                int recipient_item_count;
                                int their_item_count;
                                int your_item_count;
                                long their_items_total_value;
                                long your_items_total_value;
                                int total_image_count_temp = offer_count;

                                for(int i = 0; i < offer_count; i ++) {
                                    offer = offers.getJSONObject(i);
                                    total_image_count_temp += offer.getJSONObject("sender").getJSONArray("items").length() + offer.getJSONObject("recipient").getJSONArray("items").length();
                                }

                                // -----

                                final int total_image_count = total_image_count_temp;

                                for(int i = 0; i < offer_count; i ++) {
                                    offer = offers.getJSONObject(i);
                                    sender = offer.getJSONObject("sender");
                                    recipient = offer.getJSONObject("recipient");
                                    sender_items = sender.getJSONArray("items");
                                    recipient_items = recipient.getJSONArray("items");
                                    message = offer.getString("message");
                                    state_name = offer.getString("state_name");
                                    sender_item_count = sender_items.length();
                                    recipient_item_count = recipient_items.length();
                                    their_items_total_value = 0;
                                    your_items_total_value = 0;

                                    if(selected_offer_type == Constant.OFFER_TYPE_RECEIVED) {
                                        their_items = sender_items;
                                        their_item_count = sender_item_count;

                                        your_items = recipient_items;
                                        your_item_count = recipient_item_count;

                                        user_avatar = sender.getString("avatar");
                                        user_name = sender.getString("display_name");
                                        user_verified = sender.getBoolean("verified");
                                        user_case_opening = offer.getBoolean("is_case_opening");

                                        if(recipient_item_count == 0) {
                                            trade_summary = "Has sent you a gift!";
                                        }
                                        else if(sender_item_count == 0) {
                                            trade_summary = "Requests a gift from you";
                                        }
                                        else {
                                            trade_summary = "Sent " + recipient_item_count + ((recipient_item_count == 1) ? (" item") : (" items")) + " and received " + sender_item_count + ((sender_item_count == 1) ? (" item") : (" items"));
                                        }
                                    }
                                    else {
                                        their_items = recipient_items;
                                        their_item_count = recipient_item_count;

                                        your_items = sender_items;
                                        your_item_count = sender_item_count;

                                        user_avatar = recipient.getString("avatar");
                                        user_name = recipient.getString("display_name");
                                        user_verified = recipient.getBoolean("verified");
                                        user_case_opening = offer.getBoolean("is_case_opening");

                                        trade_summary = "Sent " + sender_item_count + ((sender_item_count == 1) ? (" item") : (" items")) + " and received " + recipient_item_count + ((recipient_item_count == 1) ? (" item") : (" items"));
                                    }

                                    trade_summary += " <font color = \"#AAAAAA\">| Status:</font>";

                                    if(state_name.equals("Declined")) {
                                        trade_summary += " <font color = \"#990000\"><b>" + state_name + "</b></font>";
                                    }
                                    else {
                                        trade_summary += " <font color = \"#58CCCC\"><b>" + state_name + "</b></font>";
                                    }

                                    offer_outline_container_layout = new LinearLayout(context);
                                    offer_outline_container_layout.setPadding(unit_conversion_1, unit_conversion_1, unit_conversion_1, unit_conversion_1);
                                    offer_outline_container_layout.setBackgroundDrawable(container_outline_drawable);
                                    offer_outline_container_layout.setOrientation(LinearLayout.VERTICAL);

                                    offer_outline_container_layout_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                    if(i != 0) {
                                        offer_outline_container_layout_params.setMargins(0, unit_conversion_2, 0, 0);
                                    }

                                    // -----

                                    offer_header_layout = new LinearLayout(context);
                                    offer_header_layout.setPadding(unit_conversion_3, unit_conversion_3, unit_conversion_3, unit_conversion_9);
                                    offer_header_layout.setBackgroundDrawable(offer_header_container_drawable);
                                    offer_header_layout.setOrientation(LinearLayout.VERTICAL);

                                    offer_header_inner_layout = new LinearLayout(context);
                                    offer_header_inner_layout.setGravity(Gravity.CENTER);
                                    offer_header_inner_layout.setOrientation(LinearLayout.HORIZONTAL);

                                    if(user_avatar.equals("null") || user_avatar.equals("/images/opskins-logo-avatar.png")) {
                                        user_avatar_view_1 = new ImageView(context);
                                        user_avatar_view_1.setImageDrawable(opskins_logo_drawable);
                                        user_avatar_view_1.setColorFilter(ContextCompat.getColor(context, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
                                        offer_header_inner_layout.addView(user_avatar_view_1, unit_conversion_4, unit_conversion_4);

                                        // -----

                                        image_load_count += 1;
                                    }
                                    else {
                                        user_avatar_view_2 = new de.hdodenhof.circleimageview.CircleImageView(context);
                                        user_avatar_view_2.setBorderWidth(unit_conversion_1);
                                        user_avatar_view_2.setBorderColor(color_white);
                                        offer_header_inner_layout.addView(user_avatar_view_2, unit_conversion_4, unit_conversion_4);

                                        Glide.with(context)
                                                .load(user_avatar)
                                                .apply(new RequestOptions().fitCenter())
                                                .listener(new RequestListener<Drawable>() {
                                                    @Override
                                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                        return false;
                                                    }

                                                    @Override
                                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                        if(!offers_loaded && (++ image_load_count) == total_image_count) {
                                                            offers_loaded = true;

                                                            // -----

                                                            clearView_LinearLayout(offers_container);

                                                            offers_container.addView(offer_inner_container_layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                                        }
                                                        return false;
                                                    }
                                                })
                                                .into(user_avatar_view_2);
                                    }

                                    user_name_view = new TextView(context);
                                    user_name_view.setPadding(unit_conversion_5, 0, 0, 0);
                                    user_name_view.setText(user_name);
                                    user_name_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                    user_name_view.setTextColor(color_white);
                                    offer_header_inner_layout.addView(user_name_view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                    if(user_verified) {
                                        user_status_view = new ImageView(context);
                                        user_status_view.setImageDrawable(verified_drawable);

                                        user_status_view_layout_params = new LinearLayout.LayoutParams(unit_conversion_6, unit_conversion_6);
                                        user_status_view_layout_params.setMargins(unit_conversion_7, 0, 0, 0);

                                        offer_header_inner_layout.addView(user_status_view, user_status_view_layout_params);
                                    }
                                    else if(user_case_opening) {
                                        user_status_view = new ImageView(context);
                                        user_status_view.setImageDrawable(vcase_drawable);

                                        user_status_view_layout_params = new LinearLayout.LayoutParams(unit_conversion_8, unit_conversion_8);
                                        user_status_view_layout_params.setMargins(unit_conversion_3, 0, 0, 0);

                                        offer_header_inner_layout.addView(user_status_view, user_status_view_layout_params);
                                    }

                                    offer_header_layout.addView(offer_header_inner_layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                    // -

                                    offer_header_inner_layout = new LinearLayout(context);
                                    offer_header_inner_layout.setPadding(0, unit_conversion_3, 0, unit_conversion_9);
                                    offer_header_inner_layout.setOrientation(LinearLayout.HORIZONTAL);

                                    separator_view = new RelativeLayout(context);
                                    separator_view.setBackgroundColor(color_pickled_bluewood);
                                    offer_header_inner_layout.addView(separator_view, RelativeLayout.LayoutParams.MATCH_PARENT, unit_conversion_1);

                                    offer_header_layout.addView(offer_header_inner_layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                    // -

                                    offer_header_inner_layout = new LinearLayout(context);
                                    offer_header_inner_layout.setGravity(Gravity.CENTER_HORIZONTAL);
                                    offer_header_inner_layout.setOrientation(LinearLayout.HORIZONTAL);

                                    trade_summary_view = new TextView(context);
                                    trade_summary_view.setText(main.fromHTML(trade_summary));
                                    trade_summary_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                    trade_summary_view.setTextColor(color_white);
                                    trade_summary_view.setGravity(Gravity.CENTER_HORIZONTAL);
                                    offer_header_inner_layout.addView(trade_summary_view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                    offer_header_layout.addView(offer_header_inner_layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                    // -

                                    if(state_name.equals("Open")) {
                                        final int offer_id = offer.getInt("id");

                                        if(selected_offer_type == Constant.OFFER_TYPE_RECEIVED) {
                                            offer_header_inner_layout = new LinearLayout(context);
                                            offer_header_inner_layout.setPadding(0, unit_conversion_9, 0, 0);
                                            offer_header_inner_layout.setGravity(Gravity.CENTER_HORIZONTAL);
                                            offer_header_inner_layout.setOrientation(LinearLayout.HORIZONTAL);

                                            button_view_1 = new TextView(context);
                                            button_view_1.setPadding(unit_conversion_2, unit_conversion_3, unit_conversion_2, unit_conversion_3);
                                            button_view_1.setBackgroundColor(color_red_berry);
                                            button_view_1.setText(decline_button_text);
                                            button_view_1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                            button_view_1.setTextColor(color_white);
                                            offer_header_inner_layout.addView(button_view_1, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                            button_view_2 = new TextView(context);
                                            button_view_2.setPadding(unit_conversion_2, unit_conversion_3, unit_conversion_2, unit_conversion_3);
                                            button_view_2.setBackgroundColor(color_denim);
                                            button_view_2.setText(accept_button_text);
                                            button_view_2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                            button_view_2.setTextColor(color_white);

                                            button_view_layout_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                            button_view_layout_params.setMargins(unit_conversion_5, 0, 0, 0);

                                            offer_header_inner_layout.addView(button_view_2, button_view_layout_params);

                                            button_view_2.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    if(perform_action) {
                                                        final Object[] input_dialog_response = main.showInputDialog(context, "Security", "Enter two factor authentication code:", "");

                                                        if(input_dialog_response != null) {
                                                            final EditText edit_text_view = (EditText) input_dialog_response[1];

                                                            ((AlertDialog) input_dialog_response[0]).setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                                @Override
                                                                public void onDismiss(DialogInterface dialog) {
                                                                    on2FAEntered(fragment, edit_text_view, offer_id);
                                                                }
                                                            });

                                                            edit_text_view.setOnKeyListener(new View.OnKeyListener() {
                                                                public boolean onKey(View v, int keyCode, KeyEvent event) {
                                                                    if(keyCode ==  KeyEvent.KEYCODE_ENTER) {
                                                                        ((AlertDialog) input_dialog_response[0]).dismiss();

                                                                        // -----

                                                                        on2FAEntered(fragment, edit_text_view, offer_id);
                                                                        return true;
                                                                    }
                                                                    return false;
                                                                }
                                                            });
                                                        }
                                                    }
                                                }
                                            });

                                            offer_header_layout.addView(offer_header_inner_layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                        }
                                        else {
                                            offer_header_inner_layout = new LinearLayout(context);
                                            offer_header_inner_layout.setPadding(0, unit_conversion_9, 0, 0);
                                            offer_header_inner_layout.setGravity(Gravity.CENTER_HORIZONTAL);
                                            offer_header_inner_layout.setOrientation(LinearLayout.HORIZONTAL);

                                            button_view_1 = new TextView(context);
                                            button_view_1.setPadding(unit_conversion_2, unit_conversion_3, unit_conversion_2, unit_conversion_3);
                                            button_view_1.setBackgroundColor(color_red_berry);
                                            button_view_1.setText(cancel_button_text);
                                            button_view_1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                            button_view_1.setTextColor(color_white);
                                            offer_header_inner_layout.addView(button_view_1, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                            offer_header_layout.addView(offer_header_inner_layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                        }

                                        button_view_1.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if(perform_action) {
                                                    perform_action = false;
                                                    main.set_perform_action(false);

                                                    // -----

                                                    final RequestParams params = new RequestParams();
                                                    params.put("offer_id", offer_id);

                                                    opskins_trade_api.post_SetBearerAuth("ITrade/CancelOffer/v1", params, false, new JsonHttpResponseHandler() {
                                                        @Override
                                                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                                            loadOffers(fragment, 1, true);
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
                                                                        main.showDialog(context, "An error occurred", errorResponse.getString("error_description"));
                                                                    }
                                                                    else {
                                                                        main.showDialog(context, "An error occurred", errorResponse.getString("message"));
                                                                    }
                                                                }
                                                                catch (JSONException e) {
                                                                    main.showDialog(context, "An error occurred", "Expected return data not found.");
                                                                }
                                                            }

                                                            // -----

                                                            perform_action = true;
                                                            main.set_perform_action(true);
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }

                                    // -

                                    if(!message.isEmpty()) {
                                        offer_header_inner_layout = new LinearLayout(context);
                                        offer_header_inner_layout.setPadding(0, unit_conversion_9, 0, unit_conversion_9);
                                        offer_header_inner_layout.setGravity(Gravity.CENTER_HORIZONTAL);
                                        offer_header_inner_layout.setOrientation(LinearLayout.HORIZONTAL);

                                        separator_view = new RelativeLayout(context);
                                        separator_view.setBackgroundColor(color_pickled_bluewood);
                                        offer_header_inner_layout.addView(separator_view, RelativeLayout.LayoutParams.MATCH_PARENT, unit_conversion_1);

                                        offer_header_layout.addView(offer_header_inner_layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                        // -

                                        offer_header_inner_layout = new LinearLayout(context);
                                        offer_header_inner_layout.setGravity(Gravity.CENTER_HORIZONTAL);
                                        offer_header_inner_layout.setOrientation(LinearLayout.HORIZONTAL);

                                        message_view = new TextView(context);
                                        message_view.setText(main.fromHTML("Message: <font color = \"#416990\">\"" + message + "\"</font>"));
                                        message_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                        message_view.setTextColor(color_white);
                                        message_view.setGravity(Gravity.CENTER_HORIZONTAL);
                                        offer_header_inner_layout.addView(message_view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                        offer_header_layout.addView(offer_header_inner_layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    }

                                    // -----

                                    offer_detail_layout = new LinearLayout(context);
                                    offer_detail_layout.setPadding(unit_conversion_13, unit_conversion_13, unit_conversion_13, unit_conversion_13);
                                    offer_detail_layout.setBackgroundDrawable(offer_detail_container_drawable);
                                    offer_detail_layout.setOrientation(LinearLayout.HORIZONTAL);

                                    offer_detail_inner_layout = new LinearLayout(context);
                                    offer_detail_inner_layout.setOrientation(LinearLayout.HORIZONTAL);

                                    offer_detail_view = new TextView(context);
                                    offer_detail_view.setText(offer_detail_text);
                                    offer_detail_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                                    offer_detail_view.setTextColor(color_downy);
                                    offer_detail_inner_layout.addView(offer_detail_view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                    offer_detail_layout.addView(offer_detail_inner_layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                    // -----

                                    offer_content_layout = new LinearLayout(context);
                                    offer_content_layout.setPadding(unit_conversion_11, unit_conversion_3, unit_conversion_11, unit_conversion_11);
                                    offer_content_layout.setBackgroundDrawable(offer_content_container_drawable);
                                    offer_content_layout.setOrientation(LinearLayout.VERTICAL);

                                    if(i >= 1) {
                                        offer_detail_layout.setBackgroundDrawable(offer_content_container_drawable);

                                        offer_content_layout.setVisibility(View.GONE);
                                    }

                                    offer_content_inner_layout = new LinearLayout(context);
                                    offer_content_inner_layout.setOrientation(LinearLayout.HORIZONTAL);

                                    item_count_view = new TextView(context);
                                    item_count_view.setText(new StringBuilder("Their Items (" + their_item_count  + ")"));
                                    item_count_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                    item_count_view.setTextColor(color_white);
                                    offer_content_inner_layout.addView(item_count_view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                    offer_align_right_container_layout = new LinearLayout(context);
                                    offer_align_right_container_layout.setGravity(Gravity.END);

                                    offer_align_right_container_layout_params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

                                    item_count_view = new TextView(context);
                                    item_count_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                    item_count_view.setTextColor(color_white);
                                    item_count_view.setGravity(Gravity.END);
                                    offer_align_right_container_layout.addView(item_count_view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                    offer_content_inner_layout.addView(offer_align_right_container_layout, offer_align_right_container_layout_params);
                                    offer_content_layout.addView(offer_content_inner_layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                    for(int j = 0; j < their_item_count; j ++) {
                                        item = their_items.getJSONObject(j);

                                        try {
                                            final JSONObject item_images = item.getJSONObject("image");

                                            item_image = item_images.getString("300px");
                                        }
                                        catch (JSONException e) {
                                            item_image = item.getString("image");
                                        }

                                        try {
                                            item_suggested_price = item.getLong("suggested_price");
                                            their_items_total_value += item_suggested_price;
                                        }
                                        catch (JSONException e) {
                                            item_suggested_price = 0;
                                        }

                                        try {
                                            item_wear = item.getDouble("wear");
                                        }
                                        catch (JSONException e) {
                                            item_wear = 0.00;
                                        }

                                        try {
                                            item_attributes = item.getJSONObject("attributes");
                                            item_serial_number = item_attributes.getLong("serial_sku_wear");
                                        }
                                        catch (JSONException e) {
                                            item_serial_number = 0;
                                        }

                                        offer_content_inner_layout = new LinearLayout(context);
                                        offer_content_inner_layout.setGravity(Gravity.CENTER_VERTICAL);
                                        offer_content_inner_layout.setOrientation(LinearLayout.HORIZONTAL);

                                        item_image_view = new ImageView(context);
                                        offer_content_inner_layout.addView(item_image_view, unit_conversion_10, unit_conversion_10);

                                        Glide.with(context)
                                                .load(item_image)
                                                .apply(new RequestOptions().fitCenter())
                                                .listener(new RequestListener<Drawable>() {
                                                    @Override
                                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                        return false;
                                                    }

                                                    @Override
                                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                        if(!offers_loaded && (++ image_load_count) == total_image_count) {
                                                            offers_loaded = true;

                                                            // -----

                                                            clearView_LinearLayout(offers_container);

                                                            offers_container.addView(offer_inner_container_layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                                        }
                                                        return false;
                                                    }
                                                })
                                                .into(item_image_view);

                                        offer_align_right_container_layout = new LinearLayout(context);
                                        offer_align_right_container_layout.setGravity(Gravity.CENTER_HORIZONTAL);

                                        offer_align_right_container_layout_params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.75f);

                                        item_name_view = new TextView(context);
                                        item_name_view.setPadding(unit_conversion_12, 0, 0, 0);
                                        item_name_view.setText(main.fromHTML("<font color = \"" + item.getString("color") + "\">" + item.getString("name") + "</font>" + ((item_wear == 0.00 && item_serial_number == 0) ? ("") : ("<br><font color = \"#CCCCCC\">Wear:</font> " + String.format(Locale.getDefault(), "%.5f", item_wear * 100) + "% <font color = \"#443836\">|</font> <font color = \"#CCCCCC\">Serial Number:</font> " + item_serial_number))));
                                        item_name_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                        item_name_view.setTextColor(color_white);
                                        item_name_view.setGravity(Gravity.CENTER_HORIZONTAL);
                                        offer_align_right_container_layout.addView(item_name_view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                        offer_content_inner_layout.addView(offer_align_right_container_layout, offer_align_right_container_layout_params);

                                        offer_align_right_container_layout = new LinearLayout(context);
                                        offer_align_right_container_layout.setGravity(Gravity.END);

                                        offer_align_right_container_layout_params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.25f);

                                        item_price_view = new TextView(context);
                                        item_price_view.setText(((item_suggested_price == 0) ? ("") : ("$" + main.currencyFormat(String.valueOf((double) item_suggested_price / 100)))));
                                        item_price_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                        item_price_view.setTextColor(color_kashmir_blue);
                                        item_price_view.setGravity(Gravity.END);
                                        offer_align_right_container_layout.addView(item_price_view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                        offer_content_inner_layout.addView(offer_align_right_container_layout, offer_align_right_container_layout_params);
                                        offer_content_layout.addView(offer_content_inner_layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    }

                                    if(their_item_count != 0) {
                                        item_count_view.setText(main.fromHTML("<font color = \"#CCCCCC\">Total Value</font><br><b>$" + main.currencyFormat(String.valueOf((double) their_items_total_value / 100)) + "</b>"));
                                    }

                                    // -----

                                    offer_content_inner_layout = new LinearLayout(context);
                                    offer_content_inner_layout.setPadding(0, unit_conversion_9, 0, unit_conversion_9);
                                    offer_content_inner_layout.setOrientation(LinearLayout.HORIZONTAL);

                                    separator_view = new RelativeLayout(context);
                                    separator_view.setBackgroundColor(color_pickled_bluewood);
                                    offer_content_inner_layout.addView(separator_view, RelativeLayout.LayoutParams.MATCH_PARENT, unit_conversion_1);

                                    offer_content_layout.addView(offer_content_inner_layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                    // -----

                                    offer_content_inner_layout = new LinearLayout(context);
                                    offer_content_inner_layout.setOrientation(LinearLayout.HORIZONTAL);

                                    item_count_view = new TextView(context);
                                    item_count_view.setText(new StringBuilder("Your Items (" + your_item_count  + ")"));
                                    item_count_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                    item_count_view.setTextColor(color_white);
                                    offer_content_inner_layout.addView(item_count_view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                    offer_align_right_container_layout = new LinearLayout(context);
                                    offer_align_right_container_layout.setGravity(Gravity.END);

                                    offer_align_right_container_layout_params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

                                    item_count_view = new TextView(context);
                                    item_count_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                    item_count_view.setTextColor(color_white);
                                    item_count_view.setGravity(Gravity.END);
                                    offer_align_right_container_layout.addView(item_count_view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                    offer_content_inner_layout.addView(offer_align_right_container_layout, offer_align_right_container_layout_params);
                                    offer_content_layout.addView(offer_content_inner_layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                    for(int j = 0; j < your_item_count; j ++) {
                                        item = your_items.getJSONObject(j);

                                        try {
                                            final JSONObject item_images = item.getJSONObject("image");

                                            item_image = item_images.getString("300px");
                                        }
                                        catch (JSONException e) {
                                            item_image = item.getString("image");
                                        }

                                        try {
                                            item_suggested_price = item.getLong("suggested_price");
                                            your_items_total_value += item_suggested_price;
                                        }
                                        catch (JSONException e) {
                                            item_suggested_price = 0;
                                        }

                                        try {
                                            item_wear = item.getDouble("wear");
                                        }
                                        catch (JSONException e) {
                                            item_wear = 0.00;
                                        }

                                        try {
                                            item_attributes = item.getJSONObject("attributes");
                                            item_serial_number = item_attributes.getLong("serial_sku_wear");
                                        }
                                        catch (JSONException e) {
                                            item_serial_number = 0;
                                        }

                                        offer_content_inner_layout = new LinearLayout(context);
                                        offer_content_inner_layout.setGravity(Gravity.CENTER_VERTICAL);
                                        offer_content_inner_layout.setOrientation(LinearLayout.HORIZONTAL);

                                        item_image_view = new ImageView(context);
                                        offer_content_inner_layout.addView(item_image_view, unit_conversion_10, unit_conversion_10);

                                        Glide.with(context)
                                                .load(item_image)
                                                .apply(new RequestOptions().fitCenter())
                                                .listener(new RequestListener<Drawable>() {
                                                    @Override
                                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                        return false;
                                                    }

                                                    @Override
                                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                        if(!offers_loaded && (++ image_load_count) == total_image_count) {
                                                            offers_loaded = true;

                                                            // -----

                                                            clearView_LinearLayout(offers_container);

                                                            offers_container.addView(offer_inner_container_layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                                        }
                                                        return false;
                                                    }
                                                })
                                                .into(item_image_view);

                                        offer_align_right_container_layout = new LinearLayout(context);
                                        offer_align_right_container_layout.setGravity(Gravity.CENTER_HORIZONTAL);

                                        offer_align_right_container_layout_params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.75f);

                                        item_name_view = new TextView(context);
                                        item_name_view.setPadding(unit_conversion_12, 0, 0, 0);
                                        item_name_view.setText(main.fromHTML("<font color = \"" + item.getString("color") + "\">" + item.getString("name") + "</font>" + ((item_wear == 0.00 && item_serial_number == 0) ? ("") : ("<br><font color = \"#CCCCCC\">Wear:</font> " + String.format(Locale.getDefault(), "%.5f", item_wear * 100) + "% <font color = \"#443836\">|</font> <font color = \"#CCCCCC\">Serial Number:</font> " + item_serial_number))));
                                        item_name_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                        item_name_view.setTextColor(color_white);
                                        item_name_view.setGravity(Gravity.CENTER_HORIZONTAL);
                                        offer_align_right_container_layout.addView(item_name_view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                        offer_content_inner_layout.addView(offer_align_right_container_layout, offer_align_right_container_layout_params);

                                        offer_align_right_container_layout = new LinearLayout(context);
                                        offer_align_right_container_layout.setGravity(Gravity.END);

                                        offer_align_right_container_layout_params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.25f);

                                        item_price_view = new TextView(context);
                                        item_price_view.setText(((item_suggested_price == 0) ? ("") : ("$" + main.currencyFormat(String.valueOf((double) item_suggested_price / 100)))));
                                        item_price_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                        item_price_view.setTextColor(color_kashmir_blue);
                                        item_price_view.setGravity(Gravity.END);
                                        offer_align_right_container_layout.addView(item_price_view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                        offer_content_inner_layout.addView(offer_align_right_container_layout, offer_align_right_container_layout_params);
                                        offer_content_layout.addView(offer_content_inner_layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    }

                                    if(your_item_count != 0) {
                                        item_count_view.setText(main.fromHTML("<font color = \"#CCCCCC\">Total Value</font><br><b>$" + main.currencyFormat(String.valueOf((double) your_items_total_value / 100)) + "</b>"));
                                    }

                                    // -----

                                    final LinearLayout offer_detail_layout_final = offer_detail_layout;
                                    final LinearLayout offer_content_layout_final = offer_content_layout;

                                    offer_detail_layout.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if(offer_content_layout_final.getVisibility() == View.GONE) {
                                                offer_detail_layout_final.setBackgroundDrawable(offer_detail_container_drawable);

                                                offer_content_layout_final.setVisibility(View.VISIBLE);
                                            }
                                            else {
                                                offer_detail_layout_final.setBackgroundDrawable(offer_content_container_drawable);

                                                offer_content_layout_final.setVisibility(View.GONE);
                                            }

                                            refreshContainers();
                                        }
                                    });

                                    offer_containers.add(offer_outline_container_layout);
                                    offer_containers.add(offer_header_layout);
                                    offer_containers.add(offer_content_layout);

                                    // -----

                                    offer_outline_container_layout.addView(offer_header_layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    offer_outline_container_layout.addView(offer_detail_layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    offer_outline_container_layout.addView(offer_content_layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                    offer_inner_container_layout.addView(offer_outline_container_layout, offer_outline_container_layout_params);
                                }

                                if(!offers_loaded && image_load_count == total_image_count) {
                                    offers_loaded = true;

                                    // -----

                                    clearView_LinearLayout(offers_container);

                                    offers_container.addView(offer_inner_container_layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                }
                            }
                            else {
                                clearView_LinearLayout(offers_container);

                                // -----

                                TextView error_view = new TextView(context);
                                error_view.setText(resources.getString(R.string.fragment_offers_empty_tab));
                                error_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                error_view.setTextColor(resources.getColor(R.color.white));
                                error_view.setGravity(Gravity.CENTER_HORIZONTAL);
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
                            error_view.setGravity(Gravity.CENTER_HORIZONTAL);
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

                                // -----

                                clearView_LinearLayout(offers_container);

                                // -----

                                TextView error_view = new TextView(context);
                                error_view.setText(resources.getString(R.string.fragment_offers_error_no_internet_connection));
                                error_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                error_view.setTextColor(resources.getColor(R.color.white));
                                error_view.setGravity(Gravity.CENTER_HORIZONTAL);
                                offers_container.addView(error_view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            }
                            else {
                                main.showDialog(context, "An error occurred", "Request failed.");

                                // -----

                                clearView_LinearLayout(offers_container);

                                // -----

                                TextView error_view = new TextView(context);
                                error_view.setText(resources.getString(R.string.fragment_offers_error_request_failed));
                                error_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                error_view.setTextColor(resources.getColor(R.color.white));
                                error_view.setGravity(Gravity.CENTER_HORIZONTAL);
                                offers_container.addView(error_view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
                                    error_view.setGravity(Gravity.CENTER_HORIZONTAL);
                                    offers_container.addView(error_view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                }
                                else {
                                    clearView_LinearLayout(offers_container);

                                    // -----

                                    TextView error_view = new TextView(context);
                                    error_view.setText(new StringBuilder(resources.getString(R.string.fragment_offers_error_tag) + errorResponse.getString("message")));
                                    error_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                    error_view.setTextColor(resources.getColor(R.color.white));
                                    error_view.setGravity(Gravity.CENTER_HORIZONTAL);
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
                                error_view.setGravity(Gravity.CENTER_HORIZONTAL);
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