package com.ly.log.soket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NIOServer {
    boolean started = false;
    ServerSocketChannel serverSocketChannel;
    // 通道管理器
    private Selector selector;

    public void start(){
        try {
            // 获得一个ServerSocket通道
            serverSocketChannel = ServerSocketChannel.open();
            // 设置通道为非阻塞
            serverSocketChannel.configureBlocking(false);
            // 将该通道对于的serverSocket绑定到port端口
            serverSocketChannel.socket().bind(new InetSocketAddress(8002));
            started = true;
            System.out.println("端口已开启,占用8888端口号....");
            // 获得一个通道管理器(选择器)
            this.selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(started){
            try {
                // 当注册事件到达时，方法返回，否则该方法会一直阻塞
                selector.select();
                // 获得selector中选中的相的迭代器，选中的相为注册的事件
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while(iterator.hasNext()){
                    SelectionKey key = iterator.next();
                    // 删除已选的key以防重负处理
                    iterator.remove();
                    //客户端请求连接事件
                    if(key.isAcceptable()){
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                        // 获得和客户端连接的通道
                        SocketChannel channel = serverSocketChannel.accept();
                        // 设置成非阻塞
                        channel.configureBlocking(false);
                        // 在这里可以发送消息给客户端
                        channel.write(ByteBuffer.wrap(new String("hello client").getBytes()));
                        // 在客户端连接成功之后，为了可以接收到客户端的信息，需要给通道设置读的权限
                        channel.register(this.selector, SelectionKey.OP_READ,null);
                    }else if(key.isReadable()){ // 获得了可读的事件
                        // 服务器可读消息，得到事件发生的socket通道
                        SocketChannel channel = (SocketChannel) key.channel();
                        // 读取的缓冲区
                        int capacity = 1000;// 字节
                        ByteBuffer bf = ByteBuffer.allocate(capacity);
                        StringBuffer sb = new StringBuffer();
                        int length = 0;
                        while((length = channel.read(bf)) > 0){
                            bf.clear();
                            byte[] bytes = bf.array();
                            String star = new String(bytes,0,length);
                            sb.append(star);
                            System.out.println(star);
                        }



                        ByteBuffer outBuffer = ByteBuffer.wrap(sb.toString().getBytes());
                        channel.write(outBuffer);
                        channel.close();
                    }

                }



            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
    public static void main(String[] args) throws Throwable {
        NIOServer server = new NIOServer();
        server.start();
    }
}
