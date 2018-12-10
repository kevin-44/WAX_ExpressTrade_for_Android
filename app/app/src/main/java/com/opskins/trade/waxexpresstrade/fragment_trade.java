package com.opskins.trade.waxexpresstrade;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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
import java.util.StringTokenizer;
import cz.msebera.android.httpclient.Header;

public class fragment_trade extends Fragment {
    // ** DEFINITIONS

    private static class Constant {
        // ** GENERAL

        private static final int ITEMS_PER_PAGE = 500;
        private static final int MAX_SELECTABLE_ITEMS = 200;
    }

    // ** ARRAYS

    private static class PartnerData {
        private static Boolean valid_user = false;
        private static int id = -1;
        private static String username = null;
        private static String avatar = null;
        private static String trade_url = null;
    }

    // ** VARIABLES

    // *** GENERAL

    private List<Integer> app_ids = new ArrayList<>();
    private List<String> app_images = new ArrayList<>();
    private int selected_app_id = -1;
    private List<Integer> selected_user_items = new ArrayList<>();
    private long total_value_selected_user_items = 0;
    private List<Integer> selected_partner_items = new ArrayList<>();
    private long total_value_selected_partner_items = 0;

    // *** STATES

    private Boolean perform_action = false;

    // ** CALLBACKS

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final main main = new main();

        main.set_perform_action(false);

        // -----

        final View fragment = inflater.inflate(R.layout.fragment_trade, container, false);
        final Context context = fragment_trade.this.getContext();
        final Resources resources = getResources();
        final DisplayMetrics display_metrics = resources.getDisplayMetrics();
        final opskins_trade_api opskins_trade_api = new opskins_trade_api(new WeakReference<>(context));
        final CheckBox dont_show_items_in_active_trades_checkbox = fragment.findViewById(R.id.fragment_trade_dont_show_items_in_active_trades_checkbox);
        final TextView user_search_inventory_input = fragment.findViewById(R.id.fragment_trade_user_search_inventory_input);
        final TextView find_partner_input = fragment.findViewById(R.id.fragment_trade_find_partner_input);
        final CheckBox one_way_trade_or_gift_checkbox = fragment.findViewById(R.id.fragment_trade_one_way_trade_or_gift_checkbox);
        final TextView partner_search_inventory_input = fragment.findViewById(R.id.fragment_trade_partner_search_inventory_input);
        final TextView message_input = fragment.findViewById(R.id.fragment_trade_enter_message_input);
        final TextView twofactor_input = fragment.findViewById(R.id.fragment_trade_enter_twofactor_input);
        final String default_message = resources.getString(R.string.fragment_trade_default_message);

        if(context != null) {
            opskins_trade_api.get("ITrade/GetApps/v1", null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        final JSONObject data = response.getJSONObject("response");
                        final JSONArray apps = data.getJSONArray("apps");
                        final int app_count = apps.length();

                        if(app_count >= 1) {
                            JSONObject app_1 = apps.getJSONObject(0);
                            final ProgressBar selected_app_progress_bar_view = fragment.findViewById(R.id.fragment_trade_selected_app_progress_bar);
                            final ImageView selected_app_image_view = fragment.findViewById(R.id.fragment_trade_selected_app_image);

                            selected_app_id = app_1.getInt("internal_app_id");

                            Glide.with(context)
                                    .load(app_1.getString("img"))
                                    .apply(new RequestOptions().fitCenter())
                                    .listener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            main.showDialog(context, "An error occurred", "Failed to display the image of the selected app.");
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            selected_app_progress_bar_view.setVisibility(View.GONE);
                                            selected_app_image_view.setVisibility(View.VISIBLE);
                                            return false;
                                        }
                                    })
                                    .into(selected_app_image_view);

                            for(int i = 0; i < app_count; i ++) {
                                app_1 = apps.getJSONObject(i);

                                app_ids.add(app_1.getInt("internal_app_id"));
                                app_images.add(app_1.getString("img"));
                            }

                            fragment.findViewById(R.id.fragment_trade_selected_app_container).setOnClickListener(new View.OnClickListener() {
                                @SuppressLint("InflateParams")
                                @Override
                                public void onClick(View view) {
                                    final int unit_conversion_1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, display_metrics);
                                    final int unit_conversion_2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, display_metrics);
                                    final int unit_conversion_3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, display_metrics);
                                    final int unit_conversion_4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 33, display_metrics);

                                    // -----

                                    JSONObject app_2;
                                    final ScrollView app_list_container_layout = new ScrollView(context);

                                    final LinearLayout app_list_layout = new LinearLayout(context);
                                    app_list_layout.setPadding(unit_conversion_1, unit_conversion_2, unit_conversion_1, unit_conversion_2);
                                    app_list_layout.setOrientation(LinearLayout.VERTICAL);

                                    RelativeLayout app_list_item_layout;
                                    LinearLayout layout;
                                    RelativeLayout.LayoutParams layout_params;
                                    ImageView app_image_view;
                                    TextView app_name_view;
                                    final AlertDialog alert_dialog = new AlertDialog.Builder(context, R.style.DialogTheme).create();
                                    final int app_name_color = resources.getColor(R.color.iron);

                                    for(int i = 0; i < app_count; i ++) {
                                        try {
                                            app_2 = apps.getJSONObject(i);
                                            app_list_item_layout = new RelativeLayout(context);

                                            layout = new LinearLayout(context);
                                            layout.setGravity(Gravity.CENTER_VERTICAL);

                                            layout_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                                            layout_params.setMargins(0, unit_conversion_3, 0, unit_conversion_3);

                                            app_image_view = new ImageView(context);
                                            layout.addView(app_image_view, unit_conversion_4, unit_conversion_4);

                                            Glide.with(context).load(app_2.getString("img")).apply(new RequestOptions().fitCenter()).into(app_image_view);

                                            app_name_view = new TextView(context);
                                            app_name_view.setPadding(unit_conversion_2, 0, 0, 0);
                                            app_name_view.setText(app_2.getString("name"));
                                            app_name_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                                            app_name_view.setTextColor(app_name_color);
                                            layout.addView(app_name_view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                            app_list_item_layout.addView(layout, layout_params);
                                            app_list_layout.addView(app_list_item_layout, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                                            // -----

                                            final int app_index = i;

                                            app_list_item_layout.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    if(perform_action) {
                                                        perform_action = false;
                                                        main.set_perform_action(false);

                                                        // -----

                                                        selected_app_id = app_ids.get(app_index);

                                                        Glide.with(context)
                                                                .load(app_images.get(app_index))
                                                                .apply(new RequestOptions().fitCenter())
                                                                .listener(new RequestListener<Drawable>() {
                                                                    @Override
                                                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                                        main.showDialog(context, "An error occurred", "Failed to display the image of the selected app.");
                                                                        return false;
                                                                    }

                                                                    @Override
                                                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                                        return false;
                                                                    }
                                                                })
                                                                .into(selected_app_image_view);

                                                        alert_dialog.dismiss();

                                                        if(PartnerData.valid_user) {
                                                            loadInventory(fragment, main.getUserID(), 1, true, false);
                                                            loadInventory(fragment, PartnerData.id, 1, true, true);
                                                        }
                                                        else {
                                                            loadInventory(fragment, main.getUserID(), 1, true, true);
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                        catch (JSONException e) {
                                            main.showDialog(context, "An error occurred", "Failed to create list of apps.");
                                        }
                                    }

                                    app_list_container_layout.addView(app_list_layout, ScrollView.LayoutParams.WRAP_CONTENT, ScrollView.LayoutParams.WRAP_CONTENT);

                                    alert_dialog.setView(app_list_container_layout);
                                    alert_dialog.show();
                                }
                            });

                            if(PartnerData.valid_user) {
                                Glide.with(context).load(PartnerData.avatar).apply(new RequestOptions().placeholder(R.color.transparent).fitCenter()).into((ImageView) fragment.findViewById(R.id.fragment_trade_partner_avatar));
                                ((TextView)fragment.findViewById(R.id.fragment_trade_partner_username)).setText(PartnerData.username);

                                loadInventory(fragment, main.getUserID(), 1, true, false);
                                loadInventory(fragment, PartnerData.id, 1, true, true);
                            }
                            else {
                                if(main.get_fragment_trade_find_partner() != null) {
                                    find_partner_input.setText(main.get_fragment_trade_find_partner());
                                    find_partner_input.setTextColor(resources.getColor(R.color.white));

                                    main.set_fragment_trade_find_partner(null);

                                    // -----

                                    loadInventory(fragment, main.getUserID(), 1, true, false);

                                    // -----

                                    final String partner_trade_url = find_partner_input.getText().toString();
                                    final TextView find_partner_error_view = fragment.findViewById(R.id.fragment_trade_find_partner_error);

                                    if(URLUtil.isValidUrl(partner_trade_url)) {
                                        final String[] partner_trade_url_parts = partner_trade_url.split("/");

                                        if(partner_trade_url_parts.length > 5) {
                                            try {
                                                PartnerData.valid_user = false;
                                                PartnerData.id = Integer.parseInt(partner_trade_url_parts[4]);
                                                PartnerData.trade_url = partner_trade_url;

                                                if(PartnerData.id != main.getUserID()) {
                                                    loadInventory(fragment, PartnerData.id, 1, true, true);
                                                }
                                                else {
                                                    find_partner_error_view.setVisibility(View.VISIBLE);
                                                    find_partner_error_view.setText(resources.getString(R.string.fragment_trade_find_partner_error_cant_trade_with_yourself));

                                                    // -----

                                                    perform_action = true;
                                                    main.set_perform_action(true);
                                                }
                                            }
                                            catch (NumberFormatException nfe) {
                                                find_partner_error_view.setVisibility(View.VISIBLE);
                                                find_partner_error_view.setText(resources.getString(R.string.fragment_trade_find_partner_error_unable_to_load_friend_inventory));

                                                // -----

                                                perform_action = true;
                                                main.set_perform_action(true);
                                            }
                                        }
                                        else {
                                            find_partner_error_view.setVisibility(View.VISIBLE);
                                            find_partner_error_view.setText(resources.getString(R.string.fragment_trade_find_partner_error_user_not_found));

                                            // -----

                                            perform_action = true;
                                            main.set_perform_action(true);
                                        }
                                    }
                                    else {
                                        find_partner_error_view.setVisibility(View.VISIBLE);
                                        find_partner_error_view.setText(resources.getString(R.string.fragment_trade_find_partner_error_user_not_found));

                                        // -----

                                        perform_action = true;
                                        main.set_perform_action(true);
                                    }
                                }
                                else {
                                    loadInventory(fragment, main.getUserID(), 1, true, true);
                                }
                            }
                        }
                        else {
                            main.showDialog(context, "An error occurred", "No apps found.");

                            // -----

                            main.set_perform_action(true);
                        }
                    }
                    catch (JSONException e) {
                        main.showDialog(context, "An error occurred", "Expected return data not found.");

                        // -----

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
                            final String message = errorResponse.getString("message");

                            if(message.equals(resources.getString(R.string.down_for_maintenance_error))) {
                                main.showDialog(context, "Down for maintenance", "Please try again later.");
                            }
                            else {
                                main.showDialog(context, "An error occurred", message);
                            }
                        }
                        catch (JSONException e) {
                            main.showDialog(context, "An error occurred", "Expected return data not found.");
                        }
                    }

                    // -----

                    main.set_perform_action(true);
                }
            });

            Glide.with(context).load(main.getUserAvatar()).apply(new RequestOptions().placeholder(R.color.transparent).fitCenter()).into((de.hdodenhof.circleimageview.CircleImageView) fragment.findViewById(R.id.fragment_trade_user_avatar));
            ((TextView) fragment.findViewById(R.id.fragment_trade_user_username)).setText(main.getUserUsername());

            dont_show_items_in_active_trades_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(perform_action) {
                        perform_action = false;
                        main.set_perform_action(false);

                        // -----

                        loadInventory(fragment, main.getUserID(), 1, true, true);
                    }
                    else {
                        dont_show_items_in_active_trades_checkbox.toggle();
                    }
                }
            });

            user_search_inventory_input.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(perform_action) {
                        final String search_inventory_hint = resources.getString(R.string.fragment_trade_search_inventory_hint);
                        final String search = user_search_inventory_input.getText().toString();
                        final Object[] input_dialog_response = main.showInputDialog(context, "Find an item in your inventory", "Enter the item's name:", ((search.equals(search_inventory_hint)) ? ("") : (search)));

                        if(input_dialog_response != null) {
                            final EditText edit_text_view = (EditText) input_dialog_response[1];

                            ((AlertDialog) input_dialog_response[0]).setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    main.onStringInputUpdated(context, user_search_inventory_input, edit_text_view, search_inventory_hint);

                                    if(!search.equals(search_inventory_hint) || !edit_text_view.getText().toString().isEmpty()) {
                                        loadInventory(fragment, main.getUserID(), 1, false, true);
                                    }
                                }
                            });

                            edit_text_view.setOnKeyListener(new View.OnKeyListener() {
                                public boolean onKey(View v, int keyCode, KeyEvent event) {
                                    if(keyCode ==  KeyEvent.KEYCODE_ENTER) {
                                        ((AlertDialog) input_dialog_response[0]).dismiss();

                                        // -----

                                        main.onStringInputUpdated(context, user_search_inventory_input, edit_text_view, search_inventory_hint);

                                        if(!search.equals(search_inventory_hint) || !edit_text_view.getText().toString().isEmpty()) {
                                            loadInventory(fragment, main.getUserID(), 1, false, true);
                                        }
                                        return true;
                                    }
                                    return false;
                                }
                            });
                        }
                    }
                }
            });

            find_partner_input.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(perform_action) {
                        final String trade_url = find_partner_input.getText().toString();
                        final Object[] input_dialog_response = main.showInputDialog(context, "Find your friend to trade with", "Enter WAX ExpressTrade URL:", ((trade_url.isEmpty()) ? ("") : (trade_url)));

                        if(input_dialog_response != null) {
                            final EditText edit_text_view = (EditText) input_dialog_response[1];

                            ((AlertDialog) input_dialog_response[0]).setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    main.onStringInputUpdated(context, find_partner_input, edit_text_view, "");
                                }
                            });

                            edit_text_view.setOnKeyListener(new View.OnKeyListener() {
                                public boolean onKey(View v, int keyCode, KeyEvent event) {
                                    if(keyCode ==  KeyEvent.KEYCODE_ENTER) {
                                        ((AlertDialog) input_dialog_response[0]).dismiss();

                                        // -----

                                        main.onStringInputUpdated(context, find_partner_input, edit_text_view, "");
                                        return true;
                                    }
                                    return false;
                                }
                            });
                        }
                    }
                }
            });

            fragment.findViewById(R.id.fragment_trade_find_partner_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(perform_action) {
                        perform_action = false;
                        main.set_perform_action(false);

                        // -----

                        final String partner_trade_url = find_partner_input.getText().toString();
                        final TextView find_partner_error_view = fragment.findViewById(R.id.fragment_trade_find_partner_error);

                        if(URLUtil.isValidUrl(partner_trade_url)) {
                            final String[] partner_trade_url_parts = partner_trade_url.split("/");

                            if(partner_trade_url_parts.length > 5) {
                                try {
                                    PartnerData.valid_user = false;
                                    PartnerData.id = Integer.parseInt(partner_trade_url_parts[4]);
                                    PartnerData.trade_url = partner_trade_url;

                                    if(PartnerData.id != main.getUserID()) {
                                        loadInventory(fragment, PartnerData.id, 1, true, true);
                                    }
                                    else {
                                        find_partner_error_view.setVisibility(View.VISIBLE);
                                        find_partner_error_view.setText(resources.getString(R.string.fragment_trade_find_partner_error_cant_trade_with_yourself));

                                        // -----

                                        perform_action = true;
                                        main.set_perform_action(true);
                                    }
                                }
                                catch (NumberFormatException nfe) {
                                    find_partner_error_view.setVisibility(View.VISIBLE);
                                    find_partner_error_view.setText(resources.getString(R.string.fragment_trade_find_partner_error_unable_to_load_friend_inventory));

                                    // -----

                                    perform_action = true;
                                    main.set_perform_action(true);
                                }
                            }
                            else {
                                find_partner_error_view.setVisibility(View.VISIBLE);
                                find_partner_error_view.setText(resources.getString(R.string.fragment_trade_find_partner_error_user_not_found));

                                // -----

                                perform_action = true;
                                main.set_perform_action(true);
                            }
                        }
                        else {
                            find_partner_error_view.setVisibility(View.VISIBLE);
                            find_partner_error_view.setText(resources.getString(R.string.fragment_trade_find_partner_error_user_not_found));

                            // -----

                            perform_action = true;
                            main.set_perform_action(true);
                        }
                    }
                }
            });

            one_way_trade_or_gift_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    updateMakeOfferButton(fragment);
                }
            });

            partner_search_inventory_input.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(perform_action && PartnerData.valid_user) {
                        final String search_inventory_hint = resources.getString(R.string.fragment_trade_search_inventory_hint);
                        final String search = partner_search_inventory_input.getText().toString();
                        final Object[] input_dialog_response = main.showInputDialog(context, "Find an item in your trading partner's inventory", "Enter the item's name:", ((search.equals(search_inventory_hint)) ? ("") : (search)));

                        if(input_dialog_response != null) {
                            final EditText edit_text_view = (EditText) input_dialog_response[1];

                            ((AlertDialog) input_dialog_response[0]).setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    main.onStringInputUpdated(context, partner_search_inventory_input, edit_text_view, search_inventory_hint);

                                    if(!search.equals(search_inventory_hint) || !edit_text_view.getText().toString().isEmpty()) {
                                        loadInventory(fragment, PartnerData.id, 1, false, true);
                                    }
                                }
                            });

                            edit_text_view.setOnKeyListener(new View.OnKeyListener() {
                                public boolean onKey(View v, int keyCode, KeyEvent event) {
                                    if(keyCode ==  KeyEvent.KEYCODE_ENTER) {
                                        ((AlertDialog) input_dialog_response[0]).dismiss();

                                        // -----

                                        main.onStringInputUpdated(context, partner_search_inventory_input, edit_text_view, search_inventory_hint);

                                        if(!search.equals(search_inventory_hint) || !edit_text_view.getText().toString().isEmpty()) {
                                            loadInventory(fragment, PartnerData.id, 1, false, true);
                                        }
                                        return true;
                                    }
                                    return false;
                                }
                            });
                        }
                    }
                }
            });

            fragment.findViewById(R.id.fragment_trade_enter_message_container).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(perform_action) {
                        final String message = message_input.getText().toString();
                        final Object[] input_dialog_response = main.showInputDialog(context, "Edit offer message", "Enter new offer message:", ((message.equals(default_message)) ? ("") : (message)));

                        if(input_dialog_response != null) {
                            final EditText edit_text_view = (EditText) input_dialog_response[1];

                            ((AlertDialog) input_dialog_response[0]).setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    main.onStringInputUpdated(context, message_input, edit_text_view, default_message);

                                    updateMakeOfferButton(fragment);
                                }
                            });

                            edit_text_view.setOnKeyListener(new View.OnKeyListener() {
                                public boolean onKey(View v, int keyCode, KeyEvent event) {
                                    if(keyCode ==  KeyEvent.KEYCODE_ENTER) {
                                        ((AlertDialog) input_dialog_response[0]).dismiss();

                                        // -----

                                        main.onStringInputUpdated(context, message_input, edit_text_view, default_message);

                                        updateMakeOfferButton(fragment);
                                        return true;
                                    }
                                    return false;
                                }
                            });
                        }
                    }
                }
            });

            twofactor_input.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(perform_action) {
                        final String twofactor_hint = resources.getString(R.string.fragment_trade_enter_twofactor_hint);
                        final String twofactor = twofactor_input.getText().toString();
                        final Object[] input_dialog_response = main.showInputDialog(context, "Security", "Enter two factor authentication code:", ((twofactor.equals(twofactor_hint)) ? ("") : (twofactor)));

                        if(input_dialog_response != null) {
                            final EditText edit_text_view = (EditText) input_dialog_response[1];

                            ((AlertDialog) input_dialog_response[0]).setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    main.onNumberInputUpdated(context, twofactor_input, edit_text_view, twofactor_hint);

                                    updateMakeOfferButton(fragment);
                                }
                            });

                            edit_text_view.setOnKeyListener(new View.OnKeyListener() {
                                public boolean onKey(View v, int keyCode, KeyEvent event) {
                                    if(keyCode ==  KeyEvent.KEYCODE_ENTER) {
                                        ((AlertDialog) input_dialog_response[0]).dismiss();

                                        // -----

                                        main.onNumberInputUpdated(context, twofactor_input, edit_text_view, twofactor_hint);

                                        updateMakeOfferButton(fragment);
                                        return true;
                                    }
                                    return false;
                                }
                            });
                        }
                    }
                }
            });

            fragment.findViewById(R.id.fragment_trade_make_offer_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(perform_action) {
                        perform_action = false;
                        main.set_perform_action(false);

                        // -----

                        final String twofactor_hint = resources.getString(R.string.fragment_trade_enter_twofactor_hint);
                        final String twofactor = twofactor_input.getText().toString();
                        final int selected_user_item_count = selected_user_items.size();
                        final int selected_partner_item_count = selected_partner_items.size();

                        if(PartnerData.valid_user && ((one_way_trade_or_gift_checkbox.isChecked() && (selected_user_item_count >= 1 || selected_partner_item_count >= 1)) || (selected_user_item_count >= 1 && selected_partner_item_count >= 1)) && !twofactor.equals(twofactor_hint)) {
                            final String default_message = resources.getString(R.string.fragment_trade_default_message);
                            final String message = message_input.getText().toString();

                            final RequestParams params = new RequestParams();
                            params.put("twofactor_code", twofactor);
                            params.put("trade_url", PartnerData.trade_url);
                            params.put("items_to_send", TextUtils.join(",", selected_user_items));
                            params.put("items_to_receive", TextUtils.join(",", selected_partner_items));

                            if(!message.equals(default_message)) {
                                params.put("message", message);
                            }

                            opskins_trade_api.post_SetBearerAuth("ITrade/SendOffer/v1", params, false, new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                    try {
                                        if(!response.has("message")) {
                                            selected_user_items.clear();
                                            total_value_selected_user_items = 0;

                                            selected_partner_items.clear();
                                            total_value_selected_partner_items = 0;

                                            // -----

                                            final String default_inventory_items_in_trade_count = resources.getString(R.string.fragment_trade_inventory_items_in_trade_count);
                                            final int color_pale_sky = resources.getColor(R.color.pale_sky);

                                            fragment.findViewById(R.id.fragment_trade_user_inventory_items_in_trade_total_value_container).setVisibility(View.GONE);
                                            ((TextView) fragment.findViewById(R.id.fragment_trade_user_inventory_items_in_trade_count)).setText(default_inventory_items_in_trade_count);

                                            fragment.findViewById(R.id.fragment_trade_partner_inventory_items_in_trade_total_value_container).setVisibility(View.GONE);
                                            ((TextView) fragment.findViewById(R.id.fragment_trade_partner_inventory_items_in_trade_count)).setText(default_inventory_items_in_trade_count);

                                            message_input.setText(default_message);
                                            message_input.setTextColor(color_pale_sky);

                                            twofactor_input.setText(twofactor_hint);
                                            twofactor_input.setTextColor(color_pale_sky);

                                            updateMakeOfferButton(fragment);

                                            main.showDialog(context, "Offer sent!", null);

                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    loadInventory(fragment, main.getUserID(), 1, true, false);
                                                    loadInventory(fragment, PartnerData.id, 1, true, true);
                                                }
                                            }, 1500);
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
                                                final String message = errorResponse.getString("message");

                                                if(message.equals(resources.getString(R.string.down_for_maintenance_error))) {
                                                    main.showDialog(context, "Down for maintenance", "Please try again later.");
                                                }
                                                else {
                                                    main.showDialog(context, "An error occurred", message);
                                                }
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
                        else {
                            perform_action = true;
                            main.set_perform_action(true);
                        }
                    }
                }
            });
        }
        return fragment;
    }

    // ** FUNCTIONS

    public void clearPartnerData() {
        PartnerData.valid_user = false;
    }

    private void updateMakeOfferButton(View fragment) {
        final Resources resources = getResources();
        final TextView make_offer_button = fragment.findViewById(R.id.fragment_trade_inner_make_offer_button);
        final int selected_user_item_count = selected_user_items.size();
        final int selected_partner_item_count = selected_partner_items.size();

        if(PartnerData.valid_user && ((((CheckBox) fragment.findViewById(R.id.fragment_trade_one_way_trade_or_gift_checkbox)).isChecked() && (selected_user_item_count >= 1 || selected_partner_item_count >= 1)) || (selected_user_item_count >= 1 && selected_partner_item_count >= 1)) && !((TextView) fragment.findViewById(R.id.fragment_trade_enter_twofactor_input)).getText().toString().equals(resources.getString(R.string.fragment_trade_enter_twofactor_hint))) {
            make_offer_button.setBackgroundColor(resources.getColor(R.color.denim));
            make_offer_button.setTextColor(resources.getColor(R.color.white));
        }
        else {
            make_offer_button.setBackgroundColor(resources.getColor(R.color.shuttle_gray));
            make_offer_button.setTextColor(resources.getColor(R.color.bombay));
        }
    }

    private void clearView_LinearLayout(View view) {
        if(((LinearLayout) view).getChildCount() > 0) {
            ((LinearLayout) view).removeAllViews();
        }
    }

    private void loadInventory(final View fragment, int user_id, int page, Boolean unrestricted_call, final Boolean reset_perform_action) {
        final main main = new main();

        if(perform_action || unrestricted_call) {
            perform_action = false;
            main.set_perform_action(false);

            // -----

            final Context context = fragment_trade.this.getContext();
            final Resources resources = getResources();
            final DisplayMetrics display_metrics = resources.getDisplayMetrics();
            final LinearLayout items_container_layout;
            final LinearLayout user_inventory_total_items_inner_container_view = fragment.findViewById(R.id.fragment_trade_user_inventory_total_items_inner_container);
            final ImageView partner_avatar_view = fragment.findViewById(R.id.fragment_trade_partner_avatar);
            final TextView partner_username_view = fragment.findViewById(R.id.fragment_trade_partner_username);
            final LinearLayout partner_inventory_total_items_inner_container_view = fragment.findViewById(R.id.fragment_trade_partner_inventory_total_items_inner_container);
            String search;
            Boolean user_inventory_temp = true;

            if(context != null) {
                if(user_id == main.getUserID()) {
                    user_inventory_total_items_inner_container_view.setVisibility(View.GONE);

                    items_container_layout = fragment.findViewById(R.id.fragment_trade_user_inventory_items_container);

                    // -----

                    search = ((TextView) fragment.findViewById(R.id.fragment_trade_user_search_inventory_input)).getText().toString();
                }
                else {
                    partner_inventory_total_items_inner_container_view.setVisibility(View.GONE);

                    items_container_layout = fragment.findViewById(R.id.fragment_trade_partner_inventory_items_container);

                    // -----

                    search = ((TextView) fragment.findViewById(R.id.fragment_trade_partner_search_inventory_input)).getText().toString();
                    user_inventory_temp = false;
                }

                clearView_LinearLayout(items_container_layout);

                // -----

                final Boolean user_inventory = user_inventory_temp;

                if(!user_inventory && !PartnerData.valid_user) {
                    selected_partner_items.clear();

                    // -----

                    Glide.with(context).load(resources.getDrawable(R.color.transparent)).into(partner_avatar_view);
                    partner_username_view.setText(null);
                }

                // -----

                final RequestParams params = new RequestParams();
                params.put("uid", user_id);
                params.put("app_id", selected_app_id);
                params.put("page", page);
                params.put("per_page", Constant.ITEMS_PER_PAGE);

                if(!search.equals(resources.getString(R.string.fragment_trade_search_inventory_hint))) {
                    params.put("search", search);
                }

                new opskins_trade_api(new WeakReference<>(context)).get("ITrade/GetUserInventory/v1", params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            final JSONObject data = response.getJSONObject("response");
                            final JSONArray items = data.getJSONArray("items");
                            JSONObject items_in_active_offers;

                            try {
                                items_in_active_offers = data.getJSONObject("items_in_active_offers");
                            }
                            catch (JSONException e) {
                                items_in_active_offers = new JSONObject();
                            }

                            final Boolean dont_show_items_in_active_trades_is_checked = ((CheckBox) fragment.findViewById(R.id.fragment_trade_dont_show_items_in_active_trades_checkbox)).isChecked();
                            final int unit_conversion_1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 140, display_metrics);
                            final int unit_conversion_2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, display_metrics);
                            final int unit_conversion_3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, display_metrics);
                            final int unit_conversion_4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, display_metrics);
                            final int item_count = items.length();
                            final int item_count_dont_show_items_in_active_trades = item_count - ((dont_show_items_in_active_trades_is_checked) ? (items_in_active_offers.length()) : (0));

                            if(user_inventory) {
                                user_inventory_total_items_inner_container_view.setVisibility(View.VISIBLE);
                                ((TextView) fragment.findViewById(R.id.fragment_trade_user_inventory_total_items_count)).setText(String.valueOf(item_count_dont_show_items_in_active_trades));
                            }
                            else {
                                if(!PartnerData.valid_user) {
                                    PartnerData.valid_user = true;

                                    fragment.findViewById(R.id.fragment_trade_find_partner_container).setVisibility(View.GONE);

                                    ((TextView) fragment.findViewById(R.id.fragment_trade_find_partner_input)).setText(null);

                                    // -----

                                    final JSONObject user = data.getJSONObject("user_data");

                                    PartnerData.username = user.getString("username");
                                    PartnerData.avatar = user.getString("avatar");

                                    fragment.findViewById(R.id.fragment_trade_partner_avatar_container).setVisibility(View.VISIBLE);
                                    fragment.findViewById(R.id.fragment_trade_partner_info_container).setVisibility(View.VISIBLE);
                                    fragment.findViewById(R.id.fragment_trade_partner_inventory_container).setVisibility(View.VISIBLE);

                                    Glide.with(context).load(PartnerData.avatar).apply(new RequestOptions().placeholder(R.color.transparent).fitCenter()).into(partner_avatar_view);
                                    partner_username_view.setText(PartnerData.username);
                                }

                                partner_inventory_total_items_inner_container_view.setVisibility(View.VISIBLE);
                                ((TextView) fragment.findViewById(R.id.fragment_trade_partner_inventory_total_items_count)).setText(String.valueOf(item_count_dont_show_items_in_active_trades));
                            }

                            if(item_count >= 1) {
                                JSONObject item;
                                int item_id;
                                RelativeLayout layout;
                                LinearLayout.LayoutParams layout_params;
                                LinearLayout inner_layout;
                                ImageView item_image_view;
                                TextView item_name_view;
                                TextView item_condition_view;
                                TextView item_price_view;
                                String item_name;
                                StringTokenizer item_name_parts;
                                String item_condition = "";
                                String item_image;
                                String item_color;
                                long item_suggested_price_temp;
                                Boolean second = false;
                                final Drawable item_container_drawable = resources.getDrawable(R.drawable.shape_fragment_trade_item_container);
                                final Drawable selected_item_container_drawable = resources.getDrawable(R.drawable.shape_fragment_trade_selected_item_container);
                                final int color_black = resources.getColor(R.color.black);
                                final int color_white = resources.getColor(R.color.white);

                                for(int i = 0; i < item_count; i ++) {
                                    item = items.getJSONObject(i);
                                    item_id = item.getInt("id");

                                    if(!user_inventory || !dont_show_items_in_active_trades_is_checked || !items_in_active_offers.has(String.valueOf(item_id))) {
                                        try {
                                            final JSONObject item_images = item.getJSONObject("image");

                                            item_image = item_images.getString(item_images.keys().next());
                                        }
                                        catch (JSONException e) {
                                            try {
                                                item_image = item.getString("image");
                                            }
                                            catch (JSONException e_fallback) {
                                                item_image = "";
                                            }
                                        }

                                        try {
                                            item_suggested_price_temp = item.getInt("suggested_price");
                                        }
                                        catch (JSONException e) {
                                            item_suggested_price_temp = 0;
                                        }

                                        item_name = item.getString("name");
                                        item_color = item.getString("color");

                                        if(item_name.contains(" (Battle-Scarred)")) {
                                            item_name = item_name.replace(" (Battle-Scarred)", "");
                                            item_condition = "Battle-Scarred";
                                        }
                                        else if(item_name.contains(" (Well-Worn)")) {
                                            item_name = item_name.replace(" (Well-Worn)", "");
                                            item_condition = "Well-Worn";
                                        }
                                        else if(item_name.contains(" (Field-Tested)")) {
                                            item_name = item_name.replace(" (Field-Tested)", "");
                                            item_condition = "Field-Tested";
                                        }
                                        else if(item_name.contains(" (Minimal Wear)")) {
                                            item_name = item_name.replace(" (Minimal Wear)", "");
                                            item_condition = "Minimal Wear";
                                        }
                                        else if(item_name.contains(" (Factory New)")) {
                                            item_name = item_name.replace(" (Factory New)", "");
                                            item_condition = "Factory New";
                                        }

                                        item_name_parts = new StringTokenizer(item_name, "|");

                                        while(item_name_parts.hasMoreElements()) {
                                            item_name = item_name_parts.nextToken();
                                        }

                                        item_name = item_name.trim();

                                        layout = new RelativeLayout(context);
                                        layout.setBackgroundDrawable(item_container_drawable);

                                        layout_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                        if(second) {
                                            layout_params.setMargins(unit_conversion_2, 0, 0, 0);
                                        }
                                        else {
                                            second = true;
                                        }

                                        // -----

                                        item_image_view = new ImageView(context);
                                        item_image_view.setPadding(unit_conversion_3, unit_conversion_3, unit_conversion_3, unit_conversion_3);
                                        layout.addView(item_image_view, unit_conversion_1, unit_conversion_1);

                                        Glide.with(context).load(item_image).apply(new RequestOptions().fitCenter()).into(item_image_view);

                                        // -

                                        inner_layout = new LinearLayout(context);
                                        inner_layout.setOrientation(LinearLayout.VERTICAL);

                                        item_name_view = new TextView(context);
                                        item_name_view.setPadding(unit_conversion_2, unit_conversion_2, unit_conversion_2, 0);
                                        item_name_view.setText(main.fromHTML("<font color = \"" + item_color + "\">" + item_name.toUpperCase() + "</font>"));
                                        item_name_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                                        item_name_view.setTypeface(null, Typeface.BOLD);
                                        item_name_view.setTextColor(color_white);
                                        item_name_view.setShadowLayer((float) 3, (float) 1.5, (float) 1.5, color_black);
                                        inner_layout.addView(item_name_view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                        if(!item_condition.isEmpty()) {
                                            item_condition_view = new TextView(context);
                                            item_condition_view.setPadding(unit_conversion_2, 0, unit_conversion_2, 0);
                                            item_condition_view.setText(main.fromHTML("<font color = \"" + item_color + "\">" + item_condition.toUpperCase() + "</font>"));
                                            item_condition_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 7);
                                            item_condition_view.setTypeface(null, Typeface.BOLD);
                                            item_condition_view.setTextColor(color_white);
                                            item_condition_view.setShadowLayer((float) 3, (float) 1.5, (float) 1.5, color_black);
                                            inner_layout.addView(item_condition_view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                        }

                                        layout.addView(inner_layout, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                                        // -

                                        if(item_suggested_price_temp != 0) {
                                            inner_layout = new LinearLayout(context);
                                            inner_layout.setOrientation(LinearLayout.HORIZONTAL);

                                            item_price_view = new TextView(context);
                                            item_price_view.setPadding(unit_conversion_2, unit_conversion_2, unit_conversion_2, unit_conversion_4);
                                            item_price_view.setText(new StringBuilder("$" + main.currencyFormat(String.valueOf((double) item_suggested_price_temp / 100))));
                                            item_price_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                                            item_price_view.setTypeface(null, Typeface.BOLD);
                                            item_price_view.setTextColor(color_white);
                                            item_price_view.setShadowLayer((float) 3, (float) 1.5, (float) 1.5, color_black);
                                            item_price_view.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
                                            inner_layout.addView(item_price_view, unit_conversion_1, unit_conversion_1);

                                            layout.addView(inner_layout, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                        }

                                        items_container_layout.addView(layout, layout_params);

                                        // -----

                                        final RelativeLayout item_layout = layout;
                                        final int item_id_final = item_id;
                                        final long item_suggested_price = item_suggested_price_temp;

                                        if(user_inventory) {
                                            if(selected_user_items.indexOf(item_id) != -1) {
                                                item_layout.setBackgroundDrawable(selected_item_container_drawable);
                                            }
                                        }
                                        else {
                                            if(selected_partner_items.indexOf(item_id) != -1) {
                                                item_layout.setBackgroundDrawable(selected_item_container_drawable);
                                            }
                                        }

                                        layout.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if(user_inventory) {
                                                    int selected_item_count = selected_user_items.size();
                                                    final int key = selected_user_items.indexOf(item_id_final);

                                                    if(key == -1) {
                                                        if(selected_item_count < Constant.MAX_SELECTABLE_ITEMS) {
                                                            selected_user_items.add(item_id_final);
                                                            total_value_selected_user_items += item_suggested_price;

                                                            item_layout.setBackgroundDrawable(selected_item_container_drawable);
                                                        }
                                                    }
                                                    else {
                                                        selected_user_items.remove(key);
                                                        total_value_selected_user_items -= item_suggested_price;

                                                        if(total_value_selected_user_items <= 0) {
                                                            total_value_selected_user_items = 0;
                                                        }

                                                        item_layout.setBackgroundDrawable(item_container_drawable);
                                                    }

                                                    // -----

                                                    final LinearLayout user_inventory_items_in_trade_total_value_container = fragment.findViewById(R.id.fragment_trade_user_inventory_items_in_trade_total_value_container);
                                                    final TextView user_inventory_items_in_trade_total_value = fragment.findViewById(R.id.fragment_trade_user_inventory_items_in_trade_total_value);

                                                    if(total_value_selected_user_items == 0) {
                                                        user_inventory_items_in_trade_total_value_container.setVisibility(View.GONE);
                                                    }
                                                    else {
                                                        user_inventory_items_in_trade_total_value_container.setVisibility(View.VISIBLE);
                                                        user_inventory_items_in_trade_total_value.setText(new StringBuilder("$" + main.currencyFormat(String.valueOf((double) total_value_selected_user_items / 100))));
                                                    }

                                                    ((TextView) fragment.findViewById(R.id.fragment_trade_user_inventory_items_in_trade_count)).setText(String.valueOf(selected_user_items.size()));
                                                }
                                                else {
                                                    final int selected_item_count = selected_partner_items.size();
                                                    final int key = selected_partner_items.indexOf(item_id_final);

                                                    if(key == -1) {
                                                        if(selected_item_count < Constant.MAX_SELECTABLE_ITEMS) {
                                                            selected_partner_items.add(item_id_final);
                                                            total_value_selected_partner_items += item_suggested_price;

                                                            item_layout.setBackgroundDrawable(selected_item_container_drawable);
                                                        }
                                                    }
                                                    else {
                                                        selected_partner_items.remove(key);
                                                        total_value_selected_partner_items -= item_suggested_price;

                                                        if(total_value_selected_partner_items <= 0) {
                                                            total_value_selected_partner_items = 0;
                                                        }

                                                        item_layout.setBackgroundDrawable(item_container_drawable);
                                                    }

                                                    // -----

                                                    final LinearLayout partner_inventory_items_in_trade_total_value_container = fragment.findViewById(R.id.fragment_trade_partner_inventory_items_in_trade_total_value_container);
                                                    final TextView partner_inventory_items_in_trade_total_value = fragment.findViewById(R.id.fragment_trade_partner_inventory_items_in_trade_total_value);

                                                    if(total_value_selected_partner_items == 0) {
                                                        partner_inventory_items_in_trade_total_value_container.setVisibility(View.GONE);
                                                    }
                                                    else {
                                                        partner_inventory_items_in_trade_total_value_container.setVisibility(View.VISIBLE);
                                                        partner_inventory_items_in_trade_total_value.setText(new StringBuilder("$" + main.currencyFormat(String.valueOf((double) total_value_selected_partner_items / 100))));
                                                    }

                                                    ((TextView) fragment.findViewById(R.id.fragment_trade_partner_inventory_items_in_trade_count)).setText(String.valueOf(selected_partner_items.size()));
                                                }

                                                updateMakeOfferButton(fragment);
                                            }
                                        });

                                        // -----

                                        final JSONObject item_info = item;

                                        layout.setOnLongClickListener(new View.OnLongClickListener() {
                                            @Override
                                            public boolean onLongClick(View view) {
                                                main.showItemInfoDialog(context, item_info);
                                                return false;
                                            }
                                        });
                                    }
                                }
                            }
                        }
                        catch (JSONException e) {
                            main.showDialog(context, "An error occurred", "Expected return data not found.");
                        }

                        // -----

                        if(reset_perform_action) {
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
                                final String message = errorResponse.getString("message");

                                if(!user_inventory && !PartnerData.valid_user && message.equals("User does not exist")) {
                                    final TextView find_partner_error_view = fragment.findViewById(R.id.fragment_trade_find_partner_error);

                                    find_partner_error_view.setVisibility(View.VISIBLE);
                                    find_partner_error_view.setText(resources.getString(R.string.fragment_trade_find_partner_error_unable_to_load_friend_inventory));
                                }
                                else {
                                    if(message.equals(resources.getString(R.string.down_for_maintenance_error))) {
                                        main.showDialog(context, "Down for maintenance", "Please try again later.");
                                    }
                                    else {
                                        main.showDialog(context, "An error occurred", message);
                                    }
                                }
                            }
                            catch (JSONException e) {
                                main.showDialog(context, "An error occurred", "Expected return data not found.");
                            }
                        }

                        // -----

                        if(reset_perform_action) {
                            perform_action = true;
                            main.set_perform_action(true);
                        }
                    }
                });
            }
        }
    }
}