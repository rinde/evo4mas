package rinde.solver.spdptw.mip;

import java.util.Arrays;

import org.junit.Test;

import rinde.solver.spdptw.SolutionObject;
import rinde.solver.spdptw.Solver;
import rinde.solver.spdptw.SolverDebugger;
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

	@Test
	public void testBig() {

		final int[][] travelTime = new int[][] { { 0, 132, 233, 491, 644, 513, 284, 447, 435, 255, 140, 246 },
				{ 132, 0, 168, 380, 548, 508, 228, 399, 394, 301, 161, 163 },
				{ 233, 168, 0, 276, 414, 351, 61, 232, 227, 216, 127, 35 },
				{ 491, 380, 276, 0, 185, 439, 256, 263, 282, 463, 402, 250 },
				{ 644, 548, 414, 185, 0, 427, 373, 283, 307, 555, 534, 399 },
				{ 513, 508, 351, 439, 427, 0, 297, 177, 160, 280, 376, 373 },
				{ 284, 228, 61, 256, 373, 297, 0, 172, 167, 210, 162, 77 },
				{ 447, 399, 232, 263, 283, 177, 172, 0, 25, 286, 312, 242 },
				{ 435, 394, 227, 282, 307, 160, 167, 25, 0, 265, 298, 240 },
				{ 255, 301, 216, 463, 555, 280, 210, 286, 265, 0, 141, 250 },
				{ 140, 161, 127, 402, 534, 376, 162, 312, 298, 141, 0, 155 },
				{ 246, 163, 35, 250, 399, 373, 77, 242, 240, 250, 155, 0 } };
		final int[] releaseDates = { 0, 1245143, 1755843, 63643, 1330543, 711843, 1811843, 212643, 1453443, 0, 188143,
				0 };
		final int[] dueDates = { 0, 2139143, 2606143, 1758143, 2242143, 1968143, 2564143, 1395843, 2401143, 1843643,
				2030043, 2940143 };
		final int[][] servicePairs = { { 1, 2 }, { 3, 4 }, { 5, 6 }, { 7, 8 } };
		final int serviceTime = 300;

		final Solver mip = SolverDebugger.wrap(SolverValidator.wrap(new MipSolver()));
		final SolutionObject solution = mip.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTime);

	}

	@Test
	public void testBig2() {
		final int[][] travelTime = { { 0, 286, 115, 257, 19, 149, 189, 0 }, { 286, 0, 370, 461, 304, 155, 276, 286 },
				{ 115, 370, 0, 320, 105, 217, 300, 115 }, { 257, 461, 320, 0, 248, 379, 193, 257 },
				{ 19, 304, 105, 248, 0, 167, 195, 19 }, { 149, 155, 217, 379, 167, 0, 237, 149 },
				{ 189, 276, 300, 193, 195, 237, 0, 189 }, { 0, 286, 115, 257, 19, 149, 189, 0 } };
		final int[] releaseDates = { 0, 1247680, 3696240, 6496460, 9785300, 136910, 2282530, 0 };
		final int[] dueDates = { 0, 3965640, 11615000, 11164000, 11711000, 8429800, 11541000, 12029000 };
		final int[][] servicePairs = { { 1, 2 }, { 3, 4 }, { 5, 6 } };
		final int serviceTime = 300;
		final Solver mip = SolverDebugger.wrap(SolverValidator.wrap(new MipSolver()));
		final SolutionObject solution = mip.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTime);
	}

	@Test
	public void testBig3() {
		final int[][] travelTime = { { 0, 275, 135, 266, 79, 0 }, { 275, 0, 379, 295, 313, 275 },
				{ 135, 379, 0, 245, 184, 135 }, { 266, 295, 245, 0, 345, 266 }, { 79, 313, 184, 345, 0, 79 },
				{ 0, 275, 135, 266, 79, 0 } };
		final int[] releaseDates = { 0, 3131, 11933, 3627, 11529, 0 };
		final int[] dueDates = { 0, 11712, 12390, 8548, 12446, 12824000 };
		final int[][] servicePairs = { { 1, 2 }, { 3, 4 } };
		final int serviceTime = 300;
		final Solver mip = SolverValidator.wrap(SolverDebugger.wrap(new MipSolver()));
		final SolutionObject solution = mip.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTime);
	}

	@Test
	public void testBig4() {
		final int[][] travelTime = { { 0, 420, 290, 286, 412, 78, 129, 382, 254, 518, 450, 272 },
				{ 420, 0, 168, 302, 161, 380, 548, 508, 228, 132, 301, 163 },
				{ 290, 168, 0, 303, 127, 276, 414, 351, 61, 233, 216, 35 },
				{ 286, 302, 303, 0, 406, 210, 387, 593, 325, 434, 518, 268 },
				{ 412, 161, 127, 406, 0, 402, 534, 376, 162, 140, 141, 155 },
				{ 78, 380, 276, 210, 402, 0, 185, 439, 256, 491, 463, 250 },
				{ 129, 548, 414, 387, 534, 185, 0, 427, 373, 644, 555, 399 },
				{ 382, 508, 351, 593, 376, 439, 427, 0, 297, 513, 280, 373 },
				{ 254, 228, 61, 325, 162, 256, 373, 297, 0, 284, 210, 77 },
				{ 518, 132, 233, 434, 140, 491, 644, 513, 284, 0, 255, 246 },
				{ 450, 301, 216, 518, 141, 463, 555, 280, 210, 255, 0, 250 },
				{ 272, 163, 35, 268, 155, 250, 399, 373, 77, 246, 250, 0 } };
		final int[] releaseDates = { 0, 2564, 3075, 0, 1507, 1383, 2650, 2031, 3131, 0, 0, 0 };
		final int[] dueDates = { 0, 3458, 3925, 1713, 3349, 3077, 3561, 3287, 3883, 1840, 3163, 4259 };
		final int[][] servicePairs = { { 1, 2 }, { 3, 4 }, { 5, 6 }, { 7, 8 } };
		final int serviceTime = 300;
		final Solver mip = SolverValidator.wrap(SolverDebugger.wrap(new MipSolver()));
		final SolutionObject solution = mip.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTime);
	}

	// takes about 27 minutes!
	// @Test
	public void testBig5() {
		final int[][] travelTime = { { 0, 207, 187, 191, 150, 172, 253, 375, 303, 64, 222, 277, 142 },
				{ 207, 0, 59, 147, 208, 280, 140, 183, 459, 203, 66, 83, 66 },
				{ 187, 59, 0, 187, 229, 228, 94, 236, 466, 201, 122, 139, 61 },
				{ 191, 147, 187, 0, 87, 338, 279, 226, 342, 143, 98, 154, 136 },
				{ 150, 208, 229, 87, 0, 318, 321, 313, 258, 88, 175, 236, 170 },
				{ 172, 280, 228, 338, 318, 0, 228, 462, 445, 234, 327, 363, 228 },
				{ 253, 140, 94, 279, 321, 228, 0, 280, 548, 282, 205, 201, 152 },
				{ 375, 183, 236, 226, 313, 462, 280, 0, 565, 351, 154, 101, 243 },
				{ 303, 459, 466, 342, 258, 445, 548, 565, 0, 267, 433, 493, 408 },
				{ 64, 203, 201, 143, 88, 234, 282, 351, 267, 0, 198, 258, 145 },
				{ 222, 66, 122, 98, 175, 327, 205, 154, 433, 198, 0, 62, 100 },
				{ 277, 83, 139, 154, 236, 363, 201, 101, 493, 258, 62, 0, 142 },
				{ 142, 66, 61, 136, 170, 228, 152, 243, 408, 145, 100, 142, 0 } };
		final int[] releaseDates = { 0, 104, 1900, 0, 0, 1200, 2133, 0, 0, 459, 0, 0, 0 };
		final int[] dueDates = { 0, 3195, 3553, 174, 3444, 2935, 3462, 3371, 2832, 3248, 3514, 3472, 3913 };
		final int[][] servicePairs = { { 1, 2 }, { 3, 4 }, { 5, 6 } };
		final int serviceTime = 300;
		final Solver mip = SolverValidator.wrap(SolverDebugger.wrap(new MipSolver()));
		final SolutionObject solution = mip.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTime);
	}
}
