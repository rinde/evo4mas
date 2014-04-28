/**
 * 
 */
package rinde.evo4mas.fabrirecht;

import rinde.ecj.Heuristic;
import rinde.evo4mas.common.TruckContext;
import rinde.sim.core.Simulator;
import rinde.sim.pdptw.common.AddParcelEvent;
import rinde.sim.pdptw.common.AddVehicleEvent;
import rinde.sim.pdptw.common.DefaultParcel;
import rinde.sim.pdptw.common.DynamicPDPTWProblem;
import rinde.sim.pdptw.common.DynamicPDPTWProblem.Creator;
import rinde.sim.pdptw.common.DynamicPDPTWProblem.SimulationInfo;
import rinde.sim.pdptw.common.DynamicPDPTWProblem.StopConditions;
import rinde.sim.pdptw.common.StatisticsDTO;
import rinde.sim.pdptw.fabrirecht.FabriRechtScenario;

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
    problemInstance.addStopCondition(createStopCondition());

    // add an additional renderer for the CoordModel
    problemInstance.addRendererToUI(new CoordModelRenderer());
    if (showGui) {
      problemInstance.enableUI();
    }
  }

  public StatisticsDTO start() {
    return problemInstance.simulate();
  }

  protected Predicate<SimulationInfo> createStopCondition() {
    return Predicates.or(StopConditions.ANY_TARDINESS, EARLY_STOP_CONDITION);
  }

  /**
   * Terminate very bad solutions, stops when more than 10% of the time has
   * elapsed without one pickup.
   * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
   */
  public static final Predicate<SimulationInfo> EARLY_STOP_CONDITION = new Predicate<SimulationInfo>() {
    @Override
    public boolean apply(SimulationInfo context) {
      final FabriRechtScenario s = ((FabriRechtScenario) context.scenario);
      final long perc10 = (long) (0.1 * s.timeWindow.end - s.timeWindow.begin);
      return context.stats.simulationTime - s.timeWindow.begin > perc10
          && context.stats.totalPickups == 0;
    }
  };

}
