/**
 * 
 */
package rinde.dynscale;

import java.util.List;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
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
		final RandomGenerator rng = new MersenneTwister(0);
		for (int i = 0; i < 1000; i++) {
			final List<Long> list = atg.generate(rng);

			System.out.println(list.size() + " " + list);
		}

	}

}
