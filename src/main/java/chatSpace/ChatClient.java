package chatSpace;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.Scanner;

public class ChatClient {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024 * 1024, 5, 4, 0, 0));
                     ch.pipeline().addLast(new MessageCodec());
                     ch.pipeline().addLast(new SimpleChannelInboundHandler<ProtocolMessage>() {
                         @Override
                         protected void channelRead0(ChannelHandlerContext ctx, ProtocolMessage msg) {
                             if (msg instanceof ProtocolMessage.LoginResponseMessage loginResp) {
                                 System.out.println("[系统提示]: " + loginResp.getReason());
                             } else if (msg instanceof ProtocolMessage.ChatResponseMessage chatResp) {
                                 System.out.println("\n【" + chatResp.getFrom() + " 对你说】: " + chatResp.getContent());
                                 System.out.print("> ");
                             }
                         }
                     });
                 }
             });

            Channel channel = b.connect("127.0.0.1", 8080).sync().channel();
            System.out.println(">>> 已连接到服务器！");

            // 专门开一个线程接收控制台的用户输入
            new Thread(() -> {
                Scanner scanner = new Scanner(System.in);
                System.out.print("请输入你的昵称进行登录: ");
                String myName = scanner.nextLine();
                // 发送登录请求
                channel.writeAndFlush(new ProtocolMessage.LoginRequestMessage(myName));

                System.out.println("登录完成！发送格式：[对方昵称] [内容]，输入 quit 退出");
                while (true) {
                    System.out.print("> ");
                    String line = scanner.nextLine();
                    if ("quit".equalsIgnoreCase(line)) {
                        channel.close();
                        break;
                    }
                    String[] parts = line.split(" ", 2);
                    if (parts.length == 2) {
                        String toUser = parts[0];
                        String content = parts[1];
                        channel.writeAndFlush(new ProtocolMessage.ChatRequestMessage(myName, toUser, content));
                    } else {
                        System.out.println("格式错误！请按照: 目标昵称 内容 发送");
                    }
                }
            }).start();

            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}