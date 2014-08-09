/**
 * 
 */
package com.github.rinde.evo4mas.fabrirecht;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import rinde.ecj.GPProgram;
import rinde.ecj.GPProgramParser;

import com.github.rinde.evo4mas.common.TruckContext;
import com.github.rinde.evo4mas.fabrirecht.CoordModel;
import com.github.rinde.evo4mas.fabrirecht.FRFunctions;
import com.github.rinde.evo4mas.fabrirecht.Simulation;
import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.pdp.PDPScenarioEvent;
import com.github.rinde.rinsim.core.pdptw.ParcelDTO;
import com.github.rinde.rinsim.core.pdptw.VehicleDTO;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.pdptw.common.DynamicPDPTWProblem;
import com.github.rinde.rinsim.pdptw.fabrirecht.FabriRechtScenario;
import com.github.rinde.rinsim.scenario.AddDepotEvent;
import com.github.rinde.rinsim.scenario.AddParcelEvent;
import com.github.rinde.rinsim.scenario.AddVehicleEvent;
import com.github.rinde.rinsim.scenario.TimedEvent;
import com.github.rinde.rinsim.util.TimeWindow;
import com.google.common.base.Predicate;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class FeasibilityTest {

  FabriRechtScenario scenario;
  GPProgram<TruckContext> program;

  VehicleDTO vehicleDTO;
  TimeWindow timeWindow;

  AddDepotEvent depotEvent;
  AddVehicleEvent vehicleEvent;
  AddParcelEvent parcelEvent1;

  @Before
  public void setup() {
    timeWindow = new TimeWindow(0, 500);
    depotEvent = new AddDepotEvent(0, new Point(10, 10));
    vehicleEvent = new AddVehicleEvent(0, VehicleDTO.builder()
        .startPosition(new Point(10, 10))
        .speed(1d).capacity(4)
        .availabilityTimeWindow(timeWindow)
        .build());

    parcelEvent1 = new AddParcelEvent(new ParcelDTO(new Point(10, 0),
        new Point(10, 5), new TimeWindow(5, 21), new TimeWindow(10, 40), 1, 5,
        5, 5));

    final TimedEvent timeOutEvent = new TimedEvent(PDPScenarioEvent.TIME_OUT,
        500);

    final Set<Enum<?>> eventTypes = newHashSet();
    eventTypes.addAll(asList(PDPScenarioEvent.values()));

    final Point min = new Point(0, 0);
    final Point max = new Point(20, 20);
    final List<TimedEvent> events = asList(depotEvent, vehicleEvent,
        parcelEvent1, timeOutEvent);
    scenario = new FabriRechtScenario(events, eventTypes, min, max, timeWindow,
        vehicleDTO);

    program = GPProgramParser.parseProgramFunc("(add dist 0.0)",
        new FRFunctions().create());
  }

  @Ignore("needs to be updated or removed")
  @Test
  public void test() {

    final TestSimulation s = new TestSimulation(scenario, program);

    s.continueUntil(4);

    final CoordModel coordModel = s.problemInstance.getSimulator()
        .getModelProvider().getModel(CoordModel.class);

    final ParcelDTO dto = new ParcelDTO(new Point(10, 0), new Point(10, 5),
        new TimeWindow(5, 50), new TimeWindow(10, 100), 1, 5, 5, 5);

    assertTrue(coordModel.acceptParcel(dto));

  }

  class TestSimulation extends Simulation {
    long nextStopTime = -1;

    public TestSimulation(FabriRechtScenario scenario,
        GPProgram<TruckContext> prog) {
      super(scenario, prog);

      problemInstance.addStopCondition(new Predicate<Simulator>() {
        @Override
        public boolean apply(Simulator context) {
          return nextStopTime >= 0
              && nextStopTime >= DynamicPDPTWProblem.getStats(context).simulationTime;
        }
      });
    }

    public void continueUntil(long time) {
      nextStopTime = time;
      start();
    }
  }
}
