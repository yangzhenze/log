package com.ly.log.soket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    private static Logger logger = LoggerFactory.getLogger(TCPServer.class);

    private static final int PORT = 8002;   //定义一个端口号
    public void listen(){    //定义监听，抛出异常
        try {
            //1.创建一个serverSocket，绑定监听端口
            ServerSocket serverSocket = new ServerSocket(PORT);
            logger.info("服务器即将启动");

            while(true){
                logger.info("服务器已启动.......");
                //2.调用accept()方法开始监听，等待客户端连接
                Socket socket = serverSocket.accept();
                //3.获取输入流，用来读取客户端发送的信息
                InputStream is=socket.getInputStream();//字节输入流;
                InputStreamReader isr=new InputStreamReader(is);//将字节输入流转换为字符输入流
                BufferedReader br=new BufferedReader(isr);//为输入流添加缓冲

                String info = null;
                while((info = br.readLine()) != null){//循环读取
                    System.out.println(info);
                }
                //关闭输入流，防止造成阻塞
                socket.shutdownInput();
                //服务器向客户端进行响应
                //获取输出流，响应客户端的请求
                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(os);//包装为打印流
                pw.write("welcome!");
                //调用flush()方法刷新缓冲输出
                pw.flush();
                //关闭资源
                pw.close();
                os.close();
                br.close();
                isr.close();
                is.close();
                socket.close();
                //serverSocket.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws Exception {
        new TCPServer().listen();
    }

}
