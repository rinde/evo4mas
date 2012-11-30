package rinde.evo4mas.mas.fabrirecht;

import static java.util.Arrays.asList;

import java.util.List;

import rinde.ecj.GPProgram;
import rinde.evo4mas.mas.common.TruckContext;

/**
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class FRSimulationDTO {

	private static final long serialVersionUID = 8203533494614545727L;
	protected final GPProgram<TruckContext> truckHeuristic;
	protected final String scenarioFile;

	public FRSimulationDTO(GPProgram<TruckContext> p, String scenario) {
		truckHeuristic = p;
		scenarioFile = scenario;
	}

	// public String getComputerClassName() {
	// return "rinde.evo4mas.mas.fabrirecht.FRSimulationComputer";
	// }

	public List<GPProgram<TruckContext>> getPrograms() {
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