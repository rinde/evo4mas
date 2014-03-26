/**
 * 
 */
package rinde.evo4mas.gendreau06;

import static java.util.Arrays.asList;

import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import rinde.ecj.GPFuncNode;
import rinde.ecj.GPProgram;
import rinde.ecj.GenericFunctions;
import rinde.ecj.Heuristic;
import rinde.evo4mas.gendreau06.comm.EvoHeuristicBidder;
import rinde.logistics.pdptw.mas.TruckConfiguration;
import rinde.logistics.pdptw.mas.comm.AuctionCommModel;
import rinde.logistics.pdptw.mas.comm.CommunicationIntegrationTest;
import rinde.logistics.pdptw.mas.route.RandomRoutePlanner;
import rinde.sim.pdptw.experiment.MASConfiguration;

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
