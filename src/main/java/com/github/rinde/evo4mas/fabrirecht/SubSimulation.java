/**
 * 
 */
package com.github.rinde.evo4mas.fabrirecht;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Set;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.ParcelState;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.pdptw.DefaultParcel;
import com.github.rinde.rinsim.core.pdptw.ParcelDTO;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.pdptw.common.DynamicPDPTWProblem;
import com.github.rinde.rinsim.pdptw.common.DynamicPDPTWProblem.StopConditions;
import com.github.rinde.rinsim.pdptw.fabrirecht.FabriRechtScenario;
import com.github.rinde.rinsim.scenario.TimedEvent;
import com.google.common.base.Predicate;

/**
 * @author Rinde van Lon 
 * 
 */
public class SubSimulation extends Simulation {

  public SubSimulation(final FabriRechtScenario srcScenario,
      final long startTime, Truck t, ParcelDTO firstParcel,
      long processTimeLeft, boolean isPickup, Point truckPos,
      Set<ParcelDTO> todoSet, Set<ParcelDTO> contents) {
    super(new FabriRechtScenario(new ArrayList<TimedEvent>(),
        srcScenario.getPossibleEventTypes(), srcScenario.min,
        srcScenario.max, srcScenario.timeWindow, srcScenario.defaultVehicle), t
        .getProgram());

    if (startTime > 0) {
      // fast forward time
      problemInstance.addStopCondition(new Predicate<Simulator>() {
        @Override
        public boolean apply(Simulator context) {
          return DynamicPDPTWProblem.getStats(context).simulationTime == startTime - 1;
        }
      });
      problemInstance.simulate();
      // problemInstance.getSimulator().addTickListener(this);

    }
    checkState(problemInstance.getStatistics().simulationTime == startTime,
        "time is: %s should be: %s", problemInstance
            .getStatistics().simulationTime, startTime);

    // move truck to current position
    final SubTruck newT = new SubTruck(t.getDTO(), t.getProgram(), srcScenario);
    problemInstance.getSimulator().register(newT);
    final RoadModel roadModel = problemInstance.getSimulator()
        .getModelProvider().getModel(RoadModel.class);
    final PDPModel pdpModel = problemInstance.getSimulator().getModelProvider()
        .getModel(DefaultPDPModel.class);
    roadModel.removeObject(newT);
    roadModel.addObjectAt(newT, truckPos);

    if (firstParcel != null) {
      final long pickupDur = isPickup ? processTimeLeft
          : firstParcel.pickupDuration;
      final long deliverDur = isPickup ? firstParcel.deliveryDuration
          : processTimeLeft;

      final ParcelDTO dto = new ParcelDTO(firstParcel.pickupLocation,
          firstParcel.deliveryLocation,
          firstParcel.pickupTimeWindow, firstParcel.deliveryTimeWindow,
          firstParcel.neededCapacity,
          firstParcel.orderAnnounceTime, pickupDur, deliverDur);

      final Parcel p = new DefaultParcel(dto);
      problemInstance.getSimulator().register(p);
      if (!isPickup) {
        roadModel.removeObject(p);
        pdpModel.addParcelIn(newT, p);
      }
      newT.setFirstActionSubject(p);
    }

    // add cargo
    for (final ParcelDTO p : contents) {
      final Parcel newP = new DefaultParcel(p);
      problemInstance.getSimulator().register(newP);
      roadModel.removeObject(newP);
      pdpModel.addParcelIn(newT, newP);
    }

    // add todo list
    for (final ParcelDTO p : todoSet) {
      final Parcel newP = new DefaultParcel(p);
      problemInstance.getSimulator().register(newP);
      newT.receiveOrder(newP);
    }

    checkState(pdpModel.getParcels(ParcelState.values()).size() == contents
        .size() + todoSet.size()
        + (firstParcel == null ? 0 : 1));
  }

  @Override
  protected Predicate<Simulator> createStopCondition(FabriRechtScenario scenario) {
    return StopConditions.ANY_TARDINESS;
  }

  // this is overridden to avoid early termination of the subsimulation

  // @Override
  // public void stopCriterium(TimeLapse timeLapse) {
  // if (timeLapse.getTime() >= fabriRechtScenario.timeWindow.end) {
  // stop();
  // }
  //
  // }

  // @Override
  // protected ParcelAssesor createParcelAssesor() {
  // return null;
  // }

}
