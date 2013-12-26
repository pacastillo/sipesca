package com.pacv.bt;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.widget.Toast;

public class PrefsActivity extends PreferenceActivity {

	public static final String NODO_ID_KEY = "nodo_id_preference";
	public static final String URL_KEY = "url_preference";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.preferences);
	}

}
