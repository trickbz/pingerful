package com.trickbz.pingerful;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.trickbz.pingerful.helpers.GeneralHelper;

import java.util.ArrayList;
import java.util.Date;

public class HostArrayAdapter extends ArrayAdapter<Host> {

    private Context _context;
    private ArrayList<Host> _hosts;
    private int _layoutResourceId;

    public HostArrayAdapter(Context context, int resource, ArrayList<Host> hosts)
    {
        super(context, resource, hosts);
        _context = context;
        _hosts = hosts;
        _layoutResourceId = resource;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        final Host host = _hosts.get(position);
        if (convertView == null)
        {
            LayoutInflater inflater = ((Activity) _context).getLayoutInflater();
            convertView = inflater.inflate(_layoutResourceId, parent, false);
        }

        TextView textViewHostTitle = (TextView) convertView.findViewById(R.id.host_title_list_host_item);
        String portFormatted = String.format(host.checkPortOnly ? ":<font color='silver'>%s</font>" : ":%s", host.portNumber);
        String portPart = host.portNumber == null || host.portNumber.isEmpty() ? "" : portFormatted;
        String hostNameWithPortPart = String.format("%s%s", host.nameOrIp, portPart);

        String hostTitle = hostNameWithPortPart;
        if (host.title != null && !host.title.isEmpty())
        {
            hostTitle = host.showTitleOnly ?
                host.title :
                String.format("%s (%s)", host.title, hostNameWithPortPart);
        }
        textViewHostTitle.setText(Html.fromHtml(hostTitle));

        TextView tvLastChecked = (TextView) convertView.findViewById(R.id.last_checked_list_host_item);
        TextView tvLastOnline = (TextView) convertView.findViewById(R.id.last_online_list_host_item);

        String lastCheckedMessage= host.lastCheckedDate == null ? "Never" : GeneralHelper.toShortDateTimeFormat(host.lastCheckedDate);
        String lastOnlineMessage = host.lastOnlineDate  == null ? "Never" : GeneralHelper.toShortDateTimeFormat(host.lastOnlineDate);

        tvLastChecked.setText(lastCheckedMessage);
        tvLastOnline.setText(lastOnlineMessage);

        ImageView statusIcon = (ImageView) convertView.findViewById(R.id.status_icon_list_host_item);
        int imageResourceId = !host.isActive ?
                R.drawable.pause_blue_32px :
                host.isOnline ?
                        R.drawable.ok_green_32px :
                        R.drawable.alert_red_32px;
        statusIcon.setImageResource(imageResourceId);

        ImageView useNotificationsIcon = (ImageView) convertView.findViewById(R.id.use_notification_icon_list_host_item);
        imageResourceId = host.notifyWhenPingFails ?
                R.drawable.bulb_yellow_32px :
                R.drawable.bulb_grey_32px;
        useNotificationsIcon.setImageResource(imageResourceId);

        ToggleButton checkBoxSkip = (ToggleButton) convertView.findViewById(R.id.skip_check_list_host_item);
        checkBoxSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
            ToggleButton checkBox = (ToggleButton) v;
            boolean currentCheckedState = checkBox.isChecked();
            host.isActive = currentCheckedState;
            host.save();
            replaceAt(position, host);
            }
        });
        checkBoxSkip.setChecked(host.isActive);

        return convertView;
    }

    public void replaceAt(int position, Host replacingHost)
    {
        Host obsoleteHost = this.getItem(position);
        this.remove(obsoleteHost);
        this.insert(replacingHost, position);
    }
}
