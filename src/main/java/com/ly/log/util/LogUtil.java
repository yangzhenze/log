package com.ly.log.util;

import com.aliyun.openservices.log.Client;
import com.aliyun.openservices.log.common.QueriedLog;
import com.aliyun.openservices.log.exception.LogException;
import com.aliyun.openservices.log.request.GetHistogramsRequest;
import com.aliyun.openservices.log.response.GetHistogramsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * 获取日志工具类
 */
public class LogUtil {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(LogUtil.class);
    private String endpoint; // 选择与上面步骤创建 project 所属区域匹配的
    // Endpoint
    private String accessKeyId; // 使用您的阿里云访问密钥 AccessKeyId
    private String accessKeySecret; // 使用您的阿里云访问密钥
    // AccessKeySecret
    private String project; // 上面步骤创建的项目名称

    private String logStore; // 上面步骤创建的日志库名称
    private String logStoreFlag;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd");

   public LogUtil(String logStore, String logStoreFlag){
       Properties properties = FileUtil.loadPropertiesFile("config.properties");
       this.endpoint = (String) properties.get("endpoint");
       this.accessKeyId = (String) properties.get("accessKeyId");
       this.accessKeySecret = (String) properties.get("accessKeySecret");
       this.project = (String) properties.get("project");
       this.logStore = logStore;
       this.logStoreFlag = logStoreFlag;
   }

    int logLine = 100;//log_line 最大值为100，每次获取100行数据。若需要读取更多数据，请使用offset翻页。offset和lines只对关键字查询有效，若使用SQL查询，则无效。在SQL查询中返回更多数据，请使用limit语法。

    public List<QueriedLog> queriedLogs = null;

    // 查询日志分布情况(获取总行数)
    public GetHistogramsResponse getLogsRequest(int beginSecond, int endSecond){
        GetHistogramsResponse res3 = null;
        GetHistogramsRequest req3 = new GetHistogramsRequest(project, logStore, "", "", beginSecond, endSecond);// 构建一个客户端实例
        Client client = new Client(endpoint, accessKeyId, accessKeySecret);
        try {
            res3 = client.GetHistograms(req3);
        } catch (LogException e) {
            System.out.print("查询日志分布情况出错！");
            logger.error("查询日志分布情况出错！");
            res3 =  null;
        }
        if (res3 != null && res3.IsCompleted()){
            System.out.print("============================================logSize============================================"+res3.GetTotalCount());
            return res3;
        }
        return null;
    }

    // 查询日志数据（分页）
    public void getLogsPage(int beginSecond, int endSecond) throws Exception {
        long totalLogLines = getLogsRequest(beginSecond,endSecond).GetTotalCount();
        int logOffset = 0;
        queriedLogs = new ArrayList<>();
        System.out.print("============================================beforeSize============================================"+totalLogLines);

        int logTotalPage = (int) (totalLogLines%logLine == 0?totalLogLines/logLine:totalLogLines/logLine+1);

        CountDownLatch countDownLatch = new CountDownLatch(logTotalPage);

        String tableName = "log_"+logStoreFlag+"_"+simpleDateFormat.format(new Date());
        /*DBController dbController = new DBController();


        if(!dbController.tableExist(tableName)){
            if(!dbController.createTable(tableName)){
                logger.error("创建{}表失败！",tableName);
                throw new Exception();
            }
        }*/

        DataHubUtil dataHubUtil = new DataHubUtil();
        if(!dataHubUtil.existTopic(tableName)){
            dataHubUtil.createTopic(tableName);
            MaxComputeUtil maxCompute = new MaxComputeUtil();
            maxCompute.createTable(tableName);
            dataHubUtil.createDataConnector(maxCompute.getProjectName(),tableName);
        }


        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 5,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());

        while (logOffset <= totalLogLines) {
            ALiYun aLiYun = new ALiYun(endpoint,accessKeyId,accessKeySecret,project,logStore,beginSecond,endSecond,logOffset,logLine);
            //threadPoolExecutor.execute(new ThreadGetLogs(aLiYun,queriedLogs,countDownLatch,tableName));
            threadPoolExecutor.execute(new ThreadLogs(aLiYun,queriedLogs,countDownLatch,tableName));
            logOffset += logLine;
        }
        countDownLatch.await();
        threadPoolExecutor.shutdown();


    }

    /**
     * 时间戳转换成日期格式字符串
     * @param seconds 精确到秒的字符串
     * @param format
     * @return
     */
    public static String timeStamp2Date(Long seconds,String format) {
        if(format == null || format.isEmpty()){
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(seconds));
    }


    public static void main(String [] args) throws IOException {
        Logger logger = LoggerFactory.getLogger(LogUtil.class);

        Properties properties = FileUtil.loadPropertiesFile("config.properties");
        //间隔时间
        int intervalSecond = Integer.parseInt(properties.get("interval_second").toString());
        int countDown = intervalSecond;
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

                    /*System.out.println("============================================log============================================");
                    StringBuffer sb =new StringBuffer();
                    for(QueriedLog log:logUtil.queriedLogs ){
                        LogItem item = log.GetLogItem();
                        for(LogContent content : item.GetLogContents()){
                            System.out.print(content.GetKey()+":"+content.GetValue());
                            sb.append(content.GetKey()+":"+content.GetValue()+" \t");
                        }
                        sb.append("\n");
                        System.out.println();
                    }

                    File file = new File(filePath+"/logsss.txt");

                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(sb.toString().getBytes());
                    fileOutputStream.close();*/

                    System.out.println("============================================afterSize============================================"+logUtil.queriedLogs.size());
                    long endTime = System.currentTimeMillis();
                    logger.info("{}日志服务读写完成用时:{}秒",store,((endTime-beginTime)/1000));
                    System.out.println("用时:"+((endTime-beginTime)/1000)+"秒");
                    countDown = intervalSecond;
                }

                FileUtil.setLastTime(curTime,filePath,fileName);
                lastTime = curTime;
            }else{
                try {
                    System.out.println("请稍后..."+intervalSecond+"秒后执行");
                    Thread.sleep(intervalSecond);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }



        //LogUtil logUtil = new LogUtil();
        //System.out.println(logUtil.getLastTime());

        //logUtil.setLastTime(5000);


    }
}
