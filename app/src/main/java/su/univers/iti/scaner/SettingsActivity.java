package su.univers.iti.scaner;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import su.univers.iti.scaner.requests.GetSubjectInfo;
import su.univers.iti.scaner.utils.SettingsStorage;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        String versionName = BuildConfig.VERSION_NAME;
        findPreference("version").setSummary(String.format("Ваша версия: %s, последняя версия: %s", versionName, SettingsStorage.max_version));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SettingsStorage.updatePreferences();
        new GetSubjectInfo().execute();
    }
}
