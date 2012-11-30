/**
 * 
 */
package rinde.evo4mas.fabrirecht;

import rinde.ecj.GPProgram;
import rinde.evo4mas.common.TruckContext;
import rinde.sim.core.Simulator;
import rinde.sim.problem.common.AddParcelEvent;
import rinde.sim.problem.common.AddVehicleEvent;
import rinde.sim.problem.common.DefaultParcel;
import rinde.sim.problem.common.DynamicPDPTWProblem;
import rinde.sim.problem.common.DynamicPDPTWProblem.Creator;
import rinde.sim.problem.common.DynamicPDPTWProblem.SimulationInfo;
import rinde.sim.problem.common.DynamicPDPTWProblem.StopCondition;
import rinde.sim.problem.common.StatsTracker.StatisticsDTO;
import rinde.sim.problem.fabrirecht.FabriRechtScenario;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class Simulation {

	protected final GPProgram<TruckContext> program;
	protected final boolean useGui;
	protected final DynamicPDPTWProblem problemInstance;

	public Simulation(FabriRechtScenario scenario, GPProgram<TruckContext> prog) {
		this(scenario, prog, false);
	}

	public Simulation(final FabriRechtScenario scenario, GPProgram<TruckContext> prog, boolean showGui) {
		program = prog;
		useGui = showGui;

		final CoordModel coordModel = new CoordModel();
		problemInstance = new DynamicPDPTWProblem(scenario, 123, coordModel);

		// plug in the GP truck
		problemInstance.addCreator(AddVehicleEvent.class, new Creator<AddVehicleEvent>() {
			public boolean create(Simulator sim, AddVehicleEvent event) {
				return sim.register(new Truck(event.vehicleDTO, program, scenario));
			}
		});
		// plug in the CoordModel which decides whether to accept a parcel
		problemInstance.addCreator(AddParcelEvent.class, new Creator<AddParcelEvent>() {
			public boolean create(Simulator sim, AddParcelEvent event) {
				if (coordModel.acceptParcel(event.parcelDTO)) {
					return sim.register(new DefaultParcel(event.parcelDTO));
				}
				return true;
			}
		});

		// tardiness is unacceptable -> stop immediately
		problemInstance.addStopCondition(createStopCondition());

		// add an additional renderer for the CoordModel
		problemInstance.addRendererToUI(new CoordModelRenderer());
		if (showGui) {
			problemInstance.enableUI();
		}
	}

	public StatisticsDTO start() {
		return problemInstance.simulate();
	}

	protected StopCondition createStopCondition() {
		return StopCondition.ANY_TARDINESS.or(EARLY_STOP_CONDITION);
	}

	/**
	 * Terminate very bad solutions, stops when more than 10% of the time has
	 * elapsed without one pickup.
	 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
	 */
	public static final StopCondition EARLY_STOP_CONDITION = new StopCondition() {
		@Override
		public boolean isSatisfiedBy(SimulationInfo context) {
			final FabriRechtScenario s = ((FabriRechtScenario) context.scenario);
			final long perc10 = (long) (0.1 * s.timeWindow.end - s.timeWindow.begin);
			return context.stats.simulationTime - s.timeWindow.begin > perc10 && context.stats.totalPickups == 0;
		}
	};

}
