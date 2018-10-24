package java_lang.invoke;

import org.junit.Test;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

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


    class VarHandleDemo {
        private int field;

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

}
