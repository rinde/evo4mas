/**
 * 
 */
package rinde.evo4mas.fabrirecht;

import org.jppf.task.storage.DataProvider;

import rinde.ecj.ComputationTask;
import rinde.ecj.GPProgram;
import rinde.evo4mas.common.ResultDTO;
import rinde.evo4mas.common.TruckContext;
import rinde.sim.problem.common.StatsTracker.StatisticsDTO;
import rinde.sim.problem.fabrirecht.FabriRechtParser;
import rinde.sim.problem.fabrirecht.FabriRechtScenario;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class FRSimulationTask extends ComputationTask<ResultDTO, TruckContext> {

	private final String scenarioKey;
	private final int numVehicles;
	private final int vehicleCapacity;

	public FRSimulationTask(String scenario, GPProgram<TruckContext> p, int pNumVehicles, int pVehicleCapacity) {
		super(p);
		scenarioKey = scenario;
		program = p;
		numVehicles = pNumVehicles;
		vehicleCapacity = pVehicleCapacity;
	}

	public void run() {
		final DataProvider dataProvider = getDataProvider();

		Simulation s = null;
		try {
			final FabriRechtScenario scenario = FabriRechtParser
					.fromJson((String) dataProvider.getValue(scenarioKey), numVehicles, vehicleCapacity);
			s = new Simulation(scenario, program);

			final StatisticsDTO stat = s.start();
			float fitness;
			if (!stat.simFinish || stat.acceptedParcels != stat.totalDeliveries || stat.acceptedParcels == 0) {
				fitness = Float.MAX_VALUE;
			} else {

				// final float rejectionPenalty = (stat.totalParcels -
				// stat.acceptedParcels);// *
				// 1000f;

				// final float pickupFailPenalty = (stat.acceptedParcels -
				// stat.totalPickups) * 50f;
				// final float deliveryFailPenalty = (stat.acceptedParcels -
				// stat.totalDeliveries) * 50f;

				// fitness = rejectionPenalty + pickupFailPenalty +
				// deliveryFailPenalty + stat.pickupTardiness
				// + stat.deliveryTardiness + (float) stat.totalDistance;

				// final float distPenalty = 1f - (1f / (float)
				// stat.totalDistance);
				// fitness = rejectionPenalty + distPenalty;
				fitness = (float) stat.costPerDemand;
			}
			setResult(new ResultDTO(scenarioKey, program, stat, fitness));
		} catch (final Exception e) {
			throw new RuntimeException("Failed simulation task: " + program, e);
		}
	}

	@Override
	public String getGPId() {
		return program.toString();
	}

	@Override
	public ResultDTO getComputationResult() {
		return (ResultDTO) getResult();
	}
}