package com.trickbz.pingerful;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.trickbz.pingerful.helpers.PingHelper;
import com.trickbz.pingerful.tasks.PingHostTask;
import com.trickbz.pingerful.tasks.TaskCompletedBooleanCallback;
import com.trickbz.pingerful.tasks.VoidCallback;

public class AddHostDialog extends DialogFragment
{
    private EditText _editTextHostTitle;
    private EditText _editTextHostNameOrIp;
    private EditText _editTextPortNumber;
    private CheckBox _checkBoxSkip;
    private CheckBox _checkBoxNotifyPingFails;

    private static final String IS_HOST_ACTIVE_ARG_KEY = "IS_HOST_ACTIVE_ARG_KEY";
    private static final String IS_NOTIFY_PING_FAILS = "IS_NOTIFY_PING_FAILS";
    private static final String HOST_TITLE_ARG_KEY = "HOST_TITLE_ARG_KEY";
    private static final String HOST_NAME_OR_IP_ARG_KEY = "HOST_NAME_OR_IP_ARG_KEY";
    private static final String PORT_NUMBER_ARG_KEY = "PORT_NUMBER_ARG_KEY";
    private static final String CREATE_OR_UPDATE_ARG_KEY = "CREATE_OR_UPDATE_ARG_KEY";
    private static final String SUBMIT_BUTTON_TITLE_ARG_KEY = "SUBMIT_BUTTON_TITLE_ARG_KEY";
    private static final String HOST_ID_FOR_EDIT_ARG_KEY = "HOST_ID";

    private VoidCallback _callback;
    private CreateUpdate _operationType;
    private long _hostId;

    public AddHostDialog() { }

    public void setCallback(VoidCallback callback) { this._callback = callback; }

    public static AddHostDialog newInstance(Host host, CreateUpdate operationType)
    {
        AddHostDialog fragment = new AddHostDialog();
        Bundle args = new Bundle();
        args.putBoolean(IS_HOST_ACTIVE_ARG_KEY, operationType == CreateUpdate.CREATE || host.isActive);
        args.putBoolean(IS_NOTIFY_PING_FAILS, operationType != CreateUpdate.CREATE && host.notifyWhenPingFails);
        args.putString(HOST_TITLE_ARG_KEY, host.title);
        args.putString(HOST_NAME_OR_IP_ARG_KEY, host.nameOrIp);
        args.putString(PORT_NUMBER_ARG_KEY, host.portNumber);
        args.putInt(CREATE_OR_UPDATE_ARG_KEY, operationType.value());
        args.putString(SUBMIT_BUTTON_TITLE_ARG_KEY, operationType == CreateUpdate.CREATE ? "Create" : "Update");
        args.putLong(HOST_ID_FOR_EDIT_ARG_KEY, operationType == CreateUpdate.CREATE ? -1 : host.getId());

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View view = inflater.inflate(R.layout.dialog_add_host, null);

        _editTextHostTitle = (EditText) view.findViewById(R.id.edit_host_title_add_host);
        _editTextHostNameOrIp = (EditText) view.findViewById(R.id.edit_host_ip_add_host);
        _editTextPortNumber = (EditText) view.findViewById(R.id.edit_port_number_add_host);
        _checkBoxSkip = (CheckBox) view.findViewById(R.id.checkbox_is_active_add_host);
        _checkBoxNotifyPingFails = (CheckBox) view.findViewById(R.id.checkbox_notify_ping_fails);

        Bundle arguments = getArguments();
        String hostTitle = arguments.getString(HOST_TITLE_ARG_KEY, "");
        String hostNameOrIp = arguments.getString(HOST_NAME_OR_IP_ARG_KEY, "");
        String portNumber = arguments.getString(PORT_NUMBER_ARG_KEY);
        boolean skipHost = arguments.getBoolean(IS_HOST_ACTIVE_ARG_KEY, true);
        boolean notifyPingFails = arguments.getBoolean(IS_NOTIFY_PING_FAILS, false);
        _operationType = CreateUpdate.values()[arguments.getInt(CREATE_OR_UPDATE_ARG_KEY)];
        String dialogTitle = _operationType == CreateUpdate.CREATE ?
                "Create Host" :
                String.format("Update '%s' host", hostTitle == null || hostTitle.isEmpty() ? hostNameOrIp : hostTitle);
        String submitButtonTitle = arguments.getString(SUBMIT_BUTTON_TITLE_ARG_KEY);
        _hostId = arguments.getLong(HOST_ID_FOR_EDIT_ARG_KEY);

        _editTextHostTitle.setText(hostTitle);
        _editTextHostNameOrIp.setText(hostNameOrIp);
        _editTextPortNumber.setFilters(new InputFilter[] { new InputFilterMinMax(0, 65535) });
        if (portNumber != null) _editTextPortNumber.setText(portNumber);
        _checkBoxSkip.setChecked(skipHost);
        _checkBoxNotifyPingFails.setChecked(notifyPingFails);

        builder.setTitle(dialogTitle);
        builder.setView(view);
        builder.setCancelable(true);

        builder.setPositiveButton(submitButtonTitle, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // Don't delete this EMPTY handler and don't define a code here.
                // Look at the 'onStart' override. There should be the code!
                // Otherwise you cannot prevent this dialog to stay opened
                // in case you e.g. need to use validation.
                // This dialog will always be closed.
                // This empty handler is needed for compatibility
                // with earlier version of Android.
                // Info from stack overflow.
            }
        });

        builder.setNegativeButton(R.string.cancel_add_host_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { dismiss(); }
        });

        final Button btnCheckHostAddHostDialog = (Button) view.findViewById(R.id.check_host_add_host);
        btnCheckHostAddHostDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Context context = getActivity();
                if (PingHelper.isNetworkConnectionAvailable(context))
                {
                    final ProgressBar progressSpinner = (ProgressBar) view.findViewById(R.id.progress_spinner_check_host_add_host_dialog);
                    final ImageView hostStatusImage = (ImageView) view.findViewById(R.id.host_status_image_add_host);
                    final EditText editHostNameOrIp = (EditText) view.findViewById(R.id.edit_host_ip_add_host);
                    final String hostNameOrIp = String.valueOf(editHostNameOrIp.getText());

                    if (validateAddHostDialog(getDialog(), hostNameOrIp)) {
                        view.setEnabled(false);
                        hostStatusImage.setVisibility(View.GONE);
                        progressSpinner.setVisibility(View.VISIBLE);

                        PingHostTask taskPingHost = new PingHostTask();
                        taskPingHost.setCallback(new TaskCompletedBooleanCallback() {
                            @Override
                            public void taskCompletedBooleanCallback(boolean result) {
                                int imageResourceId = result ?
                                        R.drawable.ok_green_32px :
                                        R.drawable.alert_red_32px;
                                hostStatusImage.setImageResource(imageResourceId);
                                hostStatusImage.setVisibility(View.VISIBLE);
                                progressSpinner.setVisibility(View.GONE);
                                view.setEnabled(true);
                            }
                        });
                        taskPingHost.execute(hostNameOrIp);
                    }
                }
                else
                {
                    Toast.makeText(context, context.getString(R.string.no_internet_connection_message), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return builder.create();
    }

    @Override
    public void onStart()
    {
        super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
        final AlertDialog alertDialog = (AlertDialog) getDialog();
        if(alertDialog != null)
        {
            Button positiveButton = alertDialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    final EditText editHostTitle = (EditText) alertDialog.findViewById(R.id.edit_host_title_add_host);
                    final EditText editHostNameOrIP = (EditText) alertDialog.findViewById(R.id.edit_host_ip_add_host);
                    final EditText editPortNumber = (EditText) alertDialog.findViewById(R.id.edit_port_number_add_host);
                    final CheckBox checkBoxIsActive = (CheckBox) alertDialog.findViewById(R.id.checkbox_is_active_add_host);
                    final CheckBox checkBoxNotifyPingFails = (CheckBox) alertDialog.findViewById(R.id.checkbox_notify_ping_fails);

                    String hostTitle = String.valueOf(editHostTitle.getText());
                    String hostNameOrIp = String.valueOf(editHostNameOrIP.getText());
                    String portNumber = String.valueOf(editPortNumber.getText());

                    if (validateAddHostDialog(alertDialog, hostNameOrIp))
                    {
                        Host host =_operationType == CreateUpdate.CREATE ?
                                new Host() :
                                Host.getById(_hostId);

                        host.isActive = checkBoxIsActive.isChecked();
                        host.title = hostTitle;
                        host.nameOrIp = hostNameOrIp;
                        if (portNumber != null) host.portNumber = portNumber;
                        host.notifyWhenPingFails = checkBoxNotifyPingFails.isChecked();

                        host.save();
                        dismiss();
                        _callback.onActionFinished();
                    }
                }
            });
        }
    }

    private boolean validateAddHostDialog(Dialog dialog, String hostNameOrIp)
    {
        final EditText editHostNameOrIP = (EditText) dialog.findViewById(R.id.edit_host_ip_add_host);
        boolean isHostValid = hostNameOrIp != null && !hostNameOrIp.isEmpty();
        if (!isHostValid)
        {
            if(editHostNameOrIP.requestFocus())
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            editHostNameOrIP.setError(getString(R.string.host_name_or_ip_required_error_message));
        }
        return isHostValid;
    }
}
