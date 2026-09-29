package RPC_Demo.Client;

import RPC_Demo.Message.RpcRequestMessage;
import RPC_Demo.Service.HelloService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultPromise;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

public class RpcClient {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);
    private static Channel activeChannel;

    @SuppressWarnings("unchecked")
    public static <T> T getProxyService(Class<T> ServiceClass) {
        ClassLoader classLoader = ServiceClass.getClassLoader();
        Class<?>[] interfaces = new Class[]{ServiceClass};

        return (T) Proxy.newProxyInstance(classLoader,interfaces,((proxy, method, args) -> {
            int seqId = ID_GENERATOR.getAndIncrement();

            RpcRequestMessage msg = new RpcRequestMessage(
                    seqId,
                    ServiceClass.getName(),
                    method.getName(),
                    method.getParameterTypes(),
                    args
            );

            DefaultPromise<Object> promise = new DefaultPromise<>(activeChannel.eventLoop());
            RpcClientHandle.PROMISES.put(seqId, promise);

            activeChannel.writeAndFlush(msg);

            promise.await();
            if(promise.isSuccess()) {
                return promise.getNow();
            }else{
                throw new RuntimeException(promise.cause());
            }
        }));
    }

    public static void main(String[] args) throws Exception {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(worker)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 使用与服务端完全对称的原生对象编解码器
                            ch.pipeline().addLast(new io.netty.handler.codec.serialization.ObjectDecoder(
                                    io.netty.handler.codec.serialization.ClassResolvers.cacheDisabled(null)
                            ));
                            ch.pipeline().addLast(new io.netty.handler.codec.serialization.ObjectEncoder());
                            // 放入我们写好的回执处理器
                            ch.pipeline().addLast(new RpcClientHandle());
                        }
                    });
            // 连接服务端的 8080 端口
            activeChannel = bootstrap.connect("127.0.0.1", 8080).sync().channel();
            System.out.println(">>> 客户端已成功连接到 RPC 服务端！");
            // ================= 见证奇迹的时刻 =================
            // 客户端没有 HelloServiceImpl 实现类，完全靠动态代理凭空调用！
            HelloService helloService = getProxyService(HelloService.class);
            System.out.println(">>> 正在发起 RPC 远程调用...");
            String result = helloService.Hello("张三");
            System.out.println(">>> [远程调用成功返回结果]: " + result);
            activeChannel.close().sync();
        } finally {
            worker.shutdownGracefully();
        }
    }
}
