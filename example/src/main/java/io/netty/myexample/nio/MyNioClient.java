package io.netty.myexample.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class MyNioClient {
    public static void main(String[] args) throws Exception {
        //1.创建客户端
        SocketChannel socketChannel = SocketChannel.open();
        //连接服务端
        socketChannel.connect(new InetSocketAddress("localhost", 8888));
        //2 秒后写入数据
        Thread.sleep(2 * 1000);
        socketChannel.write(StandardCharsets.UTF_8.encode("nio"));
        //3.读取服务端返回数据
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        socketChannel.read(byteBuffer);
        byteBuffer.flip();
        System.out.println("服务端返回数据=======：" + StandardCharsets.UTF_8.decode(byteBuffer).toString());
        //断开连接
        socketChannel.close();
    }
}
