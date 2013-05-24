public class Test {

	public static void main(String[] args) {
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
}
