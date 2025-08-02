package org.jeecg.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.List;

/**
 * * 写入 导入错误日志
 * 
 * @Description: PmsUtil
 * @author: jeecg-boot
 */
@Slf4j
@Component
public class PmsUtil {

    private static String uploadPath;

    /**
     * * 使用 setter 自动注入配置属性
     * 
     * @param uploadPath
     */
    @Value("${jeecg.path.upload:}")
    public void setUploadPath(String uploadPath) {
        PmsUtil.uploadPath = uploadPath;
    }

    public static String saveErrorTxtByList(List<String> msg, String name) {
        // * 定义目录
        Date d = new Date();
        String saveDir = "logs" + File.separator + DateUtils.yyyyMMdd.get().format(d) + File.separator;
        String saveFullDir = uploadPath + File.separator + saveDir;

        // * 创建保存目录
        File saveFile = new File(saveFullDir);
        if (!saveFile.exists()) {
            saveFile.mkdirs();
        }

        // * 定义具体名称
        name += DateUtils.yyyymmddhhmmss.get().format(d) + Math.round(Math.random() * 10000);
        String saveFilePath = saveFullDir + name + ".txt";

        try {
            // * 封装目的地
            BufferedWriter bw = new BufferedWriter(new FileWriter(saveFilePath));
            for (String s : msg) {
                // * 写数据
                if (s.indexOf("_") > 0) {
                    String[] arr = s.split("_");
                    bw.write("第" + arr[0] + "行:" + arr[1]);
                } else {
                    bw.write(s);
                }
                bw.write("\r\n");
            }
            bw.flush();
            bw.close();
        } catch (Exception e) {
            log.info("excel导入生成错误日志文件异常:" + e.getMessage());
        }
        return saveDir + name + ".txt";
    }

}
