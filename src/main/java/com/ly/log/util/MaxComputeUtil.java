package com.ly.log.util;

import com.aliyun.datahub.DatahubClient;
import com.aliyun.odps.Instance;
import com.aliyun.odps.Odps;
import com.aliyun.odps.OdpsException;
import com.aliyun.odps.Table;
import com.aliyun.odps.account.Account;
import com.aliyun.odps.account.AliyunAccount;
import com.aliyun.odps.data.Record;
import com.aliyun.odps.data.RecordReader;
import com.aliyun.odps.task.SQLTask;
import com.aliyun.odps.tunnel.InstanceTunnel;
import com.aliyun.odps.tunnel.TableTunnel;
import com.aliyun.odps.tunnel.TunnelException;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class MaxComputeUtil {
    private Odps odps;
    private String endpoint;
    private String projectName;
    private String accessKeyId;
    private String accessKeySecret;

    public String getProjectName() {
        return projectName;
    }

    public MaxComputeUtil(){
        Properties properties = FileUtil.loadPropertiesFile("config.properties");
        this.endpoint = "http://service.odps.aliyun.com/api";
        this.accessKeyId = (String) properties.get("accessKeyId");
        this.accessKeySecret = (String) properties.get("accessKeySecret");
        this.projectName = (String) properties.get("datahub_project");
        Account account = new AliyunAccount(accessKeyId, accessKeySecret);
        this.odps = new Odps(account);
        odps.setEndpoint(endpoint);
        odps.setDefaultProject(projectName);
    }

    public boolean createTable(String tableName){
        StringBuffer sb = new StringBuffer("create table ");
        sb.append(tableName);
        sb.append(" (");
        sb.append(" program_type STRING,");
        sb.append(" log_type STRING,");
        sb.append(" event_type STRING,");
        sb.append(" logname STRING,");
        sb.append(" roleid STRING,");
        sb.append(" rolename STRING,");
        sb.append(" userid STRING,");
        sb.append(" account STRING,");
        sb.append(" serverid STRING,");
        sb.append(" logtime DATETIME,");
        for(int i =1; i < 20; i++) {
            sb.append(" params").append(i).append(" STRING");
            if(i !=19){
                sb.append(",");
            }
        }
        sb.append(");");
        return this.excute(sb.toString());
    }

    public List<Record> run(String sql){
        Instance i;
        List<Record> records;
        try {
            i= SQLTask.run(this.odps,sql);
            i.waitForSuccess();
            records = SQLTask.getResultByInstanceTunnel(i);
        } catch (OdpsException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return records;
    }

    public boolean deleteTable(String tableName){
        StringBuffer sb = new StringBuffer("DROP TABLE ").append(tableName).append(";");
        return this.excute(sb.toString());
    }

    public boolean excute(String sql){
        Instance i;
        try {
            i= SQLTask.run(this.odps,sql);
            i.waitForSuccess();
            return true;
        } catch (OdpsException e) {
            e.printStackTrace();
            return false;
        }
    }


    public void tunnel(String tableName){
        TableTunnel tableTunnel = new TableTunnel(this.odps);

        try {
            TableTunnel.DownloadSession downloadSession = tableTunnel.createDownloadSession(this.projectName,tableName);
            long count = downloadSession.getRecordCount();
            System.out.println("结果集总数为:"+count);
            RecordReader recordReader = downloadSession.openRecordReader(0,count);
            Record record;
            while ((record = recordReader.read()) != null) {
                System.out.println(record);
            }
            recordReader.close();

        } catch (TunnelException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String [] args){
        MaxComputeUtil maxCompute = new MaxComputeUtil();


        //maxCompute.age(100,10000);
        //String sql = "select * from (select *,ROW_NUMBER() OVER (ORDER BY logtime) AS rank  from (select * from log_1_2018_11_02_1 union all select * from log_1_2018_11_02_2) as l) as b where b.rank > 0 and b.rank < 11;";
        //String sql = "select * from (select a.*,ROW_NUMBER() OVER (ORDER BY a.logtime) AS rank from  (select * from `log_1_2018_11_02_1` where log_type = 1004001 and  event_type = 1005007 union all select * from `log_1_2018_11_02_2`  where log_type = 1004001 and  event_type = 1005007 union all select * from `log_1_2018_11_03_2`  where log_type = 1004001 and  event_type = 1005007 union all select * from `log_1_2018_11_04_2`  where log_type = 1004001 and  event_type = 1005007 union all select * from `log_1_2018_11_05_2`  where log_type = 1004001 and  event_type = 1005007 ) as a) as l;";

       /* String sql = "select count(*) as bb from  `log_1_2018_11_05`;";
        long start = System.currentTimeMillis();
        List<Record> records = maxCompute.run(sql);

        System.out.println("总条数："+records.get(0).get("bb"));*/

        String sqls = " select f.* from (select l.* from  (select * from `log_1_2018_11_05` where log_type = 1004001 and  event_type = 1005007 union all select * from `log_1_2018_11_06`  where log_type = 1004001 and  event_type = 1005007 union all select * from `log_1_2018_11_07`  where log_type = 1004001 and  event_type = 1005007 union all select * from `temp1541732299555`  where log_type = 1004001 and  event_type = 1005007 union all select * from `temps`  where log_type = 1004001 and  event_type = 1005007 ) l) as f;";

        /*List<Record> records2 = maxCompute.run(sqls);

        InstanceTunnel instanceTunnel = new InstanceTunnel(maxCompute.odps);

        System.out.println("获取list的条数："+records2.size());*/
        /*String tableName = String.valueOf("temp"+System.currentTimeMillis());
        System.out.println(tableName);
        System.out.println(maxCompute.excute(tableName,sqls));*/
        //maxCompute.tunnel("temp1541732299555");
        StringBuffer stringBuffer = new StringBuffer("Create Table ").append("temp98756413").append(" lifecycle 1 as ").append(sqls);
        maxCompute.excute(stringBuffer.toString());




        /*long end = System.currentTimeMillis();

        System.out.println(records.size());
        for(Record r:records){
            System.out.println(r.getDatetime("logtime"));
        }*/

        /*for (Table t : maxCompute.odps.tables()) {
            System.out.println(t);
            System.out.println(t.getName());
        }*/

        /*for (Instance i : maxCompute.odps.instances()) {
            System.out.println(i);
            System.out.println(i);
        }*/
    }

    public void age(int sumCount, int size){
        int totalPage = sumCount%size == 0?sumCount/size:sumCount/size+1;
        System.out.println(totalPage);
    }


}
