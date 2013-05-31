/**
 * 
 */
package rinde.solver.spdptw;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import rinde.ecj.GPFuncNode;
import rinde.ecj.GPProgram;
import rinde.ecj.GenericFunctions;
import rinde.ecj.Heuristic;
import rinde.evo4mas.gendreau06.AuctionOptTruck;
import rinde.evo4mas.gendreau06.GSimulationTask;
import rinde.evo4mas.gendreau06.GendreauContext;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.problem.common.AddParcelEvent;
import rinde.sim.problem.common.DynamicPDPTWProblem;
import rinde.sim.problem.common.ObjectiveFunction;
import rinde.sim.problem.common.ParcelDTO;
import rinde.sim.problem.gendreau06.Gendreau06ObjectiveFunction;
import rinde.sim.problem.gendreau06.Gendreau06Scenario;
import rinde.sim.problem.gendreau06.GendreauTestUtil;
import rinde.sim.scenario.TimedEvent;
import rinde.sim.util.TimeWindow;

/**
 * Checks whether the objective as calculated by the simulator via
 * {@link Gendreau06ObjectiveFunction} is 'equal' to the objective as calculated
 * by the {@link Solver}. Note that due to the fact that the solver works with
 * integers (doubles are rounded up), a discrepancy is expected. In fact, this
 * discrepancy is checked to see if the objective calculated by the
 * {@link Solver} is always worse compared to the objective calculated by the
 * {@link Gendreau06ObjectiveFunction}.
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public class SolverTest {

	static final double EPSILON = 0.1;

	static Heuristic<GendreauContext> DUMMY_HEURISTIC = new GPProgram<GendreauContext>(new GPFuncNode<GendreauContext>(
			new GenericFunctions.Constant<GendreauContext>(0d)));

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
		final TestSimTask sim = new TestSimTask(testScen, false);
		sim.run();

		final ObjectiveFunction objFunc = new Gendreau06ObjectiveFunction();
		assertTrue(objFunc.isValidResult(sim.getComputationResult().stats));
		final double simObj = objFunc.computeCost(sim.getComputationResult().stats);
		final double solverObj = sim.truck.getSolutionObject().objectiveValue / 60.0;

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
			events.add(newParcelEvent(points.get(i), points.get(points.size() - 1 - i)));
		}
		final Gendreau06Scenario testScen = GendreauTestUtil.create(events);
		final TestSimTask sim = new TestSimTask(testScen, false);
		sim.run();

		final ObjectiveFunction objFunc = new Gendreau06ObjectiveFunction();
		assertTrue(objFunc.isValidResult(sim.getComputationResult().stats));
		final double simObj = objFunc.computeCost(sim.getComputationResult().stats);
		final double solverObj = sim.truck.getSolutionObject().objectiveValue / 60.0;

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
			events.add(newParcelEvent(points.get(i), points.get(points.size() - 1 - i), timeWindows.get(i), timeWindows
					.get(points.size() - 1 - i)));
		}
		final Gendreau06Scenario testScen = GendreauTestUtil.create(events);
		final TestSimTask sim = new TestSimTask(testScen, true);
		sim.run();

		final ObjectiveFunction objFunc = new Gendreau06ObjectiveFunction();
		assertTrue(objFunc.isValidResult(sim.getComputationResult().stats));
		final double simObj = objFunc.computeCost(sim.getComputationResult().stats);
		final double solverObj = sim.truck.getSolutionObject().objectiveValue / 60.0;

		assertEquals(simObj, solverObj, EPSILON);
		assertTrue("the solver should have a slightly pessimistic view on the world", solverObj > simObj);
	}

	AddParcelEvent newParcelEvent(Point origin, Point destination) {
		return new AddParcelEvent(new ParcelDTO(origin, destination, new TimeWindow(0, 3600000), new TimeWindow(
				1800000, 5400000), 0, -1, 300000, 300000));
	}

	AddParcelEvent newParcelEvent(Point origin, Point destination, TimeWindow pickup, TimeWindow delivery) {
		return new AddParcelEvent(new ParcelDTO(origin, destination, pickup, delivery, 0, -1, 300000, 300000));
	}

	/**
	 * Test class for simulations with a dummy heuristic. This class is intended
	 * to allow testing of a single vehicle.
	 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
	 */
	class TestSimTask extends GSimulationTask {

		private static final long serialVersionUID = -8787595237384109794L;
		protected Gendreau06Scenario scenario;
		protected AuctionOptTruck truck;
		protected DynamicPDPTWProblem problem;

		protected final boolean showGui;

		public TestSimTask(Gendreau06Scenario scen, boolean showGui) {
			super(null, DUMMY_HEURISTIC, -1, 1000, SolutionType.AUCTION_OPT);
			this.showGui = showGui;
			scenario = scen;
		}

		@Override
		public void run() {
			runOnScenario(scenario);

			final Set<AuctionOptTruck> trucks = problem.getSimulator().getModelProvider().getModel(RoadModel.class)
					.getObjectsOfType(AuctionOptTruck.class);
			checkState(trucks.size() == 1);
			truck = trucks.iterator().next();
		}

		@Override
		protected void preSimulate(DynamicPDPTWProblem p) {
			problem = p;
			if (showGui) {
				problem.enableUI(new GendreauUI(problem, true, false));
			}
		}

	}
}
