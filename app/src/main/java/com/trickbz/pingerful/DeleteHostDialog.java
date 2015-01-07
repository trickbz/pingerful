package com.trickbz.pingerful;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.trickbz.pingerful.tasks.VoidCallback;

public class DeleteHostDialog extends DialogFragment {

    public DeleteHostDialog() { }

    private VoidCallback _callback;
    private long _hostId;
    private static final String HOST_ID_TO_DELETE = "HOST_ID_TO_DELETE";

    public void setCallback(VoidCallback callback) {
        this._callback = callback;
    }

    public static DeleteHostDialog newInstance(long hostId)
    {
        DeleteHostDialog fragment = new DeleteHostDialog();
        Bundle args = new Bundle();
        args.putLong(HOST_ID_TO_DELETE, hostId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        _hostId = getArguments().getLong(HOST_ID_TO_DELETE);
        Host.getById(_hostId);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Host host = Host.getById(_hostId);
        builder.setTitle(String.format("Delete '%s' host", host.getTitleIfNotEmptyOrName()))
                .setMessage(String.format("Do you really want to delete host '%s'", host.getTitleIfNotEmptyOrName()))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Host.deleteHost(_hostId);
                        _callback.onActionFinished();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { dismiss(); }
                });
        return builder.create();
    }
}