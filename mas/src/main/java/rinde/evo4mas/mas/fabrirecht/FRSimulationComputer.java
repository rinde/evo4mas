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

	public FRSimulationComputer() {}

	public FRResultDTO compute(FRSimulationDTO job) {

		// final BufferedReader reader = ;
		// /reader.

		// TODO scenario caching!
		FabriRechtScenario scen = null;
		try {
			scen = FabriRechtParser.fromJson(new BufferedReader(new FileReader(job.scenarioFile)));
		} catch (final FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		Simulation s = null;
		try {
			s = new Simulation(scen, job.truckHeuristic);
			s.start();
		} catch (final ConfigurationException e) {
			throw new RuntimeException(e);
		}

		final StatisticsDTO stat = s.getStatistics();
		float fitness;
		if (s.isShutDownPrematurely() || stat.acceptedParcels != stat.totalDeliveries) {
			fitness = Float.MAX_VALUE;
		} else {

			final float rejectionPenalty = (stat.totalParcels - stat.acceptedParcels);// *
																						// 1000f;

			// final float pickupFailPenalty = (stat.acceptedParcels -
			// stat.totalPickups) * 50f;
			// final float deliveryFailPenalty = (stat.acceptedParcels -
			// stat.totalDeliveries) * 50f;

			// fitness = rejectionPenalty + pickupFailPenalty +
			// deliveryFailPenalty + stat.pickupTardiness
			// + stat.deliveryTardiness + (float) stat.totalDistance;

			final float distPenalty = 1f - (1f / (float) stat.totalDistance);
			fitness = rejectionPenalty + distPenalty;
		}

		return new FRResultDTO(job, stat, fitness);
	}
}
