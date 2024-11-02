package su.univers.iti.scaner.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.SpannableString;
import android.text.style.StyleSpan;

import androidx.preference.PreferenceManager;

public class SettingsStorage {
    public static String site_url, user_login, user_password, iti_id, subject_id, subject_name, max_version;
    public static boolean result_or_barcode;
    public static int max_frames;

    public static Activity activity;

    public static void updatePreferences(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity.getBaseContext());
        site_url = prefs.getString("site_url", "iti.univers.su");
        try {
            max_frames = Integer.parseInt(prefs.getString("max_frames", "30"));
        } catch (NumberFormatException e) {
            max_frames = 30;
        }
        user_login = prefs.getString("user_login", "-");
        user_password = prefs.getString("user_password", "-");
        iti_id = prefs.getString("iti_id", "-");
        subject_id = prefs.getString("subject_id", "-");
        result_or_barcode = prefs.getBoolean("result_or_barcode", false);
    }

    public static void updateActivity(Activity cur_activity) {
        activity = cur_activity;
        activity.setTitle(getTitle());
    }

    public static SpannableString getTitle() {
        SpannableString spanString = null;
        if (result_or_barcode) {
            spanString = new SpannableString("ИТИ " + SettingsStorage.iti_id);
        } else {
            spanString = new SpannableString(subject_name + ", ИТИ " + SettingsStorage.iti_id);
        }
        spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
        return spanString;
    }

    public static boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
