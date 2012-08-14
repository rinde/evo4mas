/**
 * 
 */
package rinde.evo4mas.evo.gp;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.unmodifiableList;

import java.util.Collection;
import java.util.List;

import ec.gp.koza.KozaFitness;

/**
 * Fitness with an additional field for storing computation results.
 * 
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public class GPFitness<T extends GPComputationResult> extends KozaFitness {

	private static final long serialVersionUID = -2256611595987359990L;
	protected List<T> results;

	public GPFitness() {
		super();
		results = newArrayList();
	}

	public void addResults(Collection<T> res) {
		// checkState(results.isEmpty(), "results can be added only once!");
		results = newArrayList(res);
	}

	public List<T> getResults() {
		return unmodifiableList(results);
	}

}
