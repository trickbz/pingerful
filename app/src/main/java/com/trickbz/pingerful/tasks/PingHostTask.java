package com.trickbz.pingerful.tasks;

import android.os.AsyncTask;

import com.trickbz.pingerful.helpers.PingHelper;

public class PingHostTask extends AsyncTask<String, Void, Boolean> {

    private String hostNameOrIp;
    private TaskCompletedBooleanCallback callback;

    @Override
    protected Boolean doInBackground(String... params) {

        this.hostNameOrIp = params[0];
        boolean result = PingHelper.PingHost(hostNameOrIp);
        return result;

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
