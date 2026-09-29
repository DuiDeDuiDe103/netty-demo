package reflect.Proxy;

import java.lang.reflect.Proxy;

public class ProxyTest {
    interface single{
        String Song(String songName);
        void dance();
    }

    static class Zhou implements single{
        public Zhou() {
            super();
        }

        @Override
        public String Song(String songName) {
            System.out.println("现在开始唱"+ songName);
            return "啊啊啊啊啊啊啊";
        }

        @Override
        public void dance() {
            System.out.println("It's showtime");
        }
    }

    public static single createProxy(single realSonger) {
        Class<?> clazz = realSonger.getClass();
        ClassLoader classLoader = clazz.getClassLoader();
        Class<?>[] interfaces = clazz.getInterfaces();

        return (single) Proxy.newProxyInstance(classLoader, interfaces, ((proxy, method, args) ->{
            System.out.println("收10000块钱门票");
            Object result = method.invoke(realSonger, args);
            System.out.println(">>> [经纪人介入]: 演出顺利结束，经纪人护送明星离开现场！\\n");
            return result;
        }));
    }

    public static void main(String[] args) {
        single realSonger = new  Zhou();
        single proxy0 = createProxy(realSonger);
        System.out.println("========= 第一次测试：请明星唱歌 =========");
        String applause = proxy0.Song("晴天");
        System.out.println("最终反馈: " + applause);
        System.out.println("========= 第二次测试：请明星跳舞 =========");
        proxy0.dance();
    }
}
