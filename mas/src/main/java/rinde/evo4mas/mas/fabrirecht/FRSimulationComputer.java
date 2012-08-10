/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import rinde.cloud.javainterface.Computer;
import rinde.sim.problem.fabrirecht.FabriRechtParser;
import rinde.sim.problem.fabrirecht.FabriRechtProblem.StatisticsDTO;
import rinde.sim.problem.fabrirecht.FabriRechtScenario;
import rinde.sim.scenario.ConfigurationException;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class FRSimulationComputer implements Computer<FRSimulationDTO, FRResultDTO> {

	public FRResultDTO compute(FRSimulationDTO job) {

		// final BufferedReader reader = ;
		// /reader.

		FabriRechtScenario scen = null;
		try {
			scen = FabriRechtParser.fromJson(new BufferedReader(new FileReader(job.scenarioFile)));
		} catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Simulation s = null;
		try {
			s = new Simulation(scen, job.program);
			s.start();
		} catch (final ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		final StatisticsDTO stat = s.getStatistics();
		float fitness;
		if (s.isShutDownPrematurely()) {
			fitness = Float.MAX_VALUE;
		} else {
			fitness = (float) stat.totalDistance;
		}

		return new FRResultDTO(job, fitness);
	}
}
