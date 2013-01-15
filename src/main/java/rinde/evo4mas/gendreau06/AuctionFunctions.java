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
import rinde.evo4mas.gendreau06.GendreauFunctions.Adc;
import rinde.evo4mas.gendreau06.GendreauFunctions.CargoSize;
import rinde.evo4mas.gendreau06.GendreauFunctions.IsInCargo;
import rinde.evo4mas.gendreau06.GendreauFunctions.Madc;
import rinde.evo4mas.gendreau06.GendreauFunctions.Midc;
import rinde.evo4mas.gendreau06.GendreauFunctions.TimeUntilAvailable;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class AuctionFunctions extends GPFuncSet<GendreauContext> {

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
				new Madc<GendreauContext>() /* */
		);
	}

}
