/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import static com.google.common.collect.Lists.newArrayList;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import rinde.evo4mas.evo.gp.GPProgram;
import rinde.evo4mas.evo.gp.GPProgramParser;
import rinde.sim.problem.fabrirecht.AddVehicleEvent;
import rinde.sim.problem.fabrirecht.FabriRechtParser;
import rinde.sim.problem.fabrirecht.FabriRechtScenario;
import rinde.sim.scenario.ConfigurationException;
import rinde.sim.scenario.TimedEvent;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class SubSimulationTest {

	FabriRechtScenario scenario;
	GPProgram<FRContext> program;

	@Before
	public void setUp() throws FileNotFoundException {
		final FabriRechtScenario tmp = FabriRechtParser.fromJson(new FileReader(
				"files/scenarios/fabri-recht/pdp100_mitAnrufzeit/lc107.scenario"));
		int vehicleCount = 0;
		final List<TimedEvent> newEventList = newArrayList();
		for (final TimedEvent te : tmp.asList()) {
			if (te instanceof AddVehicleEvent) {
				if (vehicleCount == 0) {
					newEventList.add(te);
					vehicleCount++;
				}
			} else {
				newEventList.add(te);
			}
		}
		scenario = new FabriRechtScenario(newEventList, tmp.getPossibleEventTypes(), tmp.min, tmp.max, tmp.timeWindow);

		program = GPProgramParser.parseProgram("(add dist 0.0)", new GPFunctions().create());
	}

	@Test
	public void test() throws ConfigurationException {
		final Simulation sim = new Simulation(scenario, program);

		sim.start();

		System.out.println(sim.getStatistics());
	}
}
