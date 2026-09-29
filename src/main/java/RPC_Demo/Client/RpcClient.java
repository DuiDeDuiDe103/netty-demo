package RPC_Demo.Client;

import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultPromise;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

public class RpcClient {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);
    private static Channel activeChannel;

    @SuppressWarnings("unchecked")
    public static <T> T getProxyService(Class<T> ServiceClass) {
        ClassLoader classLoader = ServiceClass.getClassLoader();
        Class<?>[] interfaces = ServiceClass.getInterfaces();

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


}
