package com.trickbz.pingerful;

public class HostNamePort
{
    private String _nameOrIp;
    private String _port;

    HostNamePort(String nameOrIp, String port)
    {
        _nameOrIp = nameOrIp;
        _port = port;
    }

    public String get_nameOrIp() { return _nameOrIp; }
    public String get_port() { return _port; }
}
