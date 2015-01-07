package com.trickbz.pingerful;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.trickbz.pingerful.tasks.PingHostListTask;
import com.trickbz.pingerful.tasks.VoidCallback;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends ActionBarActivity {

    private HostArrayAdapter _adapterListViewHosts;
    private ListView _listViewHosts;
    private ArrayList<Host> _hosts;
    private Handler _pingHandler = new Handler();
    private Runnable _pingHostHandlerRunnable = new Runnable() {
        @Override
        public void run()
        {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            boolean isPingAutomatically = prefs.getBoolean(getString(R.string.pref_ping_automatically), true);
            if (isPingAutomatically)
            {
                int pingIntervalMinutes = Integer.parseInt(prefs.getString(getString(R.string.pref_ping_interval), "2"));
                runPingAllHostsTask();
                _pingHandler.postDelayed(this, TimeUnit.MINUTES.toMillis(pingIntervalMinutes));
            }
        }
    };
    private Menu _optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _hosts = Host.all();

        // TODO - find a find to do the same from XML
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ping);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        _adapterListViewHosts = new HostArrayAdapter(this, R.layout.host_list_item, _hosts);
        _listViewHosts = (ListView) findViewById(R.id.list_hosts);
        _listViewHosts.setAdapter(_adapterListViewHosts);
        _listViewHosts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


            }
        });
        findViewById(R.id.button_ping_all).performClick();
        registerForContextMenu(_listViewHosts);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.OnSharedPreferenceChangeListener prefsChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
            {
                if (key.equals(getString(R.string.pref_ping_automatically)))
                {
                    boolean pingAutomatically = prefs.getBoolean(getString(R.string.pref_ping_automatically), true);
                    if (pingAutomatically)
                        _pingHandler.postDelayed(_pingHostHandlerRunnable, 0);
                    else
                        _pingHandler.removeCallbacks(_pingHostHandlerRunnable);
                }
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(prefsChangeListener);

        Boolean isPingAutomatically = prefs.getBoolean(getString(R.string.pref_ping_automatically), true);
        if (isPingAutomatically) _pingHandler.postDelayed(_pingHostHandlerRunnable, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        _optionsMenu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_settings:
                return true;
            case R.id.action_refresh_hosts:
                runPingAllHostsTask();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo contextMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Host host = _adapterListViewHosts.getItem(contextMenuInfo.position);
        menu.setHeaderTitle(String.format("Host '%s' options", host.getTitleIfNotEmptyOrName()));

        menu.add(1, host.getId().intValue(), HostContextMenuItem.UPDATE.value(), "Edit");
        menu.add(1, host.getId().intValue(), HostContextMenuItem.DELETE.value(), "Delete");
        menu.add(1, host.getId().intValue(), HostContextMenuItem.FORCE_PING.value(), "Force ping");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        final int listItemIndex = item.getItemId();
        final Host host = Host.getById(listItemIndex);
        final int order = item.getOrder();
        HostContextMenuItem selectedContextMenuItem = HostContextMenuItem.values()[order];

        switch (selectedContextMenuItem) {
            case UPDATE:
                LaunchAddHostDialog(host, CreateUpdate.UPDATE);
                break;
            case DELETE:
                LaunchDeleteHostDialog(host.getId());
                break;
            case FORCE_PING:
                break;
        }
        return true;
    }

    private void LaunchDeleteHostDialog(long hostId) {
        FragmentManager fragmentManager = getFragmentManager();
        DeleteHostDialog dialog = DeleteHostDialog.newInstance(hostId);
        dialog.setCallback(new VoidCallback() {
            @Override
            public void onActionFinished() { UpdateHostsList(); }
        });
        dialog.show(fragmentManager, getString(R.string.delete_host_dialog_tag));
    }

    private void LaunchAddHostDialog(Host host, CreateUpdate operationType)
    {
        FragmentManager fragmentManager = getFragmentManager();
        AddHostDialog dialog = AddHostDialog.newInstance(host, operationType);
        dialog.setCallback(new VoidCallback() {
            @Override
            public void onActionFinished() { UpdateHostsList(); }
        });
        dialog.show(fragmentManager, getString(R.string.add_host_dialog_fragment_tag));
    }

    public void onCreateHostButtonClick(View v) { LaunchAddHostDialog(new Host(), CreateUpdate.CREATE); }

    private void UpdateHostsList()
    {
        _hosts = Host.all();
        _adapterListViewHosts.clear();
        _adapterListViewHosts.addAll(_hosts);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _adapterListViewHosts.notifyDataSetChanged();
            }
        });
    }

    public void onActionBarSettingsClick(MenuItem item)
    {
        Intent i = new Intent(this, PreferencesActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    public void onPingAllButtonClick(View view)
    {
        runPingAllHostsTask();
    }

    private void runPingAllHostsTask()
    {
        setRefreshActionButtonState(true);
        PingHostListTask taskPingHostList = new PingHostListTask(this);
        final View pingAllButton = findViewById(R.id.button_ping_all);
        pingAllButton.setEnabled(false);

        taskPingHostList.setCallback(new VoidCallback() {
            @Override
            public void onActionFinished() {
                UpdateHostsList();
                setRefreshActionButtonState(false);
                pingAllButton.setEnabled(true);
            }
        });
        taskPingHostList.execute();
    }

    public void setRefreshActionButtonState(final boolean refreshing)
    {
        if (_optionsMenu != null)
        {
            final MenuItem refreshItem = _optionsMenu.findItem(R.id.action_refresh_hosts);
            if (refreshItem != null)
            {
                if (refreshing)
                {
                    refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
                    refreshItem.setVisible(true);
                }
                else
                {
                    refreshItem.setActionView(null);
                    refreshItem.setVisible(false);
                }
            }
        }
    }
}
