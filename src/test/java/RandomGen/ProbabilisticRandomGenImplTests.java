package RandomGen;

import org.apache.commons.math3.util.Precision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProbabilisticRandomGenImplTests {

    private List<ProbabilisticRandomGen.NumAndProbability> validProbabilities;
    private List<ProbabilisticRandomGen.NumAndProbability> invalidProbabilities;

    @BeforeEach
    void init() {
        validProbabilities = Arrays.asList(
                new ProbabilisticRandomGen.NumAndProbability(1, 0.2F),
                new ProbabilisticRandomGen.NumAndProbability(2, 0.5F),
                new ProbabilisticRandomGen.NumAndProbability(3, 0.3F)
        );

        invalidProbabilities = Arrays.asList(
                new ProbabilisticRandomGen.NumAndProbability(1, 0.4F),
                new ProbabilisticRandomGen.NumAndProbability(2, 0.4F),
                new ProbabilisticRandomGen.NumAndProbability(3, 0.3F)
        );
    }

    @Test
    void newInstanceWithValidProbabilities() {
        try {
            ProbabilisticRandomGenImpl gen = ProbabilisticRandomGenImpl.newInstance(validProbabilities, new Random());
            assertNotNull(gen, "Instance should not be null");
        } catch (InvalidProbabilitiesException e) {
            fail("Exception should not be thrown for valid probabilities");
        }
    }

    @Test
    void newInstanceWithInvalidProbabilities() {
        Exception exception = assertThrows(InvalidProbabilitiesException.class, () -> {
            ProbabilisticRandomGenImpl.newInstance(invalidProbabilities, new Random());
        });

        String expectedMessage = "Invalid probabilities distribution";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage), "Exception message should contain expected text");
    }

    @Test
    void nextFromSample() throws InvalidProbabilitiesException {
        Random mockRandom = mock(Random.class);
        when(mockRandom.nextFloat()).thenReturn(0.1F, 0.299999F, 0.699999F, 0.95F);

        ProbabilisticRandomGenImpl gen = ProbabilisticRandomGenImpl.newInstance(validProbabilities, mockRandom);


        assertEquals(1, gen.nextFromSample(), "Random value 0.99999 should return 1");
        assertEquals(2, gen.nextFromSample(), "Random value 0.299999F should return 2");
        assertEquals(2, gen.nextFromSample(), "Random value 0.699999F should return 2");
        assertEquals(3, gen.nextFromSample(), "Random value 0.95 should return 3");


        verify(mockRandom, times(4)).nextFloat();
    }
}
