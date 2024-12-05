package EventBus;

import java.util.function.Predicate;

public interface EventBus {
    // Feel free to replace Object with something more specific,
// but be prepared to justify it
    void publishEvent(Object o);
    // How would you denote the subscriber?
    <T> void  addSubscriber(Class<T> clazz, EventListener callback);
    // Would you allow clients to filter the events they receive? How would the interface look like?
    <T> void addSubscriberForFilteredEvents(Class<T> clazz, EventListener callback, Predicate<Object> isAccepted);
}
