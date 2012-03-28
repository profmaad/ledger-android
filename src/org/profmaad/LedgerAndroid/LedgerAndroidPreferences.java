package org.profmaad.LedgerAndroid;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class LedgerAndroidPreferences extends PreferenceActivity
{
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
	}
}
