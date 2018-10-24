package atomic;


import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicIntegerTest {

    @Test
    public void test() {
        AtomicInteger atomic = new AtomicInteger();
        atomic.addAndGet(1);
        atomic.addAndGet(2);
        assert atomic.compareAndSet(3, 4);
        assert !atomic.compareAndSet(5, 4);
        assert atomic.get() == 4;
        // http://www.importnew.com/27596.html
        // weakCompareAndSetVolatile

        atomic.lazySet(1);
        assert !atomic.weakCompareAndSetVolatile(5, 6);
        assert atomic.compareAndSet(4, 12);
    }

}
