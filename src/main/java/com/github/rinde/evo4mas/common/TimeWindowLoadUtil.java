/**
 * 
 */
package com.github.rinde.evo4mas.common;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * @author Rinde van Lon 
 * 
 */
public final class TimeWindowLoadUtil {

	private TimeWindowLoadUtil() {}

	public static class TimeWindowLoad {
		public final TimeWindow timeWindow;
		public final double load;

		public TimeWindowLoad(TimeWindow tw, double l) {
			timeWindow = tw;
			load = l;
		}
	}

	static long getOverlap(TimeWindow tw1, TimeWindow tw2) {
		return Math.max(0L, Math.min(tw1.end, tw2.end) - Math.max(tw1.begin, tw2.begin));
	}

	static boolean hasOverlap(TimeWindow tw1, TimeWindow tw2) {
		return Math.min(tw1.end, tw2.end) - Math.max(tw1.begin, tw2.begin) > 0;
	}

	static TimeWindow getOverlapInterval(TimeWindow tw1, TimeWindow tw2) {
		final long right = Math.min(tw1.end, tw2.end);
		final long left = Math.max(tw1.begin, tw2.begin);
		if (right - left > 0) {
			return new TimeWindow(left, right);
		}
		return null;
	}

	public static double getOverlapLoad(TimeWindowLoad twl, List<TimeWindowLoad> list) {
		if (list.isEmpty()) {
			return twl.load;
		}
		// sum load within interval
		return getOverlapLoadPoints(twl, getLoads(twl, list));
	}

	// test all boundary conditions, e.g. with just one TW in the list?

	public static double getMaxOverlapLoad(final TimeWindowLoad twl, List<TimeWindowLoad> list) {
		if (list.isEmpty()) {
			return twl.load;
		}
		final List<Point> loads = getLoads(twl, list);
		final Collection<Point> intersection = Collections2.filter(loads, new LoadInTWPredicate(twl.timeWindow));
		if (intersection.isEmpty()) {
			return twl.load;
		}
		return Collections.max(intersection, LOAD_COMPARATOR).y;
	}

	public static double getMinOverlapLoad(TimeWindowLoad twl, List<TimeWindowLoad> list) {
		if (list.isEmpty()) {
			return twl.load;
		}
		final List<Point> loads = getLoads(twl, list);
		final Collection<Point> intersection = Collections2.filter(loads, new LoadInTWPredicate(twl.timeWindow));
		if (intersection.isEmpty()) {
			return twl.load;
		}
		return Collections.min(intersection, LOAD_COMPARATOR).y;
	}

	static List<Point> getLoads(TimeWindowLoad twl, List<TimeWindowLoad> list) {
		final List<Point> overlappingTWs = gatherOverlappingTimeWindows(twl, list);

		// merge points with same x
		final List<Point> mergedDifferentials = mergePointsWithSameX(overlappingTWs);
		// convert to actual loads
		return convertDifferentialsToLoads(mergedDifferentials);
	}

	private static final Comparator<Point> LOAD_COMPARATOR = new Comparator<Point>() {
		public int compare(Point o1, Point o2) {
			return Double.compare(o1.y, o2.y);
		}
	};

	private static class LoadInTWPredicate implements Predicate<Point> {
		protected final TimeWindow timeWindow;

		public LoadInTWPredicate(TimeWindow tw) {
			timeWindow = tw;
		}

		public boolean apply(Point input) {
			return input.x >= timeWindow.begin && input.x < timeWindow.end;
		}
	}

	static List<Point> gatherOverlappingTimeWindows(TimeWindowLoad twl, List<TimeWindowLoad> list) {
		final List<Point> points = newArrayList();
		points.add(new Point(twl.timeWindow.begin, twl.load));
		points.add(new Point(twl.timeWindow.end, -twl.load));
		// gather all overlapping timewindows
		for (final TimeWindowLoad cur : list) {
			if (hasOverlap(twl.timeWindow, cur.timeWindow)) {
				points.add(new Point(cur.timeWindow.begin, cur.load));
				points.add(new Point(cur.timeWindow.end, -cur.load));
			}
		}
		// sort by x (time)
		Collections.sort(points, new Comparator<Point>() {
			public int compare(Point o1, Point o2) {
				return Double.compare(o1.x, o2.x);
			}
		});
		return points;
	}

	static List<Point> convertDifferentialsToLoads(List<Point> differentials) {
		final List<Point> loads = newArrayList();
		double currentLoad = 0;
		for (final Point diff : differentials) {
			currentLoad += diff.y;
			loads.add(new Point(diff.x, currentLoad));
		}
		return loads;
	}

	// expects a list of points where x = time, y = load starting at time x.
	// list is sorted by x. list does not contain points with same x.
	static double getOverlapLoadPoints(TimeWindowLoad twl, List<Point> loads) {
		final Iterator<Point> iterator = loads.iterator();
		// calculate load at beginning of timewindow
		Point cur = iterator.next();
		double curLoad = 0d;
		while (iterator.hasNext() && cur.x <= twl.timeWindow.begin) {
			curLoad = cur.y;
			cur = iterator.next();
		}

		double totalLoad = 0d;
		Point prev = new Point(twl.timeWindow.begin, curLoad);
		while (iterator.hasNext() && cur.x <= twl.timeWindow.end) {
			totalLoad += (cur.x - prev.x) * prev.y;
			prev = cur;
			cur = iterator.next();
		}
		if (cur.x < twl.timeWindow.end) {
			totalLoad += (cur.x - prev.x) * prev.y;
			totalLoad += (twl.timeWindow.end - cur.x) * cur.y;
		} else {
			totalLoad += (twl.timeWindow.end - prev.x) * prev.y;
		}
		return totalLoad;
	}

	// expects a list of points sorted by x in ascending order
	static List<Point> mergePointsWithSameX(List<Point> points) {
		final List<Point> merged = newArrayList();
		final Iterator<Point> it = points.iterator();
		Point cur = it.next();
		while (it.hasNext()) {
			final Point next = it.next();
			if (cur.x == next.x) {
				cur = new Point(cur.x, cur.y + next.y);
			} else {
				merged.add(cur);
				cur = next;
			}
		}
		merged.add(cur);
		return merged;
	}
}
