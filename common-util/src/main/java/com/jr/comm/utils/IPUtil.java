package com.jr.comm.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IP工具类
 *
 * @author: Dao-yang.
 * @date: Created in 2025/2/28 17:50
 */
public class IPUtil {

    public static boolean isPrivateIP(String ip) {
        try {
            byte[] addressBytes = InetAddress.getByName(ip).getAddress();
            int firstOctet = (addressBytes[0] & 0xFF);
            if ((firstOctet == 10) || // 10.x.x.x
                (firstOctet == 172 && (addressBytes[1] & 0xFF) >= 16 && (addressBytes[1] & 0xFF) <= 31) || // 172.16.x.x to 172.31.x.x
                (firstOctet == 192 && addressBytes[1] == 168)) { // 192.168.x.x
                return true;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return false;
    }
}
