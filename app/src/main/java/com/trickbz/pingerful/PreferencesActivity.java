package com.trickbz.pingerful;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;


public class PreferencesActivity extends Activity
{
    private int _pingInterval;
    private boolean _pingAutomatically;
    private boolean _notifyHostOnline;
    private String _hostOfflineNotificationRingtone;
    private String _hostOnlineNotificationRingtone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PreferencesFragment())
                .commit();

        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_file_key), 0);

        _pingInterval = preferences.getInt(getString(R.string.pref_ping_interval), 2);
        _pingAutomatically = preferences.getBoolean(getString(R.string.pref_ping_automatically), true);
        _notifyHostOnline = preferences.getBoolean(getString(R.string.pref_notification_host_back_online), true);

        _hostOfflineNotificationRingtone = preferences.getString(getString(R.string.pref_button_set_ping_fails_notification_ringtone), "NO_SOUND");
        _hostOnlineNotificationRingtone = preferences.getString(getString(R.string.pref_button_set_host_online_notification_ringtone), "NO_SOUND");
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences_file_key), 0);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(getString(R.string.pref_ping_interval), _pingInterval);
        editor.putBoolean(getString(R.string.pref_ping_automatically), _pingAutomatically);
        editor.putBoolean(getString(R.string.pref_notification_host_back_online), _notifyHostOnline);
        editor.putString(getString(R.string.pref_button_set_ping_fails_notification_ringtone), _hostOfflineNotificationRingtone);
        editor.putString(getString(R.string.pref_button_set_host_online_notification_ringtone), _hostOnlineNotificationRingtone);

        editor.commit();
    }
}
