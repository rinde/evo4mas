/**
 * 
 */
package rinde.evo4mas.evo.gp;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GPTree<C> {

	protected Multimap<GPFunc<C>, GPFunc<C>> map;
	protected GPFunc<C> root;

	public GPTree() {
		map = LinkedHashMultimap.create();
	}

	public double execute(C context) {
		return executeNode(root, context);
	}

	protected double executeNode(GPFunc<C> current, C context) {
		final List<GPFunc<C>> children = newArrayList(map.get(current));
		final double[] vals = new double[children.size()];
		for (int i = 0; i < children.size(); i++) {
			vals[i] = executeNode(children.get(i), context);
		}
		return current.execute(vals, context);
	}
}
