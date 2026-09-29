package RPC_Demo.Message;


import java.io.Serializable;

public class RpcRequestMessage implements Serializable {
    //请求id，序列化类型，请求的那个方法，参数列表
    private int sequenceid;
    private String interfaceName;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameterValues;

    public RpcRequestMessage(int sequenceid, String interfaceName, String methodName, Class<?>[] parameterTypes,Object[] parameterValues) {
        this.sequenceid = sequenceid;
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.parameterValues = parameterValues;
    }

    public int getSequenceid() {
        return sequenceid;
    }

    public String getInterfaceName() {
        return interfaceName;
    }
    public String getMethodName() {
        return methodName;
    }
    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }
    public Object[] getParameterValues() {
        return parameterValues;
    }
}
