/**
 * 
 */
package rinde.evo4mas.gendreau06;

import java.io.IOException;
import java.util.Collection;

import rinde.ecj.GPFunc;
import rinde.ecj.GPProgram;
import rinde.ecj.GPProgramParser;
import rinde.sim.problem.gendreau06.Gendreau06ObjectiveFunction;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class ProgramTest {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		// final String progString =
		// "(sub (pow (pow 1.0 ado) (add mido mido)) (div (add mido mido) (pow (sub 1.0 est) (add ado 1.0))))";
		// final String progString = "(add (dist) (est))";

		// final String progString =
		// "(sub (pow 0.0 ttl) (if4 0.0 mado mado ttl))";
		// final String progString =
		// "(if4 (pow urge 0.0) dist dist (if4 est urge dist ttl))";
		// final String progString =
		// "(mul (div (add (mul est 0.0) (if4 ttl 1.0 est mado)) (pow (mul 0.0 (div (sub est dist) (if4 mado 0.0 ttl dist))) (pow ttl mado))) (add (mul 0.0 mado) (div (div (add (add (sub ado mado) dist) (mul (div (add (mul est 0.0) (if4 ttl 1.0 est mado)) (pow (mul 0.0 mado) (pow ttl mado))) (add (add (mul est 0.0) (pow ttl mado)) (div (div (sub est dist) (if4 mado 0.0 ttl dist)) (if4 mado 0.0 ttl dist))))) (if4 mado 0.0 ttl dist)) (pow ttl mado))))";

		// final String progString =
		// "(div (div (mul (pow (div 1.0 dist) (add ado (sub (add 1.0 ado) (add ado mido)))) (add (sub mado mido) (if4 dist est urge est))) (pow (div 0.0 (mul 0.0 ado)) (div 0.0 (mul 0.0 ado)))) (if4 (mul (div (mul (pow (div 1.0 dist) (add ado (sub (add 1.0 ado) (add ado mido)))) (add (sub mado mido) (if4 mado 1.0 ttl (pow mido dist)))) (pow (div 0.0 (if4 0.0 urge 0.0 ttl)) (div (mul 0.0 ado) (add ado ttl)))) (mul (div (div (mul (pow (div 1.0 dist) (add ado (sub (add 1.0 ado) (add ado mido)))) (add (add ado ttl) (pow mido est))) (pow (div 0.0 (mul 0.0 ado)) (div (sub est 1.0) (add ado ttl)))) (if4 (mul (if4 (sub est 1.0) (sub mado dist) (mul ttl (pow (div 1.0 dist) (div 0.0 (mul 0.0 ado)))) (if4 mido ttl 1.0 urge)) (mul (if4 mado (div mado ado) ttl (pow mido dist)) (div 0.0 est))) (add (add 0.0 mado) (div 1.0 dist)) (add (add (div ttl est) (div 1.0 ado)) (div (pow mido est) (mul 0.0 ttl))) (add (div (div mado ado) (sub dist mado)) (sub (add 0.0 mado) (mul (if4 (pow ttl urge) (sub mado dist) (mul ttl 0.0) mido) (mul (if4 mado 1.0 ttl (pow mido dist)) (div 0.0 est))))))) (div (mul mido mado) est))) (add ttl (add (add ttl 1.0) (if4 0.0 urge 0.0 ttl))) (add (add (div ttl est) (div 1.0 ado)) (div (pow mido est) (mul 0.0 ttl))) (add (div (div mado ado) (sub dist mado)) (sub (div (if4 ado ado mido urge) (sub dist mado)) (add dist mido)))))";

		// final final String progString =
		// "(sub (if4 (add (pow (if4 dist est ado est) (pow mido mido)) (sub (mul 1.0 ado) (if4 1.0 est mido mado))) (pow (add (div ado ado) (sub mado dist)) (if4 (add urge urge) 1.0 (pow dist 0.0) (add ttl 1.0))) (if4 (add (pow (if4 dist est ado est) (pow mido mido)) (sub (mul 0.0 ado) (if4 1.0 est mido mado))) (pow (add (div ado ado) (sub mado dist)) (pow (add (div ado ado) (sub mado dist)) (if4 (add (pow (add (div ado ado) (sub mado dist)) (if4 (add urge urge) (if4 1.0 (div urge 1.0) mado dist) (pow dist 0.0) (add ttl 1.0))) urge) (if4 1.0 (div urge 1.0) mado dist) (pow dist 0.0) (add ttl 1.0)))) (div (add (sub dist mado) (pow mido ado)) (if4 (mul (mul (add urge ado) (if4 ttl mido ttl 0.0)) (add (sub 0.0 mido) (sub 0.0 urge))) (add ttl est) (mul est mido) (add 1.0 1.0))) (pow (div (add 1.0 0.0) (mul 1.0 0.0)) (div (mul dist 0.0) (sub urge ttl)))) (pow (div (add 1.0 0.0) (mul 1.0 0.0)) (add ado ttl))) (pow (add (div (div ttl ttl) (if4 mado dist ttl 1.0)) (sub (pow ttl urge) (div ttl mido))) (div (add (div ttl mado) (div (div (mul (pow (div 1.0 dist) (add (div (div ttl ttl) (if4 mado dist ttl 1.0)) (sub (pow ttl urge) (div ttl mido)))) (add (sub mado mido) (if4 dist est urge est))) (pow (div (if4 0.0 ttl urge urge) (mul 0.0 ado)) (div (sub est 1.0) (add ado ttl)))) (if4 (mul (if4 (add (sub mido (sub est urge)) (add (add mido est) (mul 1.0 ado))) (if4 (mul (if4 (sub ttl mido) (sub mado dist) (mul ttl mido) (if4 mido ttl 1.0 urge)) (sub (add 1.0 ado) (mul 1.0 0.0))) (pow mido 0.0) (add (sub (if4 1.0 est mido mado) (sub est urge)) (add (add mido est) (mul urge dist))) (add (div (div mado ado) (sub dist mado)) (sub (add 0.0 mado) (add dist mido)))) (mul ttl 0.0) (if4 mido ttl 1.0 urge)) (sub (add 1.0 ado) (add ado mido))) (add (if4 (mul est dist) (sub dist 0.0) (add mido urge) (add (sub 0.0 mido) (sub 0.0 urge))) (div (sub est 1.0) (div (sub 0.0 mido) (sub dist mado)))) (add (sub (pow (if4 dist est ado est) (pow mido mido)) (sub est urge)) (add (add mido est) (mul 1.0 ado))) (add (div (div mado ado) (sub dist mado)) (sub (add 0.0 mado) (add dist mido)))))) (pow (add ado ado) (if4 0.0 mado mido 0.0)))))";
		//
		final String progString = "(add (div (sub est 0.0) (add (if4 mado (if4 (if4 (sub ttl est) cargosize isincargo isincargo) (if4 mado (if4 (add (if4 mado (if4 (if4 1.0 cargosize isincargo isincargo) (sub (sub (mul (if4 mado (if4 (sub mado cargosize) (sub ttl est) 1.0 (div isincargo cargosize)) (sub (sub cargosize dist) (sub isincargo ttl)) (sub mado cargosize)) mado) (sub isincargo ttl)) est) (sub isincargo ttl) (sub 1.0 est)) (sub (sub (div mado 0.0) (sub cargosize dist)) (sub (sub (div mado 0.0) (sub (mul 1.0 mado) (sub isincargo ttl))) ttl)) (sub cargosize ttl)) (sub est 0.0)) (sub ttl est) (div isincargo cargosize) (sub 1.0 est)) (sub (mul (if4 est (sub isincargo ttl) ado 0.0) mado) (sub isincargo ttl)) (sub mado 1.0)) (div isincargo cargosize) (sub 1.0 est)) (sub (mul (if4 mado (if4 (sub mado cargosize) (sub ttl est) 1.0 (div isincargo cargosize)) (sub (sub cargosize dist) (sub isincargo ttl)) (sub mado cargosize)) mado) (sub isincargo ttl)) (sub mado cargosize)) (sub est 0.0))) (sub mido (sub cargosize dist)))";

		final Collection<GPFunc<GendreauContext>> funcs = new GendreauFunctions().create();
		final GPProgram<GendreauContext> prog = GPProgramParser.parseProgramFunc(progString, funcs);

		final GSimulationTask task = GSimulationTask
				.createTestableTask("files/scenarios/gendreau06/req_rapide_1_240_24", prog, 10, true);

		task.run();

		final Gendreau06ObjectiveFunction obj = new Gendreau06ObjectiveFunction();

		System.out.println(obj.computeCost(task.getComputationResult().stats));
		System.out.println(obj.printHumanReadableFormat(task.getComputationResult().stats));

		System.out.println(task.getComputationResult());

	}
}
