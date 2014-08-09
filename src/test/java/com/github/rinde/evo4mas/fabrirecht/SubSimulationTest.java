/**
 * 
 */
package com.github.rinde.evo4mas.fabrirecht;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.github.rinde.evo4mas.common.TruckContext;
import com.github.rinde.evo4mas.fabrirecht.FRFunctions;
import com.github.rinde.evo4mas.fabrirecht.SubSimulation;
import com.github.rinde.evo4mas.fabrirecht.SubTruck;
import com.github.rinde.evo4mas.fabrirecht.Truck;
import com.github.rinde.rinsim.core.graph.Point;
import com.github.rinde.rinsim.core.model.pdp.PDPScenarioEvent;
import com.github.rinde.rinsim.core.pdptw.ParcelDTO;
import com.github.rinde.rinsim.core.pdptw.VehicleDTO;
import com.github.rinde.rinsim.pdptw.common.StatisticsDTO;
import com.github.rinde.rinsim.pdptw.fabrirecht.FabriRechtScenario;
import com.github.rinde.rinsim.scenario.TimedEvent;
import com.github.rinde.rinsim.util.TimeWindow;

import rinde.ecj.GPProgram;
import rinde.ecj.GPProgramParser;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class SubSimulationTest {

  private static final double EPSILON = 0.0001;

  FabriRechtScenario scenario;
  GPProgram<TruckContext> program;

  VehicleDTO vehicleDTO;
  TimeWindow timeWindow;

  @Before
  public void setUp() throws FileNotFoundException {
    program = GPProgramParser.parseProgramFunc("(add dist 0.0)",
        new FRFunctions().create());

    timeWindow = new TimeWindow(0, 500);
    vehicleDTO = VehicleDTO.builder()
        .startPosition(new Point(10, 10))
        .speed(1d)
        .capacity(4)
        .availabilityTimeWindow(timeWindow)
        .build();
    final Point min = new Point(0, 0);
    final Point max = new Point(20, 20);
    final Set<Enum<?>> eventTypes = newHashSet();
    eventTypes.addAll(asList(PDPScenarioEvent.values()));
    scenario = new FabriRechtScenario(new ArrayList<TimedEvent>(), eventTypes,
        min, max, timeWindow, vehicleDTO);
  }

  @Ignore("needs to be updated or removed")
  @Test
  public void subTest1() {
    final Truck t = new SubTruck(vehicleDTO, program, scenario);
    final Set<ParcelDTO> contents = newHashSet();
    final Set<ParcelDTO> todo = newHashSet(new ParcelDTO(new Point(10, 0),
        new Point(10, 5), new TimeWindow(10, 100), new TimeWindow(50, 120), 1,
        0, 10, 10));
    final SubSimulation subsim = new SubSimulation(scenario, 0, t, null, 0,
        false, new Point(10, 10), todo, contents);

    final StatisticsDTO stats = subsim.start();
    assertFalse(stats.simFinish);
    assertEquals(1, stats.totalDeliveries);
    assertEquals(scenario.timeWindow.end,
        subsim.problemInstance.getStatistics().simulationTime);
    assertEquals(0, stats.deliveryTardiness);
    assertEquals(20, stats.totalDistance, EPSILON);
  }

  @Ignore("needs to be updated or removed")
  @Test
  public void subTest2() {
    final Truck t = new SubTruck(vehicleDTO, program, scenario);
    final Set<ParcelDTO> contents = newHashSet();
    final Set<ParcelDTO> todo = newHashSet(new ParcelDTO(new Point(10, 0),
        new Point(10, 5), new TimeWindow(10, 100), new TimeWindow(50, 120), 1,
        0, 10, 10));
    final SubSimulation subsim = new SubSimulation(scenario, 3, t, null, 0,
        false, new Point(10, 10), todo, contents);

    final StatisticsDTO stats = subsim.start();
    assertFalse(stats.simFinish);
    assertEquals(1, stats.totalDeliveries);
    assertEquals(scenario.timeWindow.end,
        subsim.problemInstance.getStatistics().simulationTime);
    assertEquals(0, stats.deliveryTardiness);
    assertEquals(20, stats.totalDistance, EPSILON);

  }
}
