package com.trickbz.pingerful.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public final class PingHelper {

    private PingHelper() { }

    public static boolean isNetworkConnectionAvailable(Context context)
    {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public static boolean PingPort(String hostNameOrIp, int port,  int timeoutMs) {

        boolean isAvailable = false;
        Socket sock = new Socket();

        try {

            InetAddress ip = inetAddressByHostName(hostNameOrIp);
            if (ip != null) {
                SocketAddress socketAddress = new InetSocketAddress(ip, port);
                // This method will block no more than timeoutMs.
                // If the timeout occurs, SocketTimeoutException is thrown.
                sock.connect(socketAddress, timeoutMs);
                isAvailable = true;
                sock.close();
            }

        } catch(UnknownHostException e) {

        } catch (SocketTimeoutException e) {

        } catch (IOException e) {

        }

        return isAvailable;
    }

    public static InetAddress inetAddressByHostName(String hostnameOrIp) {

        InetAddress inetAddress = null;
        try
        {
            inetAddress = InetAddress.getByName(hostnameOrIp);
        }
        catch (UnknownHostException e) { }

        return inetAddress;
    }

    public static boolean PingHost(String ipOrHostName) {

        boolean isPingPassed = false;
        InetAddress inetAddress = inetAddressByHostName(ipOrHostName);

        if (inetAddress != null)
        {
            Runtime runtime = Runtime.getRuntime();
            try {
                String ipString = inetAddress.getHostAddress();
                String prefix = "/system/bin/ping -c 1 ";
                Process processPing = runtime.exec(prefix + ipString);
                int pingProcessExitCode = processPing.waitFor();
                if (pingProcessExitCode == 0)
                    isPingPassed = true;

            }
            catch (InterruptedException  e) { }
            catch (IOException e) { }
        }

        return isPingPassed;
    }
}
