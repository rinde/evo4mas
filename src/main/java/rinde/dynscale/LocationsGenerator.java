/**
 * 
 */
package rinde.dynscale;

import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.graph.Point;

import com.google.common.collect.ImmutableList;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 *
 */
public interface LocationsGenerator {

	ImmutableList<Point> generate(RandomGenerator rng);

}