package com.trickbz.pingerful.tasks;

import android.os.AsyncTask;

import com.trickbz.pingerful.PingHostModel;
import com.trickbz.pingerful.helpers.PingHelper;

public class PingHostTask extends AsyncTask<PingHostModel, Void, Boolean> {

    private PingHostModel _model;
    private TaskCompletedBooleanCallback callback;

    @Override
    protected Boolean doInBackground(PingHostModel... params)
    {
        _model = params[0];
        return PingHelper.PingByPingAndPort(_model);
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        super.onPostExecute(result);
        callback.taskCompletedBooleanCallback(result);
    }

    public void setCallback(TaskCompletedBooleanCallback callback)
    {
        this.callback = callback;
    }
}
