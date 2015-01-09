package com.trickbz.pingerful.tasks;

import android.os.AsyncTask;

import com.trickbz.pingerful.HostNamePort;
import com.trickbz.pingerful.helpers.PingHelper;

public class PingHostTask extends AsyncTask<HostNamePort, Void, Boolean> {

    private HostNamePort _nameAndPort;
    private TaskCompletedBooleanCallback callback;

    @Override
    protected Boolean doInBackground(HostNamePort... params) {

        _nameAndPort = params[0];
        String nameOrIp = _nameAndPort.get_nameOrIp();
        String portString = _nameAndPort.get_port();
        boolean pingPassed = PingHelper.PingHost(nameOrIp);
        if (portString != null && !portString.isEmpty())
        {
            int port = Integer.parseInt(portString);
            boolean portOpened = PingHelper.PingPort(nameOrIp, port, 1000);
            pingPassed = pingPassed && portOpened;
        }
        return pingPassed;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        callback.taskCompletedBooleanCallback(aBoolean);
    }

    public void setCallback(TaskCompletedBooleanCallback callback) {

        this.callback = callback;

    }
}
