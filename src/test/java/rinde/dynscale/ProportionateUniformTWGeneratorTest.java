/**
 * 
 */
package rinde.dynscale;

import static org.junit.Assert.assertTrue;

import java.math.RoundingMode;
import java.util.List;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

import rinde.sim.core.graph.Point;
import rinde.sim.util.TimeWindow;

import com.google.common.math.DoubleMath;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class ProportionateUniformTWGeneratorTest {

	@Test
	public void test() {
		final long serviceTime = 5;
		final long endTime = 180;
		final Point depotLocation = new Point(5, 5);
		final ProportionateUniformTWGenerator twg = new ProportionateUniformTWGenerator(depotLocation, endTime,
				serviceTime, 30, 40);
		final RandomGenerator rng = new MersenneTwister(123);
		for (int i = 0; i < 10000; i++) {
			final Point p1 = new Point(6, 6);
			final Point p2 = new Point(4, 4);
			final List<TimeWindow> tws = twg
					.generate(DoubleMath.roundToLong(rng.nextDouble() * 120, RoundingMode.HALF_DOWN), p1, p2, rng);
			assertTrue(tws.toString(), tws.get(0).end <= tws.get(1).end + twg.travelTime(p1, p2) + serviceTime);
			assertTrue(tws.toString() + " tt: " + twg.travelTime(p1, p2), tws.get(0).begin + twg.travelTime(p1, p2)
					+ serviceTime <= tws.get(1).begin);
			assertTrue(tws.get(1).end <= endTime - (twg.travelTime(p2, depotLocation) + serviceTime));

		}
	}
}
