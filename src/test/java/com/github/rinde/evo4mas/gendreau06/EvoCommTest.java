/**
 * 
 */
package com.github.rinde.evo4mas.gendreau06;

import static java.util.Arrays.asList;

import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import rinde.ecj.GPFuncNode;
import rinde.ecj.GPProgram;
import rinde.ecj.GenericFunctions;
import rinde.ecj.Heuristic;

import com.github.rinde.evo4mas.gendreau06.GendreauContext;
import com.github.rinde.evo4mas.gendreau06.comm.EvoHeuristicBidder;
import com.github.rinde.logistics.pdptw.mas.TruckConfiguration;
import com.github.rinde.logistics.pdptw.mas.comm.AuctionCommModel;
import com.github.rinde.logistics.pdptw.mas.comm.CommunicationIntegrationTest;
import com.github.rinde.logistics.pdptw.mas.route.RandomRoutePlanner;
import com.github.rinde.rinsim.pdptw.experiment.MASConfiguration;
import com.google.common.collect.ImmutableList;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
@RunWith(Parameterized.class)
public class EvoCommTest extends CommunicationIntegrationTest {

  static Heuristic<GendreauContext> DUMMY_HEURISTIC = new GPProgram<GendreauContext>(
      new GPFuncNode<GendreauContext>(
          new GenericFunctions.Constant<GendreauContext>(0d)));

  public EvoCommTest(MASConfiguration c) {
    super(c);
  }

  @Parameters
  public static Collection<Object[]> configs() {
    return asList(new Object[][] { /* */
    { new TruckConfiguration(RandomRoutePlanner.supplier(),
        EvoHeuristicBidder.supplier(DUMMY_HEURISTIC), ImmutableList.of(
            AuctionCommModel.supplier(), CommTestModel.supplier())) } });
  }

}
