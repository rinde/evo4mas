/**
 * 
 */
package com.github.rinde.evo4mas.fabrirecht;

import org.jppf.task.storage.DataProvider;

import com.github.rinde.evo4mas.common.ResultDTO;
import com.github.rinde.evo4mas.common.TruckContext;
import com.github.rinde.rinsim.pdptw.common.StatisticsDTO;
import com.github.rinde.rinsim.pdptw.fabrirecht.FabriRechtParser;
import com.github.rinde.rinsim.pdptw.fabrirecht.FabriRechtScenario;

import rinde.ecj.Heuristic;
import rinde.jppf.ComputationTask;

/**
 * @author Rinde van Lon 
 * 
 */
public class FRSimulationTask extends
    ComputationTask<ResultDTO, Heuristic<TruckContext>> {

  private final String scenarioKey;
  private final int numVehicles;
  private final int vehicleCapacity;

  public FRSimulationTask(String scenario, Heuristic<TruckContext> p,
      int pNumVehicles, int pVehicleCapacity) {
    super(p);
    scenarioKey = scenario;
    numVehicles = pNumVehicles;
    vehicleCapacity = pVehicleCapacity;
  }

  @Override
  public void run() {
    final DataProvider dataProvider = getDataProvider();

    Simulation s = null;
    try {
      final FabriRechtScenario scenario = FabriRechtParser.fromJson(
          (String) dataProvider.getValue(scenarioKey), numVehicles,
          vehicleCapacity);
      s = new Simulation(scenario, taskData);

      final StatisticsDTO stat = s.start();
      float fitness;
      if (!stat.simFinish || stat.acceptedParcels != stat.totalDeliveries
          || stat.acceptedParcels == 0) {
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

        fitness = (float) (stat.totalDistance / (stat.totalPickups + stat.totalDeliveries));
        // fitness = (float) stat.costPerDemand;
      }
      setResult(new ResultDTO(scenarioKey, taskData.getId(), stat, fitness));
    } catch (final Exception e) {
      throw new RuntimeException("Failed simulation task: " + taskData, e);
    }
  }

  @Override
  public ResultDTO getComputationResult() {
    return (ResultDTO) getResult();
  }
}
