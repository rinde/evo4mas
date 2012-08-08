/**
 * 
 */
package rinde.evo4mas.evo.gp;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import ec.EvolutionState;
import ec.gp.GPFunctionSet;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPType;
import ec.util.Parameter;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GPFuncSet extends GPFunctionSet {

	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		// What's my name?
		name = state.parameters.getString(base.push(P_NAME), null);
		if (name == null) {
			state.output.fatal("No name was given for this function set.", base.push(P_NAME));
		}
		// Register me
		final GPFunctionSet old_functionset = (GPFunctionSet) (((GPInitializer) state.initializer).functionSetRepository
				.put(name, this));
		if (old_functionset != null) {
			state.output
					.fatal("The GPFunctionSet \"" + name + "\" has been defined multiple times.", base.push(P_NAME));
		}

		// How many functions do I have?
		final int numFuncs = state.parameters.getInt(base.push(P_SIZE), null, 1);
		if (numFuncs < 1) {
			state.output.error("The GPFunctionSet \"" + name + "\" has no functions.", base.push(P_SIZE));
		}

		nodesByName = new Hashtable();

		// TODO ADD FUNCTIONS HERE!!!!

		final Parameter p = base.push(P_FUNC);
		final Vector tmp = new Vector();
		for (int x = 0; x < numFuncs; x++) {
			// load
			final Parameter pp = p.push("" + x);
			final GPNode gpfi = (GPNode) (state.parameters.getInstanceForParameter(pp, null, GPNode.class));
			gpfi.setup(state, pp);

			// add to my collection
			tmp.addElement(gpfi);

			// Load into the nodesByName hashtable
			final GPNode[] nodes = (GPNode[]) (nodesByName.get(gpfi.name()));
			if (nodes == null) {
				nodesByName.put(gpfi.name(), new GPNode[] { gpfi });
			} else {
				// O(n^2) but uncommon so what the heck.
				final GPNode[] nodes2 = new GPNode[nodes.length + 1];
				System.arraycopy(nodes, 0, nodes2, 0, nodes.length);
				nodes2[nodes2.length - 1] = gpfi;
				nodesByName.put(gpfi.name(), nodes2);
			}
		}

		// Make my hash tables
		nodes_h = new Hashtable();
		terminals_h = new Hashtable();
		nonterminals_h = new Hashtable();

		// Now set 'em up according to the types in GPType

		final Enumeration e = ((GPInitializer) state.initializer).typeRepository.elements();
		final GPInitializer initializer = ((GPInitializer) state.initializer);
		while (e.hasMoreElements()) {
			final GPType typ = (GPType) (e.nextElement());

			// make vectors for the type.
			final Vector nodes_v = new Vector();
			final Vector terminals_v = new Vector();
			final Vector nonterminals_v = new Vector();

			// add GPNodes as appropriate to each vector
			final Enumeration v = tmp.elements();
			while (v.hasMoreElements()) {
				final GPNode i = (GPNode) (v.nextElement());
				if (typ.compatibleWith(initializer, i.constraints(initializer).returntype)) {
					nodes_v.addElement(i);
					if (i.children.length == 0) {
						terminals_v.addElement(i);
					} else {
						nonterminals_v.addElement(i);
					}
				}
			}

			// turn nodes_h' vectors into arrays
			GPNode[] ii = new GPNode[nodes_v.size()];
			nodes_v.copyInto(ii);
			nodes_h.put(typ, ii);

			// turn terminals_h' vectors into arrays
			ii = new GPNode[terminals_v.size()];
			terminals_v.copyInto(ii);
			terminals_h.put(typ, ii);

			// turn nonterminals_h' vectors into arrays
			ii = new GPNode[nonterminals_v.size()];
			nonterminals_v.copyInto(ii);
			nonterminals_h.put(typ, ii);
		}

		// I don't check to see if the generation mechanism will be valid here
		// -- I check that in GPTreeConstraints, where I can do the weaker check
		// of going top-down through functions rather than making sure that
		// every
		// single function has a compatible argument function (an unneccessary
		// check)

		state.output.exitIfErrors(); // because I promised when I called
										// n.setup(...)

		// postprocess the function set
		postProcessFunctionSet();
	}
}
