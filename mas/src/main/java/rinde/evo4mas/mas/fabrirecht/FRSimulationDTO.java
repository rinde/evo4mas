package rinde.evo4mas.mas.fabrirecht;

import static java.util.Arrays.asList;

import java.util.List;

import rinde.evo4mas.evo.gp.GPComputationJob;
import rinde.evo4mas.evo.gp.GPProgram;

/**
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class FRSimulationDTO implements GPComputationJob<FRContext> {

	private static final long serialVersionUID = 8203533494614545727L;
	protected final GPProgram<FRContext> truckHeuristic;
	protected final String scenarioFile;

	public FRSimulationDTO(GPProgram<FRContext> p, String scenario) {
		truckHeuristic = p;
		scenarioFile = scenario;
	}

	public String getComputerClassName() {
		return "rinde.evo4mas.mas.fabrirecht.FRSimulationComputer";
	}

	public List<GPProgram<FRContext>> getPrograms() {
		return asList(truckHeuristic);
	}

	public String getId() {
		return truckHeuristic.toString();
	}

	@Override
	public String toString() {
		return scenarioFile + " " + truckHeuristic.toString();
	}

}