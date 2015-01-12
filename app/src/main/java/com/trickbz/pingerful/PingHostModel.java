package com.trickbz.pingerful;

public class PingHostModel
{
    private String _nameOrIp;
    private String _port;
    private boolean _checkPortOnly;
    private int _pingTimes;

    public PingHostModel(String nameOrIp, String port, boolean ignorePingCheck, int pingTimes)
    {
        _nameOrIp = nameOrIp;
        _port = port;
        _checkPortOnly = ignorePingCheck;
        _pingTimes = pingTimes;
    }

    public String get_nameOrIp() { return _nameOrIp; }
    public String get_port() { return _port; }
    public boolean is_checkPortOnly() { return _checkPortOnly; }
    public int get_pingTimes() { return _pingTimes; }
}
