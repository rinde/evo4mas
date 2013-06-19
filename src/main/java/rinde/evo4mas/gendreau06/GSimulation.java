/**
 * 
 */
package rinde.evo4mas.gendreau06;

import rinde.evo4mas.gendreau06.deprecated.AuctionPanel;
import rinde.evo4mas.gendreau06.deprecated.HeuristicTruckRenderer;
import rinde.sim.core.model.Model;
import rinde.sim.problem.common.AddVehicleEvent;
import rinde.sim.problem.common.DynamicPDPTWProblem;
import rinde.sim.problem.common.DynamicPDPTWProblem.Creator;
import rinde.sim.problem.common.DynamicPDPTWProblem.DefaultUICreator;
import rinde.sim.problem.common.DynamicPDPTWProblem.SimulationInfo;
import rinde.sim.problem.common.DynamicPDPTWProblem.StopCondition;
import rinde.sim.problem.common.StatsTracker.StatisticsDTO;
import rinde.sim.problem.common.TimeLinePanel;
import rinde.sim.problem.gendreau06.Gendreau06Scenario;
import rinde.sim.ui.renderers.CanvasRenderer;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public final class GSimulation {

	private GSimulation() {}

	// TODO create convenience methods for scenario loading
	public static StatisticsDTO simulate(Gendreau06Scenario scenario, Creator<AddVehicleEvent> vehicleCreator,
			boolean showGui, Model<?>... models) {
		final DynamicPDPTWProblem problem = new DynamicPDPTWProblem(scenario, 123, models);
		problem.addCreator(AddVehicleEvent.class, vehicleCreator);
		problem.addStopCondition(new StopCondition() {
			@Override
			public boolean isSatisfiedBy(SimulationInfo context) {
				return context.stats.simulationTime > 8 * 60 * 60 * 1000;
			}
		});
		if (showGui) {
			problem.enableUI(new GendreauUI(problem));
		}
		return problem.simulate();
	}

	public static class GendreauUI extends DefaultUICreator {

		public GendreauUI(DynamicPDPTWProblem p, boolean enableTimeLine, boolean enableAuctionPanel) {
			super(p);
			if (enableTimeLine) {
				addRenderer(new TimeLinePanel());
			}
			if (enableAuctionPanel) {
				addRenderer(new AuctionPanel());
			}
		}

		public GendreauUI(DynamicPDPTWProblem p) {
			this(p, true, true);
		}

		@Override
		protected CanvasRenderer pdpModelRenderer() {
			return new HeuristicTruckRenderer();
		}

	}

}
