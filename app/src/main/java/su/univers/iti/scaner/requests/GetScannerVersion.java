package su.univers.iti.scaner.requests;

import android.annotation.SuppressLint;
import android.widget.Toast;

import org.json.JSONException;

import su.univers.iti.scaner.utils.SettingsStorage;

public class GetScannerVersion extends RequestParent {
    public GetScannerVersion() {
        super("scanner_version", false, false);
    }

    @SuppressLint({"ShowToast", "SetTextI18n"})
    @Override
    protected void onPostExecute(String res) {
        super.onPostExecute(res);
        if (status_ok) {
            try {
                SettingsStorage.max_version = jsonGet.getString("version");
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(SettingsStorage.activity.getBaseContext(), "Ошибка на сервере", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(SettingsStorage.activity.getBaseContext(), "Ошибка: " + msg, Toast.LENGTH_SHORT).show();
        }
    }
}
