/**
 * 
 */
package com.github.rinde.evo4mas.fabrirecht;

import rinde.ecj.Heuristic;

import com.github.rinde.evo4mas.common.TruckContext;
import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.pdptw.common.DefaultParcel;
import com.github.rinde.rinsim.pdptw.common.DynamicPDPTWProblem;
import com.github.rinde.rinsim.pdptw.common.StatisticsDTO;
import com.github.rinde.rinsim.pdptw.common.DynamicPDPTWProblem.Creator;
import com.github.rinde.rinsim.pdptw.common.DynamicPDPTWProblem.StopConditions;
import com.github.rinde.rinsim.pdptw.fabrirecht.FabriRechtScenario;
import com.github.rinde.rinsim.scenario.AddParcelEvent;
import com.github.rinde.rinsim.scenario.AddVehicleEvent;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class Simulation {

  protected final Heuristic<TruckContext> program;
  protected final boolean useGui;
  protected final DynamicPDPTWProblem problemInstance;

  public Simulation(FabriRechtScenario scenario, Heuristic<TruckContext> prog) {
    this(scenario, prog, false);
  }

  public Simulation(final FabriRechtScenario scenario,
      Heuristic<TruckContext> prog, boolean showGui) {
    program = prog;
    useGui = showGui;

    final CoordModel coordModel = new CoordModel();
    problemInstance = new DynamicPDPTWProblem(scenario, 123, coordModel);

    // plug in the GP truck
    problemInstance.addCreator(AddVehicleEvent.class,
        new Creator<AddVehicleEvent>() {
          @Override
          public boolean create(Simulator sim, AddVehicleEvent event) {
            return sim.register(new Truck(event.vehicleDTO, program, scenario));
          }
        });
    // plug in the CoordModel which decides whether to accept a parcel
    problemInstance.addCreator(AddParcelEvent.class,
        new Creator<AddParcelEvent>() {
          @Override
          public boolean create(Simulator sim, AddParcelEvent event) {
            if (coordModel.acceptParcel(event.parcelDTO)) {
              return sim.register(new DefaultParcel(event.parcelDTO));
            }
            return true;
          }
        });

    // tardiness is unacceptable -> stop immediately
    problemInstance.addStopCondition(createStopCondition(scenario));

    // add an additional renderer for the CoordModel
    problemInstance.addRendererToUI(new CoordModelRenderer());
    if (showGui) {
      problemInstance.enableUI();
    }
  }

  public StatisticsDTO start() {
    return problemInstance.simulate();
  }

  protected Predicate<Simulator> createStopCondition(final FabriRechtScenario s) {
    return Predicates.or(StopConditions.ANY_TARDINESS,
        /**
         * Terminate very bad solutions, stops when more than 10% of the time
         * has elapsed without one pickup.
         * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
         */
        new Predicate<Simulator>() {
          @Override
          public boolean apply(Simulator context) {
            final long perc10 = (long) (0.1 * s.timeWindow.end - s.timeWindow.begin);
            final StatisticsDTO stats = DynamicPDPTWProblem.getStats(context);
            return stats.simulationTime - s.timeWindow.begin > perc10
                && stats.totalPickups == 0;
          }
        });
  }

}
