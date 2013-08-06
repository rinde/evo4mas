/**
 * 
 */
package rinde.dynscale;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

import rinde.sim.scenario.Scenario;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class ScenarioGeneratorTest {

    @Test
    public void test() {
        final ScenarioGenerator sg = ScenarioGenerator.builder()
                .setAnnouncementIntensityPerKm2(1d)
                .setOrdersPerAnnouncement(1.3) //
                .setScale(.1, 5) //
                .setScenarioLength(120) //
                .build();
        final RandomGenerator rng = new MersenneTwister(123);

        for (int i = 0; i < 1000; i++) {
            final Scenario s = sg.generate(rng);

            Metrics.checkTimeWindowStrictness(s);

            // measure dynamism
            // measure load

            System.out.println(Metrics.measureLoad(s));

            System.out.println(s.size() + " " + Metrics.measureDynamism(s));
        }

    }
}
