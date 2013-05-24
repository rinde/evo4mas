package rinde.solver.spdptw.mip;

import java.util.Arrays;

import org.junit.Test;

import rinde.solver.spdptw.SolutionObject;
import rinde.solver.spdptw.SolverValidator;

public class MipTest {

	@Test
	public void test1() {
		final int[][] travelTime = { { 0, 1, 2, 3, 4 }, { 1, 0, 2, 3, 4 }, { 2, 2, 0, 4, 4 }, { 3, 3, 4, 0, 4 },
				{ 4, 4, 4, 4, 0 } };

		final int[] releaseDates = { 0, 1, 2, 3, 0 };
		final int[] dueDates = { 0, 8, 9, 10, 11 };
		final int[][] servicePairs = { { 1, 2 } };
		final int serviceTime = 2;

		final MipSolver mip = new MipSolver();
		final SolutionObject solution = mip.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTime);
		System.out.println(solution);

		SolverValidator.validate(solution, travelTime, releaseDates, dueDates, servicePairs, serviceTime);
	}

	@Test
	public void test2() {
		final int[][] locations = new int[][] { { 1, 2 }, { 3, 4 }, { 8, 6 }, { 4, 5 }, { 5, 4 }, { 0, 0 } };

		final int[][] travelTime = new int[locations.length][locations.length];
		for (int i = 0; i < locations.length; i++) {
			for (int j = 0; j < locations.length; j++) {
				if (i == j) {
					continue;
				}
				travelTime[i][j] = (int) Math.ceil(Math.sqrt(Math.pow(locations[i][0] - locations[j][0], 2)
						+ Math.pow(locations[i][1] - locations[j][1], 2)));
			}
		}

		System.out.println(Arrays.deepToString(travelTime));

		final int[] releaseDates = { 0, 1, 2, 3, 4, 0 };
		final int[] dueDates = { 0, 8, 9, 10, 11, 15 };
		final int[][] servicePairs = { { 1, 2 }, { 3, 4 } };
		final int vehicleLocation = 0;
		final int serviceTime = 2;

		final MipSolver mip = new MipSolver();
		final SolutionObject solution = mip.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTime);
		System.out.println(solution);

		SolverValidator.validate(solution, travelTime, releaseDates, dueDates, servicePairs, serviceTime);
	}
}
