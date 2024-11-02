package su.univers.iti.scaner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import su.univers.iti.scaner.requests.GetScannerVersion;
import su.univers.iti.scaner.requests.GetSubjectInfo;
import su.univers.iti.scaner.utils.SettingsStorage;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_APP_SETTINGS = 168;
    private static final String[] requiredPermissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        SettingsStorage.updateActivity(this);
        SettingsStorage.updatePreferences();
        new GetSubjectInfo().execute();
        new GetScannerVersion().execute();

        Button button_start_scan = (Button) findViewById(R.id.button_start_scan);

        button_start_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, CameraActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
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

    @Override
    protected void onResume() {
        super.onResume();
        SettingsStorage.updateActivity(this);
        if (!hasPermissions(requiredPermissions)) {
            Toast.makeText(this, "Получены не все разрешения", Toast.LENGTH_LONG).show();
            goToSettings();
        }
    }
}
