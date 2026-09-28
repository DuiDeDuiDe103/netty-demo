import java.io.Serializable;

// 消息必须实现 Serializable 以便二进制序列化
public abstract class ProtocolMessage implements Serializable {
    public static final byte LOGIN_REQUEST = 1;
    public static final byte LOGIN_RESPONSE = 2;
    public static final byte CHAT_REQUEST = 3;
    public static final byte CHAT_RESPONSE = 4;

    public abstract byte getMessageType();

    // 1. 登录请求
    public static class LoginRequestMessage extends ProtocolMessage {
        private String username;
        public LoginRequestMessage(String username) { this.username = username; }
        public String getUsername() { return username; }
        @Override
        public byte getMessageType() { return LOGIN_REQUEST; }
    }

    // 2. 登录响应
    public static class LoginResponseMessage extends ProtocolMessage {
        private boolean success;
        private String reason;
        public LoginResponseMessage(boolean success, String reason) {
            this.success = success;
            this.reason = reason;
        }
        public boolean isSuccess() { return success; }
        public String getReason() { return reason; }
        @Override
        public byte getMessageType() { return LOGIN_RESPONSE; }
    }

    // 3. 单聊请求（发件人、收件人、内容）
    public static class ChatRequestMessage extends ProtocolMessage {
        private String from;
        private String to;
        private String content;
        public ChatRequestMessage(String from, String to, String content) {
            this.from = from;
            this.to = to;
            this.content = content;
        }
        public String getFrom() { return from; }
        public String getTo() { return to; }
        public String getContent() { return content; }
        @Override
        public byte getMessageType() { return CHAT_REQUEST; }
    }

    // 4. 单聊响应
    public static class ChatResponseMessage extends ProtocolMessage {
        private String from;
        private String content;
        public ChatResponseMessage(String from, String content) {
            this.from = from;
            this.content = content;
        }
        public String getFrom() { return from; }
        public String getContent() { return content; }
        @Override
        public byte getMessageType() { return CHAT_RESPONSE; }
    }
}