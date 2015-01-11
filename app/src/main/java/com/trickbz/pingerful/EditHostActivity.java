package com.trickbz.pingerful;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.trickbz.pingerful.helpers.PingHelper;
import com.trickbz.pingerful.tasks.IpAddressByHostNameTask;
import com.trickbz.pingerful.tasks.PingHostTask;
import com.trickbz.pingerful.tasks.StringCallback;
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
    private TextView _textViewHostIp;
    private CheckBox _checkBoxCheckPortOnly;
    private CheckBox _checkBoxShowTitleOnly;

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
        setContentView(R.layout.activity_edit_host);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        _operationType = (CreateUpdate) getIntent().getSerializableExtra(CREATE_OR_UPDATE_EXTRAS_KEY);
        _hostId = getIntent().getLongExtra(HOST_ID_EXTRAS_KEY, -1);
        _host = _operationType == CreateUpdate.CREATE ? new Host() : Host.getById(_hostId);

        String dialogTitle = _operationType == CreateUpdate.CREATE ? "Create Host" : "Update Host";
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
        _textViewHostIp = (TextView) findViewById(R.id.text_view_ip_edit_host);
        _checkBoxCheckPortOnly = (CheckBox) findViewById(R.id.checkbox_check_port_only);
        _checkBoxShowTitleOnly = (CheckBox) findViewById(R.id.checkbox_show_title_only_add_host);

        // setting initial data of controls
        _editTextHostTitle.setText(_host.title);
        _editTextHostTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                _checkBoxShowTitleOnly.setEnabled(s.length() > 0);
                _checkBoxShowTitleOnly.setChecked(s.length() > 0);
            }
        });

        _editTextHostNameOrIp.setText(_host.nameOrIp);

        _editTextPortNumber.setFilters(new InputFilter[]{new InputFilterMinMax(0, 65535)});
        _editTextPortNumber.setText(_host.portNumber);
        _editTextPortNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                _checkBoxCheckPortOnly.setEnabled(s.length() > 0);
                _checkBoxCheckPortOnly.setChecked(s.length() > 0);
            }
        });

        _checkBoxIsActive.setChecked(_operationType == CreateUpdate.CREATE || _host.isActive);
        _checkBoxNotifyPingFails.setChecked(_operationType != CreateUpdate.CREATE && _host.notifyWhenPingFails);
        _buttonCheckHost.setOnClickListener(checkHostListener);

        _checkBoxCheckPortOnly.setChecked(_host.checkPortOnly);
        _checkBoxCheckPortOnly.setEnabled(_host.portNumber != null && !_host.portNumber.isEmpty());

        _checkBoxShowTitleOnly.setChecked(_host.showTitleOnly);
        _checkBoxShowTitleOnly.setEnabled(_host.title != null && !_host.title.isEmpty());
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
            host.checkPortOnly = _checkBoxCheckPortOnly.isChecked();
            host.showTitleOnly = _checkBoxShowTitleOnly.isChecked();
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
                final boolean isCheckPortOnly = _checkBoxCheckPortOnly.isChecked();

                if (validateAddHostDialog())
                {
                    v.setEnabled(false);
                    _imageCheckStatus.setVisibility(View.GONE);
                    _progressSpinner.setVisibility(View.VISIBLE);

                    PingHostTask taskPingHost = new PingHostTask();
                    taskPingHost.setCallback(pingTaskCompletedCallback);
                    taskPingHost.execute(new PingHostModel(hostNameOrIp, portNumber, isCheckPortOnly));

                    IpAddressByHostNameTask ipByHostNameTask = new IpAddressByHostNameTask();
                    ipByHostNameTask.setCallback(new StringCallback() {
                        @Override
                        public void onActionFinished(String stringResult) {
                        _textViewHostIp.setText(stringResult);
                        }
                    });
                    ipByHostNameTask.execute(hostNameOrIp);
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

    public void onIpAddressTextViewClick(View view)
    {
        String ipAddress = _textViewHostIp.getText().toString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Host IP address", ipAddress);
        clipboard.setPrimaryClip(clipData);
        Toast.makeText(this, "IP copied to clipboard", Toast.LENGTH_SHORT).show();
    }
}
