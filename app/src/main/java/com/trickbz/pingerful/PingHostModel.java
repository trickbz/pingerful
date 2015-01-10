package com.trickbz.pingerful;

public class PingHostModel
{
    private String _nameOrIp;
    private String _port;
    private boolean _ignorePingCheck;

    public PingHostModel(String nameOrIp, String port, boolean ignorePingCheck)
    {
        _nameOrIp = nameOrIp;
        _port = port;
        _ignorePingCheck = ignorePingCheck;
    }

    public String get_nameOrIp() { return _nameOrIp; }
    public String get_port() { return _port; }
    public boolean is_ignorePingCheck() { return _ignorePingCheck; }
}
