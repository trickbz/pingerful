package com.trickbz.pingerful;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;


public class SettingsActivity extends Activity {

    private boolean mUseNotifications;
    private int mPingInterval;
    private boolean mPingAutomatically;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_file_key), 0);

        mUseNotifications = preferences.getBoolean(getString(R.string.pref_use_notifications), true);
        mPingInterval = preferences.getInt(getString(R.string.pref_ping_interval), 2);
        mPingAutomatically = preferences.getBoolean(getString(R.string.pref_ping_automatically), true);

    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_file_key), 0);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(getString(R.string.pref_use_notifications), mUseNotifications);
        editor.putInt(getString(R.string.pref_ping_interval), mPingInterval);
        editor.putBoolean(getString(R.string.pref_ping_automatically), mPingAutomatically);

        editor.commit();

    }
}
