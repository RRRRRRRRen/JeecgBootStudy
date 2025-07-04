package org.jeecg.common.util;

import lombok.extern.slf4j.Slf4j;

/**
 * * 是 Java 中线程安全的高性能 哈希表实现，用于在并发环境下安全地进行读写操作，是多线程编程中非常常用的数据结构。
 */
import java.util.concurrent.ConcurrentHashMap;

/**
 * * 防止刷短信接口（只针对绑定手机号模板：SMS_175430166）
 * 
 * * 1、同一IP，1分钟内发短信不允许超过5次（每一分钟重置每个IP请求次数）
 * * 2、同一IP，1分钟内发短信超过20次，进入黑名单，不让使用短信接口
 * 
 * * 短信接口加签和时间戳
 * * 涉及接口：
 * * - /sys/sms
 * * - /desform/api/sendVerifyCode
 * * - /sys/sendChangePwdSms
 */
@Slf4j
public class DySmsLimit {

    // * 1分钟内最大发短信数量（单一IP）
    private static final int MAX_MESSAGE_PER_MINUTE = 5;
    // * 1分钟的毫秒数
    private static final int MILLIS_PER_MINUTE = 60000;
    // * 一分钟内报警线最大短信数量，超了进黑名单（单一IP）
    private static final int MAX_TOTAL_MESSAGE_PER_MINUTE = 20;

    /**
     * * 存放IP，最后一次请求时间
     */
    private static ConcurrentHashMap<String, Long> ipLastRequestTime = new ConcurrentHashMap<>();
    /**
     * * 存放IP，请求次数
     */
    private static ConcurrentHashMap<String, Integer> ipRequestCount = new ConcurrentHashMap<>();
    /**
     * * 存放IP，是否黑名单
     */
    private static ConcurrentHashMap<String, Boolean> ipBlacklist = new ConcurrentHashMap<>();

    /**
     * * 是否发送短信的校验
     * 
     * @param ip 请求发短信的IP地址
     * @return
     */
    public static boolean canSendSms(String ip) {
        // * 当前时间戳
        long currentTime = System.currentTimeMillis();
        // * 该ip上次请求时间
        long lastRequestTime = ipLastRequestTime.getOrDefault(ip, 0L);
        // * 该ip的请求次数
        int requestCount = ipRequestCount.getOrDefault(ip, 0);
        log.info("IP：{}, Msg requestCount：{} ", ip, requestCount);

        // * 如果IP在黑名单中，则禁止发送短信
        if (ipBlacklist.getOrDefault(ip, false)) {
            log.error("IP：{}, 进入黑名单，禁止发送请求短信！", ip);
            return false;
        }

        if (currentTime - lastRequestTime >= MILLIS_PER_MINUTE) {
            // * 如果距离上次请求已经超过一分钟，则重置计数
            ipRequestCount.put(ip, 1);
            ipLastRequestTime.put(ip, currentTime);
            return true;
        } else {
            // * 如果距离上次请求不到一分钟
            // * 增加一次计数
            ipRequestCount.put(ip, requestCount + 1);
            if (requestCount < MAX_MESSAGE_PER_MINUTE) {
                // * 如果请求次数小于5次，允许发送短信
                return true;
            } else if (requestCount >= MAX_TOTAL_MESSAGE_PER_MINUTE) {
                // * 如果请求次数超过报警线短信数量，将IP加入黑名单
                ipBlacklist.put(ip, true);
                return false;
            } else {
                // * 超过五次不允许发送
                log.error("IP：{}, 1分钟内请求短信超过5次，请稍后重试！", ip);
                return false;
            }
        }
    }

    /**
     * * 验证成功之后清空数量
     * 
     * @param ip IP地址
     */
    public static void clearSendSmsCount(String ip) {
        long currentTime = System.currentTimeMillis();
        ipRequestCount.put(ip, 0);
        ipLastRequestTime.put(ip, currentTime);
    }
}
