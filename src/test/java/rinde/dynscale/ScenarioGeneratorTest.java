/**
 * 
 */
package rinde.dynscale;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.junit.Test;

import rinde.dynscale.ScenarioGenerator.ArrivalTimesGenerator;
import rinde.dynscale.ScenarioGenerator.PoissonProcessArrivalTimes;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class ScenarioGeneratorTest {

	@Test
	public void testPoissonProcess() {

		final ArrivalTimesGenerator atg = new PoissonProcessArrivalTimes(60, 10, 1.3);

		final double expectedDynamism = 1 / 1.3;
		System.out.println("expected dynamism: " + expectedDynamism);

		final Frequency f = new Frequency();

		final RandomGenerator rng = new MersenneTwister(0);
		for (int i = 0; i < 1000; i++) {
			final List<Long> list = atg.generate(rng);
			dynamismTest(list, expectedDynamism);

			// add the number of announcements
			f.addValue(newHashSet(list).size());

			// System.out.println(list.size() + " " + measureDynamism(list) +
			// " " + list);
		}

		assertTrue(poissonChiSquareTest(f, 10, 0.1));
		assertTrue(poissonChiSquareTest(f, 0.1, 0.0001));
		assertFalse(poissonChiSquareTest(f, 15, 0.001));
		assertFalse(poissonChiSquareTest(f, 1000, 0.0001));

	}

	@Test
	public void test2() {

		final double[] expected = new double[] { .000001, .000002, .000003, .00002 };
		final long[] observed = new long[] { 10, 12, 17, 6 };
		System.out.println(TestUtils.chiSquareTest(expected, observed));
		System.out.println(TestUtils.chiSquareTest(expected, observed, 0.01));
		System.out.println(TestUtils.gTest(expected, observed));
		System.out.println(TestUtils.gTest(expected, observed, 0.01));
	}

	// TODO add determinism tests

	// tests the goodness of fit of the observed frequencies to the expected
	// poisson distribution with specified intensity.
	// we use a confidence interval of 0.1% which means that we only return
	// false
	// in case we know 99.9% sure that the observations do not match the
	// expected
	// distribution.
	static boolean poissonChiSquareTest(Frequency f, double intensity, double confidence) {
		final PoissonDistribution pd = new PoissonDistribution(intensity);

		final long observed[] = new long[f.getUniqueCount()];
		final double[] expected = new double[f.getUniqueCount()];
		final Iterator<?> it = f.valuesIterator();
		int index = 0;
		while (it.hasNext()) {
			final Long l = (Long) it.next();
			observed[index] = f.getCount(l);
			System.out.println(l);
			expected[index] = pd.probability(l.intValue()) * f.getSumFreq();
			if (expected[index] == 0) {
				return false;
			}
			index++;
		}
		System.out.println(Arrays.toString(observed));
		System.out.println(Arrays.toString(expected));
		System.out.println(TestUtils.chiSquare(expected, observed));
		final double chi = TestUtils.chiSquareTest(expected, observed);

		System.out.println(TestUtils.gTest(expected, observed) + " " + TestUtils.gTest(expected, observed, confidence));
		System.out.println(chi);
		return chi < confidence;

	}

	/**
	 * This tests whether the measured dynamism is as close as possible to the
	 * expected dynamism.
	 * @param arrivalTimes
	 * @param expectedDynamism
	 */
	static void dynamismTest(List<Long> arrivalTimes, double expectedDynamism) {

		final int announcements = newHashSet(arrivalTimes).size();

		final int orders = arrivalTimes.size();

		final double actualDynamism = announcements / (double) orders;
		final double dynUp = (announcements + 1) / (double) orders;
		final double dynDown = (announcements - 1) / (double) orders;

		final double actualDist = Math.abs(actualDynamism - expectedDynamism);
		final double distUp = Math.abs(dynUp - expectedDynamism);
		final double distDown = Math.abs(dynDown - expectedDynamism);
		assertTrue(announcements + " " + actualDist + " " + distUp + " " + distDown, actualDist < distUp
				&& actualDist < distDown);
	}

	double measureDynamism(List<Long> arrivalTimes) {
		// announcements are distinct arrival times`
		final int announcements = newHashSet(arrivalTimes).size();
		final int orders = arrivalTimes.size();
		return announcements / (double) orders;
	}

}
