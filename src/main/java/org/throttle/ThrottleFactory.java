package org.throttle;

import org.throttle.impl.AsyncThrottleImpl;
import org.throttle.impl.SyncThrottleImpl;
import org.throttle.impl.ThrottleStrategy;
import org.throttle.strategy.BurstThrottleStrategy;
import org.throttle.strategy.RegularThrottleStrategy;
import org.throttle.strategy.ThrottleInformer;

import java.lang.reflect.Proxy;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * TODO remove static methods here for more testability
 * TODO Use EntityFactory interface internally
 * TODO Allows user send Executor as ThrottleFactory parameter
 * TODO use fluent API?
 *
 * Created by englishman on 2/1/16.
 */
public final class ThrottleFactory {

    private static final Executor EXECUTOR = Executors.newCachedThreadPool();

    /**
     *
     * @param resource
     * @param rate in TPS
     * @param <R>
     * @return
     */
    public static <R> Throttle<R> createAsyncRegularThrottle(R resource, double rate) {
        ThrottleStrategy strategy = new RegularThrottleStrategy(rate);
        return new AsyncThrottleImpl<>(resource, strategy, EXECUTOR);
    }

    /**
     *
     * @param resource
     * @param rate
     * @param threshold
     * @param <R>
     * @return
     */
    public static <R> Throttle<R> createAsyncBurstThrottle(R resource, double rate, int threshold) {
        InformerHolder holder = new InformerHolder();
        ThrottleStrategy strategy = new BurstThrottleStrategy(rate, threshold, holder);
        AsyncThrottleImpl asyncThrottle = new AsyncThrottleImpl<>(resource, strategy, EXECUTOR);
        // break cyclic dependency between strategy and throttle
        holder.setDelegate(asyncThrottle);
        return asyncThrottle;
    }

    private static class InformerHolder implements ThrottleInformer {

        private ThrottleInformer delegate;

        void setDelegate(ThrottleInformer delegate) {
            this.delegate = delegate;
        }

        @Override
        public int getQueueSize() {
            return delegate.getQueueSize();
        }
    }

    /**
     *
     * @param clazz
     * @param resource
     * @param rate
     * @param <R>
     * @return
     */
    public static <R> R createSyncRegularThrottle(Class<R> clazz, R resource, double rate) {
        ThrottleStrategy strategy = new RegularThrottleStrategy(rate);
        SyncThrottleImpl<R> syncThrottle = new SyncThrottleImpl<>(resource, strategy);

        return (R) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                new Class[] {clazz}, syncThrottle);
    }

    private ThrottleFactory() {}
}
