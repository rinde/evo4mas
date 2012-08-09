/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import static java.util.Arrays.asList;

import java.util.Collection;

import rinde.cloud.javainterface.Computer;
import rinde.evo4mas.evo.gp.GPEvaluator;
import rinde.evo4mas.evo.gp.GPProgram;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class FREvaluator extends GPEvaluator<FRSimulationDTO, FRResultDTO, FRContext> {

	@Override
	protected Collection<FRSimulationDTO> createComputationJobs(GPProgram<FRContext> program) {
		return asList(new FRSimulationDTO(program, "../RinSim/problem/data/test/fabri-recht/lc101.scenario"));
	}

	@Override
	protected Computer<FRSimulationDTO, FRResultDTO> createComputer() {
		return new FRSimulationComputer();
	}

	@Override
	protected int expectedNumberOfResultsPerComputationJob() {
		return 1;
	}

}
