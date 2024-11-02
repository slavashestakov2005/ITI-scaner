package su.univers.iti.scaner;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import su.univers.iti.scaner.requests.GetStudentInfo;
import su.univers.iti.scaner.requests.SaveBarcode;
import su.univers.iti.scaner.utils.BarcodesTable;
import su.univers.iti.scaner.utils.SettingsStorage;

public class DataBarcodeActivity extends AppCompatActivity {
    public TextView student_name1;
    public TextView student_name2;
    public TextView student_class;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_barcode);
        SettingsStorage.updateActivity(this);

        EditText input_student = (EditText) findViewById(R.id.data_barcode_student);
        input_student.setText("" + BarcodesTable.getEAN8());
        student_name1 = (TextView) findViewById(R.id.data_barcode_student_name1);
        student_name2 = (TextView) findViewById(R.id.data_barcode_student_name2);
        student_class = (TextView) findViewById(R.id.data_barcode_student_class);
        ArrayList<Integer> input_codes = new ArrayList<>();
        input_codes.add(R.id.data_barcode_code1);
        input_codes.add(R.id.data_barcode_code2);
        input_codes.add(R.id.data_barcode_code3);
        input_codes.add(R.id.data_barcode_code4);
        input_codes.add(R.id.data_barcode_code5);
        ArrayList<Long> ean13 = BarcodesTable.getEAN13();
        for (int i = 0; i < Math.min(input_codes.size(), ean13.size()); ++i) {
            EditText input_code = (EditText) findViewById(input_codes.get(i));
            input_code.setText("" + ean13.get(i));
        }
        Button button_update = (Button) findViewById(R.id.data_barcode_update_student);
        Button button_send = (Button) findViewById(R.id.data_barcode_send);
        Button button_back = (Button) findViewById(R.id.data_back);

        button_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String student_id = input_student.getText().toString();
                try {
                    long long_id = Long.parseLong(student_id);
                    new GetStudentInfo(long_id).execute();
                } catch (NumberFormatException e) {
                    Toast.makeText(getBaseContext(), "Код школьника не число", Toast.LENGTH_SHORT).show();
                }
            }
        });

        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String student_code = input_student.getText().toString();
                ArrayList<Long> codes = new ArrayList<>();
                try {
                    Long student_id = Long.parseLong(student_code);
                    for (Integer view_id : input_codes) {
                        String cur_code = ((EditText) findViewById(view_id)).getText().toString();
                        if (cur_code.length() > 0) codes.add(Long.parseLong(cur_code));
                    }
                    new SaveBarcode(student_id, codes).execute();
                } catch (NumberFormatException e) {
                    Toast.makeText(getBaseContext(), "Где-то записано не число", Toast.LENGTH_SHORT).show();
                }
            }
        });

        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SettingsStorage.updateActivity(this);
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
}
