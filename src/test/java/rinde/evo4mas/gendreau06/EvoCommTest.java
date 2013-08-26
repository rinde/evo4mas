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
import rinde.logistics.pdptw.mas.comm.CommTest;
import rinde.logistics.pdptw.mas.comm.Communicator;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
@RunWith(Parameterized.class)
public class EvoCommTest extends CommTest {

    static Heuristic<GendreauContext> DUMMY_HEURISTIC =
            new GPProgram<GendreauContext>(new GPFuncNode<GendreauContext>(
                    new GenericFunctions.Constant<GendreauContext>(0d)));

    static CommunicatorCreator EVO_HEURISTIC_BIDDER =
            new CommunicatorCreator() {
                public Communicator create() {
                    return new EvoHeuristicBidder(DUMMY_HEURISTIC);
                }
            };

    /**
     * @param c
     */
    public EvoCommTest(TestConfigurator c) {
        super(c);
    }

    @Parameters
    public static Collection<Object[]> configs() {
        return asList(new Object[][] { /* */
        { new TestConfigurator(CommTest.AUCTION_COMM_MODEL,
                EVO_HEURISTIC_BIDDER) } });
    }

}
