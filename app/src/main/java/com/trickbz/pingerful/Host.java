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

    public Host() {
        super();
    }

    public static ArrayList<Host> all()
    {
        ArrayList<Host> hosts = new Select().from(Host.class).execute();
        return  hosts;
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
