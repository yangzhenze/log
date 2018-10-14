package com.ly.log.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class FileUtil {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(FileUtil.class);

    /**
     * 获取最后读取的时间
     * @return
     */
    public static int getLastTime(String path,String fileName)  {

        File file = new File(path+"/"+fileName);

        int lastTime = 0;

        if(file.exists()){
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(file.getPath());
                byte[] bbuf = new byte[1024];
                int hasRead = 0;

                while((hasRead = fileInputStream.read(bbuf)) >0){
                    lastTime = Integer.parseInt(new String(bbuf,0,hasRead));
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("文件读取错误！");
            }finally {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return lastTime;
    }

    /**
     * 记录最后读取的时间
     * @param lastTime
     */
    public static void setLastTime(int lastTime,String path,String fileName) {
        File file = new File(path);

        if(!file.exists()){
            file.mkdirs();
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file.getPath()+"/"+fileName);
            fileOutputStream.write(String.valueOf(lastTime).getBytes());
        } catch (IOException e) {
            logger.error("写入失败");
            e.printStackTrace();
        }finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param fullFile 配置文件名
     * @return Properties对象
     */
    public static Properties loadPropertiesFile(String fullFile) {
        if (null == fullFile || fullFile.equals("")){
            throw new IllegalArgumentException("Properties file path can not be null" + fullFile);
        }
        InputStream inputStream = null;
        Properties p =null;
        try {
            inputStream = FileUtil.class.getClassLoader().getResourceAsStream(fullFile);
            p = new Properties();
            p.load(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != inputStream){
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return p;
    }


    public static Properties loadPropertiesFileByPath(String fullFile) {
        if (null == fullFile || fullFile.equals("")){
            throw new IllegalArgumentException("Properties file path can not be null" + fullFile);
        }
        InputStream inputStream = null;
        Properties p =null;
        try {
            inputStream = new FileInputStream(new File(fullFile));
            p = new Properties();
            p.load(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != inputStream){
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return p;
    }


    public static void main(String [] args){
    }
}
