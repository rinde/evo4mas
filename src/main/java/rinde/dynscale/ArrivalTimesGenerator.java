package rinde.dynscale;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

public interface ArrivalTimesGenerator {
	List<Long> generate(RandomGenerator rng);
}