package com.trickbz.pingerful;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
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

public class EditHostActivity extends ActionBarActivity
{
    // controls
    private EditText _editTextHostTitle;
    private EditText _editTextHostNameOrIp;
    private EditText _editTextPortNumber;
    private CheckBox _checkBoxIsActive;
    private CheckBox _checkBoxNotifyPingFails;
    private ImageView _imageCheckStatus;
    private ProgressBar _progressSpinner;
    private Button _buttonCheckHost;

    // bundle keys
    public final static String CREATE_OR_UPDATE_EXTRAS_KEY = "CREATE_OR_UPDATE_EXTRAS_KEY";
    public final static String HOST_ID_EXTRAS_KEY = "HOST_ID_EXTRAS_KEY";

    // Input extras
    private Host _host;
    private CreateUpdate _operationType;
    private long _hostId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_host);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        _operationType = (CreateUpdate) getIntent().getSerializableExtra(CREATE_OR_UPDATE_EXTRAS_KEY);
        _hostId = getIntent().getLongExtra(HOST_ID_EXTRAS_KEY, -1);
        _host = _operationType == CreateUpdate.CREATE ? new Host() : Host.getById(_hostId);

        String hostTitle = _host.title;
        String hostNameOrIp = _host.nameOrIp;

        String dialogTitle = _operationType == CreateUpdate.CREATE ?
                "Create Host" :
                String.format("Update '%s' host", hostTitle == null || hostTitle.isEmpty() ? hostNameOrIp : hostTitle);
        setTitle(dialogTitle);

        // getting control references
        _editTextHostTitle = (EditText) findViewById(R.id.edit_host_title_add_host);
        _editTextHostNameOrIp = (EditText) findViewById(R.id.edit_host_ip_add_host);
        _editTextPortNumber = (EditText) findViewById(R.id.edit_port_number_add_host);
        _checkBoxIsActive = (CheckBox) findViewById(R.id.checkbox_is_active_add_host);
        _checkBoxNotifyPingFails = (CheckBox) findViewById(R.id.checkbox_notify_ping_fails);
        _imageCheckStatus = (ImageView) findViewById(R.id.host_status_image_add_host);
        _progressSpinner = (ProgressBar) findViewById(R.id.progress_spinner_check_host_add_host_dialog);
        _buttonCheckHost = (Button) findViewById(R.id.button_check_host_add_host);

        // setting initial data of controls
        _editTextHostTitle.setText(_host.title);
        _editTextHostNameOrIp.setText(_host.nameOrIp);
        _editTextPortNumber.setText(_host.portNumber);
        _checkBoxIsActive.setChecked(_operationType == CreateUpdate.CREATE || _host.isActive);
        _checkBoxNotifyPingFails.setChecked(_operationType != CreateUpdate.CREATE && _host.notifyWhenPingFails);
        _buttonCheckHost.setOnClickListener(checkHostListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.edit_host_menu, menu);
        menu.findItem(R.id.action_save_host).setTitle(_operationType == CreateUpdate.CREATE ? "Create" : "Update");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_save_host:
                CreateUpdateHostHandler();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void CreateUpdateHostHandler()
    {
        String hostTitle = String.valueOf(_editTextHostTitle.getText());
        String hostNameOrIp = String.valueOf(_editTextHostNameOrIp.getText());
        String portNumber = String.valueOf(_editTextPortNumber.getText());

        if (validateAddHostDialog())
        {
            Host host =_operationType == CreateUpdate.CREATE ? new Host() : _host;

            host.isActive = _checkBoxIsActive.isChecked();
            host.title = hostTitle;
            host.nameOrIp = hostNameOrIp;
            if (portNumber != null) host.portNumber = portNumber;
            host.notifyWhenPingFails = _checkBoxNotifyPingFails.isChecked();
            host.save();
            this.setResult(RESULT_OK);
            finish();
        }
    }

    private View.OnClickListener checkHostListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (PingHelper.isNetworkConnectionAvailable(v.getContext()))
            {
                final String hostNameOrIp = String.valueOf(_editTextHostNameOrIp.getText());
                final String portNumber = String.valueOf(_editTextPortNumber.getText());

                if (validateAddHostDialog())
                {
                    v.setEnabled(false);
                    _imageCheckStatus.setVisibility(View.GONE);
                    _progressSpinner.setVisibility(View.VISIBLE);

                    PingHostTask taskPingHost = new PingHostTask();
                    taskPingHost.setCallback(pingTaskCompletedCallback);
                    taskPingHost.execute(new HostNamePort(hostNameOrIp, portNumber));
                }
            }
            else
            {
                Toast.makeText(v.getContext(), getString(R.string.no_internet_connection_message), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private TaskCompletedBooleanCallback pingTaskCompletedCallback = new TaskCompletedBooleanCallback() {
        @Override
        public void taskCompletedBooleanCallback(boolean result)
        {
            int imageResourceId = result ? R.drawable.ok_green_32px : R.drawable.alert_red_32px;
            _imageCheckStatus.setImageResource(imageResourceId);
            _imageCheckStatus.setVisibility(View.VISIBLE);
            _progressSpinner.setVisibility(View.GONE);
            _buttonCheckHost.setEnabled(true);
        }
    };

    private boolean validateAddHostDialog()
    {
        String hostNameOrIp = String.valueOf(_editTextHostNameOrIp.getText());
        boolean isHostValid = hostNameOrIp != null && !hostNameOrIp.isEmpty();
        if (!isHostValid)
        {
            if(_editTextHostNameOrIp.requestFocus())
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            _editTextHostNameOrIp.setError(getString(R.string.host_name_or_ip_required_error_message));
        }
        return isHostValid;
    }
}
