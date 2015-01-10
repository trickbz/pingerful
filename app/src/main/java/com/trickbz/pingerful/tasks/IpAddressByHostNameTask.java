package com.trickbz.pingerful.tasks;

import android.os.AsyncTask;

import com.trickbz.pingerful.helpers.PingHelper;

public class IpAddressByHostNameTask extends AsyncTask<String, Void, String>
{
    private StringCallback _callback;
    private String _hostName;

    @Override
    protected String doInBackground(String... params)
    {
        _hostName = params[0];
        String ipAddress = PingHelper.GetIpByHostName(_hostName);
        return ipAddress;
    }

    public void setCallback(StringCallback callback)
    {
        _callback = callback;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        _callback.onActionFinished(s);
    }
}
