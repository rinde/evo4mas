/**
 * 
 */
package rinde.evo4mas.evo.gp;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static rinde.evo4mas.evo.gp.GenericFunctions.newAdd;
import static rinde.evo4mas.evo.gp.GenericFunctions.newConstants;
import static rinde.evo4mas.evo.gp.GenericFunctions.newDiv;
import static rinde.evo4mas.evo.gp.GenericFunctions.newIf4;
import static rinde.evo4mas.evo.gp.GenericFunctions.newMul;
import static rinde.evo4mas.evo.gp.GenericFunctions.newPow;
import static rinde.evo4mas.evo.gp.GenericFunctions.newSub;

import java.util.List;

import org.junit.Test;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class TestGPProgram {

	private static final double PRECISION = 0.0000001;

	List<GPFunc<Object>> functionList = init();

	public static List<GPFunc<Object>> init() {
		final List<GPFunc<Object>> list = newConstants(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		list.addAll(asList(newIf4(), newAdd(), newSub(), newMul(), newPow(), newDiv()));
		return list;
	}

	@Test
	public void test() {

		// final GPBaseNode<Object> node =
		// GPProgramParser.parseProgramFunc("(sub 1.0 0.0)", functionList);
		//
		// final GPProgram<Object> prog =
		// GPProgramCreator.convertToGPProgram(node, Object.class);
		// System.out.println(GPProgramParser.toLisp(prog));

		assertFunc(1, "(sub 1.0 0.0)");
		assertFunc(216, "(pow (div (sub (mul 10.0 2.0) 8.0) 2.0) 3.0)");
		assertFunc(4, "(pow 2.0 2.0)");

	}

	public void assertFunc(double expectedValue, String func) {
		assertEquals(expectedValue, GPProgramParser.parseProgramFunc(func, functionList).execute(null), PRECISION);
	}
}
