import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.charset.StandardCharsets;

public class MyClient {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 客户端也挂一个处理器来接收服务端的回复
                            ch.pipeline().addLast(new ClientBusinessHandler());
                        }
                    });

            // 连接服务端的 8080 端口
            ChannelFuture f = b.connect("127.0.0.1", 8080).sync();
            Channel channel = f.channel();
            System.out.println(">>> 客户端已成功连接到服务端！");

            // 准备一条要发送的数据并写入 ByteBuf
            String msgToSend = "hello netty! i am learning step by step.";
            ByteBuf sendBuf = ByteBufAllocator.DEFAULT.buffer();
            sendBuf.writeBytes(msgToSend.getBytes(StandardCharsets.UTF_8));

            // 发送给服务端
            System.out.println(">>> 客户端正在发送消息: " + msgToSend);
            channel.writeAndFlush(sendBuf);

            // 休眠 1 秒让数据收发完成，然后关闭
            Thread.sleep(1000);
            channel.close().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    // 内部类：客户端的业务 Handler
    static class ClientBusinessHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf buf = (ByteBuf) msg;
            try {
                System.out.println(">>> [客户端收到服务端的响应]: " + buf.toString(StandardCharsets.UTF_8));
            } finally {
                buf.release();
            }
        }
    }
}