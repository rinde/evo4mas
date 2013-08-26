/**
 * 
 */
package rinde.evo4mas.gendreau06;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import rinde.ecj.GPFuncNode;
import rinde.ecj.GPProgram;
import rinde.ecj.GenericFunctions;
import rinde.ecj.Heuristic;
import rinde.evo4mas.gendreau06.route.EvoHeuristicRoutePlanner;
import rinde.logistics.pdptw.mas.route.RoutePlanner;
import rinde.logistics.pdptw.mas.route.RoutePlannerTest;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
@RunWith(Parameterized.class)
public class EvoRoutePlannerTest extends RoutePlannerTest {

    /**
     * @param rp
     */
    public EvoRoutePlannerTest(RPBuilder rp) {
        super(rp);
    }

    static Heuristic<GendreauContext> DUMMY_HEURISTIC =
            new GPProgram<GendreauContext>(new GPFuncNode<GendreauContext>(
                    new GenericFunctions.Constant<GendreauContext>(0d)));

    @Parameters
    public static Collection<Object[]> configs() {
        return Arrays.asList(new Object[][] {
        /* */
        { new RPBuilder() {
            public RoutePlanner build() {
                return new EvoHeuristicRoutePlanner(DUMMY_HEURISTIC);
            }
        } }, /* */
        });
    }
}
