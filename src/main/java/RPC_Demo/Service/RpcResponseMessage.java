package RPC_Demo.Service;

public class RpcResponseMessage {
    private int sequenceid;
    private Object result;
    private String error;

    public RpcResponseMessage(int sequenceid, Object result, String error) {
        this.sequenceid = sequenceid;
        this.result = result;
        this.error = error;
    }

    public int getSequenceid() {
        return sequenceid;
    }
    public Object getResult() {
        return result;
    }
    public String getError() {
        return error;
    }
}
