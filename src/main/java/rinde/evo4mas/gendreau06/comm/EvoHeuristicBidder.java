/**
 * 
 */
package rinde.evo4mas.gendreau06.comm;

import javax.annotation.Nullable;

import rinde.ecj.Heuristic;
import rinde.evo4mas.gendreau06.GendreauContext;
import rinde.evo4mas.gendreau06.GendreauContextBuilder;
import rinde.logistics.pdptw.mas.comm.AbstractBidder;
import rinde.logistics.pdptw.mas.comm.Bidder;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.pdptw.common.DefaultParcel;
import rinde.sim.pdptw.common.DefaultVehicle;

/**
 * This is a {@link Bidder} implementation that uses an evolved heuristic to
 * compute its bids.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public class EvoHeuristicBidder extends AbstractBidder {

    protected final Heuristic<GendreauContext> heuristic;
    @Nullable
    protected GendreauContextBuilder gendreauContextBuilder;

    /**
     * Create the bidder using the specified {@link Heuristic}.
     * @param h The heuristic to use for computing bids.
     */
    public EvoHeuristicBidder(Heuristic<GendreauContext> h) {
        heuristic = h;
    }

    public double getBidFor(DefaultParcel p, long time) {
        final GendreauContextBuilder gcb = gendreauContextBuilder;
        if (gcb == null) {
            throw new RuntimeException(
                    "The GendreauContextBuilder has not been set.");
        }
        return heuristic.compute(gcb.buildFromScatch(time, p, false, false));
    }

    public void init(RoadModel rm, PDPModel pm, DefaultVehicle dv) {
        gendreauContextBuilder = new GendreauContextBuilder(rm, pm, dv);
    }
}
