package su.univers.iti.scaner.requests;

import android.annotation.SuppressLint;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import su.univers.iti.scaner.utils.SettingsStorage;

public class GetSubjectInfo extends RequestParent {
    String subject_id;

    public GetSubjectInfo() {
        super("subject_info", true, false);
        this.subject_id = SettingsStorage.subject_id;
        try {
            jsonSend.put("subject_id", subject_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint({"ShowToast", "SetTextI18n"})
    @Override
    protected void onPostExecute(String res) {
        super.onPostExecute(res);
        if (status_ok) {
            try {
                JSONObject subject = jsonGet.getJSONObject("subject");
                SettingsStorage.subject_name = subject.getString("name");
                SettingsStorage.activity.setTitle(SettingsStorage.getTitle());
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(SettingsStorage.activity.getBaseContext(), "Ошибка на сервере", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(SettingsStorage.activity.getBaseContext(), "Ошибка: " + msg, Toast.LENGTH_SHORT).show();
        }
    }
}
