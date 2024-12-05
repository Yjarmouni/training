package EventBus;

import java.util.function.Predicate;

public interface ConflatingEventBus extends EventBus{
    <T> void  addConflatingSubscriber(Class<T> clazz, EventListener callback);
    <T> void addConflatingSubscriberForFilteredEvents(Class<T> clazz, EventListener callback, Predicate<Object> isAccepted);
}
