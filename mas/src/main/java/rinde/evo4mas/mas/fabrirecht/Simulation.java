/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import rinde.evo4mas.evo.gp.GPProgram;
import rinde.sim.core.Simulator;
import rinde.sim.core.TickListener;
import rinde.sim.core.TimeLapse;
import rinde.sim.event.Event;
import rinde.sim.event.Listener;
import rinde.sim.problem.fabrirecht.AddVehicleEvent;
import rinde.sim.problem.fabrirecht.FRDepot;
import rinde.sim.problem.fabrirecht.FRParcel;
import rinde.sim.problem.fabrirecht.FabriRechtProblem;
import rinde.sim.problem.fabrirecht.FabriRechtScenario;
import rinde.sim.problem.fabrirecht.ParcelAssesor;
import rinde.sim.scenario.ConfigurationException;
import rinde.sim.ui.View;
import rinde.sim.ui.renderers.PDPModelRenderer;
import rinde.sim.ui.renderers.PlaneRoadModelRenderer;
import rinde.sim.ui.renderers.RoadUserRenderer;
import rinde.sim.ui.renderers.UiSchema;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class Simulation extends FabriRechtProblem implements Listener {

	// public Simulation(String coordinateFile, String ordersFile) throws
	// IOException, ConfigurationException {
	// super(FabriRechtParser.parse(coordinateFile, ordersFile));
	// initialize();
	// }

	protected final GPProgram<FRContext> program;
	protected final boolean useGui;

	protected boolean shutDownPrematurely = false;

	protected CoordModel coordModel;

	public Simulation(FabriRechtScenario scenario, GPProgram<FRContext> prog) throws ConfigurationException {
		this(scenario, prog, false);
	}

	public Simulation(final FabriRechtScenario scenario, GPProgram<FRContext> prog, boolean showGui)
			throws ConfigurationException {
		super(scenario);

		program = prog;
		useGui = showGui;
		statisticsListener.getEventAPI()
				.addListener(this, StatisticsEventType.PICKUP_TARDINESS, StatisticsEventType.DELIVERY_TARDINESS);
		initialize();
		getSimulator().addTickListener(new TickListener() {
			public void tick(TimeLapse timeLapse) {}

			public void afterTick(TimeLapse timeLapse) {
				stopCriterium(timeLapse);
			}
		});
	}

	public void stopCriterium(TimeLapse timeLapse) {
		final long perc10 = (long) (0.1 * (fabriRechtScenario.timeWindow.end - fabriRechtScenario.timeWindow.begin));
		if (timeLapse.getStartTime() - fabriRechtScenario.timeWindow.begin > perc10
				&& statisticsListener.getTotalPickups() == 0) {
			shutDownPrematurely = true;
			stop();
		}
	}

	@Override
	protected Simulator createSimulator() throws Exception {
		final Simulator sim = super.createSimulator();
		sim.register(coordModel);
		return sim;
	}

	public boolean isShutDownPrematurely() {
		return shutDownPrematurely;
	}

	@Override
	protected boolean handleAddVehicle(AddVehicleEvent event) {
		return getSimulator().register(new Truck(event.vehicleDTO, program, fabriRechtScenario));
	}

	@Override
	protected boolean handleTimeOut() {
		return true;
	}

	@Override
	protected boolean createUserInterface() {
		if (useGui) {
			final UiSchema schema = new UiSchema(false);
			schema.add(Truck.class, "/graphics/perspective/bus-44.png");
			schema.add(FRDepot.class, "/graphics/flat/warehouse-32.png");
			schema.add(FRParcel.class, "/graphics/flat/hailing-cab-32.png");
			View.startGui(getSimulator(), 1, new PlaneRoadModelRenderer(40), new RoadUserRenderer(schema, false), new PDPModelRenderer(), new CoordModelRenderer());
		}
		return useGui;
	}

	@Override
	protected ParcelAssesor createParcelAssesor() {
		coordModel = new CoordModel(pdpModel);
		return coordModel;
	}

	public void handleEvent(Event e) {
		if (e.getEventType() == StatisticsEventType.PICKUP_TARDINESS
				|| e.getEventType() == StatisticsEventType.DELIVERY_TARDINESS) {
			// any tardiness is unacceptable -> stop immediately
			shutDownPrematurely = true;
			stop();

			// if (!(this instanceof SubSimulation)) {
			// throw new RuntimeException();
			// }

		}
	}

}
