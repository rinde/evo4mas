/**
 * 
 */
package rinde.evo4mas.evo.gp;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import rinde.cloud.javainterface.Computer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import ec.Evaluator;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.koza.KozaFitness;
import ec.util.Parameter;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
// note: the C here must correspond to the type of the GPFuncSet !
public abstract class GPEvaluator<J extends GPComputationJob<C>, R extends GPComputationResult, C> extends Evaluator {

	public final static String P_HOST = "host";

	enum ComputationStrategy {
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
		}
	}

	@Override
	public void evaluatePopulation(EvolutionState state) {
		for (final Individual ind : state.population.subpops[0].individuals) {
			final GPIndividual gpInd = (GPIndividual) ind;
			System.out.println(gpInd.trees[0].child.makeLispTree());
		}

		final Multimap<GPNodeHolder, IndividualHolder> mapping = getGPFitnessMapping(state);

		final List<J> jobs = newArrayList();
		for (final GPNodeHolder key : mapping.keySet()) {
			jobs.addAll(createComputationJobs(new GPProgram<C>((GPFunc<C>) key.node)));
		}

		// either use RinCloud or compute locally
		Collection<R> results = null;
		if (compStrategy == ComputationStrategy.LOCAL) {
			results = newArrayList();
			final Computer<J, R> computer = createComputer();
			for (final J j : jobs) {
				results.add(computer.compute(j));
			}
		} else {
			throw new UnsupportedOperationException("not yet implemented!");
			// compute on rincloud

		}
		processResults(state, mapping, results);
	}

	protected void processResults(EvolutionState state, Multimap<GPNodeHolder, IndividualHolder> mapping,
			Collection<R> results) {
		final Multimap<String, Float> gatheredFitnessValues = HashMultimap.create();
		for (final R res : results) {
			final String programString = ((J) res.getComputationJob()).getProgram().root.makeLispTree();
			gatheredFitnessValues.put(programString, res.getFitness());
		}

		for (final Entry<String, Collection<Float>> entry : gatheredFitnessValues.asMap().entrySet()) {
			if (entry.getValue().size() != expectedNumberOfResultsPerGPIndividual()) {
				throw new IllegalStateException(
						"Number of received results does not match the number of expected results! received: "
								+ entry.getValue().size() + " expected: " + expectedNumberOfResultsPerGPIndividual());
			}

			float sum = 0;
			boolean notGood = false;
			for (final Float f : entry.getValue()) {
				if (f.floatValue() == Float.MAX_VALUE) {
					notGood = true;
				}
				sum += f.doubleValue();
			}
			if (notGood) {
				sum = Float.MAX_VALUE;
			} else {
				sum /= expectedNumberOfResultsPerGPIndividual();
			}
			final Collection<IndividualHolder> inds = mapping.get(new GPNodeHolder(entry.getKey()));
			for (final IndividualHolder ind : inds) {

				((KozaFitness) ind.ind.fitness).setStandardizedFitness(state, sum);
				ind.ind.evaluated = true;
			}
		}

	}

	protected abstract Collection<J> createComputationJobs(GPProgram<C> program);

	protected abstract Computer<J, R> createComputer();

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
				mapping.put(new GPNodeHolder(gpInd.trees[0].child), new IndividualHolder(gpInd));
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

}

class IndividualHolder {
	public final Individual ind;

	public IndividualHolder(Individual ind) {
		this.ind = ind;
	}
}

class GPNodeHolder {
	public final String string;
	public final GPNode node;

	public GPNodeHolder(GPNode node, String string) {
		this.node = node;
		this.string = string;
	}

	public GPNodeHolder(GPNode node) {
		this(node, node.makeLispTree());
	}

	public GPNodeHolder(String string) {
		this(null, string);
	}

	@Override
	public int hashCode() {
		return string.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o.hashCode() == hashCode();
	}
}
