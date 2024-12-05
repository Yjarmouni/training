package EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class EventBusOneThreaded implements  EventBus{
    private final List<Subscriber<?>> subscribers = new ArrayList<>();
    @Override
    public void publishEvent(Object o) {
        for (var subscriber : subscribers){
            if (shouldPublish(o, subscriber)){
                subscriber.callback.consume(o);
            }
        }
    }

    private <T> boolean shouldPublish(Object o, Subscriber<?> subscriber) {
        if(!subscriber.clazz.isInstance(o)){
            return false;
        }else return subscriber.isAccepted == null || subscriber.isAccepted.test(o);
    }

    @Override
    public <T> void addSubscriber(Class<T> clazz, EventListener callback) {
        subscribers.add(new Subscriber<T>(clazz, callback, null));
    }

    @Override
    public <T> void addSubscriberForFilteredEvents(Class<T> clazz, EventListener callback, Predicate<Object> isAccepted) {
        subscribers.add(new Subscriber<T>(clazz, callback, isAccepted));
    }

    private static class Subscriber<T> {
        Class<T> clazz;
        EventListener callback;
        Predicate<Object> isAccepted;

        public Subscriber(Class<T> clazz, EventListener callback, Predicate<Object> isAccepted){
            this.clazz = clazz;
            this.callback = callback;
            this.isAccepted = isAccepted;
        }
    }
}
