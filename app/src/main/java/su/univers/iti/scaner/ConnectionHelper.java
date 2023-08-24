package su.univers.iti.scaner;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConnectionHelper {
    public static String site_url, user_login, user_password, iti_id, subject_id;
    public static boolean result_or_barcode;

    public static void updatePreferences(Context ctx){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        site_url = prefs.getString("site_url", "-");
        user_login = prefs.getString("user_login", "-");
        user_password = prefs.getString("user_password", "-");
        iti_id = prefs.getString("iti_id", "-");
        subject_id = prefs.getString("subject_id", "-");
        result_or_barcode = prefs.getBoolean("result_or_barcode", false);
    }

    public static String getUrl(String path){
        return "http://" + site_url + "/" + path;
    }

    public static String getUrlForITI(String path){
        return "http://" + site_url + "/" + iti_id + "/" + path;
    }

    public static String getUrlForSubject(String path){
        return "http://" + site_url + "/" + iti_id + "/" + subject_id + "/" + path;
    }

    public static HttpResponse makeRequest(String path, JSONObject data) throws IOException {
        try {
            data.put("user_login", user_login);
            data.put("user_password", user_password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("PATH", path);
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httpost = new HttpPost(path);
        StringEntity params = new StringEntity(data.toString());
        httpost.setEntity(params);
        httpost.setHeader("Accept", "application/json");
        httpost.setHeader("Content-type", "application/json");
        Log.e("PATH", "Execute");
        return httpclient.execute(httpost);
    }

    public static JSONObject getJSON(HttpResponse response) throws IOException, JSONException {
        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        Log.e("JSON ", sb.toString());
        return new JSONObject(sb.toString());
    }
}
