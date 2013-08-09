/**
 * 
 */
package rinde.evo4mas.gendreau06.comm;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.problem.common.DefaultParcel;

/**
 * A {@link Bidder} implementation that creates random bids.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public class RandomBidder extends AbstractBidder {

    protected final RandomGenerator rng;

    /**
     * Create a random bidder using the specified random seed.
     * @param seed The random seed.
     */
    public RandomBidder(long seed) {
        rng = new MersenneTwister(seed);
    }

    public double getBidFor(DefaultParcel p, long time) {
        return rng.nextDouble();
    }
}
