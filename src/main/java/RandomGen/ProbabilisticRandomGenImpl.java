package RandomGen;

import org.apache.commons.math3.util.Precision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ProbabilisticRandomGenImpl implements ProbabilisticRandomGen{
    private final List<NumAndProbability> probabilitiesOfNumbers;
    private final List<Double> cumulativeProbabilities;
    private final Random random;
    private static final double EPSILON = .000001;
    private ProbabilisticRandomGenImpl(List<NumAndProbability> probabilitiesOfNumbers, List<Double> cumulativeProbabilities, Random random){
        this.probabilitiesOfNumbers = probabilitiesOfNumbers;
        this.cumulativeProbabilities = cumulativeProbabilities;
        this.random = random;
    }
    public static ProbabilisticRandomGenImpl newInstance(List<NumAndProbability> probabilitiesOfNumbers, Random randomGenerator) throws InvalidProbabilitiesException {

        double cumProba = 0.0;
        List<Double> cumulativeProbabilities = new ArrayList<>();
        for (NumAndProbability probabilitiesOfNumber : probabilitiesOfNumbers) {
            cumProba += probabilitiesOfNumber.getProbabilityOfSample();
            cumulativeProbabilities.add(cumProba);
        }
        if(Precision.compareTo(cumProba, 1.0, EPSILON) != 0){
            throw new InvalidProbabilitiesException("Invalid probabilities distribution: " + probabilitiesOfNumbers);
        }
        return new ProbabilisticRandomGenImpl(new ArrayList<>(probabilitiesOfNumbers), cumulativeProbabilities, randomGenerator);

    }
    @Override
    public int nextFromSample() {
        double randomValue = random.nextFloat();
        int index = Collections.binarySearch(cumulativeProbabilities, randomValue, (a,b) -> Precision.compareTo(a, b, EPSILON));
        if(index < 0){
            index = -index - 1;
        }
        return probabilitiesOfNumbers.get(index).getNumber();
    }
}
