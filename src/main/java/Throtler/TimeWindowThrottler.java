package Throtler;

import java.util.ArrayDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimeWindowThrottler implements Throttler{
    NoticationConsumer subscriber;
    private final ArrayDeque<Long> lastPublicationTimeStamps;
    private final long timeIntervalInMillis;
    private final int allowedPublicationsCount;
    private boolean isNotificationScheduled;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();


    public TimeWindowThrottler(long timeIntervalInMillis, int allowedPubs) {
        this.timeIntervalInMillis = timeIntervalInMillis;
        this.allowedPublicationsCount = allowedPubs;
        lastPublicationTimeStamps = new ArrayDeque<>();
    }

    @Override
    public synchronized ThrottleResult shouldProceed() {
        cleanTimeStamps();
        if (lastPublicationTimeStamps.size() >= allowedPublicationsCount ) {
            scheduleNotificationIfNeeded();
            return ThrottleResult.DO_NOT_PROCEED;
        } else {
            var now = System.currentTimeMillis();
            lastPublicationTimeStamps.add(now);// assuming a positive 'should proceed' means a publication
            return ThrottleResult.PROCEED;
        }
    }

    private synchronized void scheduleNotificationIfNeeded() {
        long now = System.currentTimeMillis();
        if (lastPublicationTimeStamps.size() >= allowedPublicationsCount && !isNotificationScheduled){
            isNotificationScheduled = true;
            long oldest = lastPublicationTimeStamps.getFirst();
            long elapsed = now - oldest;
            long delay = timeIntervalInMillis - elapsed;
            scheduler.schedule(this::runScheduledTask, delay, TimeUnit.MILLISECONDS);
        }
    }
    private synchronized void runScheduledTask(){
        isNotificationScheduled = false;
        cleanTimeStamps();
        if(subscriber==null)
            return;// no subscriber yet ==> log this
        if (lastPublicationTimeStamps.size() < allowedPublicationsCount){
            subscriber.onNotification();
        } else {
            scheduleNotificationIfNeeded();
        }
    }

    private void cleanTimeStamps() {
        long now = System.currentTimeMillis();
        while (!lastPublicationTimeStamps.isEmpty() && (lastPublicationTimeStamps.getFirst() + timeIntervalInMillis ) <= now){
            lastPublicationTimeStamps.removeFirst();
        }
    }

    @Override
    public void notifyWhenCanProceed(NoticationConsumer subscriber) {
        this.subscriber = subscriber;
        cleanTimeStamps();
        if (lastPublicationTimeStamps.size() >= allowedPublicationsCount ) {
            scheduleNotificationIfNeeded();
        }
    }
}
