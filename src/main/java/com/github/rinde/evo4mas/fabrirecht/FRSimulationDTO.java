package com.github.rinde.evo4mas.fabrirecht;

import static java.util.Arrays.asList;

import java.util.List;

import com.github.rinde.evo4mas.common.TruckContext;

import rinde.ecj.GPProgram;

/**
 * 
 * @author Rinde van Lon 
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