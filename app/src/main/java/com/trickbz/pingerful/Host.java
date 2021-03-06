package com.trickbz.pingerful;

import android.support.annotation.Nullable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

@Table(name = "Hosts")
public class Host extends Model implements Serializable {

    @Column(name = "Title")
    public String title;

    @Column(name = "NameOrIp")
    public String nameOrIp;

    @Column(name = "isActive")
    public boolean isActive;

    @Column(name = "LastCheckedDate")
    public Date lastCheckedDate;

    @Column(name = "LastOnlineDate")
    public Date lastOnlineDate;

    @Column(name = "IsOnline")
    public boolean isOnline;

    @Column(name = "Position")
    public int position;

    @Column(name = "NotifyPingFails")
    public boolean notifyWhenPingFails;

    @Column(name = "Port")
    public String portNumber;

    @Column(name = "CheckPortOnly")
    public boolean checkPortOnly;

    @Column(name = "ShowTitleOnly")
    public boolean showTitleOnly;

    public Host() {
        super();
    }

    public Host(Host another)
    {
        this.title = another.title;
        this.nameOrIp = another.nameOrIp;
        this.isActive = another.isActive;
        this.lastCheckedDate = null;
        this.lastOnlineDate = null;
        this.isOnline = false;
        // /this.position = another.position ?
        this.notifyWhenPingFails = another.notifyWhenPingFails;
        this.portNumber = another.portNumber;
        this.checkPortOnly = another.checkPortOnly;
        this.showTitleOnly = another.showTitleOnly;
    }

    public static ArrayList<Host> all()
    {
        ArrayList<Host> hosts = new Select().from(Host.class).execute();
        return  hosts;
    }

    public static Host Duplicate(Host hostToDuplicate)
    {
        Host copy = new Host(hostToDuplicate);
        copy.save();
        return copy;
    }

    public static Host getById(long hostId)
    {
        Host host = new Select().from(Host.class).where("id = ?", hostId).executeSingle();
        return host;
    }

    public static void deleteHost(long hostId)
    {
        new Delete().from(Host.class).where("Id = ?", hostId).execute();
    }

    public String getTitleIfNotEmptyOrName() {
        return title == null || title.isEmpty() ?
                nameOrIp :
                title;
    }
}
