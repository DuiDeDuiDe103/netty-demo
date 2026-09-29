package RPC_Demo.Client;

import RPC_Demo.Message.RpcResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcClientHandle extends SimpleChannelInboundHandler<RpcResponseMessage> {
    public static final Map<Integer, Promise<Object>> PROMISES = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponseMessage msg) throws Exception {
        System.out.println(">>> [客户端收到服务端的响应回包]: 序号 " + msg.getSequenceid());
        Promise<Object> promise = PROMISES.remove(msg.getSequenceid());
        if(promise != null ){
            if(msg.getError() != null){
                promise.setFailure(new RuntimeException(msg.getError()));
            }else{
                promise.setSuccess(msg.getResult());
            }
        }
    }
}
