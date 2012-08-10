/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.junit.Before;
import org.junit.Test;

import rinde.evo4mas.evo.gp.Constant;
import rinde.evo4mas.evo.gp.GPProgram;
import rinde.sim.problem.fabrirecht.FabriRechtParser;
import rinde.sim.problem.fabrirecht.FabriRechtScenario;
import rinde.sim.scenario.ConfigurationException;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class SimulationTest {

	protected FabriRechtScenario scenario;
	protected GPProgram<FRContext> dummyProgram;

	@Before
	public void setup() throws FileNotFoundException {
		scenario = FabriRechtParser
				.fromJson(new FileReader("../../RinSim/problem/data/test/fabri-recht/lc101.scenario"));
		dummyProgram = new GPProgram<FRContext>(new Constant<FRContext>(0));
	}

	@Test
	public void test() throws ConfigurationException {
		final Simulation sim = new Simulation(scenario, dummyProgram, false);

		sim.start();

		System.out.println("shutdown prematurely? " + sim.isShutDownPrematurely());
		System.out.println(sim.getStatistics());

	}
}
