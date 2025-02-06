package org.yx.hoststack.center.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yx.lib.utils.logger.KvLogger;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import static org.yx.lib.utils.logger.LogFieldConstants.*;

@Component
@Slf4j
public class LocalHostUtil {
    private final Set<String> localIpAddresses;

    public LocalHostUtil() {
        this.localIpAddresses = initLocalIpAddresses();
    }

    private Set<String> initLocalIpAddresses() {
        Set<String> addresses = new HashSet<>();
        try {
            // Gets all network interfaces
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress addr = inetAddresses.nextElement();
                    if (!addr.isLoopbackAddress()) {
                        addresses.add(addr.getHostAddress());
                    }
                }
            }
            // Add localhost
            addresses.add("127.0.0.1");
            addresses.add("localhost");
        } catch (SocketException e) {
            KvLogger.instance(this)
                    .p(EVENT, "LocalHostUtil")
                    .p(ACTION, "initLocalIpAddresses")
                    .p(ERR_MSG, "Failed to get local IP addresses")
                    .p(ERR_MSG, e.getMessage())
                    .p(Alarm, 0)
                    .e(e);
        }
        return Collections.unmodifiableSet(addresses);
    }

    public boolean isLocalAddress(String address) {
        return localIpAddresses.contains(address);
    }
}