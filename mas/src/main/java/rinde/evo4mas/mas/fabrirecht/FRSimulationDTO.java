package rinde.evo4mas.mas.fabrirecht;

import rinde.evo4mas.evo.gp.GPComputationJob;
import rinde.evo4mas.evo.gp.GPProgram;

/**
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class FRSimulationDTO implements GPComputationJob<FRContext> {

	protected final GPProgram<FRContext> program;
	protected final String scenarioFile;

	public FRSimulationDTO(GPProgram<FRContext> p, String scenario) {
		program = p;
		scenarioFile = scenario;
	}

	public String getComputerClassName() {
		return "rinde.evo4mas.mas.fabrirecht.FRSimulationComputer";
	}

	public GPProgram<FRContext> getProgram() {
		return program;
	}

}