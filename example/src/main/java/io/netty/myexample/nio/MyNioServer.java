package io.netty.myexample.nio;

import com.sun.security.ntlm.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class MyNioServer {
    public static void main(String[] args) throws Exception{
        // 打开服务端SocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 设置为非阻塞模式
        serverSocketChannel.configureBlocking(false);

        Selector selector = Selector.open();

        SelectionKey selectionKey = serverSocketChannel.register(selector, 0, serverSocketChannel);
        selectionKey.interestOps(SelectionKey.OP_ACCEPT);

        // Socket绑定端口
        serverSocketChannel.bind(new InetSocketAddress(8888));

        while (true){
            System.out.println("来啦老弟");
            selector.select();

            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            while (iterator.hasNext()){
                SelectionKey sk = iterator.next();
                iterator.remove();
                if (sk.isAcceptable()){
                    ServerSocketChannel ssc = (ServerSocketChannel) sk.channel();
                    SocketChannel socketChannel = ssc.accept();

                    socketChannel.configureBlocking(false);

                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    SelectionKey register = socketChannel.register(selector, 0, byteBuffer);
                    register.interestOps(SelectionKey.OP_READ);
                }else if(sk.isReadable()){//可读事件
                    try {
                        //取到Channel
                        SocketChannel socketChannel = (SocketChannel) sk.channel();
                        //获取到绑定的ByteBuffer
                        ByteBuffer byteBuffer = (ByteBuffer) sk.attachment();
                        int read = socketChannel.read(byteBuffer);
                        //如果是正常断开 read = -1
                        if(read == -1){
                            //取消事件
                            sk.cancel();
                            continue;
                        }
                        byteBuffer.flip();
                        String str = StandardCharsets.UTF_8.decode(byteBuffer).toString();
                        System.out.println("服务端读取到数据===========：" + str);
                        //写数据回客户端
                        ByteBuffer writeBuffer = StandardCharsets.UTF_8.encode("this is result");
                        socketChannel.write(writeBuffer);
                        //如果数据一次没写完关注可写事件进行再次写入（大数据一次写不完的情况）
                        if(writeBuffer.hasRemaining()){
                            //关注可写事件，添加事件，用interestOps()方法获取到之前的事件加上可写事件（类似linux系统的赋权限 777）
                            sk.interestOps(sk.interestOps() + SelectionKey.OP_WRITE);
                            sk.attach(writeBuffer);
                            //位运算符也可以
                            //sk.interestOps(sk.interestOps() | SelectionKey.OP_WRITE);
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                        //客户端异常断开连接 ，取消事件
                        sk.cancel();
                    }
                }else if(sk.isWritable()){
                    //取到Channel
                    SocketChannel socketChannel = (SocketChannel) sk.channel();
                    //获取到绑定的ByteBuffer
                    ByteBuffer writeBuffer = (ByteBuffer) sk.attachment();
                    socketChannel.write(writeBuffer);
                    //如果全部写完，取消可写事件绑定，解除writeBuffer绑定
                    if(!writeBuffer.hasRemaining()){
                        sk.attach(null);
                        //取消可写事件
                        sk.interestOps(sk.interestOps() - SelectionKey.OP_WRITE);
                    }
                }
            }
        }
    }
}
