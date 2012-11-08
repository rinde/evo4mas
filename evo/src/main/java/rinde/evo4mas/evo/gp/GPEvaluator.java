/**
 * 
 */
package rinde.evo4mas.evo.gp;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.jppf.client.JPPFClient;
import org.jppf.client.JPPFJob;
import org.jppf.server.protocol.JPPFTask;
import org.jppf.task.storage.DataProvider;
import org.jppf.task.storage.MemoryMapDataProvider;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import ec.Evaluator;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPTree;
import ec.util.Parameter;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
// note: the C here must correspond to the type of the GPFuncSet !
public abstract class GPEvaluator<T extends ComputationTask<R>, R extends GPComputationResult, C> extends Evaluator {

	private static final long serialVersionUID = -8172136113716773085L;
	public final static String P_HOST = "host";

	protected transient JPPFClient jppfClient;

	protected enum ComputationStrategy {
		LOCAL, DISTRIBUTED
	}

	protected ComputationStrategy compStrategy;

	@Override
	public void setup(final EvolutionState state, final Parameter base) {

		final String hostName = state.parameters.getString(base.push(P_HOST), null);
		if (hostName == null || hostName.equalsIgnoreCase("local")) {
			compStrategy = ComputationStrategy.LOCAL;
		} else {
			compStrategy = ComputationStrategy.DISTRIBUTED;
			jppfClient = new JPPFClient();
		}
	}

	@Override
	public void evaluatePopulation(EvolutionState state) {
		// for (final Individual ind : state.population.subpops[0].individuals)
		// {
		// final GPIndividual gpInd = (GPIndividual) ind;
		// System.out.println(gpInd.trees[0].child.makeLispTree());
		// }

		final Multimap<GPNodeHolder, IndividualHolder> mapping = getGPFitnessMapping(state);
		final DataProvider dataProvider = new MemoryMapDataProvider();
		final JPPFJob job = new JPPFJob(dataProvider);
		job.setBlocking(true);
		job.setName("Generation " + state.generation);

		try {
			for (final GPNodeHolder key : mapping.keySet()) {
				final Collection<T> coll = createComputationJobs(dataProvider, key.trees, state);
				for (final T j : coll) {
					// only needed when local, otherwise JPPF handles this
					// automatically
					if (compStrategy == ComputationStrategy.LOCAL) {
						j.setDataProvider(dataProvider);
					}
					job.addTask(j);
				}
			}
			final Collection<R> results = compute(job);
			processResults(state, mapping, results);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Collection<R> compute(JPPFJob job) throws Exception {
		// either use JPPF or compute locally
		final Collection<R> results = newArrayList();
		if (compStrategy == ComputationStrategy.LOCAL) {
			for (final JPPFTask task : job.getTasks()) {
				task.run();
				results.add(((T) task).getComputationResult());
			}
		} else {
			final List<JPPFTask> res = jppfClient.submit(job);
			for (final JPPFTask t : res) {
				if (t.getException() != null) {
					throw new RuntimeException("This exception occured on a node", t.getException());
				}
				results.add(((T) t).getComputationResult());
			}
		}
		return results;
	}

	protected void processResults(EvolutionState state, Multimap<GPNodeHolder, IndividualHolder> mapping,
			Collection<R> results) {
		final Multimap<String, R> gatheredFitnessValues = HashMultimap.create();
		for (final R res : results) {
			final String programString = res.getGPId();// res.getComputationJob().((J)
			gatheredFitnessValues.put(programString, res);
		}
		for (final Entry<String, Collection<R>> entry : gatheredFitnessValues.asMap().entrySet()) {
			if (entry.getValue().size() != expectedNumberOfResultsPerGPIndividual()) {
				throw new IllegalStateException(
						"Number of received results does not match the number of expected results! received: "
								+ entry.getValue().size() + " expected: " + expectedNumberOfResultsPerGPIndividual());
			}

			float sum = 0;
			boolean notGood = false;
			for (final R res : entry.getValue()) {
				if (res.getFitness() == Float.MAX_VALUE) {
					notGood = true;
				}
				sum += res.getFitness();
			}
			if (notGood) {
				sum = Float.MAX_VALUE;
			} else {
				sum /= expectedNumberOfResultsPerGPIndividual();
			}
			final Collection<IndividualHolder> inds = mapping.get(new GPNodeHolder(entry.getKey()));
			checkState(!inds.isEmpty(), "there must be at least one individual for every program");
			for (final IndividualHolder ind : inds) {
				((GPFitness<R>) ind.ind.fitness).addResults(entry.getValue());
				((GPFitness<R>) ind.ind.fitness).setStandardizedFitness(state, sum);
				ind.ind.evaluated = true;
			}
		}

	}

	protected abstract Collection<T> createComputationJobs(DataProvider dataProvider, GPTree[] trees,
			EvolutionState state);

	protected abstract int expectedNumberOfResultsPerGPIndividual();

	@Override
	public boolean runComplete(EvolutionState state) {
		// TODO Auto-generated method stub
		return false;
	}

	protected Multimap<GPNodeHolder, IndividualHolder> getGPFitnessMapping(EvolutionState state) {
		final Multimap<GPNodeHolder, IndividualHolder> mapping = HashMultimap.create();
		for (int i = 0; i < state.population.subpops.length; i++) {
			for (int j = 0; j < state.population.subpops[i].individuals.length; j++) {
				final GPIndividual gpInd = ((GPIndividual) state.population.subpops[i].individuals[j]);
				mapping.put(new GPNodeHolder(gpInd.trees), new IndividualHolder(gpInd));
			}
		}
		return mapping;
	}

	@Override
	public void initializeContacts(EvolutionState state) {}

	@Override
	public void reinitializeContacts(EvolutionState state) {}

	@Override
	public void closeContacts(EvolutionState state, int result) {}

	public static String treeToString(GPTree[] t) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < t.length; i++) {
			sb.append(t[i].child.makeLispTree());
		}
		return sb.toString();
	}

}

class IndividualHolder {
	public final Individual ind;

	public IndividualHolder(Individual ind) {
		this.ind = ind;
	}
}

class GPNodeHolder {
	public final String string;
	public final GPTree[] trees;

	public GPNodeHolder(GPTree[] t, String s) {
		trees = t;
		string = s;
	}

	public GPNodeHolder(GPTree[] t) {
		this(t, GPEvaluator.treeToString(t));
	}

	public GPNodeHolder(String string) {
		this(null, string);
	}

	@Override
	public int hashCode() {
		return string.hashCode();
	}

	// this is NECESSARY!
	@Override
	public boolean equals(Object o) {
		return hashCode() == o.hashCode();
	}

	@Override
	public String toString() {
		return string;
	}

	// @Override
	// public boolean equals(Object o) {
	// if (o instanceof GPNodeHolder) {
	// final GPNodeHolder other = (GPNodeHolder) o;
	// return other.string.equals(string) && Arrays.deepEquals(trees,
	// other.trees);
	// }
	// return false;
	// }
}
