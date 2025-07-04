package org.jeecg.common.util;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.constant.CommonConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * * IP地址工具类
 * 
 * @Author scott
 * @email jeecgos@163.com
 * @Date 2019年01月14日
 */
public class IpUtils {
    /**
     * * 创建一个日志记录器（Logger）实例，用于在 IpUtils 类中输出日志信息。
     */
    private static Logger logger = LoggerFactory.getLogger(IpUtils.class);

    /**
     * * 获取IP地址
     * 
     * * 注意：
     * * - 使用Nginx等反向代理软件， 则不能通过request.getRemoteAddr()获取IP地址。
     * * - 如果使用了多级反向代理的话，X-Forwarded-For的值并不止一个，
     * * - 而是一串IP地址，X-Forwarded-For中第一个非unknown的有效IP字符串，则为真实IP地址
     */
    public static String getIpAddr(HttpServletRequest request) {
        // * 初始化 IP
        String ip = null;

        try {
            // * 获取请求头 x-forwarded-for 数据
            ip = request.getHeader("x-forwarded-for");

            // * 如果 x-forwarded-for 为空，则读取 Proxy-Client-IP 头数据
            if (StringUtils.isEmpty(ip) || CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            // * 如果 Proxy-Client-IP 为空，则读取 WL-Proxy-Client-IP 头数据
            if (StringUtils.isEmpty(ip) || ip.length() == 0 || CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            // * 如果 WL-Proxy-Client-IP 为空，则读取 REMOTE_ADDR 头数据
            if (StringUtils.isEmpty(ip) || CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            // * 如果 HTTP_CLIENT_IP 为空，则读取 HTTP_X_FORWARDED_FOR 头数据
            if (StringUtils.isEmpty(ip) || CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            // * 如果都为空则从 request 中直接获取
            if (StringUtils.isEmpty(ip) || CommonConstant.UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        } catch (Exception e) {
            logger.error("IPUtils ERROR ", e);
        }

        // * 使用代理，则获取第一个IP地址
        if (StringUtils.isNotEmpty(ip) && ip.length() > 15) {
            // * 判断是否为多个ip
            if (ip.indexOf(",") > 0) {
                // * 截断为多个IP数组
                String[] ipAddresses = ip.split(",");
                for (String ipAddress : ipAddresses) {
                    ipAddress = ipAddress.trim();
                    // * 返回第一个和发的ip
                    if (isValidIpAddress(ipAddress)) {
                        return ipAddress;
                    }
                }
            }
        }

        // * 如果没使用代理，直接返回
        return ip;
    }

    /**
     * * 判断是否是IP格式
     * 
     * @param ipAddress
     * @return
     */
    public static boolean isValidIpAddress(String ipAddress) {
        String ipPattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        Pattern pattern = Pattern.compile(ipPattern);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }

    /**
     * * 获取服务器上的ip
     * 
     * @return
     */
    public static String getServerIp() {
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getLocalHost();
            String ipAddress = inetAddress.getHostAddress();
            return ipAddress;
        } catch (UnknownHostException e) {
            logger.error("获取ip地址失败", e);
        }
        return "";
    }
}
