package Throtler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimeWindowThrottler implements Throttler{
    NoticationConsumer subscriber;
    private long lastProceedTime;
    private final long timeIntervalInMillis;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();


    public TimeWindowThrottler(long timeIntervalInMillis) {
        this.timeIntervalInMillis = timeIntervalInMillis;
    }

    @Override
    public synchronized ThrottleResult shouldProceed() {
        long currentTime = System.currentTimeMillis();
        try {
            if (currentTime - lastProceedTime >= timeIntervalInMillis) {
                lastProceedTime = System.currentTimeMillis();
                return ThrottleResult.PROCEED;
            } else {
                return ThrottleResult.DO_NOT_PROCEED;
            }
        } finally {
            checkAndNotify();
        }
    }

    @Override
    public void notifyWhenCanProceed(NoticationConsumer subscriber) {
        this.subscriber = subscriber;
        checkAndNotify();
    }

    private void checkAndNotify() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastProceed = currentTime - lastProceedTime;
        long timeTowait;
        if (timeSinceLastProceed >= timeIntervalInMillis) {

            scheduler.execute(subscriber::onNotification);
            return;
        } else {
            timeTowait = timeIntervalInMillis - timeSinceLastProceed;
        }
        scheduler.schedule(() -> {
            synchronized (this) {
                lastProceedTime = System.currentTimeMillis();
            }
            subscriber.onNotification();
        }, timeTowait, TimeUnit.MILLISECONDS);
    }
}
