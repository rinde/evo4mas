/**
 * 
 */
package rinde.evo4mas.gendreau06;

import java.io.IOException;

import rinde.sim.core.model.Model;
import rinde.sim.problem.common.AddVehicleEvent;
import rinde.sim.problem.common.DynamicPDPTWProblem;
import rinde.sim.problem.common.DynamicPDPTWProblem.Creator;
import rinde.sim.problem.common.DynamicPDPTWProblem.DefaultUICreator;
import rinde.sim.problem.common.DynamicPDPTWProblem.SimulationInfo;
import rinde.sim.problem.common.DynamicPDPTWProblem.StopCondition;
import rinde.sim.problem.common.StatsTracker.StatisticsDTO;
import rinde.sim.problem.common.TimeLinePanel;
import rinde.sim.problem.gendreau06.Gendreau06Parser;
import rinde.sim.problem.gendreau06.Gendreau06Scenario;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public final class GSimulation {

	private GSimulation() {}

	public static StatisticsDTO simulate(String fileName, int vehicles, Configurator config, boolean showGui) {
		try {
			return simulate(Gendreau06Parser.parse(fileName, vehicles), config, showGui);
		} catch (final IOException e) {
			throw new RuntimeException("Failed loading scenario " + fileName);
		}
	}

	// for testing: allows getting a reference to problem instance
	static DynamicPDPTWProblem init(Gendreau06Scenario scenario, Configurator config, boolean showGui) {
		final DynamicPDPTWProblem problem = new DynamicPDPTWProblem(scenario, 123, config.createModels());
		problem.addCreator(AddVehicleEvent.class, config);
		problem.addStopCondition(new StopCondition() {
			@Override
			public boolean isSatisfiedBy(SimulationInfo context) {
				return context.stats.simulationTime > 8 * 60 * 60 * 1000;
			}
		});
		if (showGui) {
			problem.enableUI(new GendreauUI(problem));
		}
		return problem;
	}

	public static StatisticsDTO simulate(Gendreau06Scenario scenario, Configurator config, boolean showGui) {
		return init(scenario, config, showGui).simulate();
	}

	interface Configurator extends Creator<AddVehicleEvent> {
		Model<?>[] createModels();
	}

	public static class GendreauUI extends DefaultUICreator {

		public GendreauUI(DynamicPDPTWProblem p, boolean enableTimeLine, boolean enableAuctionPanel) {
			super(p);
			if (enableTimeLine) {
				addRenderer(new TimeLinePanel());
			}
			// if (enableAuctionPanel) {
			// addRenderer(new AuctionPanel());
			// }
		}

		public GendreauUI(DynamicPDPTWProblem p) {
			this(p, true, true);
		}

		// @Override
		// protected CanvasRenderer pdpModelRenderer() {
		// new HeuristicTruckRenderer();
		// }

	}

}
