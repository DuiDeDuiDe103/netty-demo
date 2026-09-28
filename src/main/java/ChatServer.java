import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    // 核心 Session 会话管理器：用户名 -> Channel 通道
    private static final Map<String, Channel> sessionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, worker)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     // 1. 拆包器（解决粘包半包）：头部 5 偏移，4 字节长
                     ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024 * 1024, 5, 4, 0, 0));
                     // 2. 协议编解码器
                     ch.pipeline().addLast(new MessageCodec());
                     // 3. 聊天室核心业务转发 Handler
                     ch.pipeline().addLast(new SimpleChannelInboundHandler<ProtocolMessage>() {
                         @Override
                         protected void channelRead0(ChannelHandlerContext ctx, ProtocolMessage msg) {
                             // A. 处理登录请求
                             if (msg instanceof ProtocolMessage.LoginRequestMessage loginMsg) {
                                 String username = loginMsg.getUsername();
                                 sessionMap.put(username, ctx.channel());
                                 System.out.println(">>> [用户上线]: " + username);
                                 ctx.writeAndFlush(new ProtocolMessage.LoginResponseMessage(true, "登录成功！欢迎 " + username));
                             } 
                             // B. 处理单聊请求并做转发
                             else if (msg instanceof ProtocolMessage.ChatRequestMessage chatMsg) {
                                 String toUser = chatMsg.getTo();
                                 Channel toChannel = sessionMap.get(toUser);
                                 if (toChannel != null && toChannel.isActive()) {
                                     // 对方在线：精准转发！
                                     toChannel.writeAndFlush(new ProtocolMessage.ChatResponseMessage(chatMsg.getFrom(), chatMsg.getContent()));
                                     System.out.println(">>> [消息转发]: " + chatMsg.getFrom() + " -> " + toUser + ": " + chatMsg.getContent());
                                 } else {
                                     // 对方不在线
                                     ctx.writeAndFlush(new ProtocolMessage.ChatResponseMessage("系统", "对方 [" + toUser + "] 不在线！"));
                                 }
                             }
                         }

                         // 客户端掉线时，自动清理 Session
                         @Override
                         public void channelInactive(ChannelHandlerContext ctx) {
                             sessionMap.values().remove(ctx.channel());
                             System.out.println(">>> 一个客户端连接已断开，自动清理 Session");
                         }
                     });
                 }
             });

            ChannelFuture f = b.bind(8080).sync();
            System.out.println(">>> 聊天室服务端已启动，监听 8080 端口...");
            f.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}