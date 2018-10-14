package com.ly.log.util;

import com.aliyun.openservices.log.Client;

public class ALiYun {

    String endpoint;
    // Endpoint
    String accessKeyId;
    String accessKeySecret;
    private String project;
    private String logstore;
    private int beginSecond;
    private int endSecond;
    private int logOffset;
    private int logLine;
    // 构建一个客户端实例
    private Client client;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getLogstore() {
        return logstore;
    }

    public void setLogstore(String logstore) {
        this.logstore = logstore;
    }

    public int getBeginSecond() {
        return beginSecond;
    }

    public void setBeginSecond(int beginSecond) {
        this.beginSecond = beginSecond;
    }

    public int getEndSecond() {
        return endSecond;
    }

    public void setEndSecond(int endSecond) {
        this.endSecond = endSecond;
    }

    public int getLogOffset() {
        return logOffset;
    }

    public void setLogOffset(int logOffset) {
        this.logOffset = logOffset;
    }

    public int getLogLine() {
        return logLine;
    }

    public void setLogLine(int logLine) {
        this.logLine = logLine;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * 配置client
     */
    public void clientConfig(){
        this.client = new Client(this.endpoint, this.accessKeyId, this.accessKeySecret);
    }
    public ALiYun() {
    }

    public ALiYun(String endpoint, String accessKeyId, String accessKeySecret, String project, String logstore, int beginSecond, int endSecond, int logOffset, int logLine) {
        this.endpoint = endpoint;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.project = project;
        this.logstore = logstore;
        this.beginSecond = beginSecond;
        this.endSecond = endSecond;
        this.logOffset = logOffset;
        this.logLine = logLine;
        clientConfig();
    }
}
