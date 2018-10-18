package atomic;


import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicIntegerExample {

    @Test
    public void test() {
        AtomicInteger atomic = new AtomicInteger();
        atomic.addAndGet(10);
        atomic.addAndGet(11);
        assert atomic.compareAndSet(21, 10);
        assert !atomic.compareAndSet(9, 11);
        assert atomic.get() == 10;
        // http://www.importnew.com/27596.html
        // weakCompareAndSetVolatile
        atomic.weakCompareAndSetVolatile(1, 1);
    }

}
