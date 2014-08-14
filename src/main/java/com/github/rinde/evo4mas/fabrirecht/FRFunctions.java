/**
 * 
 */
package com.github.rinde.evo4mas.fabrirecht;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;

import com.github.rinde.evo4mas.common.TruckContext;
import com.github.rinde.evo4mas.common.GPFunctions.Ado;
import com.github.rinde.evo4mas.common.GPFunctions.Dist;
import com.github.rinde.evo4mas.common.GPFunctions.Est;
import com.github.rinde.evo4mas.common.GPFunctions.Mado;
import com.github.rinde.evo4mas.common.GPFunctions.Mido;
import com.github.rinde.evo4mas.common.GPFunctions.Ttl;
import com.github.rinde.evo4mas.common.GPFunctions.Urge;

import rinde.ecj.GPFunc;
import rinde.ecj.GPFuncSet;
import rinde.ecj.GenericFunctions.Add;
import rinde.ecj.GenericFunctions.Constant;
import rinde.ecj.GenericFunctions.Div;
import rinde.ecj.GenericFunctions.If4;
import rinde.ecj.GenericFunctions.Mul;
import rinde.ecj.GenericFunctions.Pow;
import rinde.ecj.GenericFunctions.Sub;

/**
 * @author Rinde van Lon 
 * 
 */
public class FRFunctions extends GPFuncSet<TruckContext> {

	private static final long serialVersionUID = -1347739703291676886L;

	@SuppressWarnings("unchecked")
	@Override
	public Collection<GPFunc<TruckContext>> create() {
		return newArrayList(
		/* GENERIC FUNCTIONS */
		new If4<TruckContext>(), /* */
				new Add<TruckContext>(), /* */
				new Sub<TruckContext>(), /* */
				new Div<TruckContext>(), /* */
				new Mul<TruckContext>(), /* */
				new Pow<TruckContext>(),
				/* CONSTANTS */
				new Constant<TruckContext>(1), /* */
				new Constant<TruckContext>(0), /* */
				/* DOMAIN SPECIFIC FUNCTIONS */
				new Ado<TruckContext>(), /* */
				new Mido<TruckContext>(), /* */
				new Mado<TruckContext>(), /* */
				new Dist<TruckContext>(), /* */
				new Urge<TruckContext>(), /* */
				new Est<TruckContext>(), /* */
				new Ttl<TruckContext>() /* */

		);
	}

}
