import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.nio.charset.StandardCharsets;

public class MyServer {
    public static void main(String[] args) throws InterruptedException {
        // 1. 创建 Boss 线程组（接客）和 Worker 线程组（干活）
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 第一道工序：解码 Handler
                            ch.pipeline().addLast("decoderHandler", new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                    ByteBuf buf = (ByteBuf) msg;
                                    System.out.println("【工序 1 准备解码】: " + buf);
                                    String str = buf.toString(StandardCharsets.UTF_8);
                                    System.out.println("【工序 1 解码完成】: " + str);

                                    // 重点：调用 fireChannelRead，把转换好的 String 传递给下一个 Handler！
                                    ctx.fireChannelRead(str);

                                    buf.release(); // 第一道工序用完了 ByteBuf，释放它
                                }
                            });

                            // 第二道工序：业务 Handler（直接接盘第一道工序的成果）
                            ch.pipeline().addLast("businessHandler", new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                    // 注意：这里的 msg 已经是 String 了，不再是 ByteBuf！
                                    String str = (String) msg;
                                    System.out.println("【工序 2 拿到字符串做业务】: " + str);

                                    // 业务回复
                                    ByteBuf resp = ctx.alloc().buffer();
                                    resp.writeBytes(("处理完成: " + str.toUpperCase()).getBytes(StandardCharsets.UTF_8));
                                    ctx.writeAndFlush(resp);
                                }
                            });
                        }
                    });

            // 绑定 8080 端口，同步等待绑定成功
            ChannelFuture f = b.bind(8080).sync();
            System.out.println(">>> 服务端启动成功！正在监听 8080 端口，等待客户端发消息...");

            // 阻塞等待服务端通道关闭
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    // 内部类：服务端的业务 Handler
    static class ServerBusinessHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf inBuf = (ByteBuf) msg;
            try {
                // 读取客户端发过来的字符串
                String clientMsg = inBuf.toString(StandardCharsets.UTF_8);
                System.out.println(">>> [服务端接收到]: " + clientMsg);

                // 业务处理：转成大写字母
                String responseMsg = "服务端已收到并转为大写: " + clientMsg.toUpperCase();

                // 分配一个新的 ByteBuf 发回给客户端
                ByteBuf outBuf = ctx.alloc().buffer();
                outBuf.writeBytes(responseMsg.getBytes(StandardCharsets.UTF_8));
                ctx.writeAndFlush(outBuf);
            } finally {
                // 谁是最后使用者谁负责释放
                inBuf.release();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}