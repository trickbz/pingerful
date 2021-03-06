package com.trickbz.pingerful.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.trickbz.pingerful.Host;
import com.trickbz.pingerful.PingHostModel;
import com.trickbz.pingerful.R;
import com.trickbz.pingerful.helpers.NotificationService;
import com.trickbz.pingerful.helpers.PingHelper;

import java.util.ArrayList;
import java.util.Date;

public class PingHostListTask extends AsyncTask<Void, Void, Void> {

    private Context _context;

    public PingHostListTask(Context context)
    {
        this._context = context;
    }

    private VoidCallback _callback;

    @Override
    protected Void doInBackground(Void... params)
    {
        ArrayList<Host> hosts = Host.all();

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
        boolean isPingAutomatically = prefs.getBoolean(_context.getString(R.string.pref_ping_automatically), true);
        int pingsCount = Integer.parseInt(prefs.getString(_context.getString(R.string.pref_pings_count), "3"));

        for (Host host : hosts)
        {
            if (host.isActive)
            {
                boolean isOnlinePrevious = host.isOnline;
                PingHostModel pingModel = new PingHostModel(host.nameOrIp, host.portNumber, host.checkPortOnly, pingsCount);
                boolean pingPassed = PingHelper.PingByPingAndPort(pingModel);
                Date currentDate = new Date();
                host.isOnline = pingPassed;
                host.lastCheckedDate = currentDate;
                if (pingPassed) host.lastOnlineDate = currentDate;

                if (isPingAutomatically && host.notifyWhenPingFails)
                {
                    if (isOnlinePrevious && !pingPassed) // ping failed
                        NotificationService.notifyPingFails(this._context, host);

                    boolean prefNotifyWhenHostBackOnline = prefs.getBoolean(_context.getString(R.string.pref_notification_host_back_online), true);
                    if (prefNotifyWhenHostBackOnline && pingPassed && !isOnlinePrevious) // host online
                    {
                        NotificationService.notifyHostOnline(this._context, host);
                    }
                }
                host.save();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        this._callback.onActionFinished();
    }

    public void setCallback(VoidCallback callback) {

        this._callback = callback;

    }
}
