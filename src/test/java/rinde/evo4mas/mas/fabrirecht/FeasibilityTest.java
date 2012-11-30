/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import rinde.ecj.GPProgram;
import rinde.ecj.GPProgramParser;
import rinde.evo4mas.mas.common.TruckContext;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.PDPScenarioEvent;
import rinde.sim.problem.common.AddDepotEvent;
import rinde.sim.problem.common.AddParcelEvent;
import rinde.sim.problem.common.AddVehicleEvent;
import rinde.sim.problem.common.DynamicPDPTWProblem.SimulationInfo;
import rinde.sim.problem.common.DynamicPDPTWProblem.StopCondition;
import rinde.sim.problem.common.ParcelDTO;
import rinde.sim.problem.common.VehicleDTO;
import rinde.sim.problem.fabrirecht.FabriRechtScenario;
import rinde.sim.scenario.TimedEvent;
import rinde.sim.util.TimeWindow;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class FeasibilityTest {

	FabriRechtScenario scenario;
	GPProgram<TruckContext> program;

	VehicleDTO vehicleDTO;
	TimeWindow timeWindow;

	AddDepotEvent depotEvent;
	AddVehicleEvent vehicleEvent;
	AddParcelEvent parcelEvent1;

	@Before
	public void setup() {
		timeWindow = new TimeWindow(0, 500);
		depotEvent = new AddDepotEvent(0, new Point(10, 10));
		vehicleEvent = new AddVehicleEvent(0, new VehicleDTO(new Point(10, 10), 1, 4, timeWindow));

		parcelEvent1 = new AddParcelEvent(new ParcelDTO(new Point(10, 0), new Point(10, 5), new TimeWindow(5, 21),
				new TimeWindow(10, 40), 1, 5, 5, 5));

		final TimedEvent timeOutEvent = new TimedEvent(PDPScenarioEvent.TIME_OUT, 500);

		final Set<Enum<?>> eventTypes = newHashSet();
		eventTypes.addAll(asList(PDPScenarioEvent.values()));

		final Point min = new Point(0, 0);
		final Point max = new Point(20, 20);
		final List<TimedEvent> events = asList(depotEvent, vehicleEvent, parcelEvent1, timeOutEvent);
		scenario = new FabriRechtScenario(events, eventTypes, min, max, timeWindow, vehicleDTO);

		program = GPProgramParser.parseProgramFunc("(add dist 0.0)", new GPFunctions().create());
	}

	@Test
	public void test() {

		final TestSimulation s = new TestSimulation(scenario, program);

		s.continueUntil(4);

		final CoordModel coordModel = s.problemInstance.getSimulator().getModelProvider().getModel(CoordModel.class);

		final ParcelDTO dto = new ParcelDTO(new Point(10, 0), new Point(10, 5), new TimeWindow(5, 50), new TimeWindow(
				10, 100), 1, 5, 5, 5);

		assertTrue(coordModel.acceptParcel(dto));

	}

	class TestSimulation extends Simulation {
		long nextStopTime = -1;

		public TestSimulation(FabriRechtScenario scenario, GPProgram<TruckContext> prog) {
			super(scenario, prog);

			problemInstance.addStopCondition(new StopCondition() {
				@Override
				public boolean isSatisfiedBy(SimulationInfo context) {
					return nextStopTime >= 0 && nextStopTime >= context.stats.simulationTime;
				}
			});
		}

		public void continueUntil(long time) {
			nextStopTime = time;
			start();
		}
	}
}
