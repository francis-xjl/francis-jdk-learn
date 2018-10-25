package java_lang.invoke;

import org.junit.Test;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: francis-jdk-learn
 * @description: VarHandle测试类
 * @author: francis
 * @create: 2018-10-24 15:26
 **/
public class VarHandleTest {

    static final VarHandle VAR_HANDLE_FIELD;

    static {
        try {
            VAR_HANDLE_FIELD = MethodHandles.lookup().in(VarHandleDemo.class)
                    .findVarHandle(VarHandleDemo.class, "field", int.class);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Test
    public void testCompareAndSet() {
        VarHandleDemo demo = new VarHandleDemo(10);
        boolean r = VAR_HANDLE_FIELD.compareAndSet(demo, 10, 1);
        assert demo.getField() == 1;
        assert r;
    }

    @Test
    public void testGetAndSet() {
        VarHandleDemo demo = new VarHandleDemo(10);
        int r = (int)VAR_HANDLE_FIELD.getAndSet(demo, 5);
        assert r == 10;
        assert demo.getField() == 5;
    }

    @Test
    public void testGetVolatile() {
        VarHandleDemo demo = new VarHandleDemo(10);
        int result = (int)VAR_HANDLE_FIELD.getVolatile(demo);
        assert result == 10;
    }

    @Test
    public void testGetAndAdd() throws InterruptedException {
        int threadCount = 10000;
        VarHandleDemo demo = new VarHandleDemo(0);

        List<VarHandleAddThread> varHandleAddThreads = new ArrayList<>();
        for(int i = 0; i < threadCount; i++) {
            varHandleAddThreads.add(new VarHandleAddThread(demo, 1));
            varHandleAddThreads.add(new VarHandleAddThread(demo, -1));
        }

        for(VarHandleAddThread thread : varHandleAddThreads) {
            thread.start();
        }

        for(VarHandleAddThread thread : varHandleAddThreads) {
            thread.join();
        }

        assert demo.getField() == 0;
    }

    @Test
    public void testSet() throws InterruptedException {
        int threadCount = 10000;
        VarHandleDemo demo = new VarHandleDemo(0);

        List<VarHandleSetThread> threads = new ArrayList<>();
        for(int i = 0; i < threadCount; i++) {
            threads.add(new VarHandleSetThread(demo, 1));
            threads.add(new VarHandleSetThread(demo, -1));
        }

        for(VarHandleSetThread thread : threads) {
            thread.start();
        }

        for(VarHandleSetThread thread : threads) {
            thread.join();
        }

        // access mode将覆盖在变量声明时指定的任何内存排序效果。
        // set方法对应的就是非volatile定义，是没有可见性保证的
        // https://www.jianshu.com/p/e231042a52dd
        assert demo.getField() != 0;
    }

    class VarHandleDemo {
        private volatile int field;

        public VarHandleDemo(int field) {
            this.field = field;
        }

        public int getField() {
            return field;
        }

        public void setField(int field) {
            this.field = field;
        }
    }

    class VarHandleSetThread extends Thread {

        private VarHandleDemo demo;
        private int addValue;

        public VarHandleSetThread(VarHandleDemo demo, int addValue) {
            this.demo = demo;
            this.addValue = addValue;
        }

        @Override
        public void run() {
            int result = demo.getField() + addValue;
            VAR_HANDLE_FIELD.set(demo, result);
            System.out.println(demo.getField());
        }
    }

    class VarHandleAddThread extends Thread {

        private VarHandleDemo demo;
        private int addValue;

        public VarHandleAddThread(VarHandleDemo demo, int addValue) {
            this.demo = demo;
            this.addValue = addValue;
        }

        @Override
        public void run() {
            VAR_HANDLE_FIELD.getAndAdd(demo, addValue);
            System.out.println(demo.getField());
        }
    }
}
