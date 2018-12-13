package com.opskins.trade.waxexpresstrade;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.stevenyang.snowfalling.SnowFlakesLayout;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import cz.msebera.android.httpclient.Header;

public class main extends AppCompatActivity {
    // ** DEFINITIONS

    private static class Constant {
        // ** SHARED PREFERENCES

        // *** USER DATA

        private static final String SHARED_PREFERENCE_USER_DATA_LOGGED_IN = "user_data_logged_in";
        private static final String SHARED_PREFERENCE_USER_DATA_BEARER_TOKEN = "user_data_bearer_token";
        private static final String SHARED_PREFERENCE_USER_DATA_BEARER_TOKEN_EXPIRES_IN = "user_data_bearer_token_expires_in";
        private static final String SHARED_PREFERENCE_USER_DATA_BEARER_TOKEN_TIMESTAMP_ISSUED = "user_data_bearer_token_timestamp_issued";
        private static final String SHARED_PREFERENCE_USER_DATA_REFRESH_TOKEN = "user_data_refresh_token";
        private static final String SHARED_PREFERENCE_USER_DATA_ID = "user_data_id";
        private static final String SHARED_PREFERENCE_USER_DATA_USERNAME = "user_data_username";
        private static final String SHARED_PREFERENCE_USER_DATA_AVATAR = "user_data_avatar";
    }

    // ** ARRAYS

    private static class UserData {
        private static Boolean logged_in = false;
        private static String bearer_token = null;
        private static int bearer_token_expires_in = -1;
        private static int bearer_token_timestamp_issued = -1;
        private static String refresh_token = null;
        private static int id = -1;
        private static String username = null;
        private static String avatar = null;
    }

    // ** VARIABLES

    // *** GENERAL

    private static SharedPreferences shared_preferences;
    private DrawerLayout navigation_drawer;
    private static Intent intent_background_service = null;
    private static String fragment_trade_find_partner = null;
    private static int fragment_offers_show_offer_id = -1;

    // *** STATES

    private static Boolean authenticating = false;
    private Boolean snowing = false;
    private static Boolean perform_action = true;

    // ** CALLBACKS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context context = this;
        final ActionBar action_bar = getSupportActionBar();

        setContentView(R.layout.activity_main);

        if(action_bar != null) {
            action_bar.setDisplayHomeAsUpEnabled(true);
            action_bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            action_bar.setCustomView(R.layout.action_bar);
        }

        shared_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        navigation_drawer = findViewById(R.id.navigation_drawer_layout);

        loadUserData();
        updateNavigationDrawer(context, false);
        updateNavigationDrawerUserInfo(context, false);
        startSnowing(context, false);

        if(getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            final Intent intent = getIntent();
            final Uri intent_data = intent.getData();
            final fragment_log_in fragment_log_in = new fragment_log_in();
            final fragment_trade fragment_trade = new fragment_trade();

            if(intent.getAction() != null) {
                final String intent_url = intent.getDataString();

                if(intent_url.startsWith("https://trade.opskins.com/t/")) {
                    fragment_trade.clearPartnerData();

                    fragment_trade_find_partner = intent_url;
                }
                else if(intent_url.startsWith("https://trade.opskins.com/trade-offers/")) {
                    final String[] url_parts = intent_url.split("/");

                    try {
                        fragment_offers_show_offer_id = Integer.parseInt(url_parts[url_parts.length - 1]);
                    }
                    catch (NumberFormatException nfe) {
                        fragment_offers_show_offer_id = -1;
                    }
                }
                else if((intent_data.getQueryParameter("state") != null && intent_data.getQueryParameter("code") != null) || intent_data.getQueryParameter("error") != null) {
                    authenticating = true;
                }
            }

            // -----

            showFragment(new fragment_loading(), View.GONE, false);

            if(authenticating) {
                if(intent_data != null) {
                    if("access_denied".equals(intent_data.getQueryParameter("error"))) {
                        showDialog(context, "Unable to log in", "You have denied access.");

                        showFragment(fragment_log_in, View.GONE, false);

                        // -----

                        authenticating = false;
                    }
                    else {
                        final String authorization_code = intent_data.getQueryParameter("code");

                        if(authorization_code != null) {
                            new opskins_oauth(new WeakReference<>(context)).requestAuthorization(authorization_code);
                        }
                        else {
                            showDialog(context, "Log in failed", "Unable to get authorization code.");

                            showFragment(fragment_log_in, View.GONE, false);

                            // -----

                            authenticating = false;
                        }
                    }
                }
                else {
                    showDialog(context, "Log in failed", "No return data found.");

                    showFragment(fragment_log_in, View.GONE, false);

                    // -----

                    authenticating = false;
                }
            }
            else {
                if(UserData.logged_in) {
                    if(fragment_offers_show_offer_id != -1) {
                        showFragment(new fragment_offers(), View.VISIBLE, false);
                    }
                    else {
                        showFragment(fragment_trade, View.VISIBLE, true);
                    }
                }
                else {
                    showFragment(fragment_log_in, View.GONE, false);
                }

                // -----

                authenticating = false;
            }
        }
        else {
            if(UserData.logged_in) {
                findViewById(R.id.background_fade).setVisibility(View.VISIBLE);
            }
            else {
                findViewById(R.id.background_fade).setVisibility(View.GONE);
            }
        }

        // -----

        if(intent_background_service != null) {
            stopService(intent_background_service);
        }

        intent_background_service = new Intent(context, background_service.class);

        startService(intent_background_service);
    }

    protected void onLogIn(Context context, String bearer_token, int bearer_token_expires_in, int bearer_token_timestamp_issued, String refresh_token, JSONObject user_data) {
        try {
            final JSONObject data = user_data.getJSONObject("response");

            SharedPreferences.Editor shared_preferences_editor = shared_preferences.edit();
            shared_preferences_editor.putBoolean(Constant.SHARED_PREFERENCE_USER_DATA_LOGGED_IN, true);
            shared_preferences_editor.putString(Constant.SHARED_PREFERENCE_USER_DATA_BEARER_TOKEN, bearer_token);
            shared_preferences_editor.putInt(Constant.SHARED_PREFERENCE_USER_DATA_BEARER_TOKEN_EXPIRES_IN, bearer_token_expires_in);
            shared_preferences_editor.putInt(Constant.SHARED_PREFERENCE_USER_DATA_BEARER_TOKEN_TIMESTAMP_ISSUED, bearer_token_timestamp_issued);
            shared_preferences_editor.putString(Constant.SHARED_PREFERENCE_USER_DATA_REFRESH_TOKEN, refresh_token);
            shared_preferences_editor.putInt(Constant.SHARED_PREFERENCE_USER_DATA_ID, data.getInt("id"));
            shared_preferences_editor.putString(Constant.SHARED_PREFERENCE_USER_DATA_USERNAME, data.getString("username"));
            shared_preferences_editor.putString(Constant.SHARED_PREFERENCE_USER_DATA_AVATAR, data.getString("avatar"));
            shared_preferences_editor.apply();

            loadUserData();
            updateNavigationDrawer(context, true);
            updateNavigationDrawerUserInfo(context, true);

            showFragmentEx(context, new fragment_trade(), View.VISIBLE, true);
        }
        catch (JSONException e) {
            showDialog(context, "Log in failed", "Expected return data not found.");

            showFragmentEx(context, new fragment_log_in(), View.GONE, false);
        }

        // -----

        authenticating = false;
    }

    protected void onLogInFailed(Context context, String error) {
        showDialog(context, "Log in failed", error);

        showFragmentEx(context, new fragment_log_in(), View.GONE, false);

        // -----

        authenticating = false;
    }

    protected void onStringInputUpdated(Context context, TextView input_view, EditText edit_text, String default_input) {
        final Resources resources = context.getResources();
        final String new_input = edit_text.getText().toString();

        if(new_input.isEmpty() || new_input.equals(default_input)) {
            input_view.setText(default_input);
            input_view.setTextColor(resources.getColor(R.color.pale_sky));
        }
        else {
            input_view.setText(new_input);
            input_view.setTextColor(resources.getColor(R.color.white));
        }
    }

    protected void onNumberInputUpdated(Context context, TextView input_view, EditText edit_text, String default_input) {
        final Resources resources = context.getResources();
        final String new_input = edit_text.getText().toString();
        final int color_pale_sky = resources.getColor(R.color.pale_sky);

        if(new_input.isEmpty() || new_input.equals(default_input)) {
            input_view.setText(default_input);
            input_view.setTextColor(color_pale_sky);
        }
        else {
            try {
                Integer.parseInt(new_input);

                input_view.setText(new_input);
                input_view.setTextColor(resources.getColor(R.color.white));
            }
            catch(NumberFormatException nfe) {
                input_view.setText(default_input);
                input_view.setTextColor(color_pale_sky);
            }
        }
    }

    // ** FUNCTIONS

    protected String getUserAvatar() {
        return UserData.avatar;
    }

    protected String getUserUsername() {
        return UserData.username;
    }

    protected int getUserID() {
        return UserData.id;
    }

    protected Boolean isUserLoggedIn() {
        return UserData.logged_in;
    }

    protected void setUserRefreshToken(String refresh_token) {
        UserData.refresh_token = refresh_token;
    }

    protected String getUserRefreshToken() {
        return UserData.refresh_token;
    }

    protected void setUserBearerTokenTimestampIssued(int bearer_token_timestamp_issued) {
        UserData.bearer_token_timestamp_issued = bearer_token_timestamp_issued;
    }

    protected int getUserBearerTokenTimestampIssued() {
        return UserData.bearer_token_timestamp_issued;
    }

    protected void setUserBearerTokenExpiresIn(int bearer_token_expires_in) {
        UserData.bearer_token_expires_in = bearer_token_expires_in;
    }

    protected int getUserBearerTokenExpiresIn() {
        return UserData.bearer_token_expires_in;
    }

    protected void setUserBearerToken(String bearer_token) {
        UserData.bearer_token = bearer_token;
    }

    protected String getUserBearerToken() {
        return UserData.bearer_token;
    }

    private void stopSnowing(Context context, Boolean cast) {
        if(snowing) {
            if(cast) {
                ((SnowFlakesLayout) ((AppCompatActivity) context).findViewById(R.id.snow_flakes_layout)).stopSnowing();
            }
            else {
                ((SnowFlakesLayout) findViewById(R.id.snow_flakes_layout)).stopSnowing();
            }

            // -----

            snowing = false;
        }
    }

    private void startSnowing(Context context, Boolean cast) {
        if(!snowing) {
            SnowFlakesLayout snow_flakes_layout;

            if(cast) {
                snow_flakes_layout = ((AppCompatActivity) context).findViewById(R.id.snow_flakes_layout);
            }
            else {
                snow_flakes_layout = findViewById(R.id.snow_flakes_layout);
            }

            snow_flakes_layout.init();
            snow_flakes_layout.setWholeAnimateTiming(3000000);
            snow_flakes_layout.setAnimateDuration(7000);
            snow_flakes_layout.setGenerateSnowTiming(300);
            snow_flakes_layout.setRandomSnowSizeRange(40, 1);
            snow_flakes_layout.setEnableRandomCurving(true);
            snow_flakes_layout.setEnableAlphaFade(true);
            snow_flakes_layout.startSnowing();

            // -----

            snowing = true;
        }
    }

    public void toggleNavigationDrawer(View view) {
        if(navigation_drawer.isDrawerOpen(GravityCompat.START)) {
            navigation_drawer.closeDrawer(GravityCompat.START);
        }
        else {
            navigation_drawer.openDrawer(GravityCompat.START);
        }
    }

    private void updateNavigationDrawerUserInfo(Context context, Boolean cast) {
        View log_in_view;
        View avatar_view;
        View username_view;

        if(cast) {
            log_in_view = ((AppCompatActivity) context).findViewById(R.id.navigation_drawer_header_user_log_in);
            avatar_view = ((AppCompatActivity) context).findViewById(R.id.navigation_drawer_header_user_avatar);
            username_view = ((AppCompatActivity) context).findViewById(R.id.navigation_drawer_header_user_username);
        }
        else {
            log_in_view = findViewById(R.id.navigation_drawer_header_user_log_in);
            avatar_view = findViewById(R.id.navigation_drawer_header_user_avatar);
            username_view = findViewById(R.id.navigation_drawer_header_user_username);
        }

        if(UserData.logged_in) {
            log_in_view.setVisibility(View.GONE);

            Glide.with(((cast) ? (((AppCompatActivity) context)) : (getApplicationContext()))).load(UserData.avatar).apply(new RequestOptions().placeholder(R.color.transparent).fitCenter()).into((de.hdodenhof.circleimageview.CircleImageView) avatar_view);
            avatar_view.setVisibility(View.VISIBLE);

            ((TextView) username_view).setText(UserData.username);
            username_view.setVisibility(View.VISIBLE);
        }
        else {
            log_in_view.setVisibility(View.VISIBLE);
            avatar_view.setVisibility(View.GONE);
            username_view.setVisibility(View.GONE);
        }
    }

    private void updateNavigationDrawer(final Context context, final Boolean cast) {
        if(UserData.logged_in) {
            DrawerLayout navigation_drawer_layout_temp;
            ListView navigation_drawer_list_view;

            if(cast) {
                navigation_drawer_layout_temp = ((AppCompatActivity) context).findViewById(R.id.navigation_drawer_layout);
                navigation_drawer_list_view = ((AppCompatActivity) context).findViewById(R.id.navigation_drawer_list);
            }
            else {
                navigation_drawer_layout_temp = findViewById(R.id.navigation_drawer_layout);
                navigation_drawer_list_view = findViewById(R.id.navigation_drawer_list);
            }

            // -----

            final DrawerLayout navigation_drawer_layout = navigation_drawer_layout_temp;

            navigation_drawer_list_view.setAdapter(new ArrayAdapter<>(((cast) ? (((AppCompatActivity) context)) : (context)), R.layout.navigation_drawer_list_item, R.id.navigation_drawer_list_item, new String[] {
                    "Trade",
                    "Offers",
                    "Inventory",
                    "Settings",
                    "Log Out"
            }));

            navigation_drawer_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if(perform_action) {
                        switch (adapterView.getItemAtPosition(i).toString()) {
                            case "Trade":
                                showFragmentEx(((cast) ? (((AppCompatActivity) context)) : (context)), new fragment_trade(), View.VISIBLE, true);
                                break;
                            case "Offers":
                                showFragmentEx(((cast) ? (((AppCompatActivity) context)) : (context)), new fragment_offers(), View.VISIBLE, false);
                                break;
                            case "Inventory":
                                openTabEx_Data(((cast) ? (((AppCompatActivity) context)) : (context)), "https://trade.opskins.com/inventory");
                                break;
                            case "Settings":
                                openTabEx_Data(((cast) ? (((AppCompatActivity) context)) : (context)), "https://trade.opskins.com/settings");
                                break;
                            case "Log Out":
                                logOut(((cast) ? (((AppCompatActivity) context)) : (context)), cast);
                                break;
                        }

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                navigation_drawer_layout.closeDrawer(GravityCompat.START);
                            }
                        }, 25);
                    }
                }
            });
        }
        else {
            ((ListView) ((cast) ? (((AppCompatActivity) context)) : (this)).findViewById(R.id.navigation_drawer_list)).setAdapter(new ArrayAdapter<>(((cast) ? (((AppCompatActivity) context)) : (context)), R.layout.navigation_drawer_list_item, R.id.navigation_drawer_list_item, new String[] {}));
        }
    }

    public void logOut(final Context context, Boolean cast) {
        if(UserData.logged_in) {
            final String refresh_token = getUserRefreshToken();

            // -----

            shared_preferences.edit().clear().apply();

            UserData.logged_in = false;

            new fragment_trade().clearPartnerData();

            // -----

            updateNavigationDrawer(context, cast);
            updateNavigationDrawerUserInfo(context, cast);

            showFragmentEx(context, new fragment_log_in(), View.GONE, false);

            // -----

            final RequestParams params = new RequestParams();
            params.put("token_type", "refresh");
            params.put("token", refresh_token);

            new opskins_oauth(new WeakReference<>(context)).post_SetBasicAuth("v1/revoke_token", params, new JsonHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    if(statusCode == 0) {
                        if(!isNetworkAvailable(context)) {
                            showDialog(context, "Revoke refresh token", "No internet connection. Check your mobile data or Wi-Fi.");
                        }
                        else {
                            showDialog(context, "Revoke refresh token", "Request failed.");
                        }
                    }
                    else {
                        try {
                            showDialog(context, "Revoke refresh token", errorResponse.getString("error_description"));
                        }
                        catch (JSONException e) {
                            showDialog(context, "Revoke refresh token", "Expected return data not found.");
                        }
                    }
                }
            });
        }
    }

    private void loadUserData() {
        if(!UserData.logged_in && shared_preferences.getBoolean(Constant.SHARED_PREFERENCE_USER_DATA_LOGGED_IN, false)) {
            UserData.logged_in = true;
            UserData.bearer_token = shared_preferences.getString(Constant.SHARED_PREFERENCE_USER_DATA_BEARER_TOKEN, null);
            UserData.bearer_token_expires_in = shared_preferences.getInt(Constant.SHARED_PREFERENCE_USER_DATA_BEARER_TOKEN_EXPIRES_IN, -1);
            UserData.bearer_token_timestamp_issued = shared_preferences.getInt(Constant.SHARED_PREFERENCE_USER_DATA_BEARER_TOKEN_TIMESTAMP_ISSUED, -1);
            UserData.refresh_token = shared_preferences.getString(Constant.SHARED_PREFERENCE_USER_DATA_REFRESH_TOKEN, null);
            UserData.id = shared_preferences.getInt(Constant.SHARED_PREFERENCE_USER_DATA_ID, -1);
            UserData.username = shared_preferences.getString(Constant.SHARED_PREFERENCE_USER_DATA_USERNAME, null);
            UserData.avatar = shared_preferences.getString(Constant.SHARED_PREFERENCE_USER_DATA_AVATAR, null);
        }
    }

    public void logIn_CloseNavigationDrawer(View view) {
        navigation_drawer.closeDrawer(GravityCompat.START);

        logIn(view);
    }

    public void logIn(View view) {
        if(!UserData.logged_in && !authenticating) {
            final Context context = this;

            showFragment(new fragment_loading(), View.GONE, false);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(new opskins_oauth(new WeakReference<>(context)).authenticate() == 0) {
                        showDialog(context, "Log in failed", "There's an error in the authentication URL.");

                        // -----

                        authenticating = false;
                    }

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showFragment(new fragment_log_in(), View.GONE, false);
                        }
                    }, 1500);
                }
            }, 100);
        }
    }

    public void openTabEx_Data(Context context, String url) {
        new custom_tabs().openTab(context, url);
    }

    public void openTab(View view) {
        if(perform_action) {
            new custom_tabs().openTab(this, view.getTag().toString());
        }
    }

    public void showItemInfoDialog(Context context, JSONObject item) { // unfinished - coming soon
        final Resources resources = context.getResources();
        final DisplayMetrics display_metrics = resources.getDisplayMetrics();
        final AlertDialog alert_dialog = new AlertDialog.Builder(context, R.style.DialogTheme).create();
        final ScrollView scroll_view_layout = new ScrollView(context);
        final RelativeLayout container_layout = new RelativeLayout(context);
        final int unit_conversion_1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, display_metrics);

        container_layout.setPadding(unit_conversion_1, unit_conversion_1, unit_conversion_1, unit_conversion_1);

        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final int color_white = resources.getColor(R.color.white);

        final TextView title_view = new TextView(context);
        title_view.setText(new StringBuilder("Inspect item is coming soon!"));
        title_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
        title_view.setTypeface(null, Typeface.BOLD);
        title_view.setTextColor(color_white);
        layout.addView(title_view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        container_layout.addView(layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        scroll_view_layout.addView(container_layout, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        alert_dialog.setView(scroll_view_layout);
        alert_dialog.show();
    }

    public Object[] showInputDialog(final Context context, String title, String description, String default_input) {
        if(perform_action) {
            final Resources resources = context.getResources();
            final DisplayMetrics display_metrics = resources.getDisplayMetrics();
            final AlertDialog alert_dialog = new AlertDialog.Builder(context, R.style.DialogTheme).create();
            final ScrollView scroll_view_layout = new ScrollView(context);
            final RelativeLayout container_layout = new RelativeLayout(context);
            final int unit_conversion_1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, display_metrics);
            final int unit_conversion_2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, display_metrics);
            final int unit_conversion_3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, display_metrics);
            final int unit_conversion_4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, display_metrics);
            final int unit_conversion_5 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 13, display_metrics);

            container_layout.setPadding(unit_conversion_1, unit_conversion_1, unit_conversion_1, unit_conversion_1);

            final LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);

            final int color_white = resources.getColor(R.color.white);

            final TextView title_view = new TextView(context);
            title_view.setText(title);
            title_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
            title_view.setTypeface(null, Typeface.BOLD);
            title_view.setTextColor(color_white);
            layout.addView(title_view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            final TextView message_view = new TextView(context);
            message_view.setPadding(0, unit_conversion_2, 0, unit_conversion_3);
            message_view.setText(description);
            message_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            message_view.setTextColor(color_white);
            layout.addView(message_view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            final EditText input_view = new EditText(context);
            input_view.setPadding(unit_conversion_4, unit_conversion_5, unit_conversion_4, unit_conversion_5);
            input_view.setBackgroundResource(R.drawable.shape_input);
            input_view.setText(default_input);
            input_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            input_view.setTextColor(color_white);
            input_view.setSingleLine(true);
            input_view.setMaxLines(1);
            input_view.setHighlightColor(resources.getColor(R.color.science_blue));
            input_view.setSelection(default_input.length());
            layout.addView(input_view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            container_layout.addView(layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            scroll_view_layout.addView(container_layout, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            alert_dialog.setView(scroll_view_layout);
            alert_dialog.show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    input_view.requestFocus();

                    ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(input_view, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 25);

            // -----

            Object[] array = new Object[2];
            array[0] = alert_dialog;
            array[1] = input_view;
            return array;
        }
        return null;
    }

    public void showDialog(Context context, String title, String message) {
        final Resources resources = context.getResources();
        final DisplayMetrics display_metrics = resources.getDisplayMetrics();
        final AlertDialog alert_dialog = new AlertDialog.Builder(context, R.style.DialogTheme).create();
        final ScrollView scroll_view_layout = new ScrollView(context);
        final RelativeLayout container_layout = new RelativeLayout(context);
        final int unit_conversion_1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, display_metrics);
        final int unit_conversion_2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, display_metrics);

        container_layout.setPadding(unit_conversion_1, unit_conversion_1, unit_conversion_1, unit_conversion_1);

        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final int color_white = resources.getColor(R.color.white);

        final TextView title_view = new TextView(context);
        title_view.setText(title);
        title_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
        title_view.setTypeface(null, Typeface.BOLD);
        title_view.setTextColor(color_white);
        layout.addView(title_view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        if(message != null) {
            final TextView message_view = new TextView(context);
            message_view.setPadding(0, unit_conversion_2, 0, 0);
            message_view.setText(message);
            message_view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            message_view.setTextColor(color_white);
            layout.addView(message_view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        container_layout.addView(layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        scroll_view_layout.addView(container_layout, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        alert_dialog.setView(scroll_view_layout);
        alert_dialog.show();
    }

    private void showFragmentEx(Context context, Fragment fragment, int background_fade_visibility, Boolean snow) {
        ((AppCompatActivity) context).findViewById(R.id.background_fade).setVisibility(background_fade_visibility);

        if(snow) {
            stopSnowing(context, true);
            startSnowing(context, true);
        }
        else {
            stopSnowing(context, true);
        }

        try {
            ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }
        catch (IllegalStateException e) {
            ((AppCompatActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
        }
    }

    private void showFragment(Fragment fragment, int background_fade_visibility, Boolean snow) {
        findViewById(R.id.background_fade).setVisibility(background_fade_visibility);

        if(snow) {
            stopSnowing(null, false);
            startSnowing(null, false);
        }
        else {
            stopSnowing(null, false);
        }

        try {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }
        catch (IllegalStateException e) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
        }
    }

    // -----

    public int get_fragment_offers_show_offer_id() {
        return fragment_offers_show_offer_id;
    }

    public void set_fragment_offers_show_offer_id(int value) {
        fragment_offers_show_offer_id = value;
    }

    public String get_fragment_trade_find_partner() {
        return fragment_trade_find_partner;
    }

    public void set_fragment_trade_find_partner(String value) {
        fragment_trade_find_partner = value;
    }

    public void set_perform_action(Boolean state) {
        perform_action = state;
    }

    // -----

    public Spanned fromHTML(String string) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(string, Html.FROM_HTML_MODE_LEGACY);
        }
        return Html.fromHtml(string);
    }

    public String currencyFormat(String amount) {
        DecimalFormat formatter = new DecimalFormat("###,###,##0.00");
        return formatter.format(Double.parseDouble(amount));
    }

    public String dateFormat(int time) {
        SimpleDateFormat simple_date_format = new SimpleDateFormat("M/dd/yyyy - hh:mm aaa", Locale.getDefault());
        simple_date_format.setTimeZone(TimeZone.getDefault());
        return simple_date_format.format(new Date(time * 1000L));
    }

    public Boolean isNetworkAvailable(Context context) {
        NetworkInfo network_info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return network_info != null && network_info.isConnected();
    }
}