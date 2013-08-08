/**
 * 
 */
package rinde.solver.pdptw;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import rinde.ecj.GPFuncNode;
import rinde.ecj.GPProgram;
import rinde.ecj.GenericFunctions;
import rinde.ecj.Heuristic;
import rinde.evo4mas.gendreau06.GSimulation;
import rinde.evo4mas.gendreau06.GSimulation.Configurator;
import rinde.evo4mas.gendreau06.GendreauContext;
import rinde.evo4mas.gendreau06.Truck;
import rinde.evo4mas.gendreau06.comm.AuctionCommModel;
import rinde.evo4mas.gendreau06.comm.Communicator;
import rinde.evo4mas.gendreau06.comm.RandomBidder;
import rinde.evo4mas.gendreau06.route.SolverRoutePlanner;
import rinde.sim.core.Simulator;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.Model;
import rinde.sim.problem.common.AddParcelEvent;
import rinde.sim.problem.common.AddVehicleEvent;
import rinde.sim.problem.common.ObjectiveFunction;
import rinde.sim.problem.common.ParcelDTO;
import rinde.sim.problem.common.StatsTracker.StatisticsDTO;
import rinde.sim.problem.gendreau06.Gendreau06ObjectiveFunction;
import rinde.sim.problem.gendreau06.Gendreau06Scenario;
import rinde.sim.problem.gendreau06.GendreauTestUtil;
import rinde.sim.scenario.TimedEvent;
import rinde.sim.util.TimeWindow;
import rinde.solver.pdptw.single.HeuristicSolver;
import rinde.solver.pdptw.single.MipSolver;

/**
 * Checks whether the objective as calculated by the simulator via
 * {@link Gendreau06ObjectiveFunction} is 'equal' to the objective as calculated
 * by the {@link SingleVehicleMatrixSolver}. Note that due to the fact that the
 * solver works with integers (doubles are rounded up), a discrepancy is
 * expected. In fact, this discrepancy is checked to see if the objective
 * calculated by the {@link SingleVehicleMatrixSolver} is always worse compared
 * to the objective calculated by the {@link Gendreau06ObjectiveFunction}.
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
@RunWith(Parameterized.class)
public class SingleVehicleSolverTest {

    protected final SingleVehicleMatrixSolver solver;

    static final double EPSILON = 0.1;

    static Heuristic<GendreauContext> DUMMY_HEURISTIC = new GPProgram<GendreauContext>(
            new GPFuncNode<GendreauContext>(
                    new GenericFunctions.Constant<GendreauContext>(0d)));

    public SingleVehicleSolverTest(SingleVehicleMatrixSolver solver) {
        this.solver = solver;
    }

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {//
                { new MipSolver() },
                        { new HeuristicSolver(new MersenneTwister(123)) } });
    }

    @Test
    public void test() {
        final Point a = new Point(0, 0);
        final Point b = new Point(5, 5);
        final Point c = new Point(5, 0);
        final Point d = new Point(0, 5);
        final List<TimedEvent> events = newArrayList();
        events.add(newParcelEvent(a, b));
        events.add(newParcelEvent(c, d));

        final Gendreau06Scenario testScen = GendreauTestUtil.create(events);
        final TestConfigurator tc = new TestConfigurator(solver);
        final StatisticsDTO stats = GSimulation.simulate(testScen, tc, false);
        assertEquals(1, tc.debuggers.size());

        final ObjectiveFunction objFunc = new Gendreau06ObjectiveFunction();
        assertTrue("invalid result", objFunc.isValidResult(stats));
        final double simObj = objFunc.computeCost(stats);

        final List<SolutionObject> solObjs = tc.debuggers.get(0)
                .getOutputMemory();
        assertEquals(1, solObjs.size());
        final double solverObj = solObjs.get(0).objectiveValue / 60.0;

        assertEquals(simObj, solverObj, EPSILON);
        assertTrue("the solver should have a slightly pessimistic view on the world", solverObj > simObj);
    }

    /**
     * Scenario with no tardiness.
     */
    @Test
    public void test2() {
        // results in 10 positions -> 5 packages
        final List<Point> points = newArrayList();
        for (double i = 0.5; i <= 5; i += 3) {
            for (double j = .5; j <= 5; j++) {
                if (i % 3 != 1) {
                    points.add(new Point(i + (j * 0.1), j + (i * 0.1)));
                }
            }
        }

        final List<TimedEvent> events = newArrayList();
        for (int i = 0; i < points.size() / 2; i++) {
            events.add(newParcelEvent(points.get(i), points.get(points.size()
                    - 1 - i)));
        }
        final Gendreau06Scenario testScen = GendreauTestUtil.create(events);
        final TestConfigurator tc = new TestConfigurator(solver);
        final StatisticsDTO stats = GSimulation.simulate(testScen, tc, false);
        assertEquals(1, tc.debuggers.size());

        final ObjectiveFunction objFunc = new Gendreau06ObjectiveFunction();
        assertTrue(objFunc.isValidResult(stats));
        final double simObj = objFunc.computeCost(stats);
        final List<SolutionObject> solObjs = tc.debuggers.get(0)
                .getOutputMemory();
        assertEquals(1, solObjs.size());

        final double solverObj = solObjs.get(0).objectiveValue / 60.0;
        assertEquals(simObj, solverObj, EPSILON);
        assertTrue("the solver should have a slightly pessimistic view on the world", solverObj > simObj);
    }

    /**
     * Scenario where tardiness can not be avoided.
     */
    @Test
    public void test3() {
        // results in 10 positions -> 5 packages
        final List<Point> points = newArrayList();
        for (double i = 0.5; i <= 5; i += 3) {
            for (double j = .5; j <= 5; j++) {
                if (i % 3 != 1) {
                    points.add(new Point(i + (j * 0.1), j + (i * 0.1)));
                }
            }
        }

        final List<TimeWindow> timeWindows = newArrayList();
        for (int i = 0; i < 10; i++) {
            final long startTime = i * 600000;
            timeWindows.add(new TimeWindow(startTime, startTime + 5400000));
        }

        final List<TimedEvent> events = newArrayList();
        for (int i = 0; i < points.size() / 2; i++) {
            events.add(newParcelEvent(points.get(i), points.get(points.size()
                    - 1 - i), timeWindows.get(i), timeWindows.get(points.size()
                    - 1 - i)));
        }

        final Gendreau06Scenario testScen = GendreauTestUtil.create(events);
        final TestConfigurator tc = new TestConfigurator(solver);
        final StatisticsDTO stats = GSimulation.simulate(testScen, tc, false);
        assertEquals(1, tc.debuggers.size());

        final ObjectiveFunction objFunc = new Gendreau06ObjectiveFunction();
        assertTrue(objFunc.isValidResult(stats));
        final double simObj = objFunc.computeCost(stats);

        final List<SolutionObject> solObjs = tc.debuggers.get(0)
                .getOutputMemory();
        assertEquals(1, solObjs.size());

        final double solverObj = solObjs.get(0).objectiveValue / 60.0;
        assertEquals(simObj, solverObj, EPSILON);
        assertTrue("the solver should have a slightly pessimistic view on the world", solverObj > simObj);
    }

    static AddParcelEvent newParcelEvent(Point origin, Point destination) {
        return new AddParcelEvent(new ParcelDTO(origin, destination,
                new TimeWindow(0, 3600000), new TimeWindow(1800000, 5400000),
                0, -1, 300000, 300000));
    }

    static AddParcelEvent newParcelEvent(Point origin, Point destination,
            TimeWindow pickup, TimeWindow delivery) {
        return new AddParcelEvent(new ParcelDTO(origin, destination, pickup,
                delivery, 0, -1, 300000, 300000));
    }

    static class TestConfigurator implements Configurator {
        final List<SolverDebugger> debuggers;
        final SingleVehicleMatrixSolver solver;

        public TestConfigurator(SingleVehicleMatrixSolver solver) {
            this.solver = solver;
            debuggers = newArrayList();
        }

        public boolean create(Simulator sim, AddVehicleEvent event) {
            final Communicator c = new RandomBidder(123);
            sim.register(c);

            final SolverDebugger sd = SolverDebugger.wrap(SolverValidator
                    .wrap(solver), false);
            debuggers.add(sd);
            return sim.register(new Truck(event.vehicleDTO,
                    new SolverRoutePlanner(new SingleVehicleSolverAdapter(sd)),
                    c));
        }

        public Model<?>[] createModels() {
            return new Model<?>[] { new AuctionCommModel() };
        }
    }

}
