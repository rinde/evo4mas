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
				new Ado<GendreauContext>(), /* */
				new Mido<GendreauContext>(), /* */
				new Mado<GendreauContext>(), /* */
				new Dist<GendreauContext>(), /* */
				new Urge<GendreauContext>(), /* */
				new Est<GendreauContext>(), /* */
				new Ttl<GendreauContext>() /* */
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

}
