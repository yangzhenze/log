package com.ly.log.util;

import com.aliyun.datahub.DatahubClient;
import com.aliyun.datahub.DatahubConfiguration;
import com.aliyun.datahub.auth.AliyunAccount;
import com.aliyun.datahub.common.data.Field;
import com.aliyun.datahub.common.data.FieldType;
import com.aliyun.datahub.common.data.RecordSchema;
import com.aliyun.datahub.common.data.RecordType;
import com.aliyun.datahub.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;
import java.util.*;

public class DataHubUtil {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(DataHubUtil.class);

    private DatahubClient client;
    private String projectName;
    private String accessKeyId;
    private String accessKeySecret;


    public DataHubUtil(){
        Properties properties = FileUtil.loadPropertiesFile("config.properties");
        String endpoint = (String) properties.get("datahub_endpoint");
        this.accessKeyId = (String) properties.get("accessKeyId");
        this.accessKeySecret = (String) properties.get("accessKeySecret");
        this.projectName = (String) properties.get("datahub_project");
        AliyunAccount account = new AliyunAccount(accessKeyId, accessKeySecret);
        DatahubConfiguration conf = new DatahubConfiguration(account,endpoint);
        this.client = new DatahubClient(conf);

    }

    //创建主题
    public void createTopic(String topicName){
        RecordSchema recordSchema = this.getSchema();
        int shardCount = 5;
        int lifeCycle = 3;
        String topicDesc = "记录"+topicName+"日志";
        client.createTopic(projectName,topicName,shardCount,lifeCycle, RecordType.TUPLE,recordSchema, topicDesc);
        //等待服务端通道打开
        client.waitForShardReady(this.projectName,topicName);
    }

    // 获取字段
    public RecordSchema getSchema() {
        RecordSchema recordSchema = new RecordSchema();
        recordSchema.addField(new Field("program_type", FieldType.STRING));
        recordSchema.addField(new Field("log_type", FieldType.STRING));
        recordSchema.addField(new Field("event_type", FieldType.STRING));
        recordSchema.addField(new Field("logname", FieldType.STRING));
        recordSchema.addField(new Field("roleid", FieldType.STRING));
        recordSchema.addField(new Field("rolename", FieldType.STRING));
        recordSchema.addField(new Field("userid", FieldType.STRING));
        recordSchema.addField(new Field("account", FieldType.STRING));
        recordSchema.addField(new Field("serverid", FieldType.STRING));
        recordSchema.addField(new Field("logtime", FieldType.TIMESTAMP));
        for(int i =1; i < 20; i++) {
            recordSchema.addField(new Field("params"+i, FieldType.STRING));
        }
        return recordSchema;
    }

    // 主题是否存在
    public boolean existTopic(String topicName){
        try{
            client.getTopic(this.projectName,topicName);
        }catch (Exception e){
            return false;
        }
        return true;
    }

    public void createDataConnector (String maxProject,String tableName) {
        // Create SinkOdps DataConnector
        // ODPS相关配置设置
        String odpsProject = maxProject;
        String odpsTable = tableName;
        String odpsEndpoint = "http://service-all.ext.odps.aliyun-inc.com/api";
        String tunnelEndpoint = "http://dt-all.ext.odps.aliyun-inc.com";
        OdpsDesc odpsDesc = new OdpsDesc();
        odpsDesc.setProject(odpsProject);
        odpsDesc.setTable(odpsTable);
        odpsDesc.setOdpsEndpoint(odpsEndpoint);
        odpsDesc.setTunnelEndpoint(tunnelEndpoint);
        odpsDesc.setAccessId(this.accessKeyId);
        odpsDesc.setAccessKey(this.accessKeySecret);
        odpsDesc.setPartitionMode(OdpsDesc.PartitionMode.USER_DEFINE);
        // 顺序选中topic中部分列或全部列 同步到odps，未选中的列将不会同步
        List<String> columnFields = new ArrayList<String>();
        List<Field> fields = this.getSchema().getFields();
        fields.forEach(f ->{
            columnFields.add(f.getName());
        });
        // 默认是使用UserDefine 的分区模式，具体参见文档[https://help.aliyun.com/document_detail/47453.html?spm=5176.product53345.6.555.MpixiB]
        // 如果需要使用SYSTEM_TIME或EVENT_TIME模式，需要如下设置
        // 对于EVENT_TIME需要在schema中增加一个字段：
        // "event_time"，类型是TIMESTAMP
        // begin
        int timeRange = 15;  // 分钟，分区时间间隔，最小15分钟
        /*odpsDesc.setPartitionMode(OdpsDesc.PartitionMode.SYSTEM_TIME);
        odpsDesc.setTimeRange(timeRange);
        Map<String, String> partitionConfig = new LinkedHashMap<String, String>();
        //目前仅支持 %Y%m%d%H%M 的组合，任意多级分区
        partitionConfig.put("pt", "%Y%m%d");
        partitionConfig.put("ct", "%H%M");
        odpsDesc.setPartitionConfig(partitionConfig);*/
        // end
        client.createDataConnector(this.projectName, tableName, ConnectorType.SINK_ODPS, columnFields, odpsDesc);
        // 特殊需求下可以间歇性 如每15分钟获取Connector状态查看是否有异常,遍历所有shard
        String shard = "0";
        GetDataConnectorShardStatusResult getDataConnectorShardStatusResult =
                client.getDataConnectorShardStatus(this.projectName, tableName, ConnectorType.SINK_ODPS, shard);
        System.out.println(getDataConnectorShardStatusResult.getLastErrorMessage());
    }

    // 插入数据
    public void insert(String topicName, List<String[]> record){
        List<RecordEntry> recordEntries = new ArrayList<RecordEntry>();
        record.forEach(r ->{
            RecordEntry recordEntry = new RecordEntry(this.getSchema());
            recordEntry.setString("program_type", r[0]);
            recordEntry.setString("log_type", r[1]);
            recordEntry.setString("event_type",r[2]);
            recordEntry.setString("logname", r[3]);
            recordEntry.setString("roleid", r[4]);
            recordEntry.setString("rolename", r[5]);
            recordEntry.setString("userid", r[6]);
            recordEntry.setString("account", r[7]);
            recordEntry.setString("serverid", r[8]);
            recordEntry.setTimeStampInDate("logtime", DateUtil.stringToDate(r[9],"yyyy-MM-dd hh:mm:ss"));
            int fieldIndex = 10;
            for(int i =1; i < 20; i++) {
                recordEntry.setString("params"+i, r[fieldIndex]);
                fieldIndex++;
            }
            recordEntries.add(recordEntry);
        });

        PutRecordsResult result = client.putRecords(projectName, topicName, recordEntries);

        if (result.getFailedRecordCount() != 0) {
            List<ErrorEntry> errors = result.getFailedRecordError();
            errors.forEach( e->{
                System.out.println("错误码："+e.getErrorcode());
                System.out.println("错误信息："+e.getMessage());
            });
        }
    }





}
