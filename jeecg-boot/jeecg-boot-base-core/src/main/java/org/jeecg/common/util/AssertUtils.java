package org.jeecg.common.util;

import org.jeecg.common.exception.JeecgBootAssertException;

/**
 * * 断言检查工具
 * 
 * @author chenrui
 * @date 2017-06-22 10:05:56
 */
public class AssertUtils {

    /**
     * * 断言对象为空
     *
     * @param msg
     * @param obj
     * @throws JeecgBootAssertException
     * @author chenrui
     * @date 2017-06-22 10:05:56
     */
    public static void assertEmpty(String msg, Object obj) {
        if (oConvertUtils.isObjectNotEmpty(obj)) {
            throw new JeecgBootAssertException(msg);
        }
    }

    /**
     * * 断言对象不为空
     *
     * @param msg
     * @param obj
     * @throws JeecgBootAssertException
     * @author chenrui
     * @date 2017-06-22 10:05:56
     */
    public static void assertNotEmpty(String msg, Object obj) {
        if (oConvertUtils.isObjectEmpty(obj)) {
            throw new JeecgBootAssertException(msg);
        }
    }

    /**
     * * 断言对象相等
     *
     * @param message
     * @param expected
     * @param actual
     * @author chenrui
     * @date 2018/9/12 15:45
     */
    public static void assertEquals(String message, Object expected, Object actual) {
        if (oConvertUtils.isEqual(expected, actual)) {
            return;
        }
        throw new JeecgBootAssertException(message);
    }

    /**
     * * 断言对象不相等
     *
     * @param message
     * @param expected
     * @param actual
     * @author chenrui
     * @date 2018/9/12 15:45
     */
    public static void assertNotEquals(String message, Object expected, Object actual) {
        if (oConvertUtils.isEqual(expected, actual)) {
            throw new JeecgBootAssertException(message);
        }
    }

    /**
     * * 断言为同一个内存地址的对象
     *
     * @param message
     * @param expected
     * @param actual
     * @author chenrui
     * @date 2018/9/12 15:45
     */
    public static void assertSame(String message, Object expected, Object actual) {
        if (expected == actual) {
            return;
        }
        throw new JeecgBootAssertException(message);
    }

    /**
     * * 断言为不同一个内存地址的对象
     *
     * @param message
     * @param unexpected
     * @param actual
     * @author chenrui
     * @date 2018/9/12 15:45
     */
    public static void assertNotSame(String message, Object unexpected, Object actual) {
        if (unexpected == actual) {
            throw new JeecgBootAssertException(message);
        }
    }

    /**
     * * 断言为真
     *
     * @param message
     * @param condition
     */
    public static void assertTrue(String message, boolean condition) {
        if (!condition) {
            throw new JeecgBootAssertException(message);
        }
    }

    /**
     * * 断言为假
     *
     * @param message
     * @param condition
     */
    public static void assertFalse(String message, boolean condition) {
        assertTrue(message, !condition);
    }

    /**
     * * 断言存在
     *
     * @param message
     * @param obj
     * @param objs
     * @param <T>
     * @throws JeecgBootAssertException
     * @author chenrui
     * @date 2018/1/31 22:14
     */
    public static <T> void assertIn(String message, T obj, T... objs) {
        assertNotEmpty(message, obj);
        assertNotEmpty(message, objs);
        if (!oConvertUtils.isIn(obj, objs)) {
            throw new JeecgBootAssertException(message);
        }
    }

    /**
     * * 断言不存在
     *
     * @param message
     * @param obj
     * @param objs
     * @param <T>
     * @throws JeecgBootAssertException
     * @author chenrui
     * @date 2018/1/31 22:14
     */
    public static <T> void assertNotIn(String message, T obj, T... objs) {
        assertNotEmpty(message, obj);
        assertNotEmpty(message, objs);
        if (oConvertUtils.isIn(obj, objs)) {
            throw new JeecgBootAssertException(message);
        }
    }

    /**
     * * 断言 src 大于 des
     *
     * @param message
     * @param src
     * @param des
     * @author chenrui
     * @date 2018/9/19 15:30
     */
    public static void assertGt(String message, Number src, Number des) {
        if (oConvertUtils.isGt(src, des)) {
            return;
        }
        throw new JeecgBootAssertException(message);
    }

    /**
     * * 断言 src 大于等于 des
     *
     * @param message
     * @param src
     * @param des
     * @author chenrui
     * @date 2018/9/19 15:30
     */
    public static void assertGe(String message, Number src, Number des) {
        if (oConvertUtils.isGe(src, des)) {
            return;
        }
        throw new JeecgBootAssertException(message);
    }

    /**
     * * 断言 src 小于 des
     *
     * @param message
     * @param src
     * @param des
     * @author chenrui
     * @date 2018/9/19 15:30
     */
    public static void assertLt(String message, Number src, Number des) {
        if (oConvertUtils.isGe(src, des)) {
            throw new JeecgBootAssertException(message);
        }
    }

    /**
     * * 断言 src 小于等于 des
     *
     * @param message
     * @param src
     * @param des
     * @author chenrui
     * @date 2018/9/19 15:30
     */
    public static void assertLe(String message, Number src, Number des) {
        if (oConvertUtils.isGt(src, des)) {
            throw new JeecgBootAssertException(message);
        }
    }

}
