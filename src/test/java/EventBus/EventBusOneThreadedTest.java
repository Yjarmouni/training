package EventBus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.function.Predicate;

import static org.mockito.Mockito.*;
class EventBusOneThreadedTest {
    private EventBusOneThreaded eventBus;

    @BeforeEach
    void init() {
        eventBus = new EventBusOneThreaded();
    }

    @Test
    void testAddSubscriberAndPublishEvent() {
        EventListener listener = mock(EventListener.class);
        String event = "TestEvent";
        eventBus.addSubscriber(String.class, listener);

        eventBus.publishEvent(event);

        verify(listener, times(1)).consume(event);
    }

    @Test
    void testPublishEvent_EventNotOfSubscribedType() {
        EventListener listener = mock(EventListener.class);
        Integer event = 100;
        eventBus.addSubscriber(String.class, listener);

        eventBus.publishEvent(event);

        verify(listener, never()).consume(any());
    }

    @Test
    void testAddFilteredSubscriber_PredicateAccepts() {
        EventListener listener = mock(EventListener.class);
        Predicate<Object> predicate = o -> o instanceof String && ((String) o).startsWith("Accept");
        eventBus.addSubscriberForFilteredEvents(String.class, listener, predicate);

        String acceptedEvent = "AcceptThis";
        String rejectedEvent = "RejectThis";

        eventBus.publishEvent(acceptedEvent);
        eventBus.publishEvent(rejectedEvent);

        verify(listener, times(1)).consume(acceptedEvent);
        verify(listener, never()).consume(rejectedEvent);
    }

    @Test
    void testMultipleSubscribersReceiveCorrectEvents() {
        EventListener listener1 = mock(EventListener.class);
        EventListener listener2 = mock(EventListener.class);
        String event1 = "Event1";
        Integer event2 = 200;

        eventBus.addSubscriber(String.class, listener1);
        eventBus.addSubscriber(Integer.class, listener2);

        eventBus.publishEvent(event1);
        eventBus.publishEvent(event2);

        verify(listener1, times(1)).consume(event1);
        verify(listener1, never()).consume(event2);
        verify(listener2, times(1)).consume(event2);
        verify(listener2, never()).consume(event1);
    }

    @Test
    void testEventOrderIsPreserved() {
        EventListener listener = mock(EventListener.class);
        eventBus.addSubscriber(String.class, listener);

        String event1 = "FirstEvent";
        String event2 = "SecondEvent";

        eventBus.publishEvent(event1);
        eventBus.publishEvent(event2);

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener).consume(event1);
        inOrder.verify(listener).consume(event2);
    }
}