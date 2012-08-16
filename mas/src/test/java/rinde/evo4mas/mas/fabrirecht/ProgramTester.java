/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collection;

import rinde.evo4mas.evo.gp.GPFunc;
import rinde.evo4mas.evo.gp.GPProgram;
import rinde.evo4mas.evo.gp.GPProgramParser;
import rinde.sim.problem.fabrirecht.FabriRechtParser;
import rinde.sim.problem.fabrirecht.FabriRechtScenario;
import rinde.sim.scenario.ConfigurationException;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class ProgramTester {

	public static void main(String[] args) throws FileNotFoundException, ConfigurationException {
		final FabriRechtScenario scenario = FabriRechtParser.fromJson(new FileReader(
				"../../RinSim/problem/data/test/fabri-recht/lc101.scenario"));
		final Collection<GPFunc<FRContext>> funcs = new GPFunctions().create();
		// final String progString =
		// "(add (div (mul dist ado) (mul 0.0 dist)) (if4 (add (div urge urge) (mul urge 0.0)) (add (mul dist ado) (sub ado ado)) (if4 (add (div urge urge) (mul urge 0.0)) (add (mul dist ado) (sub ado ado)) (add (mul est ado) (mul 1.0 mido)) (if4 (sub dist mado) (sub urge ado) (sub ado ado) (div mado dist))) (if4 (sub dist mado) (sub urge ado) (mul mado ado) (div est dist))))";
		// final String progString =
		// "(add (add (add (div 1.0 urge) (div urge dist)) (sub (mul ado 0.0) (sub 0.0 dist))) (if4 (if4 (add dist dist) (if4 dist urge dist dist) (div urge ado) (add (add (div 1.0 urge) (div urge dist)) (sub (mul ado 0.0) (sub 0.0 dist)))) (div (add urge ado) (sub urge ado)) (sub (div dist urge) (if4 0.0 ado dist 0.0)) (mul (mul urge dist) (add urge 0.0))))";
		// final String progString =
		// "(if4 (sub (sub mido ado) (sub (mul mido mido) (add est urge))) (sub dist ado) (sub (if4 (sub est ado) (mul urge 1.0) (add dist ado) (div 1.0 ttl)) (div (add dist 1.0) (if4 0.0 0.0 mido mido))) (mul (div (sub est 0.0) (if4 ado ado 0.0 urge)) (sub (mul (add (if4 0.0 mado mado est) mado) (sub ttl mido)) (add (if4 ttl mado ttl dist) (add (if4 est dist dist ttl) (div 1.0 0.0))))))";

		// final String progString =
		// "(div (add est ado) (pow (add dist (div (add 0.0 0.0) ado)) dist))";
		final String progString = "(add (add dist (add (add dist (if4 mado est (pow est 1.0) 0.0)) ado)) ado)";

		final GPProgram<FRContext> prog = GPProgramParser.parseProgram(progString, funcs);
		assertEquals(progString, prog.toString());

		final Simulation sim = new Simulation(scenario, prog, true);
		sim.start();

		System.out.println(sim.getStatistics());
	}

}
