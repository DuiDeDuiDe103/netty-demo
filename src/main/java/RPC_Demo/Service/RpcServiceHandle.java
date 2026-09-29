package RPC_Demo.Service;

import RPC_Demo.Message.RpcRequestMessage;
import RPC_Demo.Message.RpcResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;

public class RpcServiceHandle extends SimpleChannelInboundHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage msg) throws Exception {
        System.out.println(">>> [服务端收到调用请求]: 想调用方法" + msg.getMethodName());
        RpcResponseMessage response;
        try{
            //找到我们的实现类
            HelloService service = new HelloServiceImpl();
            //找到这个实现类的方法
            Method method = service.getClass().getMethod(msg.getMethodName(), msg.getParameterTypes());
            //调用方法
            Object result = method.invoke(service,msg.getParameterValues());
            System.out.println(">>> [服务端执行成功，结果为]:" + result);
            //返回结果
            response = new RpcResponseMessage(msg.getSequenceid(),result,null);
        } catch (Exception e) {
            e.printStackTrace();
            response = new RpcResponseMessage(msg.getSequenceid(),null,new String(e.getMessage()));
        }
        ctx.writeAndFlush(response);
    }
}
