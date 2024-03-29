package su.univers.iti.scaner;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        String versionName = BuildConfig.VERSION_NAME;
        findPreference("version").setSummary(versionName);
    }
}
