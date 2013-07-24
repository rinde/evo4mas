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
import rinde.sim.core.model.pdp.PDPScenarioEvent;
import rinde.sim.problem.common.AddDepotEvent;
import rinde.sim.problem.common.AddParcelEvent;
import rinde.sim.problem.common.AddVehicleEvent;
import rinde.sim.problem.common.ParcelDTO;
import rinde.sim.problem.common.VehicleDTO;
import rinde.sim.scenario.Scenario;
import rinde.sim.scenario.ScenarioBuilder;
import rinde.sim.util.TimeWindow;

import com.google.common.collect.ImmutableList;
import com.google.common.math.DoubleMath;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class ScenarioGenerator {

	// private final long length;
	// private final double announcements;
	// private final double ordersPerAnnouncement;
	// private final int vehicles;
	// private final double size;

	private final ArrivalTimesGenerator arrivalTimesGenerator;
	private final LocationsGenerator locationsGenerator;
	private final TimeWindowGenerator timeWindowGenerator;

	private final int vehicles;
	private final Point depotLocation;
	private final long length;

	// input:
	// - dynamism (distribution?)
	// - scale (factor and ratios, density?)
	// - load
	private ScenarioGenerator(ArrivalTimesGenerator atg, LocationsGenerator lg, TimeWindowGenerator twg,
			int numVehicles, Point depotLoc, long scenarioLength) {
		arrivalTimesGenerator = atg;
		locationsGenerator = lg;
		timeWindowGenerator = twg;
		vehicles = numVehicles;
		depotLocation = depotLoc;
		length = scenarioLength;
	}

	public Scenario generate(RandomGenerator rng) {
		final ImmutableList<Long> times = arrivalTimesGenerator.generate(rng);
		final ImmutableList<Point> locations = locationsGenerator.generate(times.size(), rng);
		int index = 0;

		final ScenarioBuilder sb = new ScenarioBuilder(PDPScenarioEvent.ADD_DEPOT, PDPScenarioEvent.ADD_PARCEL,
				PDPScenarioEvent.ADD_VEHICLE);
		sb.addEvent(new AddDepotEvent(-1, depotLocation));

		// TODO move vehicles in own generator?
		// speed, capacity,
		for (int i = 0; i < vehicles; i++) {
			sb.addEvent(new AddVehicleEvent(-1, new VehicleDTO(depotLocation, 40, 1, new TimeWindow(0, length))));
		}

		for (final long time : times) {
			final Point pickup = locations.get(index++);
			final Point delivery = locations.get(index++);
			final ImmutableList<TimeWindow> tws = timeWindowGenerator.generate(time, pickup, delivery, rng);

			sb.addEvent(new AddParcelEvent(new ParcelDTO(pickup, delivery, tws.get(0), tws.get(1), 0, time, 5, 5)));
		}
		return sb.build();
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

		// public void setMinimumResponseTime(long minutes) {
		//
		// }

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

		// public void setLoad(double min, double avg, double max) {
		//
		// }

		public ScenarioGenerator build() {

			final double area = size * size;
			// final int numAnnouncements = DoubleMath
			// .roundToInt(area * scenarioLength * announcementIntensity,
			// RoundingMode.HALF_DOWN);

			final double globalAnnouncementIntensity = area * announcementIntensity;
			final Point depotLoc = new Point(size / 2, size / 2);

			return new ScenarioGenerator(/* */
			new PoissonProcessArrivalTimes(scenarioLength, globalAnnouncementIntensity, ordersPerAnnouncement), /* */
			new NormalLocationsGenerator(size, .15, .05), /* */
			new ProportionateUniformTWGenerator(depotLoc, scenarioLength, 5, 30, 40), /* */
			vehicles, depotLoc, scenarioLength);
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
