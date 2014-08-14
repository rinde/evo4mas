/**
 * 
 */
package com.github.rinde.evo4mas.gendreau06;

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

import com.github.rinde.evo4mas.common.GPFunctions.Ado;
import com.github.rinde.evo4mas.common.GPFunctions.Dist;
import com.github.rinde.evo4mas.common.GPFunctions.Est;
import com.github.rinde.evo4mas.common.GPFunctions.Mado;
import com.github.rinde.evo4mas.common.GPFunctions.Mido;
import com.github.rinde.evo4mas.common.GPFunctions.Ttl;
import com.github.rinde.evo4mas.common.GPFunctions.Urge;
import com.github.rinde.evo4mas.gendreau06.GendreauFunctions.Adc;
import com.github.rinde.evo4mas.gendreau06.GendreauFunctions.CargoSize;
import com.github.rinde.evo4mas.gendreau06.GendreauFunctions.IsInCargo;
import com.github.rinde.evo4mas.gendreau06.GendreauFunctions.Madc;
import com.github.rinde.evo4mas.gendreau06.GendreauFunctions.Midc;
import com.github.rinde.evo4mas.gendreau06.GendreauFunctions.TimeUntilAvailable;
import com.google.common.collect.ImmutableList;

/**
 * @author Rinde van Lon 
 * 
 */
public class BlackboardFunctions extends GPFuncSet<GendreauContext> {

  /**
   * List of GP functions which can be used with blackboard communication.
   */
  @SuppressWarnings("unchecked")
  public static ImmutableList<GPFunc<GendreauContext>> FUNCTIONS = ImmutableList
      .<GPFunc<GendreauContext>> of(
          /* GENERIC FUNCTIONS */
          new If4<GendreauContext>(),
          new Add<GendreauContext>(),
          new Sub<GendreauContext>(),
          new Div<GendreauContext>(),
          new Mul<GendreauContext>(),
          new Pow<GendreauContext>(),
          /* CONSTANTS */
          new Constant<GendreauContext>(1),
          new Constant<GendreauContext>(0),
          /* DOMAIN SPECIFIC FUNCTIONS */
          new Waiters(),
          new CargoSize<GendreauContext>(),
          new IsInCargo<GendreauContext>(),
          new TimeUntilAvailable<GendreauContext>(),
          new Ado<GendreauContext>(),
          new Mido<GendreauContext>(),
          new Mado<GendreauContext>(),
          new Dist<GendreauContext>(),
          new Urge<GendreauContext>(),
          new Est<GendreauContext>(),
          new Ttl<GendreauContext>(),
          new Adc<GendreauContext>(),
          new Midc<GendreauContext>(),
          new Madc<GendreauContext>()
      );

  private static final long serialVersionUID = 8699393992638706414L;

  @Override
  public Collection<GPFunc<GendreauContext>> create() {
    return FUNCTIONS;
  }

  /**
   * A vehicle is waiting if it aims to pickup a parcel but is too early. This
   * function returns the number of waiters of the current parcel.
   */
  public static class Waiters extends GPFunc<GendreauContext> {
    private static final long serialVersionUID = -1258248355393336918L;

    @Override
    public double execute(double[] input, GendreauContext context) {
      return context.numWaiters;
    }
  }
}
