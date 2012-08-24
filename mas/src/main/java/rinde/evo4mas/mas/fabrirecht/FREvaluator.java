/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import static java.util.Arrays.asList;

import java.util.Collection;

import rinde.cloud.javainterface.Computer;
import rinde.evo4mas.evo.gp.GPEvaluator;
import rinde.evo4mas.evo.gp.GPFunc;
import rinde.evo4mas.evo.gp.GPProgram;
import ec.gp.GPTree;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class FREvaluator extends GPEvaluator<FRSimulationDTO, FRResultDTO, FRContext> {

	private static final long serialVersionUID = 7755793133305470461L;

	@Override
	protected Collection<FRSimulationDTO> createComputationJobs(GPTree[] trees) {
		final GPProgram<FRContext> heuristic = new GPProgram<FRContext>((GPFunc<FRContext>) trees[0].child);
		// final GPProgram<FRContext> acceptance = new
		// GPProgram<FRContext>((GPFunc<FRContext>) trees[1].child);
		return asList(new FRSimulationDTO(heuristic, "files/scenarios/fabri-recht/pdp100_mitAnrufzeit/lc107.scenario"));
	}

	@Override
	protected Computer<FRSimulationDTO, FRResultDTO> createComputer() {
		return new FRSimulationComputer();
	}

	@Override
	protected int expectedNumberOfResultsPerGPIndividual() {
		return 1;
	}

}
