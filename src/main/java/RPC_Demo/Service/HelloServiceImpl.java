package RPC_Demo.Service;

public class HelloServiceImpl implements HelloService {
    @Override
    public String Hello(String name) {
        return "Hello " + name;
    }
}
