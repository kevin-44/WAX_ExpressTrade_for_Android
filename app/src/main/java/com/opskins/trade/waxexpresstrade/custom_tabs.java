package com.opskins.trade.waxexpresstrade;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

class custom_tabs {
    // ** DEFINITIONS

    private static class Constant {
        // ** GENERAL

        private static final String ACTION_CUSTOM_TABS_CONNECTION = "android.support.customtabs.action.CustomTabsService";

        // ** PACKAGES

        private static final String STABLE_PACKAGE = "com.android.chrome";
        private static final String BETA_PACKAGE = "com.chrome.beta";
        private static final String DEV_PACKAGE = "com.chrome.dev";
        private static final String LOCAL_PACKAGE = "com.google.android.apps.chrome";
    }

    // ** FUNCTIONS

    void openTab(Context context, String url) {
        final CustomTabsIntent.Builder custom_tabs_intent_builder = new CustomTabsIntent.Builder();
        final CustomTabsIntent custom_tabs_intent = custom_tabs_intent_builder.build();
        final String package_name = getPackageNameToUse(context);

        if(package_name != null) {
            custom_tabs_intent_builder.setToolbarColor(context.getResources().getColor(R.color.main_primary));

            custom_tabs_intent.intent.setPackage(package_name);
            custom_tabs_intent.launchUrl(context, Uri.parse(url));
        }
        else {
            final Intent activity_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://opskins.com"));
            final ResolveInfo default_view_handler_info = context.getPackageManager().resolveActivity(activity_intent, 0);

            if(default_view_handler_info != null) {
                custom_tabs_intent.intent.setPackage(default_view_handler_info.activityInfo.packageName);
                custom_tabs_intent.launchUrl(context, Uri.parse(url));
            }
            else {
                new main().showDialog(context, "No web browser found", "Please install a web browser. Install Chrome for the best possible experience.");
            }
        }
    }

    private static String getPackageNameToUse(Context context) {
        final PackageManager package_manager = context.getPackageManager();
        final Intent activity_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.opskins.com"));
        final ResolveInfo default_view_handler_info = package_manager.resolveActivity(activity_intent, 0);
        final List<ResolveInfo> resolved_activity_list = package_manager.queryIntentActivities(activity_intent, 0);
        final List<String> packages_supporting_custom_tabs = new ArrayList<>();
        String default_view_handler_package_name = null;
        String package_name_to_use = null;

        if(default_view_handler_info != null) {
            default_view_handler_package_name = default_view_handler_info.activityInfo.packageName;
        }

        for(ResolveInfo info : resolved_activity_list) {
            final Intent service_intent = new Intent();
            service_intent.setAction(Constant.ACTION_CUSTOM_TABS_CONNECTION);
            service_intent.setPackage(info.activityInfo.packageName);

            if(package_manager.resolveService(service_intent, 0) != null) {
                packages_supporting_custom_tabs.add(info.activityInfo.packageName);
            }
        }

        if(packages_supporting_custom_tabs.size() == 1) {
            package_name_to_use = packages_supporting_custom_tabs.get(0);
        }
        else if(!TextUtils.isEmpty(default_view_handler_package_name) && !hasSpecializedHandlerIntents(context, activity_intent) && packages_supporting_custom_tabs.contains(default_view_handler_package_name)) {
            package_name_to_use = default_view_handler_package_name;
        }
        else if(packages_supporting_custom_tabs.contains(Constant.STABLE_PACKAGE)) {
            package_name_to_use = Constant.STABLE_PACKAGE;
        }
        else if(packages_supporting_custom_tabs.contains(Constant.BETA_PACKAGE)) {
            package_name_to_use = Constant.BETA_PACKAGE;
        }
        else if(packages_supporting_custom_tabs.contains(Constant.DEV_PACKAGE)) {
            package_name_to_use = Constant.DEV_PACKAGE;
        }
        else if(packages_supporting_custom_tabs.contains(Constant.LOCAL_PACKAGE)) {
            package_name_to_use = Constant.LOCAL_PACKAGE;
        }
        return package_name_to_use;
    }

    private static boolean hasSpecializedHandlerIntents(Context context, Intent intent) {
        final List<ResolveInfo> handlers = context.getPackageManager().queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);

        if(handlers == null || handlers.size() == 0) {
            return false;
        }
        else {
            for(ResolveInfo resolve_info : handlers) {
                final IntentFilter filter = resolve_info.filter;

                if(filter == null || (filter.countDataAuthorities() == 0 || filter.countDataPaths() == 0) || resolve_info.activityInfo == null) {
                    continue;
                }
                return true;
            }
        }
        return false;
    }
}