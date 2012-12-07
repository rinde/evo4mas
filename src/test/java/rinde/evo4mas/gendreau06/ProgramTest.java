/**
 * 
 */
package rinde.evo4mas.gendreau06;

import java.io.IOException;
import java.util.Collection;

import rinde.ecj.GPFunc;
import rinde.ecj.GPProgram;
import rinde.ecj.GPProgramParser;
import rinde.evo4mas.common.TruckContext;
import rinde.evo4mas.fabrirecht.GPFunctions;
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
		final String progString = "(add (dist) (est))";

		final Collection<GPFunc<TruckContext>> funcs = new GPFunctions().create();
		final GPProgram<TruckContext> prog = GPProgramParser.parseProgramFunc(progString, funcs);

		final GSimulationTask task = GSimulationTask
				.createTestableTask("files/scenarios/gendreau06/req_rapide_1_240_24", prog, 10, false);

		task.run();

		final Gendreau06ObjectiveFunction obj = new Gendreau06ObjectiveFunction();

		System.out.println(obj.computeCost(task.getComputationResult().stats));
		System.out.println(obj.printHumanReadableFormat(task.getComputationResult().stats));

		System.out.println(task.getComputationResult());

	}

}
