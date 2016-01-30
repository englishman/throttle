package org.throttle;

/**
 * Created by ax01220 on 1/29/2016.
 */
public class NormalThrottle implements Throttle {

    private final double threshold;
    private final TimeService timer;

    private boolean firstUsage = true;
    private long lastTime;

    public NormalThrottle(double rate) {
        this(rate, () -> System.currentTimeMillis() );
    }

    NormalThrottle(double rate, TimeService timer) {
        this.threshold = 1000 / rate;
        this.timer = timer;
    }

    @Override
    public boolean isResourceAvailable() {
        long currentTime = timer.get();

        boolean verdict = getVerdict(currentTime);
        firstUsage = false;

        if (verdict)
            lastTime = currentTime;
        return verdict;
    }

    private boolean getVerdict(long currentTime) {
        return firstUsage
                ? true
                : currentTime - lastTime > threshold;
    }
}