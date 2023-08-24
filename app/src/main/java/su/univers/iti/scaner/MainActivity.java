package su.univers.iti.scaner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.SymbolSet;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;
    private FrameLayout preview;
    private LinearLayout table;
    private Button button_scan, button_send;
    private ImageScanner scanner;
    private boolean previewing = true;
    private Image codeImage;
    private static int row_num = 1;

    private static final int REQUEST_APP_SETTINGS = 168;
    private static final String[] requiredPermissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    static {
        System.loadLibrary("iconv");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        autoFocusHandler = new Handler();
        preview = (FrameLayout) findViewById(R.id.cameraPreview);
        /* Instance barcode scanner */
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);
        button_scan = (Button) findViewById(R.id.button_scan);
        button_send = (Button) findViewById(R.id.button_send);
        table = (LinearLayout) findViewById(R.id.students_table);

        button_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int result = scanner.scanImage(codeImage);

                if (result != 0) {
                    SymbolSet syms = scanner.getResults();
                    ConnectionHelper.updatePreferences(getBaseContext());
                    if (ConnectionHelper.result_or_barcode) {
                        ArrayList<BarcodesTable> data = BarcodesTable.parseData(syms);
                        if (data.size() > 0) clearTable();
                        for (BarcodesTable bar : data) {
                            addRow(bar.code, "?", "?", "?", bar.barcodes);
                        }
                    } else {
                        ArrayList<Long> ean13 = BarcodesTable.getEAN13(syms);
                        if (ean13.size() > 0) clearTable();
                        for (Long bar : ean13) {
                            addRowSubject(bar);
                        }
                    }
                }
            }
        });

        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectionHelper.updatePreferences(getBaseContext());
                if (ConnectionHelper.result_or_barcode) {
                    new SaveTable().execute();
                } else {
                    new SaveSubjectResult().execute();
                }
            }
        });
    }

    private void goToSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(myAppSettings, REQUEST_APP_SETTINGS);
    }
    public boolean hasPermissions(@NonNull String... permissions) {
        for (String permission : permissions)
            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(permission))
                return false;
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private TableRow createTextView(String description, String text) {
        TableRow row = new TableRow(this);
        TextView des = new TextView(this);
        des.setText(description);
        des.setBackgroundColor((getColor(R.color.table_cell)));
        TextView val = new TextView(this);
        val.setText(text);
        val.setBackgroundColor((getColor(R.color.table_cell)));
        row.addView(des);
        row.addView(val);
        return row;
    }

    private TableRow createEditText(String description, String text) {
        TableRow row = new TableRow(this);
        TextView des = new TextView(this);
        des.setText(description);
        des.setBackgroundColor((getColor(R.color.table_cell)));
        EditText val = new EditText(this);
        val.setText(text);
        val.setBackgroundColor((getColor(R.color.table_cell)));
        row.addView(des);
        row.addView(val);
        return row;
    }

    private TableRow createButton() {
        TableRow row = new TableRow(this);
        TextView des = new TextView(this);
        des.setText("Обновить");
        des.setBackgroundColor((getColor(R.color.table_cell)));
        Button val = new Button(this);
        val.setOnClickListener(new View.OnClickListener() {
            int row_num = MainActivity.row_num - 1;

            @Override
            public void onClick(View v) {
                Log.e("Click", "" + row_num);
                TableLayout layout = (TableLayout) table.getChildAt(row_num);
                TableRow line = (TableRow) layout.getChildAt(1);
                EditText cur = (EditText) line.getChildAt(1);
                long student_id = Long.parseLong(cur.getText().toString());
                Log.e("Click", "" + student_id);
                new GetStudentInfo(student_id, row_num).execute();
            }
        });
        val.setText("Обновить информацию о школьнике");
        val.setBackgroundColor((getColor(R.color.table_cell)));
        row.addView(des);
        row.addView(val);
        return row;
    }

    private void addRow(long id, String name1, String name2, String cls, ArrayList<Long> barcodes) {
        TableLayout row = new TableLayout(this);
        row.addView(createTextView("№", "" + row_num));
        row.addView(createEditText("ID", "" + id));
        row.addView(createButton());
        row.addView(createTextView("Фамилия", name1));
        row.addView(createTextView("Имя", name2));
        row.addView(createTextView("Класс", cls));

        while (barcodes.size() > 5) barcodes.remove(barcodes.size() - 1);
        while (barcodes.size() < 5) barcodes.add((long) 0);

        int barcode_num = 1;
        for (long barcode : barcodes) {
            String text = barcode != 0 ? "" + barcode : "";
            row.addView(createEditText("Штрих-код №" + barcode_num, text));
            barcode_num++;
        }

        table.addView(row);
        row_num++;
    }

    private void addRowSubject(long barcode) {
        TableLayout row = new TableLayout(this);
        row.addView(createTextView("№", "" + row_num));
        row.addView(createEditText("ID", "" + barcode));
        row.addView(createEditText("Результат", ""));
        table.addView(row);
        row_num++;
    }

    private void clearTable() {
        for (int i = table.getChildCount() - 1; i >= 0; i--) {
            table.removeViewAt(i);
        }
        row_num = 1;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!hasPermissions(requiredPermissions)) {
            Toast.makeText(this, "Получены не все разрешения", Toast.LENGTH_LONG).show();
            goToSettings();
        }
        resumeCamera();
    }

    public void onPause() {
        super.onPause();
        releaseCamera();
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            previewing = false;
            mCamera.cancelAutoFocus();
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void resumeCamera() {
        mCamera = getCameraInstance();
        mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
        preview.removeAllViews();
        preview.addView(mPreview);
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            Size size = parameters.getPreviewSize();
            codeImage = new Image(size.width, size.height, "Y800");
            previewing = true;
            mPreview.refreshDrawableState();
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing && mCamera != null) {
                mCamera.autoFocus(autoFocusCB);
            }
        }
    };

    PreviewCallback previewCb = new PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
//            Log.d("CameraTestActivity", "onPreviewFrame data length = " + (data != null ? data.length : 0));
            codeImage.setData(data);
            camera.addCallbackBuffer(data);
        }
    };

    // Mimic continuous auto-focusing
    final AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };


    @SuppressLint("StaticFieldLeak")
    abstract class RequestParent extends AsyncTask<String, String, String> {
        private String path;
        private boolean useITIID, useSubjectID;
        protected JSONObject jsonSend, jsonGet;
        protected String status, msg;
        protected boolean status_ok;

        public RequestParent(String path, boolean useITIID, boolean useSubjectID) {
            this.path = path;
            this.useITIID = useITIID;
            this.useSubjectID = useSubjectID;
            jsonSend = new JSONObject();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                ConnectionHelper.updatePreferences(getBaseContext());
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

    @SuppressLint("StaticFieldLeak")
    class GetStudentInfo extends RequestParent {
        long student_id;
        int row_num;

        public GetStudentInfo(long student_id, int row_num) {
            super("student_info", true, false);
            this.student_id = student_id;
            this.row_num = row_num;
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

            TableLayout line = (TableLayout) table.getChildAt(row_num);
            if (line == null) return;
            EditText id_view = (EditText) ((TableRow) line.getChildAt(1)).getChildAt(1);
            if (Long.parseLong(id_view.getText().toString()) != student_id) return;
            if (status_ok) {
                try {
                    JSONObject student = jsonGet.getJSONObject("student"), cls = jsonGet.getJSONObject("student_class");

                    TextView name1_view = (TextView) ((TableRow) line.getChildAt(3)).getChildAt(1);
                    TextView name2_view = (TextView) ((TableRow) line.getChildAt(4)).getChildAt(1);
                    TextView cls_view = (TextView) ((TableRow) line.getChildAt(5)).getChildAt(1);
                    name1_view.setText(student.getString("name_1"));
                    name2_view.setText(student.getString("name_2"));
                    cls_view.setText(cls.getString("class_number") + cls.getString("class_latter"));
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Toast.makeText(getBaseContext(), "Ошибка: " + msg, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    class SaveTable extends RequestParent {
        public SaveTable() {
            super("save_barcodes", true, false);
            ArrayList<ArrayList<Long>> data = new ArrayList<>();
            for (int i = 0; i < table.getChildCount(); i++) {
                TableLayout line = (TableLayout) table.getChildAt(i);
                ArrayList<Long> row = new ArrayList<>();
                int[] cols = {1, 6, 7, 8, 9, 10};
                for (int col : cols){
                    TableRow tableRow = (TableRow) line.getChildAt(col);
                    EditText cur = (EditText) tableRow.getChildAt(1);
                    String text = cur.getText().toString();
                    if (text.length() > 0) row.add(Long.parseLong(text));
                }
                data.add(row);
            }
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
                Toast.makeText(getBaseContext(), "Данные сохранены", Toast.LENGTH_SHORT).show();
                clearTable();
            } else {
                Toast.makeText(getBaseContext(), "Ошибка: " + msg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class SaveSubjectResult extends RequestParent {
        public SaveSubjectResult() {
            super("save_results", true, true);
            ArrayList<ArrayList<Long>> data = new ArrayList<>();
            for (int i = 0; i < table.getChildCount(); i++) {
                TableLayout line = (TableLayout) table.getChildAt(i);
                ArrayList<Long> row = new ArrayList<>();
                int[] cols = {1, 2};
                for (int col : cols){
                    TableRow tableRow = (TableRow) line.getChildAt(col);
                    EditText cur = (EditText) tableRow.getChildAt(1);
                    String text = cur.getText().toString();
                    if (text.length() > 0) row.add(Long.parseLong(text));
                }
                if (row.size() != 2) Toast.makeText(getBaseContext(), "Не удалось прочитать строку " + (i + 1), Toast.LENGTH_SHORT).show();
                else data.add(row);
            }
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
                Toast.makeText(getBaseContext(), "Данные сохранены", Toast.LENGTH_SHORT).show();
                clearTable();
            } else {
                Toast.makeText(getBaseContext(), "Ошибка: " + msg, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
