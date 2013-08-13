package rinde.sim.pdptw.generator;

import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.collect.ImmutableList;

public interface ArrivalTimesGenerator {
	/**
	 * 
	 * @param rng
	 * @return An immutable list in ascending order, may contain duplicates.
	 */
	ImmutableList<Long> generate(RandomGenerator rng);
}