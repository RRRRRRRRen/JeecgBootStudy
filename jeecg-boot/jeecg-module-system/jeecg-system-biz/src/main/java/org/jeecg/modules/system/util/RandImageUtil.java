package org.jeecg.modules.system.util;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

/**
 * * 登录验证码工具类
 * 
 * @author: jeecg-boot
 */
public class RandImageUtil {
    /**
     * * 静态标志符
     */
    public static final String KEY = "JEECG_LOGIN_KEY";

    /**
     * * 定义图形大小
     */
    private static final int WIDTH = 105;
    /**
     * * 定义图形大小
     */
    private static final int HEIGHT = 35;

    /**
     * * 定义干扰线数量
     */
    private static final int COUNT = 200;
    /**
     * * 干扰线的长度=1.414*lineWidth
     */
    private static final int LINE_WIDTH = 2;

    /**
     * * 图片格式
     */
    private static final String IMG_FORMAT = "JPEG";

    /**
     * * base64 图片前缀
     */
    private static final String BASE64_PRE = "data:image/jpg;base64,";

    /**
     * * 直接通过response 返回图片
     * 
     * @param response
     * @param resultCode
     * @throws IOException
     */
    public static void generate(HttpServletResponse response, String resultCode) throws IOException {
        // * 创建图片
        BufferedImage image = getImageBuffer(resultCode);
        // * 输出图象到页面
        ImageIO.write(image, IMG_FORMAT, response.getOutputStream());
    }

    /**
     * * 生成base64字符串
     * 
     * @param resultCode
     * @return
     * @throws IOException
     */
    public static String generate(String resultCode) throws IOException {
        // * 创建图片
        BufferedImage image = getImageBuffer(resultCode);

        // * 初始化流
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        // * 写入流中
        ImageIO.write(image, IMG_FORMAT, byteStream);
        // * 转换成字节
        byte[] bytes = byteStream.toByteArray();
        // * 转换成base64串
        String base64 = Base64.getEncoder().encodeToString(bytes).trim();
        // * 删除 换行符 \r\n
        base64 = base64.replaceAll("\n", "").replaceAll("\r", "");
        // * 返回
        return BASE64_PRE + base64;
    }

    /**
     * * 生成图片文件
     *
     * @param resultCode
     * @return
     */
    private static BufferedImage getImageBuffer(String resultCode) {
        // * 在内存中创建图象
        final BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        // * 获取图形上下文
        final Graphics2D graphics = (Graphics2D) image.getGraphics();

        // * 设定背景颜色
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, WIDTH, HEIGHT);

        // * 设定边框颜色
        graphics.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);

        // * SHA1PRNG是-种常用的随机数生成算法,处理弱随机数问题
        SecureRandom random;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            random = new SecureRandom();
        }

        // * 随机产生干扰线，使图象中的认证码不易被其它程序探测到
        for (int i = 0; i < COUNT; i++) {
            graphics.setColor(getRandColor(150, 200));
            final int x = random.nextInt(WIDTH - LINE_WIDTH - 1) + 1;
            final int y = random.nextInt(HEIGHT - LINE_WIDTH - 1) + 1;
            final int xl = random.nextInt(LINE_WIDTH);
            final int yl = random.nextInt(LINE_WIDTH);
            graphics.drawLine(x, y, x + xl, y + yl);
        }
        // * 取随机产生的认证码
        for (int i = 0; i < resultCode.length(); i++) {
            graphics.setColor(Color.BLACK);
            graphics.setFont(new Font("Times New Roman", Font.BOLD, 24));
            graphics.drawString(String.valueOf(resultCode.charAt(i)), (23 * i) + 8, 26);
        }
        // * 图象生效
        graphics.dispose();
        return image;
    }

    /**
     * * 取得给定范围随机颜色
     * 
     * @param fc
     * @param bc
     * @return
     */
    private static Color getRandColor(int fc, int bc) { // 
        final Random random = new Random();
        int length = 255;
        if (fc > length) {
            fc = length;
        }
        if (bc > length) {
            bc = length;
        }

        final int r = fc + random.nextInt(bc - fc);
        final int g = fc + random.nextInt(bc - fc);
        final int b = fc + random.nextInt(bc - fc);

        return new Color(r, g, b);
    }
}
