package su.univers.iti.scaner.requests;

import android.annotation.SuppressLint;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import su.univers.iti.scaner.DataBarcodeActivity;
import su.univers.iti.scaner.utils.SettingsStorage;

public class GetStudentInfo extends RequestParent {
    long student_id;

    public GetStudentInfo(long student_id) {
        super("student_info", true, false);
        this.student_id = student_id;
        try {
            jsonSend.put("student_id", student_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint({"ShowToast", "SetTextI18n"})
    @Override
    protected void onPostExecute(String res) {
        super.onPostExecute(res);

        if (SettingsStorage.activity instanceof DataBarcodeActivity) {
            DataBarcodeActivity activity = (DataBarcodeActivity) SettingsStorage.activity;
            if (status_ok) {
                try {
                    JSONObject student = jsonGet.getJSONObject("student"), cls = jsonGet.getJSONObject("student_class");
                    activity.student_name1.setText(student.getString("name_1"));
                    activity.student_name2.setText(student.getString("name_2"));
                    activity.student_class.setText(cls.getString("class_number") + cls.getString("class_latter"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(activity.getBaseContext(), "Ошибка на сервере", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(activity.getBaseContext(), "Ошибка: " + msg, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(SettingsStorage.activity.getBaseContext(), "Окно уже закрыто", Toast.LENGTH_SHORT).show();
        }
    }
}
