/**
 * 
 */
package rinde.evo4mas.fabrirecht;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collection;

import rinde.ecj.GPFunc;
import rinde.ecj.GPProgram;
import rinde.ecj.GPProgramParser;
import rinde.evo4mas.common.TruckContext;
import rinde.evo4mas.fabrirecht.FRFunctions;
import rinde.evo4mas.fabrirecht.Simulation;
import rinde.sim.pdptw.common.StatsTracker.StatisticsDTO;
import rinde.sim.pdptw.fabrirecht.FabriRechtParser;
import rinde.sim.pdptw.fabrirecht.FabriRechtScenario;
import rinde.sim.scenario.ConfigurationException;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class ProgramTester {

	public static void main(String[] args) throws FileNotFoundException, ConfigurationException {
		final FabriRechtScenario scenario = FabriRechtParser.fromJson(new FileReader(
				"files/scenarios/fabri-recht/pdp100_mitAnrufzeit/lc109.scenario"), 10, 4);
		final Collection<GPFunc<TruckContext>> funcs = new FRFunctions().create();
		// final String progString =
		// "(add (div (mul dist ado) (mul 0.0 dist)) (if4 (add (div urge urge) (mul urge 0.0)) (add (mul dist ado) (sub ado ado)) (if4 (add (div urge urge) (mul urge 0.0)) (add (mul dist ado) (sub ado ado)) (add (mul est ado) (mul 1.0 mido)) (if4 (sub dist mado) (sub urge ado) (sub ado ado) (div mado dist))) (if4 (sub dist mado) (sub urge ado) (mul mado ado) (div est dist))))";
		// final String progString =
		// "(add (add (add (div 1.0 urge) (div urge dist)) (sub (mul ado 0.0) (sub 0.0 dist))) (if4 (if4 (add dist dist) (if4 dist urge dist dist) (div urge ado) (add (add (div 1.0 urge) (div urge dist)) (sub (mul ado 0.0) (sub 0.0 dist)))) (div (add urge ado) (sub urge ado)) (sub (div dist urge) (if4 0.0 ado dist 0.0)) (mul (mul urge dist) (add urge 0.0))))";
		// final String progString =
		// "(if4 (sub (sub mido ado) (sub (mul mido mido) (add est urge))) (sub dist ado) (sub (if4 (sub est ado) (mul urge 1.0) (add dist ado) (div 1.0 ttl)) (div (add dist 1.0) (if4 0.0 0.0 mido mido))) (mul (div (sub est 0.0) (if4 ado ado 0.0 urge)) (sub (mul (add (if4 0.0 mado mado est) mado) (sub ttl mido)) (add (if4 ttl mado ttl dist) (add (if4 est dist dist ttl) (div 1.0 0.0))))))";

		// final String progString =
		// "(div (add est ado) (pow (add dist (div (add 0.0 0.0) ado)) dist))";
		// final String progString =
		// "(add (add dist (add (add dist (if4 mado est (pow est 1.0) 0.0)) ado)) ado)";
		// final String progString = "(if4 est dist est urge)";
		// final String progString = "(add dist 0.0)";
		// final String progString =
		// "(add (add (pow ado mido) (add (pow urge est) (add (add (pow (div mido dist) (add urge est)) (div (add (mul mado 0.0) (pow ttl est)) (mul (add urge dist) (pow (sub 1.0 mido) 1.0)))) (mul (mul ttl 1.0) (div (mul mido 0.0) (mul ttl ado)))))) (add (add (pow (div mido dist) (add urge est)) (sub (if4 0.0 ado mido (mul mido 0.0)) (pow dist 1.0))) (div (sub (div (add (pow ado urge) (mul ttl urge)) (sub (add (pow ado urge) (pow dist dist)) urge)) (sub (if4 ttl dist 0.0 est) (pow ttl est))) (sub (div (sub (pow mido ado) urge) (sub 1.0 mido)) (sub (if4 ttl dist ttl est) (mul mido 0.0))))))";

		final String progString = "(sub (pow (pow 1.0 ado) (add mido mido)) (div (add mido mido) (pow (sub 1.0 est) (add ado 1.0))))";
		// final String best =
		// "(if4 (div (mul (sub dist mido) mado) (sub 1.0 0.0)) (add mado (div (add (div (div mido 1.0) mido) (mul (if4 mido urge 1.0 ado) (mul mado ttl))) (if4 mado mido (mul (mul (if4 (sub 1.0 dist) 0.0 urge ado) (mul (sub (pow ttl 0.0) (pow (mul (sub dist mido) (add mado (div 0.0 ttl))) (if4 (if4 urge dist mado est) (if4 0.0 urge urge mado) (mul mado ttl) (div 0.0 ado)))) (add (if4 0.0 dist dist (if4 (div (sub urge mido) (sub est ttl)) (pow mido est) (div (add ado urge) mido) (mul (add dist dist) (div mido 1.0)))) (sub (add 1.0 urge) (pow (mul (if4 1.0 ado ado urge) (add mado (div 0.0 ttl))) (if4 (pow (add mado dist) 0.0) (if4 0.0 urge urge mado) (mul mado ttl) (div 0.0 ado))))))) (sub (add 1.0 urge) (pow (if4 0.0 dist dist mido) (if4 (add mido 0.0) (if4 0.0 urge urge mado) (pow ttl 1.0) (div 0.0 ado))))) (sub urge mido)))) (if4 ado urge 1.0 urge) (if4 (add mido 0.0) (sub ado mido) (add ado urge) (pow ttl 1.0)))";

		// 23/10/2012
		final String best = "(pow (pow dist (pow ttl 1.0)) (mul (div (sub (add est 0.0) (sub est mido)) 1.0) (sub (mul (sub 0.0 urge) (if4 (if4 (mul (div (sub (add est 0.0) (sub est mido)) 1.0) (sub (pow (mul ttl (mul (if4 est 0.0 0.0 mado) (add mido 0.0))) mado) (pow ttl (mul (if4 urge 0.0 0.0 mado) (sub mado 0.0))))) (if4 (if4 (sub (mul ado (mul mido mado)) (pow (pow ado mado) mado)) (if4 urge (sub mado ado) (mul mido 1.0) (pow (pow ado mado) ttl)) (if4 (div mado dist) (div est 0.0) (div dist (sub mido est)) (mul est ado)) (if4 (pow dist (pow ttl (mul (if4 (div (pow urge (mul 0.0 mido)) (add mido 0.0)) (add ado mado) (add mado mido) (mul mido mido)) (sub mado 0.0)))) (mul urge 0.0) (if4 mado 0.0 0.0 (if4 est dist dist ttl)) (add est ado))) urge dist 0.0) est dist) (div (add mado mido) (pow ttl 1.0)) (mul urge 1.0) (add (mul 0.0 (sub (mul (pow ttl (mul (mul mado est) (pow (pow dist (add (pow (add 0.0 ttl) 1.0) (mul 1.0 0.0))) (mul (div (sub (add est 0.0) (sub est mido)) 1.0) (sub (mul ado (mul mido mado)) (pow (pow ado mado) mado)))))) (add 0.0 mido)) est)) (mul (pow (pow (pow dist ttl) urge) 1.0) (add (sub mido mado) (pow dist (pow (div 0.0 mido) (pow (mul ttl mado) (sub ttl 0.0))))))))) (pow (pow ado mado) mado))))";

		// best = "(add dist 0.0)";

		final GPProgram<TruckContext> prog = GPProgramParser.parseProgramFunc(best, funcs);
		// assertEquals(progString, prog.toString());

		final boolean useGui = false;
		final Simulation sim = new Simulation(scenario, prog, useGui);

		final StatisticsDTO stats = sim.start();
		if (!stats.simFinish) {
			System.err.println("SIMULATION DID NOT RUN CORRECTLY");
		}
		System.out.println(stats);

	}
}
