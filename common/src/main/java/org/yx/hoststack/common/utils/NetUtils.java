package org.yx.hoststack.common.utils;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.yx.lib.utils.util.StringUtil;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.util.Enumeration;

public class NetUtils {
    public static InetAddress localAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        return inetAddress;
                    }
                }
            }
        } catch (Exception e) {
            // 处理异常
        }
        return null;
    }

    public static String parseChannelRemoteName(final Channel channel) {
        if (null == channel) {
            return "";
        }
        final InetSocketAddress remote = (InetSocketAddress) channel.remoteAddress();
        if (remote != null) {
            return remote.getAddress().getHostName();
        }
        return "";
    }

    public static String parseChannelRemoteAddr(final Channel channel) {
        if (null == channel) {
            return "";
        }
        Attribute<String> realClientAddress = channel.attr(AttributeKey.valueOf("realClientAddress"));
        if (realClientAddress != null && StringUtil.isNotBlank(realClientAddress.get())) {
//            HAProxyMessage message = (HAProxyMessage) HA_PROXY_MESSAGE.get();
            return realClientAddress.get();
        }
        SocketAddress remote = channel.remoteAddress();
        final String addr = remote != null ? remote.toString() : "";
        if (addr.length() > 0) {
            int index = addr.lastIndexOf("/");
            if (index >= 0) {
                return addr.substring(index + 1);
            }
            return addr;
        }
        return "";
    }

    public static String parseRemoteAddr(final SocketAddress remoteAddress) {
        if (null == remoteAddress) {
            return "";
        }
        final String addr = remoteAddress != null ? remoteAddress.toString() : "";
        if (addr.length() > 0) {
            int index = addr.lastIndexOf("/");
            if (index >= 0) {
                return addr.substring(index + 1);
            }
            return addr;
        }
        return "";
    }
}

