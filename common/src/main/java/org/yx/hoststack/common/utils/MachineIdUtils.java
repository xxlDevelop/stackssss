package org.yx.hoststack.common.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class MachineIdUtils {

    /**
     * 获取机器码.
     * @return
     */
    public static String getUniqueMachineId() {
        StringBuilder uniqueId = new StringBuilder();

        try {
            // 获取本机的IP地址
            InetAddress localHost = InetAddress.getLocalHost();
            uniqueId.append(localHost.getHostAddress()).append("_");

            // 获取网络接口并获取MAC地址
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    for (int i = 0; i < mac.length; i++) {
                        uniqueId.append(String.format("%02X", mac[i]));
                        if (i < mac.length - 1) {
                            uniqueId.append("-");
                        }
                    }
                    uniqueId.append("_");
                }
            }

            // 添加系统信息作为补充
            String osName = System.getProperty("os.name");
            String osVersion = System.getProperty("os.version");
            String userName = System.getProperty("user.name");
            uniqueId.append(osName).append("_").append(osVersion).append("_").append(userName);
        } catch (Exception ignored) {
        }

        return uniqueId.toString();
    }
}
