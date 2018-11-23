package com.ly.log.util;

import org.springframework.jdbc.core.JdbcTemplate;

public class DBController {
    DBPoolConnection dbPoolConnection = DBPoolConnection.getInstance();
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dbPoolConnection.getDruidDataSource());

    public boolean createTable(String tableName){
        String params = "";

        for(int i = 1; i<=19;i++){
            params += "  `params"+i+"` varchar(255) DEFAULT NULL ,";
        }
        params = params.substring(0,params.length()-1);

        String sql = "create table `"+tableName+"` (" +
                "  `program_type` varchar(255) DEFAULT NULL COMMENT '程序类型'," +
                "  `log_type` varchar(255) DEFAULT NULL COMMENT '日志类型'," +
                "  `event_type` varchar(255) DEFAULT NULL COMMENT '事件类型'," +
                "  `logname` varchar(255) DEFAULT NULL COMMENT '日志名称'," +
                "  `roleid` varchar(255) DEFAULT NULL COMMENT '角色id'," +
                "  `rolename` varchar(255) DEFAULT NULL COMMENT '用户名'," +
                "  `userid` varchar(255) DEFAULT NULL COMMENT '用户id'," +
                "  `account` varchar(255) DEFAULT NULL COMMENT '用户账号'," +
                "  `serverid` varchar(255) DEFAULT NULL COMMENT '用户服务'," +
                "  `logtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '日志时间'," +
                params+
                ") ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;";

        System.out.println(sql);

        try {
            jdbcTemplate.update(sql);
            return true;
        }catch (Exception e){
            return false;
        }
    }


    public boolean tableExist(String tableName){
        String sql = "SELECT count(*) FROM information_schema.TABLES WHERE table_name =?";

        int result = jdbcTemplate.queryForObject(sql, new String[]{tableName},Integer.class);

        if(result > 0){
            return true;
        }
        return false;
    }

    public boolean insertData(String insertValue,String tableName){
        StringBuffer stringBuffer = new StringBuffer("insert into ");
        stringBuffer.append("`"+tableName+"`");
        stringBuffer.append(" values ");
        stringBuffer.append(insertValue);

        int result = jdbcTemplate.update(stringBuffer.toString());


        return result>0?true:false;
    }


    public static void main(String [] args){
        /*DBController dbTest = new DBController();
        System.out.print(dbTest.createTable("eova_log2"));*/

        int i = 0;
        int j = 0;


        //System.out.println(++i >0);
        if(++i >2 & ++j >0){
            System.out.println(i);
            System.out.println(j);
        }

        System.out.println(i);
        System.out.println(j);
    }
}
