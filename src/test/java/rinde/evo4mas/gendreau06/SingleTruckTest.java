/**
 * 
 */
package rinde.evo4mas.gendreau06;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import rinde.evo4mas.gendreau06.Experiments.RandomRandom;
import rinde.evo4mas.gendreau06.Truck.Goto;
import rinde.evo4mas.gendreau06.Truck.Service;
import rinde.evo4mas.gendreau06.Truck.Wait;
import rinde.sim.core.Simulator;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.PDPModel.ParcelState;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.core.model.pdp.Vehicle;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.problem.common.AddParcelEvent;
import rinde.sim.problem.common.DynamicPDPTWProblem;
import rinde.sim.problem.common.ParcelDTO;
import rinde.sim.problem.gendreau06.Gendreau06Scenario;
import rinde.sim.problem.gendreau06.GendreauTestUtil;
import rinde.sim.scenario.TimedEvent;
import rinde.sim.util.TimeWindow;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class SingleTruckTest {

	protected DynamicPDPTWProblem prob;
	protected Simulator simulator;
	protected RoadModel roadModel;
	protected PDPModel pdpModel;
	protected Truck truck;

	protected ParcelDTO parcel1dto;

	@Before
	public void setUp() {

		final Collection<TimedEvent> events = newArrayList();
		parcel1dto = new ParcelDTO(new Point(1, 1), new Point(3, 3), new TimeWindow(0, 60000),
				new TimeWindow(0, 60000), 0, 1, 3000, 3000);
		events.add(new AddParcelEvent(parcel1dto));
		final Gendreau06Scenario scen = GendreauTestUtil.create(events);

		prob = GSimulation.init(scen, new RandomRandom(123), false);
		simulator = prob.getSimulator();
		roadModel = simulator.getModelProvider().getModel(RoadModel.class);
		pdpModel = simulator.getModelProvider().getModel(PDPModel.class);
		assertNotNull(roadModel);
		assertNotNull(pdpModel);
		assertEquals(0, simulator.getCurrentTime());
		simulator.tick();
		// check that there are no more (other) vehicles
		assertEquals(1, roadModel.getObjectsOfType(Vehicle.class).size());
		// check that the vehicle is of type truck
		assertEquals(1, roadModel.getObjectsOfType(Truck.class).size());
		// make sure there are no parcels yet
		assertTrue(roadModel.getObjectsOfType(Parcel.class).isEmpty());

		truck = roadModel.getObjectsOfType(Truck.class).iterator().next();
		assertNotNull(truck);
		assertEquals(1000, simulator.getCurrentTime());
	}

	@Test
	public void test1() {

		assertTrue(truck.stateMachine.getCurrentState() instanceof Wait);
		assertEquals(truck.getDTO().startPosition, roadModel.getPosition(truck));

		simulator.tick();
		assertEquals(1, roadModel.getObjectsOfType(Parcel.class).size());
		final Parcel parcel1 = roadModel.getObjectsOfType(Parcel.class).iterator().next();
		assertEquals(ParcelState.AVAILABLE, pdpModel.getParcelState(parcel1));
		assertTrue(truck.stateMachine.getCurrentState() instanceof Goto);
		assertFalse(truck.getDTO().startPosition.equals(roadModel.getPosition(truck)));
		assertEquals(parcel1dto.pickupLocation, ((Goto) truck.stateMachine.getCurrentState()).destination);

		while (truck.stateMachine.getCurrentState() instanceof Goto) {
			assertEquals(ParcelState.AVAILABLE, pdpModel.getParcelState(parcel1));
			simulator.tick();
		}
		assertTrue(truck.stateMachine.getCurrentState() instanceof Service);
		assertEquals(ParcelState.PICKING_UP, pdpModel.getParcelState(parcel1));
		assertEquals(parcel1dto.pickupLocation, roadModel.getPosition(truck));

		while (truck.stateMachine.getCurrentState() instanceof Service) {
			assertEquals(parcel1dto.pickupLocation, roadModel.getPosition(truck));
			assertEquals(ParcelState.PICKING_UP, pdpModel.getParcelState(parcel1));
			simulator.tick();
		}
		assertTrue(truck.stateMachine.getCurrentState() instanceof Goto);
		assertEquals(ParcelState.IN_CARGO, pdpModel.getParcelState(parcel1));

	}
}
