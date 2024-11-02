package su.univers.iti.scaner;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import su.univers.iti.scaner.requests.SaveResult;
import su.univers.iti.scaner.utils.BarcodesTable;
import su.univers.iti.scaner.utils.SettingsStorage;

public class DataResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_result);
        SettingsStorage.updateActivity(this);

        EditText input_code = (EditText) findViewById(R.id.data_result_code);
        input_code.setText("" + BarcodesTable.getOneEAN13());
        EditText input_result = (EditText) findViewById(R.id.data_result_result);
        Button button_send = (Button) findViewById(R.id.data_result_send);
        Button button_back = (Button) findViewById(R.id.data_back);

        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = input_code.getText().toString();
                String result = input_result.getText().toString();
                new SaveResult(code, result).execute();
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
