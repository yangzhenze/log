package com.ly.log;

import com.ly.log.util.FileUtil;
import com.ly.log.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;

/**
 * @author zzy
 * @Date 2018/10/14 10:20 AM
 */
public class Application {

    public static void main(String [] args) throws IOException {
        Logger logger = LoggerFactory.getLogger(LogUtil.class);

        Properties properties = FileUtil.loadPropertiesFile("config.properties");
        //间隔时间
        int intervalSecond = Integer.parseInt(properties.get("interval_second").toString());
        String filePath = (String) properties.get("projectFilePath");
        String fileName = (String) properties.get("lastTimeFilName");
        String logStoreConfig = (String) properties.get("logStoreConfig");

        int lastTime = FileUtil.getLastTime(filePath,fileName);

        while(true){
            int curTime = (int)(System.currentTimeMillis()/1000);

            if(curTime - lastTime >= intervalSecond){

                //库表配置
                Properties storeConfig = FileUtil.loadPropertiesFileByPath(filePath+"/"+logStoreConfig);

                Object [] keys = storeConfig.keySet().toArray();

                for(Object store:keys){
                    long beginTime = System.currentTimeMillis();
                    LogUtil logUtil = new LogUtil((String)store,(String) storeConfig.get(store));
                    logger.info("准备读写{}日志服务",store);
                    try {
                        logUtil.getLogsPage(lastTime,curTime);
                    } catch (InterruptedException e) {
                        System.out.print("失败");
                        logger.error("请求失败");
                    } catch (Exception e){
                        System.out.print("失败");
                    }

                    System.out.println("============================================afterSize============================================"+logUtil.queriedLogs.size());
                    long endTime = System.currentTimeMillis();
                    logger.info("日志服务:{} 时间段:{} 读写完成用时:{}秒",store,LogUtil.timeStamp2Date(System.currentTimeMillis(),null),((endTime-beginTime)/1000));
                }

                FileUtil.setLastTime(curTime,filePath,fileName);
                lastTime = curTime;
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }


    }
}
