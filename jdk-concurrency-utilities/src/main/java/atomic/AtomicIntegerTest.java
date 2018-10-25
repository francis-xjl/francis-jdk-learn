package atomic;


import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @program: francis-jdk-learn
 * @description: AtomicInteger测试类
 * @author: francis
 * @create: 2018-10-24 15:26
 **/
public class AtomicIntegerTest {

    @Test
    public void testAddAndGet() {
        AtomicInteger atomic = new AtomicInteger();
        atomic.addAndGet(1);
        atomic.addAndGet(2);
        assert atomic.compareAndSet(3, 4);
        assert !atomic.compareAndSet(5, 4);
        assert atomic.get() == 4;
    }

    @Test
    public void testLazySet() {
        // http://www.importnew.com/27596.html
        // weakCompareAndSetVolatile
        /**
         * weakCompareAndSet实现了一个变量原子的读操作和有条件的原子写操作，但是它不会创建任何happen-before排序，
         * 所以该方法不提供对weakCompareAndSet操作的目标变量以外的变量的在之前或在之后的读或写操作的保证。
         * 也就是说weakCompareAndSet底层不会创建任何happen-before的保证，也就是不会对volatile字段操作的前后加入内存屏障。
         */
        AtomicInteger atomic = new AtomicInteger();
        atomic.lazySet(1);
        assert !atomic.weakCompareAndSetVolatile(5, 6);
        assert atomic.compareAndSet(1, 12);
    }
}
