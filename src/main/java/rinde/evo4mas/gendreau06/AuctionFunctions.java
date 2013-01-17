/**
 * 
 */
package rinde.evo4mas.gendreau06;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import rinde.ecj.GPFunc;
import rinde.ecj.GPFuncSet;
import rinde.ecj.GenericFunctions.Add;
import rinde.ecj.GenericFunctions.Constant;
import rinde.ecj.GenericFunctions.Div;
import rinde.ecj.GenericFunctions.If4;
import rinde.ecj.GenericFunctions.Mul;
import rinde.ecj.GenericFunctions.Pow;
import rinde.ecj.GenericFunctions.Sub;
import rinde.evo4mas.common.GPFunctions.Ado;
import rinde.evo4mas.common.GPFunctions.Dist;
import rinde.evo4mas.common.GPFunctions.Est;
import rinde.evo4mas.common.GPFunctions.Mado;
import rinde.evo4mas.common.GPFunctions.Mido;
import rinde.evo4mas.common.GPFunctions.Ttl;
import rinde.evo4mas.common.GPFunctions.Urge;
import rinde.evo4mas.common.TimeWindowLoadUtil;
import rinde.evo4mas.common.TimeWindowLoadUtil.TimeWindowLoad;
import rinde.evo4mas.gendreau06.GendreauFunctions.Adc;
import rinde.evo4mas.gendreau06.GendreauFunctions.CargoSize;
import rinde.evo4mas.gendreau06.GendreauFunctions.IsInCargo;
import rinde.evo4mas.gendreau06.GendreauFunctions.Madc;
import rinde.evo4mas.gendreau06.GendreauFunctions.Midc;
import rinde.evo4mas.gendreau06.GendreauFunctions.TimeUntilAvailable;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.problem.common.DefaultParcel;
import rinde.sim.problem.common.ParcelDTO;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class AuctionFunctions extends GPFuncSet<GendreauContext> {

	private static final long serialVersionUID = 155866319078208865L;

	@SuppressWarnings("unchecked")
	@Override
	public Collection<GPFunc<GendreauContext>> create() {
		return newArrayList(
		/* GENERIC FUNCTIONS */
		new If4<GendreauContext>(), /* */
				new Add<GendreauContext>(), /* */
				new Sub<GendreauContext>(), /* */
				new Div<GendreauContext>(), /* */
				new Mul<GendreauContext>(), /* */
				new Pow<GendreauContext>(),
				/* CONSTANTS */
				new Constant<GendreauContext>(1), /* */
				new Constant<GendreauContext>(0), /* */
				/* DOMAIN SPECIFIC FUNCTIONS */
				new CargoSize<GendreauContext>(), /* */
				new IsInCargo<GendreauContext>(), /* */
				new TimeUntilAvailable<GendreauContext>(), /* */
				new Ado<GendreauContext>(), /* */
				new Mido<GendreauContext>(), /* */
				new Mado<GendreauContext>(), /* */
				new Dist<GendreauContext>(), /* */
				new Urge<GendreauContext>(), /* */
				new Est<GendreauContext>(), /* */
				new Ttl<GendreauContext>(), /* */
				new Adc<GendreauContext>(), /* */
				new Midc<GendreauContext>(), /* */
				new Madc<GendreauContext>(), /* */
				/* AUCTION SPECIFIC FUNCTIONS */
				new Diameter(), /* */
				new Radius(), /* */
				new RelEcc(), /* */
				new TotalTimeWindowOverlapLoad(), /* */
				new MaxTimeWindowOverlapLoad(), /* */
				new MinTimeWindowOverlapLoad(), /* */
				new MinDistToServicePoints());
	}

	/**
	 * Computes the diameter of the current todo set. 'The diameter of a graph
	 * is the maximum eccentricity of any vertex in the graph' and 'The
	 * eccentricity <code>e</code> of a vertex <code>v</code> is the greatest
	 * geodesic distance between <code>v</code> and any other vertex' definition
	 * from <a
	 * href="http://en.wikipedia.org/wiki/Distance_(graph_theory)">Wikipedia</a>
	 */
	public static class Diameter extends GPFunc<GendreauContext> {
		private static final long serialVersionUID = -2612484686166148770L;

		@Override
		public double execute(double[] input, GendreauContext context) {
			return Collections.max(distances(gatherListOfPointsToBeVisited(context)));
		}
	}

	/**
	 * Computes the radius of the current todo set. 'The radius of a graph is
	 * the minimum eccentricity of any vertex.' and 'The eccentricity
	 * <code>e</code> of a vertex <code>v</code> is the greatest geodesic
	 * distance between <code>v</code> and any other vertex' definition from <a
	 * href="http://en.wikipedia.org/wiki/Distance_(graph_theory)">Wikipedia</a>
	 */
	public static class Radius extends GPFunc<GendreauContext> {
		private static final long serialVersionUID = 1901950830355444441L;

		@Override
		public double execute(double[] input, GendreauContext context) {
			return Collections.min(distances(gatherListOfPointsToBeVisited(context)));
		}
	}

	/**
	 * Computes the relative eccentricity of the current parcel to the current
	 * todo set, it is defined as:
	 * <code>max(eccentricity(origin),eccentricity(destination)) / diameter</code>
	 * . In case the current parcel is already in cargo, only the destination is
	 * considered.
	 * @see #eccentricity(Point, List)
	 * @see Diameter
	 */
	public static class RelEcc extends GPFunc<GendreauContext> {
		private static final long serialVersionUID = -4869939989385633591L;

		@Override
		public double execute(double[] input, GendreauContext context) {
			final List<Point> points = gatherListOfPointsToBeVisited(context);
			final double diameter = Collections.max(distances(points));

			final double destEcc = eccentricity(context.parcel.destinationLocation, points);
			if (context.isInCargo) {
				return destEcc / diameter;
			}
			final double originEcc = eccentricity(context.parcel.pickupLocation, points);
			if (destEcc > originEcc) {
				return destEcc / diameter;
			}
			return originEcc / diameter;
		}
	}

	protected static List<Point> gatherListOfPointsToBeVisited(GendreauContext context) {
		final List<Point> points = newArrayList();
		for (final ParcelDTO dto : context.truckContents) {
			points.add(dto.destinationLocation);
		}

		for (final Parcel p : context.todoList) {
			points.add(((DefaultParcel) p).dto.pickupLocation);
			points.add(((DefaultParcel) p).dto.destinationLocation);
		}

		points.add(context.truckPosition);
		points.add(context.vehicleDTO.startPosition);
		return points;
	}

	protected static List<Double> distances(List<Point> points) {
		final List<Double> distances = newArrayList();
		for (int i = 0; i < points.size() - 1; i++) {
			for (int j = i + 1; j < points.size(); j++) {
				distances.add(Point.distance(points.get(i), points.get(j)));
			}
		}
		return distances;
	}

	/**
	 * 'The eccentricity <code>e</code> of a vertex <code>v</code> is the
	 * greatest geodesic distance between <code>v</code> and any other vertex'
	 * definition from <a
	 * href="http://en.wikipedia.org/wiki/Distance_(graph_theory)">Wikipedia</a>
	 */
	protected static double eccentricity(Point p, List<Point> points) {
		if (points.isEmpty()) {
			return 0d;
		}
		final Iterator<Point> pit = points.iterator();
		double max = Point.distance(p, pit.next());
		while (pit.hasNext()) {
			final double dist = Point.distance(p, pit.next());
			if (dist > max) {
				max = dist;
			}
		}
		return max;
	}

	/**
	 * Computes the overlap of the timewindows of the current parcel with all
	 * parcels in the todo list and in cargo. The total load of the current
	 * parcel is computed.
	 */
	public static class TotalTimeWindowOverlapLoad extends GPFunc<GendreauContext> {
		private static final long serialVersionUID = -7743385324252475008L;

		@Override
		public double execute(double[] input, GendreauContext context) {
			final List<TimeWindowLoad> timeWindows = gatherTimeWindowLoads(context);
			final TimeWindowLoad deliveryTWL = new TimeWindowLoad(context.parcel.deliveryTimeWindow,
					context.parcel.deliveryDuration);
			final double deliveryLoad = TimeWindowLoadUtil.getOverlapLoad(deliveryTWL, timeWindows);

			if (context.isInCargo) {
				return deliveryLoad;
			}
			final TimeWindowLoad pickupTWL = new TimeWindowLoad(context.parcel.pickupTimeWindow,
					context.parcel.pickupDuration);
			final double pickupLoad = TimeWindowLoadUtil.getOverlapLoad(pickupTWL, timeWindows);

			return (deliveryLoad + pickupLoad) / 2;
		}
	}

	public static class MaxTimeWindowOverlapLoad extends GPFunc<GendreauContext> {
		private static final long serialVersionUID = 7065389446516368532L;

		@Override
		public double execute(double[] input, GendreauContext context) {
			final List<TimeWindowLoad> timeWindows = gatherTimeWindowLoads(context);
			final TimeWindowLoad deliveryTWL = new TimeWindowLoad(context.parcel.deliveryTimeWindow,
					context.parcel.deliveryDuration);
			final double maxDeliveryLoad = TimeWindowLoadUtil.getMaxOverlapLoad(deliveryTWL, timeWindows);

			if (context.isInCargo) {
				return maxDeliveryLoad;
			}
			final TimeWindowLoad pickupTWL = new TimeWindowLoad(context.parcel.pickupTimeWindow,
					context.parcel.pickupDuration);
			final double maxPickupLoad = TimeWindowLoadUtil.getMaxOverlapLoad(pickupTWL, timeWindows);

			return Math.max(maxDeliveryLoad, maxPickupLoad);
		}
	}

	public static class MinTimeWindowOverlapLoad extends GPFunc<GendreauContext> {
		private static final long serialVersionUID = 8841364648693082041L;

		@Override
		public double execute(double[] input, GendreauContext context) {
			final List<TimeWindowLoad> timeWindows = gatherTimeWindowLoads(context);
			final TimeWindowLoad deliveryTWL = new TimeWindowLoad(context.parcel.deliveryTimeWindow,
					context.parcel.deliveryDuration);
			final double minDeliveryLoad = TimeWindowLoadUtil.getMinOverlapLoad(deliveryTWL, timeWindows);

			if (context.isInCargo) {
				return minDeliveryLoad;
			}
			final TimeWindowLoad pickupTWL = new TimeWindowLoad(context.parcel.pickupTimeWindow,
					context.parcel.pickupDuration);
			final double minPickupLoad = TimeWindowLoadUtil.getMinOverlapLoad(pickupTWL, timeWindows);

			return Math.max(minDeliveryLoad, minPickupLoad);
		}
	}

	/**
	 * Finds the minimum distance which needs to be traveled from service points
	 * already assigned to the vehicle to the current parcel's service points.
	 * In case the current parcel is already assigned to this vehicle, 0 is
	 * returned. Note that the current vehicle's position is not taken into
	 * account for comparison since diversion is not allowed.
	 */
	public static class MinDistToServicePoints extends GPFunc<GendreauContext> {

		private static final long serialVersionUID = -159086906112157903L;

		@Override
		public double execute(double[] input, GendreauContext context) {
			if (context.isAssignedToVehicle) {
				return 0d;
			}
			return closest(context.parcel.destinationLocation, context)
					+ closest(context.parcel.pickupLocation, context);
		}

		static double closest(Point ref, GendreauContext context) {
			double closestDist = Point.distance(ref, context.vehicleDTO.startPosition);
			for (final Parcel p : context.todoList) {
				closestDist = Math.min(closestDist, Point.distance(ref, p.getDestination()));
				closestDist = Math.min(closestDist, Point.distance(ref, ((DefaultParcel) p).dto.pickupLocation));
			}

			for (final ParcelDTO p : context.truckContents) {
				closestDist = Math.min(closestDist, Point.distance(ref, p.destinationLocation));
			}
			return closestDist;
		}

	}

	static List<TimeWindowLoad> gatherTimeWindowLoads(GendreauContext context) {
		final List<TimeWindowLoad> timeWindows = newArrayList();
		for (final ParcelDTO dto : context.truckContents) {
			timeWindows.add(new TimeWindowLoad(dto.deliveryTimeWindow, dto.deliveryDuration
					/ dto.deliveryTimeWindow.length()));
		}
		for (final Parcel p : context.todoList) {
			timeWindows.add(new TimeWindowLoad(p.getDeliveryTimeWindow(), p.getDeliveryDuration()
					/ p.getDeliveryTimeWindow().length()));
			timeWindows.add(new TimeWindowLoad(p.getPickupTimeWindow(), p.getPickupDuration()
					/ p.getPickupTimeWindow().length()));
		}
		return timeWindows;
	}

}
