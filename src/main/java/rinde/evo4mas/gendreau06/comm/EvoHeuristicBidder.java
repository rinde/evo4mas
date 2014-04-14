/**
 * 
 */
package rinde.evo4mas.gendreau06.comm;

import static com.google.common.base.Preconditions.checkState;
import rinde.ecj.Heuristic;
import rinde.evo4mas.gendreau06.GendreauContext;
import rinde.evo4mas.gendreau06.GendreauContextBuilder;
import rinde.logistics.pdptw.mas.comm.AbstractBidder;
import rinde.logistics.pdptw.mas.comm.Bidder;
import rinde.sim.pdptw.common.DefaultParcel;
import rinde.sim.util.SupplierRng;
import rinde.sim.util.SupplierRngs.AbstractSupplierRng;

import com.google.common.base.Optional;

/**
 * This is a {@link Bidder} implementation that uses an evolved heuristic to
 * compute its bids.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public final class EvoHeuristicBidder extends AbstractBidder {

  private final Heuristic<GendreauContext> heuristic;
  private Optional<GendreauContextBuilder> gendreauContextBuilder;

  /**
   * Create the bidder using the specified {@link Heuristic}.
   * @param h The heuristic to use for computing bids.
   */
  public EvoHeuristicBidder(Heuristic<GendreauContext> h) {
    heuristic = h;
    gendreauContextBuilder = Optional.absent();
  }

  @Override
  public double getBidFor(DefaultParcel p, long time) {
    checkState(gendreauContextBuilder.isPresent(),
        "The GendreauContextBuilder has not been set.");
    return heuristic.compute(gendreauContextBuilder.get().buildFromScatch(time,
        p, false, false));
  }

  @Override
  protected void afterInit() {
    gendreauContextBuilder = Optional.of(new GendreauContextBuilder(roadModel
        .get(), pdpModel.get(), vehicle.get()));
  }

  public static SupplierRng<EvoHeuristicBidder> supplier(
      final Heuristic<GendreauContext> h) {
    return new AbstractSupplierRng<EvoHeuristicBidder>() {
      @Override
      public EvoHeuristicBidder get(long seed) {
        return new EvoHeuristicBidder(h);
      }
    };
  }
}
