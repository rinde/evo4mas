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
		fullExperiment(new RandomRandom(123));
		System.out.println("RandomRoutePlanner + BlackboardUser");
		fullExperiment(new RandomBB(123));

	}

	static void fullExperiment(Configurator c) {
		final List<String> files = ExperimentUtil.getFilesFromDir("files/scenarios/gendreau06/", "");
		for (final String file : files) {
			final StatisticsDTO stats = GSimulation.simulate(file, 10, c, false);
			final Gendreau06ObjectiveFunction obj = new Gendreau06ObjectiveFunction();
			// System.out.println(stats);
			checkState(obj.isValidResult(stats));
			System.out.println(obj.computeCost(stats));

		}
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
	public static class RandomRandom implements Configurator {
		protected final RandomGenerator rng;

		public RandomRandom(long seed) {
			rng = new MersenneTwister(seed);
		}

		public boolean create(Simulator sim, AddVehicleEvent event) {
			final Communicator c = new RandomBidder(rng.nextLong());
			sim.register(c);
			return sim.register(new Truck(event.vehicleDTO, new RandomRoutePlanner(rng.nextLong()), c));
		}

		public Model<?>[] createModels() {
			return new Model<?>[] { new AuctionCommModel() };
		}

	}

	public static class RandomBB implements Configurator {
		protected final RandomGenerator rng;

		public RandomBB(long seed) {
			rng = new MersenneTwister(seed);
		}

		public boolean create(Simulator sim, AddVehicleEvent event) {
			final Communicator c = new BlackboardUser();
			sim.register(c);
			return sim.register(new Truck(event.vehicleDTO, new RandomRoutePlanner(rng.nextLong()), c));
		}

		public Model<?>[] createModels() {
			return new Model<?>[] { new BlackboardCommModel() };
		}
	}

}
