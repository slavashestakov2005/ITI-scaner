package su.univers.iti.scaner.requests;

import android.annotation.SuppressLint;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;

import su.univers.iti.scaner.utils.SettingsStorage;

public
class SaveResult extends RequestParent {
    public SaveResult(String code, String result) {
        super("save_results", true, true);
        ArrayList<ArrayList<String>> data = new ArrayList<>();
        ArrayList<String> row = new ArrayList<>();
        row.add(code);
        row.add(result.replace(",", "."));
        data.add(row);
        try {
            jsonSend.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("ShowToast")
    @Override
    protected void onPostExecute(String res) {
        super.onPostExecute(res);
        if (status_ok) {
            Toast.makeText(SettingsStorage.activity.getBaseContext(), "Данные сохранены", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(SettingsStorage.activity.getBaseContext(), "Ошибка: " + msg, Toast.LENGTH_SHORT).show();
        }
    }
}
