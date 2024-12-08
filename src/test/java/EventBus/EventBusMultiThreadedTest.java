package EventBus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class EventBusMultiThreadedTest {

    private EventBusMultiThreaded eventBus;

    @BeforeEach
    void init() {
        eventBus = new EventBusMultiThreaded();
    }


    @Test
    void testNonConflatingSubscriberReceivesAllEvents() {
        // Arrange
        EventListener listener = mock(EventListener.class);
        String event1 = "First Event";
        String event2 = "Second Event";

        eventBus.addSubscriber(String.class, listener);

        // Act
        eventBus.publishEvent(event1);
        eventBus.publishEvent(event2);

        // Assert
        verify(listener, timeout(1000).times(1)).consume(event1);
        verify(listener, timeout(1000).times(1)).consume(event2);
    }


    @Test
    void testConflatingSubscriberReceivesLatestEvent() throws InterruptedException {
        // Arrange
        EventListener listener = mock(EventListener.class);
        doAnswer(invocation -> {
            String event = (String) invocation.getArgument(0);
            if (event.equals("Event 1")) {
                Thread.sleep(200);
            }
            return null;
        }).when(listener).consume(anyString());
        String latestEvent = "Latest Event";

        eventBus.addConflatingSubscriber(String.class, listener);

        // Act
        eventBus.publishEvent("Event 1");
        Thread.sleep(100);

        eventBus.publishEvent("Event 2");
        eventBus.publishEvent(latestEvent);

        // Assert
        verify(listener, timeout(1000).times(1)).consume(latestEvent);
        verify(listener, timeout(1000).times(1)).consume("Event 1");
        verify(listener, never()).consume("Event 2");
    }
    @Test
    void testFilteredSubscriberReceivesOnlyAcceptedEvents() {
        EventListener listener = mock(EventListener.class);
        Predicate<Object> predicate = o -> ((String) o).startsWith("Accept");
        String acceptedEvent = "Accept this event";
        String rejectedEvent = "Reject this event";

        eventBus.addSubscriberForFilteredEvents(String.class, listener, predicate);

        eventBus.publishEvent(acceptedEvent);  // Should be accepted
        eventBus.publishEvent(rejectedEvent);  // Should be rejected

        verify(listener, timeout(1000).times(1)).consume(acceptedEvent);
        verify(listener, timeout(1000).times(0)).consume(rejectedEvent);
    }

    @Test
    void testMultipleSubscribersReceiveCorrectEvents() {
        EventListener stringListener = mock(EventListener.class);
        EventListener integerListener = mock(EventListener.class);
        String stringEvent = "String Event";
        Integer integerEvent = 1337;

        eventBus.addSubscriber(String.class, stringListener);
        eventBus.addSubscriber(Integer.class, integerListener);

        eventBus.publishEvent(stringEvent);
        eventBus.publishEvent(integerEvent);

        verify(stringListener, timeout(1000).times(1)).consume(stringEvent);
        verify(stringListener, timeout(1000).times(0)).consume(integerEvent);
        verify(integerListener, timeout(1000).times(1)).consume(integerEvent);
        verify(integerListener, timeout(1000).times(0)).consume(stringEvent);
    }

    @Test
    void testEventOrderIsPreservedForNonConflatingSubscribers() {
        EventListener listener = mock(EventListener.class);
        String event1 = "First Event";
        String event2 = "Second Event";

        eventBus.addSubscriber(String.class, listener);

        eventBus.publishEvent(event1);
        eventBus.publishEvent(event2);

        InOrder inOrder = inOrder(listener);
        inOrder.verify(listener, timeout(1000)).consume(event1);
        inOrder.verify(listener, timeout(1000)).consume(event2);
    }
}
