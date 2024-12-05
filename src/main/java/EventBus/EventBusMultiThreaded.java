package EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public class EventBusMultiThreaded  implements  ConflatingEventBus{
    private final List<EventBusMultiThreaded.Subscriber<?>> subscribers = new ArrayList<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void publishEvent(Object o) {
        for (var subscriber : subscribers){
            if (shouldPublish(o, subscriber)){
                publish(o, subscriber);
            }
        }
    }

    private void publish(Object o, Subscriber<?> subscriber) {
        if (subscriber.isConflationActivated){
            subscriber.latestEvent.set(o);
            handleConflatedEvent(subscriber);
        } else {
            executor.submit(()-> {
               subscriber.callback.consume(o);
            });
        }
    }

    private void handleConflatedEvent(Subscriber<?> subscriber) {
        if(subscriber.isProcessing.compareAndSet(false, true)){
            executor.submit(()-> {
                try{
                    Object event;
                    while((event = subscriber.latestEvent.getAndSet(null)) != null){
                        subscriber.callback.consume(event);
                    }
                } finally {
                    subscriber.isProcessing.set(false);
                }

            });
        }

    }

    private <T> boolean shouldPublish(Object o, EventBusMultiThreaded.Subscriber<?> subscriber) {
        if(!subscriber.clazz.isInstance(o)){
            return false;
        } else return subscriber.isAccepted == null || subscriber.isAccepted.test(o);
    }

    @Override
    public <T> void addSubscriber(Class<T> clazz, EventListener callback) {
        subscribers.add(new EventBusMultiThreaded.Subscriber<T>(clazz, callback, null, false));
    }

    @Override
    public <T> void addSubscriberForFilteredEvents(Class<T> clazz, EventListener callback, Predicate<Object> isAccepted) {
        subscribers.add(new EventBusMultiThreaded.Subscriber<T>(clazz, callback, isAccepted, false));
    }

    @Override
    public <T> void addConflatingSubscriber(Class<T> clazz, EventListener callback) {
        subscribers.add(new EventBusMultiThreaded.Subscriber<T>(clazz, callback, null, true));
    }

    @Override
    public <T> void addConflatingSubscriberForFilteredEvents(Class<T> clazz, EventListener callback, Predicate<Object> isAccepted) {
        subscribers.add(new EventBusMultiThreaded.Subscriber<T>(clazz, callback, isAccepted, true));
    }

    private static class Subscriber<T> {
        Class<T> clazz;
        EventListener callback;
        Predicate<Object> isAccepted;
        final boolean isConflationActivated;
        AtomicReference<Object> latestEvent = new AtomicReference<>();

        AtomicBoolean isProcessing = new AtomicBoolean(false);


        public Subscriber(Class<T> clazz, EventListener callback, Predicate<Object> isAccepted, boolean isConflationActivated){
            this.clazz = clazz;
            this.callback = callback;
            this.isAccepted = isAccepted;
            this.isConflationActivated = isConflationActivated;
        }
    }
}
