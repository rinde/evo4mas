package rinde.solver.spdptw.mip;
import rinde.solver.spdptw.SolutionObject;
import rinde.solver.spdptw.SolverAPI;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.cplex.IloCplex;

public class MipSolver implements SolverAPI {

	public static final int TRAVEL_TIME_WEIGHT = 1;
	public static final int TARDINESS_WEIGHT = 1;
	public static final int M = 10000; // BIG number! Must be bigger than
										// start_time of a job i + service time+
										// travel time from i to j, for all i,j!
	public static final double EPSILON = 0.000001; // precision
	public static final int TIMELIMIT = 100000; // time limit in seconds
	public static final int MAXTHREADS = 8; // number of threads used by the
											// optimizer. Default: 8

	private int[][] travelTime;
	private int[] releaseDates;
	private int[] dueDates;
	private int[][] servicePairs;
	private final int source = 0; // start location of the vehicle
	private int sink; // end location of the vehicle
	private int serviceTime;
	private int nrOfVertices;
	private int nrOfCustomers;

	private IloCplex cplex;
	private IloIntVar[][] flowVars;
	private IloIntVar[] startTimeVars;
	private IloIntVar[] tardinessVars;

	private SolutionObject solution;

	public MipSolver() {

	}

	public SolutionObject solve(int[][] travelTime, int[] releaseDates, int[] dueDates, int[][] servicePairs,
			int serviceTime) {

		this.travelTime = travelTime;
		this.releaseDates = releaseDates;
		this.dueDates = dueDates;
		sink = dueDates.length - 1;
		this.servicePairs = servicePairs;
		this.serviceTime = serviceTime;
		nrOfCustomers = releaseDates.length - 1;
		nrOfVertices = releaseDates.length;

		try {
			buildModel();
			this.solve();
		} catch (final IloException e) {
			e.printStackTrace();
		}

		return solution;
	}

	/**
	 * Builds the MIP Model
	 * @throws IloException
	 */
	private void buildModel() throws IloException {
		cplex = new IloCplex();
		cplex.setParam(IloCplex.DoubleParam.TiLim, TIMELIMIT);
		cplex.setParam(IloCplex.IntParam.Threads, MAXTHREADS);

		// supress output
		// cplex.setOut(null);

		// build the variables
		flowVars = new IloIntVar[nrOfVertices][nrOfVertices];
		startTimeVars = new IloIntVar[nrOfVertices];
		tardinessVars = new IloIntVar[nrOfVertices];

		// Flow variables between customers
		for (int i = 1; i < nrOfVertices - 1; i++) {
			for (int j = 1; j < nrOfVertices - 1; j++) {
				if (i == j) {
					continue;
				}
				flowVars[i][j] = cplex.boolVar("x_" + i + "," + j);
			}
		}
		// Flow variables leaving the source
		for (int j = 1; j < nrOfVertices - 1; j++) {
			flowVars[source][j] = cplex.boolVar("x_" + source + "," + j);
		}
		// Flow variables entering the sink
		for (int i = 1; i < nrOfVertices - 1; i++) {
			flowVars[i][sink] = cplex.boolVar("x_" + i + "," + sink);
		}

		// Start time variables and tardiness variables
		for (int i = 0; i < nrOfVertices; i++) {
			startTimeVars[i] = cplex.intVar(releaseDates[i], Integer.MAX_VALUE, "s" + i);
			tardinessVars[i] = cplex.intVar(0, Integer.MAX_VALUE, "g" + i);
		}
		startTimeVars[source].setMax(0);
		tardinessVars[source].setMax(0);

		// build the objective: Minimize
		// TRAVEL_TIME_WEIGHT*totalTravelTime+TARDINESS_WEIGHT*totalTardiness
		final IloLinearIntExpr obj = cplex.linearIntExpr();
		for (int i = 1; i < nrOfVertices - 1; i++) {
			for (int j = 1; j < nrOfVertices - 1; j++) {
				if (i == j) {
					continue;
				}
				obj.addTerm(TRAVEL_TIME_WEIGHT * travelTime[i][j], flowVars[i][j]);
			}
		}
		for (int j = 1; j < nrOfVertices - 1; j++) {
			obj.addTerm(TRAVEL_TIME_WEIGHT * travelTime[source][j], flowVars[source][j]);
		}
		for (int i = 1; i < nrOfVertices - 1; i++) {
			obj.addTerm(TRAVEL_TIME_WEIGHT * travelTime[i][sink], flowVars[i][sink]);
		}
		for (int i = 0; i < nrOfVertices; i++) {
			obj.addTerm(TARDINESS_WEIGHT, tardinessVars[i]);
		}
		cplex.addMinimize(obj);

		// build the constraints
		// 1. Vehicle should leave the source depot
		final IloLinearIntExpr exprLeaveSource = cplex.linearIntExpr();
		for (int j = 1; j < nrOfVertices - 1; j++) {
			exprLeaveSource.addTerm(1, flowVars[source][j]);
		}
		cplex.addEq(exprLeaveSource, 1, "leaveSource");

		// 2. Vehicle should enter the sink
		final IloLinearIntExpr exprEnterSink = cplex.linearIntExpr();
		for (int i = 1; i < nrOfVertices - 1; i++) {
			exprEnterSink.addTerm(1, flowVars[i][sink]);
		}
		cplex.addEq(exprEnterSink, 1, "enterSink");

		// 3. Flow preservation for the customer vertices
		for (int i = 1; i < nrOfVertices - 1; i++) {
			final IloLinearIntExpr expr1 = cplex.linearIntExpr();
			final IloLinearIntExpr expr2 = cplex.linearIntExpr();
			for (int j = 1; j < nrOfVertices - 1; j++) {
				if (i == j) {
					continue;
				}
				expr1.addTerm(1, flowVars[i][j]);
				expr2.addTerm(1, flowVars[j][i]);
			}
			expr1.addTerm(1, flowVars[i][sink]);
			expr2.addTerm(1, flowVars[source][i]);
			cplex.addEq(expr1, expr2, "flowPreserv" + i);
		}

		// 4. Each customer must be visited
		for (int i = 1; i < nrOfVertices - 1; i++) {
			final IloLinearIntExpr expr = cplex.linearIntExpr();
			for (int j = 1; j < nrOfVertices; j++) {
				if (i == j) {
					continue;
				}
				expr.addTerm(1, flowVars[i][j]);
			}
			cplex.addEq(expr, 1, "visitCust" + i);
		}

		// 5. Travel and processing times for each vertex pair
		for (int i = 0; i < nrOfVertices - 1; i++) {
			for (int j = 1; j < nrOfVertices; j++) {
				if (i == j || (i == source && j == sink)) {
					continue;
				}
				final IloLinearIntExpr expr = cplex.linearIntExpr();
				expr.addTerm(1, startTimeVars[i]);
				expr.addTerm(-1, startTimeVars[j]);
				expr.addTerm(M, flowVars[i][j]);

				int time = M;
				time -= (i == source ? 0 : serviceTime);
				time -= travelTime[i][j];

				cplex.addLe(expr, time, "timeConstr" + i + "," + j);
			}
		}

		// 6. Precedence constraints
		for (final int[] precede : servicePairs) {
			final int i = precede[0];
			final int j = precede[1];
			final IloLinearIntExpr expr = cplex.linearIntExpr();
			expr.addTerm(1, startTimeVars[i]);
			expr.addTerm(-1, startTimeVars[j]);
			cplex.addLe(expr, -serviceTime - travelTime[i][j], "prec_" + i + "," + j);
		}

		// 7. Tardiness constraints
		for (int i = 0; i < nrOfVertices; i++) {
			final IloLinearIntExpr expr = cplex.linearIntExpr();
			expr.addTerm(1, tardinessVars[i]);
			expr.addTerm(-1, startTimeVars[i]);
			int time = (i != source && i != sink ? serviceTime : 0);
			time -= dueDates[i];
			cplex.addGe(expr, time, "tardiness" + i);
		}

		// Export the model
		// cplex.exportModel("mip.lp");
	}

	/**
	 * Solves the MIP model and constructs a solution
	 * @throws IloException
	 */
	private void solve() throws IloException {
		if (cplex.solve()
				&& (cplex.getStatus() == IloCplex.Status.Feasible || cplex.getStatus() == IloCplex.Status.Optimal)) {
			solution = getSolution();
		} else if (cplex.getStatus() == IloCplex.Status.Unknown) {
			throw new RuntimeException("No solution could be found in the given amount of time");
		} else {
			// NOTE: when cplex does not find a solution before the default time
			// out, it throws a Status Unknown exception
			// NOTE2: Might be required to extend the default runtime.
			throw new RuntimeException("Cplex solve terminated with status: " + cplex.getStatus());
		}
		cplex.end();
	}

	/**
	 * Build a SolutionObject from the result obtained by the MIP model
	 * @throws IloException
	 */
	private SolutionObject getSolution() throws IloException {
		final int objective = (int) Math.round(cplex.getObjValue());

		final int[] serviceSequence = new int[nrOfVertices];
		final int[] successorArray = new int[nrOfVertices];
		final int[] arrivalTimes = new int[nrOfVertices];

		for (int j = 1; j < nrOfVertices - 1; j++) {
			if (doubleToBoolean(cplex.getValue(flowVars[source][j]))) {
				successorArray[source] = j;
				break;
			}
		}

		for (int i = 1; i < nrOfVertices - 1; i++) {
			for (int j = 1; j < nrOfVertices; j++) {
				if (i == j) {
					continue;
				}
				if (doubleToBoolean(cplex.getValue(flowVars[i][j]))) {
					successorArray[i] = j;
					break;
				}
			}
		}

		serviceSequence[0] = source;
		for (int i = 1; i < nrOfVertices; i++) {
			serviceSequence[i] = successorArray[serviceSequence[i - 1]];
		}

		final double[] startTimeValues = cplex.getValues(startTimeVars);
		for (int i = 0; i < nrOfVertices; i++) {
			arrivalTimes[i] = doubleToInt(startTimeValues[i]);
		}

		final SolutionObject sol = new SolutionObject(serviceSequence, arrivalTimes, objective);
		return sol;
	}

	// ==========HELPER METHODS=============

	/**
	 * Returns true if the variable is +/- 1, false if the variable is +/- 0,
	 * and throws an error otherwise
	 */
	public static boolean doubleToBoolean(double value) {
		if (Math.abs(1 - value) < EPSILON) {
			return true;
		} else if (Math.abs(value) < EPSILON) {
			return false;
		} else {
			throw new RuntimeException("Failed to convert to boolean, not near zero or one: " + value);
		}
	}

	/**
	 * Returns the nearest int. Throws an exception if the nearest int is
	 * further away than a given constant
	 */
	public static int doubleToInt(double value) {
		final int result = (int) Math.round(value);
		if (Math.abs(value - result) < EPSILON) {
			return result;
		} else {
			throw new RuntimeException("Failed to convert to int, not near an integer value: " + value);
		}
	}
}
