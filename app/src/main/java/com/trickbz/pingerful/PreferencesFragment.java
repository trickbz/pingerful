package com.trickbz.pingerful;

import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.RingtonePreference;
import android.provider.Settings;

public class PreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        updatePreference(findPreference(key));
    }

    @Override
    public void onResume()
    {
        super.onResume();
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i)
        {
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof PreferenceGroup)
            {
                PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
                for (int j = 0; j < preferenceGroup.getPreferenceCount(); ++j)
                {
                    updatePreference(preferenceGroup.getPreference(j));
                }
            }
            else
            {
                updatePreference(preference);
            }
        }
    }

    private void updatePreference(Preference preference)
    {
        final SharedPreferences sharedPrefs = preference.getSharedPreferences();
        String preferenceKey = preference.getKey();
        if (preference instanceof RingtonePreference)
        {
            String ringtonePath = sharedPrefs.getString(preferenceKey, null);
            Uri ringtoneUri = ringtonePath != null ?
                    Uri.parse(ringtonePath) :
                    Settings.System.DEFAULT_RINGTONE_URI;
            Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);
            preference.setSummary(ringtone.getTitle(getActivity()));
        }
        else if (preference instanceof EditTextPreference)
        {
            if (preferenceKey.equals(getString(R.string.pref_ping_interval)))
            {
                String value = sharedPrefs.getString(preferenceKey, "2");
                preference.setSummary(String.format("Ping each %s minute(s)", value));
            }
            else if (preferenceKey.equals(getString(R.string.pref_pings_count, "3")))
            {
                String value = sharedPrefs.getString(preferenceKey, "3");
                preference.setSummary(String.format("Ping %s time(s)", value));
            }
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}