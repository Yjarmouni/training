package RandomGen;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProbabilisticRandomGenImplTest {

    @Test
    public void testInvalidProbabilitiesException() {
        List<ProbabilisticRandomGen.NumAndProbability> invalidProbabilities = Arrays.asList(
                new ProbabilisticRandomGen.NumAndProbability(1, 0.6F),
                new ProbabilisticRandomGen.NumAndProbability(2, 0.5F)
        );

        assertThrows(InvalidProbabilitiesException.class, () -> {
            ProbabilisticRandomGenImpl.newInstance(invalidProbabilities);
        });
    }

}