/**
 * 
 */
package rinde.evo4mas.common;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static rinde.evo4mas.common.TimeWindowLoadUtil.getMaxOverlapLoad;
import static rinde.evo4mas.common.TimeWindowLoadUtil.getMinOverlapLoad;
import static rinde.evo4mas.common.TimeWindowLoadUtil.getOverlapLoadPoints;
import static rinde.evo4mas.common.TimeWindowLoadUtil.hasOverlap;
import static rinde.evo4mas.common.TimeWindowLoadUtil.mergePointsWithSameX;

import java.util.ArrayList;

import org.junit.Test;

import rinde.evo4mas.common.TimeWindowLoadUtil.TimeWindowLoad;
import rinde.sim.core.graph.Point;
import rinde.sim.util.TimeWindow;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class TimeWindowLoadUtilTest {

	public static double EPSILON = 0.000001;

	@Test
	public void testHasOverlap() {

		assertFalse(hasOverlap(new TimeWindow(0, 10), new TimeWindow(15, 20)));
		assertFalse(hasOverlap(new TimeWindow(15, 20), new TimeWindow(0, 10)));
		assertFalse(hasOverlap(new TimeWindow(0, 10), new TimeWindow(10, 20)));
		assertFalse(hasOverlap(new TimeWindow(10, 20), new TimeWindow(0, 10)));

		assertTrue(hasOverlap(new TimeWindow(0, 15), new TimeWindow(10, 20)));
		assertTrue(hasOverlap(new TimeWindow(10, 20), new TimeWindow(0, 15)));
	}

	@Test
	public void testMergePointsWithSameX() {
		assertEquals(newArrayList(new Point(0, 0), new Point(10, 5)), mergePointsWithSameX(newArrayList(new Point(0, 0), new Point(
				10, 1), new Point(10, 4))));
		assertEquals(newArrayList(new Point(-6, 7), new Point(-5, 10)), mergePointsWithSameX(newArrayList(new Point(-6,
				2), new Point(-6, 1), new Point(-6, 4), new Point(-5, 10))));
		assertEquals(newArrayList(new Point(1, 3), new Point(2, 14)), mergePointsWithSameX(newArrayList(new Point(1, 2), new Point(
				1, 1), new Point(2, 4), new Point(2, 10))));
	}

	@Test
	public void testGetOverlapLoadPoints() {
		// all overlap is completely inside
		assertEquals(20d, getOverlapLoadPoints(new TimeWindowLoad(new TimeWindow(0, 10), 1), newArrayList(new Point(2,
				2), new Point(8, 4))), EPSILON);

		// overlap is partially inside (both left and right)
		assertEquals(40d, getOverlapLoadPoints(new TimeWindowLoad(new TimeWindow(0, 10), 1), newArrayList(new Point(-2,
				2), new Point(1, 1), new Point(3, 3), new Point(5, 6), new Point(12, 0))), EPSILON);

		// no overlap
		assertEquals(0, getOverlapLoadPoints(new TimeWindowLoad(new TimeWindow(0, 10), 1), newArrayList(new Point(-2, 2), new Point(
				-1, 0), new Point(10, 3), new Point(15, 6), new Point(16, 0))), EPSILON);

	}

	@Test
	public void testMaxOverlapLoad() {
		// nothing
		assertEquals(0.1, getMaxOverlapLoad(newTWL(5, 15, 0.1), new ArrayList<TimeWindowLoad>()), EPSILON);
		// three non intersecting tws
		assertEquals(1d, getMaxOverlapLoad(newTWL(5, 15, 1), newArrayList(newTWL(0, 3, 2), newTWL(0, 4, 2), newTWL(15, 20, 3))), EPSILON);
		// one partially intersecting tw
		assertEquals(3d, getMaxOverlapLoad(newTWL(5, 15, 1), newArrayList(newTWL(0, 10, 2))), EPSILON);
		// many (non)intersecting tws
		assertEquals(13.5, getMaxOverlapLoad(newTWL(10, 40, 2.5), newArrayList(newTWL(0, 10, 2), newTWL(10, 20, 2), newTWL(15, 20, 2), newTWL(19, 20, 7), newTWL(22, 30, 3), newTWL(25, 45, 2.5))), EPSILON);
	}

	@Test
	public void testMinOverlapLoad() {
		// nothing
		assertEquals(0.1, getMinOverlapLoad(newTWL(5, 15, 0.1), new ArrayList<TimeWindowLoad>()), EPSILON);
		// three non intersecting tws
		assertEquals(1d, getMinOverlapLoad(newTWL(5, 15, 1), newArrayList(newTWL(0, 3, 2), newTWL(0, 4, 2), newTWL(15, 20, 3))), EPSILON);
		// one partially intersecting tw
		assertEquals(1d, getMinOverlapLoad(newTWL(5, 15, 1), newArrayList(newTWL(0, 10, 2))), EPSILON);
		// many (non)intersecting tws
		assertEquals(4.5, getMinOverlapLoad(newTWL(10, 40, 2.5), newArrayList(newTWL(0, 10, 2), newTWL(10, 20, 2), newTWL(15, 20, 2), newTWL(19, 20, 7), newTWL(20, 30, 3), newTWL(25, 45, 2.5))), EPSILON);
	}

	static TimeWindowLoad newTWL(long begin, long end, double load) {
		return new TimeWindowLoad(new TimeWindow(begin, end), load);
	}

}
