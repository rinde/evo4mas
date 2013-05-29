/**
 * 
 */
package rinde.solver.spdptw;

import static rinde.solver.spdptw.SolverValidator.validate;
import static rinde.solver.spdptw.SolverValidator.validateInputs;
import static rinde.solver.spdptw.SolverValidator.wrap;

import org.junit.Test;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class SolverValidatorTest {

	@Test(expected = IllegalArgumentException.class)
	public void validateInputsEmptyTravelTimeMatrix() {
		validateInputs(new int[][] {}, new int[] {}, new int[] {}, new int[][] {}, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateInputsInvalidTravelTimeMatrix1() {
		validateInputs(new int[7][6], new int[] {}, new int[] {}, new int[][] {}, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateInputsInvalidTravelTimeMatrix2() {
		validateInputs(new int[][] { new int[3], new int[3], new int[4] }, new int[] {}, new int[] {}, new int[][] {}, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateInputsInvalidReleaseDatesLength() {
		validateInputs(new int[4][4], new int[5], new int[] {}, new int[][] {}, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateInputsInvalidDueDatesLength() {
		validateInputs(new int[4][4], new int[4], new int[2], new int[][] {}, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateInputsInvalidServiceTime() {
		validateInputs(new int[7][7], new int[7], new int[7], new int[][] {}, -1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateInputsInvalidTW() {
		validateInputs(new int[3][3], new int[] { 1, 2, 3 }, new int[] { 1, 3, 2 }, new int[][] {}, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateInputsInvalidTWstart1() {
		validateInputs(new int[3][3], new int[] { 1, 2, 3 }, new int[] { 1, 3, 6 }, new int[][] {}, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateInputsInvalidTWstart2() {
		validateInputs(new int[3][3], new int[] { 0, 2, 3 }, new int[] { 1, 3, 6 }, new int[][] {}, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateInputsInvalidTWdepot() {
		validateInputs(new int[3][3], new int[] { 0, 2, 1 }, new int[] { 0, 3, 23 }, new int[][] {}, 1);
	}

	@Test
	public void validateInputsValidEmpty() {
		validateInputs(new int[3][3], new int[3], new int[3], new int[][] {}, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateInputsInvalidPairSize() {
		validateInputs(new int[4][4], new int[4], new int[4], new int[][] { new int[] { 1, 2 }, new int[3] }, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateInputsInvalidPairLocation1() {
		validateInputs(new int[6][6], new int[6], new int[6], new int[][] { new int[] { 1, 2 }, new int[] { 3, 0 } }, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateInputsInvalidPairLocation2() {
		validateInputs(new int[6][6], new int[6], new int[6], new int[][] { new int[] { 1, 2 }, new int[] { 3, 913 } }, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateInputsDuplicatePairLocation() {
		validateInputs(new int[6][6], new int[6], new int[6], new int[][] { new int[] { 1, 2 }, new int[] { 2, 0 } }, 0);
	}

	@Test
	public void validateInputsValid() {
		validateInputs(new int[6][6], new int[6], new int[6], new int[][] { new int[] { 4, 2 }, new int[] { 3, 1 } }, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateInvalidRouteLength() {
		validate(new SolutionObject(new int[3], new int[1], 0), new int[4][4], new int[4], new int[4], new int[][] {}, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateInvalidRouteStart() {
		validate(new SolutionObject(new int[] { 1, 1, 1, 1 }, new int[1], 0), new int[4][4], new int[4], new int[4], new int[][] {}, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateInvalidRouteDepot() {
		validate(new SolutionObject(new int[] { 0, 1, 1, 1 }, new int[1], 0), new int[4][4], new int[4], new int[4], new int[][] {}, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateInvaliRouteDuplicates() {
		validate(new SolutionObject(new int[] { 0, 1, 1, 3 }, new int[1], 0), new int[4][4], new int[4], new int[4], new int[][] {}, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateInvalidRouteNonExisting() {
		validate(new SolutionObject(new int[] { 0, 1, 9, 3 }, new int[1], 0), new int[4][4], new int[4], new int[4], new int[][] {}, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateInvalidArrivalTimesLength() {
		validate(new SolutionObject(new int[] { 0, 1, 2, 3 }, new int[1], 0), new int[4][4], new int[4], new int[4], new int[][] {}, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateInvalidArrivalTimesFirst() {
		validate(new SolutionObject(new int[] { 0, 1, 2, 3 }, new int[] { 1, -1, 0, 1 }, 0), new int[4][4], new int[4], new int[4], new int[][] {}, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateInvalidArrivalTimes1() {
		validate(new SolutionObject(new int[] { 0, 1, 2, 3 }, new int[] { 0, -1, 0, 1 }, 0), new int[4][4], new int[4], new int[4], new int[][] {}, 0);
	}

	final int[][] travelTimes = new int[][] {
	/* */new int[] { 0, 10, 999, 999 },
	/* */new int[] { 999, 0, 3, 999 },
	/* */new int[] { 999, 999, 0, 7 },
	/* */new int[] { 999, 999, 999, 0 } };

	@Test(expected = IllegalArgumentException.class)
	public void validateInvalidArrivalTimes2() {
		validate(new SolutionObject(new int[] { 0, 1, 2, 3 }, new int[] { 0, 0, 0, 1 }, 0), travelTimes, new int[4], new int[4], new int[][] {}, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateInvalidArrivalTimes3() {
		validate(new SolutionObject(new int[] { 0, 1, 2, 3 }, new int[] { 0, 10, 0, 1 }, 0), travelTimes, new int[4], new int[4], new int[][] {}, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateInvalidObjective() {
		validate(new SolutionObject(new int[] { 0, 1, 2, 3 }, new int[] { 0, 10, 100, 108 }, 0), travelTimes, new int[4], new int[4], new int[][] {}, 0);
	}

	@Test
	public void validateCorrect() {
		validate(new SolutionObject(new int[] { 0, 1, 2, 3 }, new int[] { 0, 10, 100, 108 }, 238), travelTimes, new int[4], new int[4], new int[][] {}, 0)
				.toString();
	}

	@Test
	public void testWrap() {

		final Solver s = wrap(new FakeSolver(new SolutionObject(new int[] { 0, 1, 2, 3 },
				new int[] { 0, 10, 100, 108 }, 238)));
		s.solve(travelTimes, new int[4], new int[4], new int[][] {}, 0);
	}

	class FakeSolver implements Solver {
		SolutionObject answer;

		FakeSolver(SolutionObject answer) {
			this.answer = answer;
		}

		public SolutionObject solve(int[][] travelTime, int[] releaseDates, int[] dueDates, int[][] servicePairs,
				int serviceTime) {
			return answer;
		}
	}
}
