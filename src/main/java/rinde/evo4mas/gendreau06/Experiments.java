/**
 * 
 */
package rinde.evo4mas.gendreau06;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.evo4mas.common.ExperimentUtil;
import rinde.evo4mas.gendreau06.GSimulation.Configurator;
import rinde.evo4mas.gendreau06.comm.AuctionCommModel;
import rinde.evo4mas.gendreau06.comm.BlackboardCommModel;
import rinde.evo4mas.gendreau06.comm.BlackboardUser;
import rinde.evo4mas.gendreau06.comm.Communicator;
import rinde.evo4mas.gendreau06.comm.RandomBidder;
import rinde.evo4mas.gendreau06.route.RandomRoutePlanner;
import rinde.sim.core.Simulator;
import rinde.sim.core.model.Model;
import rinde.sim.problem.common.AddVehicleEvent;
import rinde.sim.problem.common.StatsTracker.StatisticsDTO;
import rinde.sim.problem.gendreau06.Gendreau06ObjectiveFunction;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class Experiments {

	public static void main(String[] args) {

		System.out.println("RandomRoutePlanner + RandomBidder");
		fullExperiment(new RandomRandom(), 123, 2);
		System.out.println("RandomRoutePlanner + BlackboardUser");
		fullExperiment(new RandomBB(), 123, 10);

	}

	static void fullExperiment(Configurator c, long masterSeed, int repetitions) {
		final List<String> files = ExperimentUtil.getFilesFromDir("files/scenarios/gendreau06/", "_450_24");

		final RandomGenerator rng = new MersenneTwister(masterSeed);
		final long[] seeds = new long[repetitions];
		for (int i = 0; i < repetitions; i++) {
			seeds[i] = rng.nextLong();
		}

		for (final String file : files) {
			System.out.println(file);
			for (int i = 0; i < repetitions; i++) {
				if (c instanceof RandomSeed) {
					((RandomSeed) c).setSeed(seeds[i]);
				}

				final StatisticsDTO stats = GSimulation.simulate(file, 20, c, false);
				final Gendreau06ObjectiveFunction obj = new Gendreau06ObjectiveFunction();
				checkState(obj.isValidResult(stats));

				final StringBuilder sb = new StringBuilder().append(obj.computeCost(stats)).append(',')
						.append(obj.tardiness(stats)).append(',').append(obj.travelTime(stats)).append(',')
						.append(obj.overTime(stats));

				System.out.println(sb.toString());
			}
		}
	}

	interface RandomSeed {
		void setSeed(long seed);
	}

	/**
	 * <ul>
	 * <li>RandomRoutePlanner</li>
	 * <li>AuctionCommModel</li>
	 * <li>RandomBidder</li>
	 * </ul>
	 * 
	 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
	 */
	public static class RandomRandom implements Configurator, RandomSeed {
		protected final RandomGenerator rng;

		public RandomRandom() {
			rng = new MersenneTwister(0L);
		}

		public boolean create(Simulator sim, AddVehicleEvent event) {
			final Communicator c = new RandomBidder(rng.nextLong());
			sim.register(c);
			return sim.register(new Truck(event.vehicleDTO, new RandomRoutePlanner(rng.nextLong()), c));
		}

		public Model<?>[] createModels() {
			return new Model<?>[] { new AuctionCommModel() };
		}

		public void setSeed(long seed) {
			rng.setSeed(seed);
		}

	}

	public static class RandomBB implements Configurator, RandomSeed {
		protected final RandomGenerator rng;

		public RandomBB() {
			rng = new MersenneTwister(0L);
		}

		public boolean create(Simulator sim, AddVehicleEvent event) {
			final Communicator c = new BlackboardUser();
			sim.register(c);
			return sim.register(new Truck(event.vehicleDTO, new RandomRoutePlanner(rng.nextLong()), c));
		}

		public Model<?>[] createModels() {
			return new Model<?>[] { new BlackboardCommModel() };
		}

		public void setSeed(long seed) {
			rng.setSeed(seed);
		}
	}

}
