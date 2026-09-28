import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.io.*;
import java.util.List;

public class MessageCodec extends ByteToMessageCodec<ProtocolMessage> {

    // 出站：Java 对象 -> 带魔数的二进制 ByteBuf
    @Override
    protected void encode(ChannelHandlerContext ctx, ProtocolMessage msg, ByteBuf out) throws Exception {
        // 1. 4 字节魔数: 0xCAFEBABE (咖啡宝贝)
        out.writeBytes(new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE});
        // 2. 1 字节指令类型
        out.writeByte(msg.getMessageType());
        
        // 3. 将 Java 对象序列化为字节数组
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        byte[] bytes = bos.toByteArray();

        // 4. 4 字节正文长度
        out.writeInt(bytes.length);
        // 5. 真正的数据内容
        out.writeBytes(bytes);
    }

    // 入站：二进制 ByteBuf -> Java 对象
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 1. 读魔数
        int magicNum = in.readInt();
        // 简单校验魔数（0xCAFEBABE 对应的 int 值）
        if (magicNum != 0xCAFEBABE) {
            throw new RuntimeException("非法数据包，魔数不匹配！");
        }
        // 2. 读消息类型
        byte messageType = in.readByte();
        // 3. 读长度
        int length = in.readInt();
        // 4. 读内容
        byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length);

        // 5. 反序列化
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        ProtocolMessage message = (ProtocolMessage) ois.readObject();

        // 传递给流水线下个业务 Handler
        out.add(message);
    }
}