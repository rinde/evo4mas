/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import java.io.IOException;

import rinde.sim.problem.fabrirecht.AddVehicleEvent;
import rinde.sim.problem.fabrirecht.FRDepot;
import rinde.sim.problem.fabrirecht.FRParcel;
import rinde.sim.problem.fabrirecht.FabriRechtParser;
import rinde.sim.problem.fabrirecht.FabriRechtProblem;
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
public class Simulation extends FabriRechtProblem {

	public Simulation(String coordinateFile, String ordersFile) throws IOException, ConfigurationException {
		super(FabriRechtParser.parse(coordinateFile, ordersFile));
		initialize();
	}

	@Override
	protected boolean handleAddVehicle(AddVehicleEvent event) {
		return getSimulator().register(new Truck(event.vehicleDTO));
	}

	@Override
	protected boolean handleTimeOut() {
		System.out.println(statisticsListener.report());
		return true;
	}

	@Override
	protected boolean createUserInterface() {
		final UiSchema schema = new UiSchema(false);
		schema.add(Truck.class, "/graphics/perspective/bus-44.png");
		schema.add(FRDepot.class, "/graphics/flat/warehouse-32.png");
		schema.add(FRParcel.class, "/graphics/flat/hailing-cab-32.png");
		View.startGui(getSimulator(), 1, new PlaneRoadModelRenderer(40), new RoadUserRenderer(schema, false), new PDPModelRenderer());
		return true;
	}

}
