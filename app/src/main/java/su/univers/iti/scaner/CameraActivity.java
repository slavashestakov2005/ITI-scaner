package su.univers.iti.scaner;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.SymbolSet;

import su.univers.iti.scaner.utils.BarcodesTable;
import su.univers.iti.scaner.utils.CameraPreview;
import su.univers.iti.scaner.utils.SettingsStorage;

public class CameraActivity extends AppCompatActivity {
    public enum State {
        BEGIN, SCANNING, END;
    }
    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;
    private FrameLayout preview;
    private ImageScanner scanner;
    private Button button_start_stop;
    private boolean previewing = true;
    private State state;

    private Image codeImage;

    static {
        System.loadLibrary("iconv");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);
        SettingsStorage.updateActivity(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        autoFocusHandler = new Handler();
        preview = (FrameLayout) findViewById(R.id.frame_camera);
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);
        button_start_stop = (Button) findViewById(R.id.button_start_stop);

        button_start_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state == State.BEGIN) {
                    BarcodesTable.clear();
                    state = State.SCANNING;
                    button_start_stop.setText("Стоп");
                } else if (state == State.SCANNING) {
                    state = State.END;
                    nextActivity();
                }
            }
        });
    }

    void nextActivity() {
        button_start_stop.setText("Сканирование завершено");
        if (SettingsStorage.result_or_barcode) {
            Intent myIntent = new Intent(this, DataBarcodeActivity.class);
            startActivity(myIntent);
        } else {
            Intent myIntent = new Intent(this, DataResultActivity.class);
            startActivity(myIntent);
        }
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
        state = State.BEGIN;
        button_start_stop.setText("Старт");
        BarcodesTable.clear();
        resumeCamera();
    }

    public void onPause() {
        super.onPause();
        releaseCamera();
    }

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
            codeImage.setData(data);
            if (state == State.SCANNING) {
                int result = scanner.scanImage(codeImage);
                if (result != 0) {
                    SymbolSet symbols = scanner.getResults();
                    BarcodesTable.parse(symbols);
                    if (BarcodesTable.getUpdatesCount() > SettingsStorage.max_frames) {
                        state = State.END;
                        nextActivity();
                    }
                }
            }
            camera.addCallbackBuffer(data);
        }
    };

    // Mimic continuous auto-focusing
    final AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };
}
