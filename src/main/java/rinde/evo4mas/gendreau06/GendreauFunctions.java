/**
 * 
 */
package rinde.evo4mas.gendreau06;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;

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
import rinde.sim.core.graph.Point;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GendreauFunctions extends GPFuncSet<GendreauContext> {

	private static final long serialVersionUID = -1347739703291676886L;

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
				new Waiters(), /* */
				new CargoSize(), /* */
				new IsInCargo(), /* */
				new TimeUntilAvailable(), /* */
				new Ado<GendreauContext>(), /* */
				new Mido<GendreauContext>(), /* */
				new Mado<GendreauContext>(), /* */
				new Dist<GendreauContext>(), /* */
				new Urge<GendreauContext>(), /* */
				new Est<GendreauContext>(), /* */
				new Ttl<GendreauContext>(), /* */
				new Adc(), /* */
				new Midc(), /* */
				new Madc() /* */
		);
	}

	public static class CargoSize extends GPFunc<GendreauContext> {
		private static final long serialVersionUID = -3041300164485908524L;

		@Override
		public double execute(double[] input, GendreauContext context) {
			return context.truckContents.size();
		}
	}

	public static class IsInCargo extends GPFunc<GendreauContext> {
		private static final long serialVersionUID = -3041300164485908524L;

		@Override
		public double execute(double[] input, GendreauContext context) {
			return context.isInCargo ? 1d : 0d;
		}
	}

	public static class Waiters extends GPFunc<GendreauContext> {
		private static final long serialVersionUID = -1258248355393336918L;

		@Override
		public double execute(double[] input, GendreauContext context) {
			return context.numWaiters;
		}
	}

	public static class TimeUntilAvailable extends GPFunc<GendreauContext> {
		private static final long serialVersionUID = -3527221929651639824L;

		@Override
		public double execute(double[] input, GendreauContext context) {
			final boolean isPickup = !context.isInCargo;

			final Point loc = isPickup ? context.parcel.pickupLocation : context.parcel.destinationLocation;
			final long travelTime = (long) ((Point.distance(loc, context.truckPosition) / 30d) * 3600000d);
			final long timeToBegin = (isPickup ? context.parcel.pickupTimeWindow.begin
					: context.parcel.deliveryTimeWindow.begin) - context.time - travelTime;

			return Math.min(0, timeToBegin);
		}
	}

	public static class Adc extends GPFunc<GendreauContext> {
		private static final long serialVersionUID = -3351761832322115361L;

		@Override
		public double execute(double[] input, GendreauContext context) {
			final Point poi = context.isInCargo ? context.parcel.destinationLocation : context.parcel.pickupLocation;
			double dist = 0;
			for (final Point p : context.otherVehiclePositions) {
				dist += Point.distance(poi, p);
			}
			dist /= context.otherVehiclePositions.size();
			return dist;
		}
	}

	public static class Midc extends GPFunc<GendreauContext> {
		private static final long serialVersionUID = 4350517514894413992L;

		@Override
		public double execute(double[] input, GendreauContext context) {
			final Point poi = context.isInCargo ? context.parcel.destinationLocation : context.parcel.pickupLocation;
			double min = Double.POSITIVE_INFINITY;
			for (final Point p : context.otherVehiclePositions) {
				final double dist = Point.distance(poi, p);
				if (dist < min) {
					min = dist;
				}
			}
			return min;
		}
	}

	public static class Madc extends GPFunc<GendreauContext> {
		private static final long serialVersionUID = -7052728913139911309L;

		@Override
		public double execute(double[] input, GendreauContext context) {
			final Point poi = context.isInCargo ? context.parcel.destinationLocation : context.parcel.pickupLocation;
			double max = Double.NEGATIVE_INFINITY;
			for (final Point p : context.otherVehiclePositions) {
				final double dist = Point.distance(poi, p);
				if (dist > max) {
					max = dist;
				}
			}
			return max;
		}
	}

}
