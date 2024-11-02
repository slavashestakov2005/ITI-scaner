package su.univers.iti.scaner.requests;

import android.annotation.SuppressLint;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;

import su.univers.iti.scaner.utils.SettingsStorage;

public class SaveBarcode extends RequestParent {
    public SaveBarcode(Long student_id, ArrayList<Long> codes) {
        super("save_barcodes", true, false);
        ArrayList<ArrayList<Long>> data = new ArrayList<>();
        ArrayList<Long> row = new ArrayList<>();
        row.add(student_id);
        row.addAll(codes);
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
