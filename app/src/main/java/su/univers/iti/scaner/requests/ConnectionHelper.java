package su.univers.iti.scaner.requests;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import su.univers.iti.scaner.utils.SettingsStorage;

public class ConnectionHelper {
    public static String getUrl(String path){
        return "http://" + SettingsStorage.site_url + "/" + path;
    }

    public static String getUrlForITI(String path){
        return "http://" + SettingsStorage.site_url + "/" + SettingsStorage.iti_id + "/" + path;
    }

    public static String getUrlForSubject(String path){
        return "http://" + SettingsStorage.site_url + "/" + SettingsStorage.iti_id + "/" + SettingsStorage.subject_id + "/" + path;
    }

    public static HttpResponse makeRequest(String path, JSONObject data) throws IOException {
        try {
            data.put("user_login", SettingsStorage.user_login);
            data.put("user_password", SettingsStorage.user_password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httpost = new HttpPost(path);
        StringEntity params = new StringEntity(data.toString());
        httpost.setEntity(params);
        httpost.setHeader("Accept", "application/json");
        httpost.setHeader("Content-type", "application/json");
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
        return new JSONObject(sb.toString());
    }
}
