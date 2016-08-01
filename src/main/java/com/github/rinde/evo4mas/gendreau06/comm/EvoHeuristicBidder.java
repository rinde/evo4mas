/**
 *
 */
package com.github.rinde.evo4mas.gendreau06.comm;

import static com.google.common.base.Preconditions.checkState;

import com.github.rinde.ecj.PriorityHeuristic;
import com.github.rinde.evo4mas.gendreau06.GendreauContext;
import com.github.rinde.evo4mas.gendreau06.GendreauContextBuilder;
import com.github.rinde.logistics.pdptw.mas.comm.AbstractBidder;
import com.github.rinde.logistics.pdptw.mas.comm.Auctioneer;
import com.github.rinde.logistics.pdptw.mas.comm.Bidder;
import com.github.rinde.logistics.pdptw.mas.comm.DoubleBid;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.geom.Graphs.Heuristic;
import com.github.rinde.rinsim.util.StochasticSupplier;
import com.github.rinde.rinsim.util.StochasticSuppliers.AbstractStochasticSupplier;
import com.google.common.base.Optional;

/**
 * This is a {@link Bidder} implementation that uses an evolved heuristic to
 * compute its bids.
 * @author Rinde van Lon
 */
public final class EvoHeuristicBidder extends AbstractBidder<DoubleBid> {

  private final PriorityHeuristic<GendreauContext> heuristic;
  private Optional<GendreauContextBuilder> gendreauContextBuilder;

  /**
   * Create the bidder using the specified {@link Heuristic}.
   * @param h The heuristic to use for computing bids.
   */
  public EvoHeuristicBidder(PriorityHeuristic<GendreauContext> h) {
    heuristic = h;
    gendreauContextBuilder = Optional.absent();
  }

  // public DoubleBid getBidFor(Parcel p, long time) {

  // }

  @Override
  protected void afterInit() {
    gendreauContextBuilder = Optional.of(new GendreauContextBuilder(roadModel
        .get(), pdpModel.get(), vehicle.get()));
  }

  public static StochasticSupplier<EvoHeuristicBidder> supplier(
      final PriorityHeuristic<GendreauContext> h) {
    return new AbstractStochasticSupplier<EvoHeuristicBidder>() {
      @Override
      public EvoHeuristicBidder get(long seed) {
        return new EvoHeuristicBidder(h);
      }
    };
  }

  @Override
  public void callForBids(Auctioneer<DoubleBid> auctioneer, Parcel p,
      long time) {
    checkState(gendreauContextBuilder.isPresent(),
        "The GendreauContextBuilder has not been set.");
    auctioneer.submit(DoubleBid.create(time, this, p,
        heuristic.compute(gendreauContextBuilder.get().buildFromScatch(time,
            p, false, false))));

  }

  @Override
  public void endOfAuction(Auctioneer auctioneer, Parcel p,
      long auctionStartTime) {
    // TODO Auto-generated method stub

  }
}
