/**
 * 
 */
package com.github.rinde.evo4mas.gendreau06;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.github.rinde.evo4mas.gendreau06.GendreauContext;
import com.github.rinde.evo4mas.gendreau06.route.EvoHeuristicRoutePlanner;
import com.github.rinde.logistics.pdptw.mas.route.RoutePlanner;
import com.github.rinde.logistics.pdptw.mas.route.RoutePlannerTest;
import com.github.rinde.rinsim.util.StochasticSupplier;

import rinde.ecj.GPFuncNode;
import rinde.ecj.GPProgram;
import rinde.ecj.GenericFunctions;
import rinde.ecj.Heuristic;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
@RunWith(Parameterized.class)
public class EvoRoutePlannerTest extends RoutePlannerTest {

  public EvoRoutePlannerTest(StochasticSupplier<RoutePlanner> rp) {
    super(rp);
  }

  static Heuristic<GendreauContext> DUMMY_HEURISTIC = new GPProgram<GendreauContext>(
      new GPFuncNode<GendreauContext>(
          new GenericFunctions.Constant<GendreauContext>(0d)));

  @Parameters
  public static Collection<Object[]> configs() {
    return Arrays.asList(new Object[][] { { EvoHeuristicRoutePlanner
        .supplier(DUMMY_HEURISTIC) } });
  }
}
