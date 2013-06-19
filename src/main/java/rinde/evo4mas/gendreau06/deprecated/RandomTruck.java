/**
 * 
 */
package rinde.evo4mas.gendreau06.deprecated;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.ecj.Heuristic;
import rinde.evo4mas.gendreau06.GendreauContext;
import rinde.sim.problem.common.VehicleDTO;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class RandomTruck extends MyopicTruck {

	/**
	 * @param pDto
	 * @param p
	 * @param sm
	 */
	protected RandomTruck(VehicleDTO pDto, long seed) {
		super(pDto, new RandomHeuristic(seed));
	}

	static class RandomHeuristic implements Heuristic<GendreauContext> {
		private final RandomGenerator rng;

		public RandomHeuristic(long seed) {
			rng = new MersenneTwister(seed);
		}

		public double compute(GendreauContext input) {
			return rng.nextDouble();
		}

		public String getId() {
			return getClass().getName();
		}
	}

}
