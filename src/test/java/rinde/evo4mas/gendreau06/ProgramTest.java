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
		// "(div (mul (mul (mul (div ttl dist) (if4 (mul dist ttl) (add isincargo ado) (div timeuntilavailable (mul (sub (add isincargo mido) (if4 (div (add timeuntilavailable isincargo) (pow (div 0.0 est) mado)) (pow cargosize 0.0) (add timeuntilavailable waiters) (add (sub (div mido isincargo) (sub cargosize timeuntilavailable)) timeuntilavailable))) (div (mul (if4 (add ttl ttl) (if4 est 0.0 timeuntilavailable (sub 0.0 ttl)) (add ttl dist) (add urge 1.0)) (pow (add est ttl) (div mido isincargo))) (pow (div 0.0 est) mado)))) (sub urge timeuntilavailable))) (div (add timeuntilavailable isincargo) (pow 0.0 mado))) (div (pow (add (add est est) urge) (if4 ado mido dist cargosize)) (mul (sub (sub (add mado waiters) (mul ttl waiters)) (mul 0.0 dist)) (if4 cargosize dist dist dist)))) (add (add (sub (div (if4 (div (mul (if4 (add urge 1.0) (if4 est 0.0 timeuntilavailable waiters) (add isincargo mido) (add urge 1.0)) (pow (add est ttl) (div mido isincargo))) isincargo) (sub 0.0 ttl) (sub cargosize timeuntilavailable) (if4 1.0 (div (div (div 0.0 est) (div (pow (add (add est est) urge) (if4 ado mido dist cargosize)) (mul (sub (sub (add mado waiters) (mul urge waiters)) (mul 0.0 dist)) (if4 cargosize dist dist dist)))) (div (add timeuntilavailable isincargo) (add est est))) waiters (add (add est est) urge))) (pow (add urge 1.0) (add ttl urge))) (sub cargosize timeuntilavailable)) (add timeuntilavailable (mul 0.0 urge))) (mul (if4 (if4 dist 1.0 waiters est) (if4 est 0.0 timeuntilavailable waiters) (mul (mul (mul (if4 0.0 waiters mado est) (add isincargo ado)) (div (div dist isincargo) (if4 (if4 ttl mado (add isincargo ado) waiters) (div ttl dist) (add timeuntilavailable waiters) (sub (add (div ttl dist) (div timeuntilavailable 0.0)) 0.0)))) (div (pow (add ttl urge) (if4 ado mido dist cargosize)) (pow (mul dist dist) (if4 cargosize mado timeuntilavailable 1.0)))) (add urge 1.0)) (pow (add est ttl) (div mido isincargo)))))";

		// final String progString =
		// "(sub (if4 (div (div (div (add (div ado mido) (pow ado mado)) (mul (sub dist waiters) (div mido isincargo))) (add (div (sub est timeuntilavailable) (add est 0.0)) (mul (if4 mado urge mido isincargo) (if4 mado mido urge dist)))) (mul urge 0.0)) (add (div cargosize timeuntilavailable) (add 0.0 waiters)) (if4 (sub est isincargo) (mul (pow (sub (mul timeuntilavailable urge) (if4 mado urge (if4 (sub est timeuntilavailable) (add (div cargosize timeuntilavailable) (add 0.0 waiters)) (pow mido 1.0) (pow (sub est timeuntilavailable) (div ado mido))) isincargo)) (if4 (pow 0.0 isincargo) (pow mido 1.0) (pow (sub (mul timeuntilavailable (pow (pow (mul dist mado) (div mado (mul mido 0.0))) (pow (div ado mido) (pow timeuntilavailable waiters)))) (if4 (div (sub est timeuntilavailable) (div cargosize timeuntilavailable)) (add (if4 mado urge mido isincargo) (add 0.0 waiters)) (div mido isincargo) (pow (sub est timeuntilavailable) (pow cargosize waiters)))) (if4 (add 0.0 waiters) (pow mido 1.0) (if4 1.0 est mado timeuntilavailable) (sub ado dist))) (add (div (div ado mido) timeuntilavailable) (add 0.0 waiters)))) 0.0) (pow urge (if4 (add (div (sub est timeuntilavailable) (add est 0.0)) (mul (if4 mado urge mido isincargo) (if4 mado mido urge dist))) (add (div cargosize timeuntilavailable) (add 0.0 waiters)) (if4 (sub est isincargo) (pow mido 1.0) (pow (add (pow (sub (mul timeuntilavailable (pow (pow (mul dist mado) (div mado (mul mido 0.0))) (div cargosize timeuntilavailable))) (if4 mado urge mido isincargo)) (if4 (div (div (add (div ado mido) (pow ado mado)) (mul (sub dist waiters) (div mido isincargo))) (add (div mido isincargo) (if4 mado urge mido isincargo))) (pow mido 1.0) (if4 1.0 est mado timeuntilavailable) (if4 mado urge mido isincargo))) est) (if4 mido est dist 1.0)) (if4 mado urge (if4 (pow mido 1.0) (add (div mado (mul mido 0.0)) (add 0.0 waiters)) (pow mido 1.0) (pow (sub est timeuntilavailable) (pow cargosize waiters))) isincargo)) (pow (sub est timeuntilavailable) (pow cargosize waiters)))) (sub mado dist)) (pow (sub est timeuntilavailable) (pow cargosize waiters))) (pow (sub (mul timeuntilavailable urge) (if4 mado urge (if4 mado urge (if4 (sub est timeuntilavailable) (add (div cargosize (div cargosize timeuntilavailable)) (add 0.0 waiters)) (pow mido 1.0) (pow (pow (sub est timeuntilavailable) (div ado mido)) (div ado mido))) isincargo) isincargo)) (if4 (pow 0.0 isincargo) (pow mido 1.0) (pow (sub (div (div (sub est timeuntilavailable) (mul (sub dist waiters) (add est 0.0))) (add (div (if4 cargosize 0.0 cargosize waiters) (add est 0.0)) (if4 mado urge mido isincargo))) (pow (sub est timeuntilavailable) (if4 mido est dist 1.0))) (if4 (div (div (add (if4 (div (div (add (div ado mido) (pow ado mado)) (mul (sub dist waiters) (div mido isincargo))) (add (div (if4 cargosize 0.0 cargosize waiters) (add est 0.0)) (if4 mado urge mido isincargo))) (pow mido 1.0) (if4 1.0 est mado timeuntilavailable) (if4 mado urge mido isincargo)) (pow ado mado)) (if4 mado urge mido isincargo)) (add (div (if4 cargosize 0.0 cargosize waiters) (add est 0.0)) (if4 mado urge mido isincargo))) (pow mido 1.0) (if4 1.0 est mado timeuntilavailable) (sub ado dist))) (sub ado dist))))";

		// final String progString =
		// "(sub (div (pow (if4 cargosize (mul est urge) isincargo cargosize) (if4 cargosize cargosize isincargo (if4 timeuntilavailable mido urge urge))) (div (add 0.0 timeuntilavailable) (sub (div (add dist (div (mul est urge) (sub 0.0 timeuntilavailable))) timeuntilavailable) timeuntilavailable))) (sub (if4 (pow (div (mul est urge) (sub (sub 0.0 timeuntilavailable) timeuntilavailable)) est) (if4 mado cargosize (add dist (if4 (div (if4 (div (pow (add timeuntilavailable urge) (sub 0.0 (mul est urge))) (pow ttl ttl)) (div (pow (sub 0.0 timeuntilavailable) (mul timeuntilavailable est)) (pow (if4 cargosize (sub 0.0 est) isincargo cargosize) (mul timeuntilavailable est))) (pow (sub 0.0 timeuntilavailable) (div (add (div mado timeuntilavailable) timeuntilavailable) (sub 0.0 timeuntilavailable))) (div (add 0.0 timeuntilavailable) (add dist (div (add 0.0 timeuntilavailable) (div (mul timeuntilavailable est) 1.0))))) timeuntilavailable) 1.0 (sub dist ttl) (div (mul est urge) (sub 0.0 timeuntilavailable)))) isincargo) (mul 0.0 dist) (pow ttl ttl)) (mul (add mido 1.0) (add dist (div mado timeuntilavailable)))))";

		// final String progString =
		// "(add urge (add dist (if4 timeuntilavailable 0.0 0.0 (pow timeuntilavailable timeuntilavailable))))";
		// final String progString = "(urge)";

		// overfitted
		// final String progString =
		// "(sub (mul (pow (mul (sub mado dist) (mul mado timeuntilavailable)) (mul (div ttl mado) (sub waiters isincargo))) (mul (sub (sub (mul (pow (mul est (mul mado timeuntilavailable)) (mul (div ttl mado) (sub waiters isincargo))) (mul (sub (sub (if4 (div dist (pow (sub waiters isincargo) (sub mado waiters))) (add (if4 (sub est timeuntilavailable) dist est 0.0) (add 0.0 waiters)) timeuntilavailable (pow (sub est timeuntilavailable) (pow cargosize waiters))) (mul waiters ado)) (pow 0.0 est)) (div timeuntilavailable (mul ttl 0.0)))) (if4 (sub (mul mido ado) (if4 (if4 ado urge cargosize mado) (mul mido mado) (pow 1.0 ado) (sub (if4 ado (add (pow cargosize waiters) (pow (sub waiters isincargo) (if4 ado urge (sub mado waiters) mado))) (if4 ado (mul mido 0.0) (sub (sub waiters isincargo) isincargo) (add timeuntilavailable 0.0)) (pow 0.0 (pow cargosize (sub est timeuntilavailable)))) (sub timeuntilavailable mado)))) (pow 0.0 est) (div (div (div dist ttl) (sub (mul mido 0.0) (if4 (if4 ado urge cargosize mado) (mul mido mado) (pow 1.0 ado) (sub (if4 ado (pow (sub waiters isincargo) (sub mado waiters)) (if4 (sub est isincargo) (div dist (sub est timeuntilavailable)) (if4 urge dist est 0.0) (mul ttl 0.0)) (sub (mul (if4 urge dist est 0.0) (sub waiters isincargo)) mado)) (div dist (sub est timeuntilavailable)))))) (pow (mul mido ado) (sub timeuntilavailable mado))) (mul ttl timeuntilavailable))) (pow 0.0 est)) (div (sub (mul mido ado) (if4 (pow 0.0 est) (mul mido mado) (pow 1.0 ado) (sub (if4 ado (add (div (sub mado est) (mul urge 0.0)) timeuntilavailable) (if4 (sub est (mul mado timeuntilavailable)) (add (div (sub mado est) (mul urge (add timeuntilavailable (mul waiters ado)))) timeuntilavailable) (if4 urge dist est 0.0) (add timeuntilavailable (if4 ado (add (pow cargosize waiters) (pow 0.0 est)) (if4 (sub est (mul mado timeuntilavailable)) (mul mido 0.0) (sub waiters isincargo) (add timeuntilavailable 0.0)) (pow (mul (if4 urge dist est 0.0) (sub waiters isincargo)) (pow cargosize (sub est timeuntilavailable)))))) (div (div (add timeuntilavailable 0.0) (sub est urge)) (pow (if4 isincargo isincargo 1.0 1.0) (mul waiters ado)))) (sub timeuntilavailable mado)))) (mul ttl 0.0)))) (if4 (sub (mul mido ado) (if4 (if4 ado urge cargosize (add timeuntilavailable 0.0)) (div (div (div dist ttl) (sub est urge)) (pow (if4 isincargo isincargo 1.0 1.0) (mul waiters ado))) (pow 1.0 ado) (mul (if4 (sub (mul mido ado) (if4 (if4 ado urge cargosize mado) (mul mido mado) (pow 1.0 ado) (sub (if4 ado (add (pow cargosize waiters) (pow 0.0 est)) (if4 (add (if4 (sub est timeuntilavailable) dist est 0.0) (add 0.0 waiters)) (mul mido 0.0) (sub waiters isincargo) (add timeuntilavailable 0.0)) (pow 0.0 (pow cargosize (sub est timeuntilavailable)))) (sub timeuntilavailable mado)))) (pow 0.0 est) (div (div (div dist ttl) (sub (mul mido 0.0) (if4 (if4 ado urge cargosize mado) (mul mido mado) (if4 ado urge cargosize mado) (sub (if4 ado (pow (sub waiters isincargo) (sub mado waiters)) (if4 (sub est isincargo) (div dist (sub est timeuntilavailable)) (if4 urge dist est 0.0) (add timeuntilavailable 0.0)) (sub (mul urge 0.0) mado)) (div dist (sub est timeuntilavailable)))))) (pow (mul mido ado) (sub timeuntilavailable mado))) (mul ttl timeuntilavailable)) (sub waiters isincargo)))) (sub (if4 ado (add (div (sub est timeuntilavailable) (mul urge 0.0)) (pow 0.0 est)) (if4 (sub est (mul mado (mul ttl 0.0))) (mul mido 0.0) (if4 urge dist est 0.0) (add timeuntilavailable 0.0)) (pow 0.0 (if4 (pow 0.0 est) (mul mido mado) (sub timeuntilavailable mado) (sub (if4 ado (if4 urge dist est 0.0) (if4 (if4 (if4 ado urge cargosize mado) (div (div (div dist ttl) (sub est urge)) (pow (if4 isincargo isincargo 1.0 1.0) (mul waiters ado))) (pow 1.0 ado) (mul (sub timeuntilavailable mado) (sub waiters isincargo))) (div dist (sub est timeuntilavailable)) (if4 urge dist est 0.0) (add timeuntilavailable 0.0)) (sub (mul (mul urge 0.0) (sub (pow cargosize (sub est timeuntilavailable)) isincargo)) mado)) (div dist (sub est timeuntilavailable)))))) (sub timeuntilavailable mado)) (div (div (div dist ttl) (sub waiters isincargo)) (pow (sub (mul (div ttl (pow 0.0 est)) (div (div ado dist) (mul ttl 0.0))) mado) (sub (mul (div ttl (pow 0.0 est)) (div (div ado dist) (mul ttl 0.0))) mado))) (mul (mul (sub mado dist) (mul mado timeuntilavailable)) timeuntilavailable)))";

		// 732
		// final String progString =
		// "(div (add isincargo midc) (add (if4 (add (mul waiters ttl) (div (div (add 0.0 (sub timeuntilavailable est)) (pow (pow (add waiters 0.0) (mul midc midc)) (div (mul midc midc) (mul 0.0 cargosize)))) (add (if4 (add (add (mul waiters ttl) (add waiters timeuntilavailable)) (add waiters timeuntilavailable)) (mul (div 1.0 urge) (if4 (add mido (pow adc madc)) (div (pow cargosize (add (mul 1.0 timeuntilavailable) (pow adc madc))) (mul midc midc)) (pow dist (if4 ado mido est urge)) (pow adc madc))) (sub (mul (if4 1.0 est dist mido) (add adc (sub (pow adc madc) (pow 1.0 madc)))) (pow 1.0 madc)) (pow (div mado ttl) (sub madc 1.0))) (div (mul (div (div (add 0.0 urge) (pow (pow (add waiters 0.0) (add isincargo midc)) (div (mul (div mado ttl) (add adc timeuntilavailable)) (div urge dist)))) (add (if4 (add (mul waiters ttl) (add waiters timeuntilavailable)) (mul (div 1.0 urge) (pow dist (if4 ado mido est urge))) (sub (div urge mido) (pow (if4 (mul (if4 1.0 est dist mido) (add adc timeuntilavailable)) cargosize (pow dist (if4 ado mido est urge)) (mul (div urge dist) (mul midc midc))) madc)) (mul midc midc)) (div (pow (div mado ttl) (sub madc 1.0)) (add timeuntilavailable 0.0)))) (mul 1.0 timeuntilavailable)) (add timeuntilavailable 0.0))))) (mul (div 1.0 urge) (add 0.0 urge)) (sub (sub timeuntilavailable est) (pow 1.0 madc)) (pow (mul midc midc) (mul (div urge dist) (mul (add (add (mul waiters ttl) (add waiters timeuntilavailable)) (add waiters timeuntilavailable)) midc)))) (div (mul (if4 1.0 est dist mido) (add adc timeuntilavailable)) (add (div urge mido) (div (sub 1.0 est) urge)))))";

		// first evo result
		final String progString = "(add (div (mul (add (pow (pow (sub est timeuntilavailable) (div est timeuntilavailable)) (mul mido waiters)) (add (add (div (mul (pow (if4 (sub (if4 (sub (mul 1.0 0.0) (if4 0.0 urge mado 1.0)) (mul cargosize (sub est timeuntilavailable)) (mul cargosize (if4 midc madc mado madc)) (sub est timeuntilavailable)) (sub isincargo urge)) urge mado 1.0) (mul mido waiters)) (mul waiters dist)) waiters) (add (sub (add (add 0.0 ttl) (sub est timeuntilavailable)) (mul (pow (if4 (sub (sub (sub est ado) adc) (pow (if4 ttl est mido mado) (sub est timeuntilavailable))) urge mado 1.0) (mul mido waiters)) (mul waiters dist))) (if4 (add mado (div isincargo mido)) (div ttl isincargo) (if4 ttl est mido mado) (mul cargosize (if4 midc madc (mul cargosize (if4 midc madc mado (if4 ttl (sub est ado) mido mado))) madc))))) (if4 (if4 ttl est mido mado) (div ttl isincargo) (pow (div isincargo mido) 1.0) (sub (if4 (add (if4 ttl est mido mado) (mul madc cargosize)) (div isincargo mido) (mul cargosize (if4 midc madc mado madc)) (sub est timeuntilavailable)) 1.0)))) (if4 midc madc mado madc)) (if4 ttl (pow mado ado) est (sub isincargo urge))) (add (add (div (mul (sub est timeuntilavailable) (mul waiters dist)) waiters) (add (sub (add (add 0.0 ttl) (sub est timeuntilavailable)) (if4 0.0 urge mado 1.0)) (if4 (add mado (div isincargo mido)) (div ttl isincargo) (if4 ttl est mido mado) (sub (if4 (if4 (add mado (if4 ttl est mido mado)) dist (pow (add (if4 (add madc (mul madc cargosize)) mado (if4 (if4 ttl est mido mado) (sub (sub (sub est ado) adc) (sub isincargo urge)) (pow (div isincargo mido) 1.0) (if4 (add madc (mul madc cargosize)) (pow (if4 ttl est mido mado) (div est timeuntilavailable)) (if4 ttl est mido mado) (sub est timeuntilavailable))) (sub est timeuntilavailable)) (sub est ado)) 1.0) (sub (if4 (add (sub isincargo urge) (mul madc cargosize)) (add mado (pow adc madc)) (mul cargosize (if4 midc madc mado madc)) (div est timeuntilavailable)) 1.0)) (pow (div urge adc) (div est timeuntilavailable)) (mul cargosize (add (pow (pow (if4 ttl est mido mado) (div est timeuntilavailable)) (mul mido waiters)) (pow adc madc))) (sub est timeuntilavailable)) 1.0)))) (if4 (if4 ttl est mido (mul (add (pow (pow (if4 ttl est mido mado) (div est timeuntilavailable)) (mul mido waiters)) (pow (div isincargo mido) 1.0)) (if4 midc madc mado madc))) (div ttl isincargo) (pow (div isincargo mido) 1.0) (sub (if4 (add madc (mul madc cargosize)) (pow (if4 ttl est mido (add mado (div est timeuntilavailable))) (sub est timeuntilavailable)) (if4 (if4 ttl est mido mado) (div ttl isincargo) (if4 ttl est mido mado) (sub (if4 (add madc (sub est timeuntilavailable)) (pow (if4 ttl est mido mado) (sub est timeuntilavailable)) (if4 ttl est mido mado) (sub est timeuntilavailable)) 1.0)) (sub est timeuntilavailable)) 1.0))))";

		final Collection<GPFunc<GendreauContext>> funcs = new GendreauFunctions().create();
		final GPProgram<GendreauContext> prog = GPProgramParser.parseProgramFunc(progString, funcs);

		final GSimulationTask task = GSimulationTask
				.createTestableTask("files/scenarios/gendreau06/req_rapide_1_240_24", prog, 10, true, 1000);

		task.run();

		final Gendreau06ObjectiveFunction obj = new Gendreau06ObjectiveFunction();

		System.out.println(obj.computeCost(task.getComputationResult().stats));
		System.out.println(obj.printHumanReadableFormat(task.getComputationResult().stats));

		System.out.println(task.getComputationResult());

	}
}
