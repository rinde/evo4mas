/**
 * 
 */
package rinde.dynscale;

import static com.google.common.collect.Lists.newArrayList;

import java.math.RoundingMode;
import java.util.List;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.graph.Point;
import rinde.sim.scenario.Scenario;

import com.google.common.math.DoubleMath;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class ScenarioGenerator {

	private final long length;
	private final double announcements;
	private final double ordersPerAnnouncement;
	private final int vehicles;
	private final double size;

	// input:
	// - dynamism (distribution?)
	// - scale (factor and ratios, density?)
	// - load
	private ScenarioGenerator(long l, double a, double o, int v, double s) {
		length = l;
		announcements = a;
		ordersPerAnnouncement = o;
		vehicles = v;
		size = s;
	}

	public Scenario generate(RandomGenerator rng) {

		final Point depotLocation = new Point(size / 2d, size / 2d);

		for (int i = 0; i < vehicles; i++) {

		}

		// we model the announcements as a Poisson process, which means that the
		// interarrival times are exponentially distributed.
		final ExponentialDistribution ed = new ExponentialDistribution(1d / announcements);
		double sum = 0;
		final List<Integer> arrivalTimes = newArrayList();
		while (sum < 1) {
			sum += ed.sample();
			arrivalTimes.add(DoubleMath.roundToInt(sum * 60d, RoundingMode.HALF_DOWN));
		}
		// now we know the real number of announcements.

		if (DoubleMath.isMathematicalInteger(ordersPerAnnouncement)) {
			for (final int arrivalTime : arrivalTimes) {
				for (int i = 0; i < ordersPerAnnouncement; i++) {
					// TODO create AddParcelEvent at arrivalTime
				}
			}
		} else {
			// in this case we need to make sure that some announcements contain
			// floor(ordersPerAnnouncement) and some announcements contain
			// ceil(ordersPerAnnouncement) in the right proportion

			final int floor = DoubleMath.roundToInt(ordersPerAnnouncement, RoundingMode.FLOOR);
			final int ceil = DoubleMath.roundToInt(ordersPerAnnouncement, RoundingMode.CEILING);

			// how to find the right proportion?

			final double ratio = ordersPerAnnouncement - floor;

			final int expectedNumOrders = DoubleMath
					.roundToInt(arrivalTimes.size() * ordersPerAnnouncement, RoundingMode.HALF_DOWN);

		}

		return null;
	}

	public static class Builder {

		private int vehicles;
		private double size;
		private double announcementIntensity;
		private double ordersPerAnnouncement;
		private long scenarioLength;

		private Builder() {

		}

		public Builder setAnnouncementIntensityPerKm2(double intensity) {
			announcementIntensity = intensity;
			return this;
		}

		public Builder setScenarioLength(long minutes) {
			scenarioLength = minutes;
			return this;
		}

		// avg number of orders per announcement, must be >= 1
		public Builder setOrdersPerAnnouncement(double orders) {
			ordersPerAnnouncement = orders;
			return this;
		}

		public void setMinimumResponseTime(long minutes) {

		}

		// note: these are averages of the entire area, this says nothing about
		// the actual spatial distribution!
		// num of vehicles per km2
		// num of parcels per km2 -> results in 2*parcels service points
		// area: size x size km
		public Builder setScale(double numVehiclesKM2, double size) {
			this.size = size;
			final double area = size * size;
			vehicles = DoubleMath.roundToInt(numVehiclesKM2 * area, RoundingMode.HALF_DOWN);
			return this;
		}

		public void setLoad(double min, double avg, double max) {

		}

		public ScenarioGenerator build() {

			final double area = size * size;
			final int numAnnouncements = DoubleMath
					.roundToInt(area * scenarioLength * announcementIntensity, RoundingMode.HALF_DOWN);

			return new ScenarioGenerator();
		}
	}

	public static void main(String[] args) {

		final int lambda = 33;
		final double mean = 1d / lambda;
		System.out.println("lambda " + lambda + " mean " + mean);
		final ExponentialDistribution ed = new ExponentialDistribution(mean);
		ed.reseedRandomGenerator(0);

		final double totalTime = 1;
		double sum = 0;
		final List<Double> samples = newArrayList();
		while (sum < totalTime) {
			final double sample = ed.sample();
			sum += sample;
			samples.add(sample);
		}
		System.out.println(samples.size());
		System.out.println(samples);
	}

}
