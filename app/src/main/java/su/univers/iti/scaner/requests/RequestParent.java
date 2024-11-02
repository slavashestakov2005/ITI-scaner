package su.univers.iti.scaner.requests;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import su.univers.iti.scaner.utils.SettingsStorage;

abstract class RequestParent extends AsyncTask<String, String, String> {
    private final String path;
    private final boolean useITIID;
    private final boolean useSubjectID;
    protected JSONObject jsonSend, jsonGet;
    protected String status, msg;
    protected boolean status_ok;

    public RequestParent(String path, boolean useITIID, boolean useSubjectID) {
        this.path = path;
        this.useITIID = useITIID;
        this.useSubjectID = useSubjectID;
        jsonSend = new JSONObject();
        if (!SettingsStorage.isInternetAvailable()) {
            status_ok = false;
            msg = "Нет поделючения к интернету";
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        if (msg != null) return null;
        try {
            String server;
            if (useITIID && useSubjectID) server = ConnectionHelper.getUrlForSubject(path);
            else if (useITIID) server = ConnectionHelper.getUrlForITI(path);
            else server = ConnectionHelper.getUrl(path);
            HttpResponse answer = ConnectionHelper.makeRequest(server, jsonSend);
            jsonGet = ConnectionHelper.getJSON(answer);
            status = jsonGet.getString("status");
            status_ok = status.equals("OK");
            if (status_ok) msg = null;
            else msg = jsonGet.getString("msg");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
