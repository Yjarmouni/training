package Throtler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimeWindowThrottlerTest {

    private TimeWindowThrottler throttler;
    private NoticationConsumer consumer;
    private final long TIME_INTERVAL_MS = 200;
    private final int ALLOWED_PUBS = 2;

    @BeforeEach
    void init() {
        throttler = new TimeWindowThrottler(TIME_INTERVAL_MS, ALLOWED_PUBS);
        consumer = mock(NoticationConsumer.class);
    }

    @Test
    void testShouldProceedUnderLimit() {
        for (int i = 0; i < ALLOWED_PUBS; i++) {
            ThrottleResult result = throttler.shouldProceed();
            assertEquals(ThrottleResult.PROCEED, result, "Throttler should allow proceeding.");
        }
    }

    @Test
    void testShouldNotProceedOverLimit() {
        for (int i = 0; i < ALLOWED_PUBS; i++) {
            ThrottleResult result = throttler.shouldProceed();
            assertEquals(ThrottleResult.PROCEED, result, "Throttler should allow proceeding.");
        }

        ThrottleResult result = throttler.shouldProceed();

        assertEquals(ThrottleResult.DO_NOT_PROCEED, result, "Throttler should block proceeding.");
    }

    @Test
    void testNotificationIsCalledAfterTimeWindow(){
        throttler.notifyWhenCanProceed(consumer);

        for (int i = 0; i < ALLOWED_PUBS; i++) {
            ThrottleResult result = throttler.shouldProceed();
            assertEquals(ThrottleResult.PROCEED, result, "Throttler should allow proceeding.");
        }

        ThrottleResult result = throttler.shouldProceed();
        assertEquals(ThrottleResult.DO_NOT_PROCEED, result, "Throttler should block proceeding.");

        verify(consumer, timeout(TIME_INTERVAL_MS + 100)).onNotification();
    }
}
