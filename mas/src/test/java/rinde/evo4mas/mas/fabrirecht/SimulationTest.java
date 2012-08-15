/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import rinde.evo4mas.evo.gp.GPFunc;
import rinde.evo4mas.evo.gp.GPProgram;
import rinde.evo4mas.evo.gp.GPProgramParser;
import rinde.evo4mas.evo.gp.GenericFunctions.Constant;
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

	@Test
	public void testProgram1() throws ConfigurationException {
		final Collection<GPFunc<FRContext>> funcs = new GPFunctions().create();
		// (add (if4 (add (add 0.0 1.0) (add 0.0 1.0)) (add ado ado) (if4 dist
		// ado ado 1.0) (if4 1.0 ado dist dist)) (if4 dist 0.0 0.0 dist))
		final String progString = "(add (if4 (add (add 0.0 1.0) (add 0.0 1.0)) (add ado ado) (if4 dist ado ado 1.0) (if4 1.0 ado dist dist)) (if4 dist 0.0 0.0 dist))";
		final GPProgram<FRContext> prog = GPProgramParser.parseProgram(progString, funcs);
		assertEquals(progString, prog.toString());

		final Simulation sim = new Simulation(scenario, prog);
		sim.start();

	}

}
