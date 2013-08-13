package rinde.solver.pdptw;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.cplex.IloCplex;

import java.util.Arrays;

import rinde.sim.pdptw.central.arrays.SingleVehicleArraysSolver;
import rinde.sim.pdptw.central.arrays.SolutionObject;

public class MipSolver implements SingleVehicleArraysSolver {

    public static final int TRAVEL_TIME_WEIGHT = 1;
    public static final int TARDINESS_WEIGHT = 1;

    // max travel time = (7.08/30) * 3600 = 849,6
    // max start time = 27000
    // max service time = 300
    // ---------------------- +
    // total = 28149,6

    public static final int M = 28150;// 28150; // BIG number! Must be bigger
                                      // than
                                      // start_time of a job i + service
                                      // time+
                                      // travel time from i to j, for all
                                      // i,j!
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
    private int[] serviceTime;
    private int nrOfVertices;
    private int nrOfCustomers;

    private IloCplex cplex;
    private IloIntVar[][] flowVars;
    private IloIntVar[] startTimeVars;
    private IloIntVar[] tardinessVars;
    private int nrOfFlowVars;

    private SolutionObject solution;

    public MipSolver() {

    }

    public SolutionObject solve(int[][] travelTime, int[] releaseDates,
            int[] dueDates, int[][] servicePairs, int[] serviceTime) {
        return this
                .solve(travelTime, releaseDates, dueDates, servicePairs, serviceTime, null);
    }

    public SolutionObject solve(int[][] travelTime, int[] releaseDates,
            int[] dueDates, int[][] servicePairs, int[] serviceTime,
            SolutionObject initSolution) {

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
            if (initSolution != null) {
                warmStart(initSolution);
                // this.validateSolution(initSolution); //Enable to fix mip
                // model to initial solution. Model or solution are incorrect if
                // solution is infeasible.
            }
            long time = System.currentTimeMillis();
            this.solve();
            time = System.currentTimeMillis() - time;
            // System.out.println("MIP Solve Time: " + time);
        } catch (final IloException e) {
            e.printStackTrace();
        }

        // ADDED BY RINDE TO CONFORM TO CHANGED SOLUTION OBJECT SPEC
        // SEE SolutionObject.arrivalTimes
        final int[] newArrivalTimes = new int[solution.route.length];
        for (int i = 0; i < solution.arrivalTimes.length; i++) {
            newArrivalTimes[i] = solution.arrivalTimes[solution.route[i]];
        }
        for (int i = 0; i < solution.arrivalTimes.length; i++) {
            solution.arrivalTimes[i] = newArrivalTimes[i];
        }

        // END ADDED BY RINDE

        return solution;
    }

    private void warmStart(SolutionObject initSolution) throws IloException {
        final IloIntVar[] vars = new IloIntVar[nrOfFlowVars + nrOfVertices
                + nrOfVertices - 1];
        final double[] values = new double[vars.length];

        final int[] routeSuccessors = new int[nrOfVertices - 1];
        for (int i = 0; i < nrOfVertices - 1; i++) {
            final int v1 = initSolution.route[i];
            final int v2 = initSolution.route[i + 1];
            routeSuccessors[v1] = v2;
        }

        int offset = 0;

        // Initialize the Flow Vars:
        for (int i = 0; i < nrOfVertices; i++) {
            for (int j = 0; j < nrOfVertices; j++) {
                if (flowVars[i][j] == null) {
                    continue;
                }
                vars[offset] = flowVars[i][j];
                values[offset] = (routeSuccessors[i] == j ? 1 : 0);
                offset++;
            }
        }

        // Initialize the startTimeVars and tardinessVars:
        for (int i = 0; i < nrOfVertices; i++) {
            final int arrivalTime = initSolution.arrivalTimes[i];
            vars[offset] = startTimeVars[i];
            values[offset] = arrivalTime;
            // System.out.println(vars[offset]+"= "+values[offset]);
            offset++;

            if (i == source) {
                continue;
            }
            vars[offset] = tardinessVars[i];
            values[offset] = Math.max(0, (arrivalTime + serviceTime[i])
                    - dueDates[i]);
            // System.out.println(vars[offset]+"= "+values[offset]);
            offset++;
        }
        cplex.addMIPStart(vars, values, IloCplex.MIPStartEffort.CheckFeas);
    }

    private void validateSolution(SolutionObject initSolution)
            throws IloException {
        final IloIntVar[] vars = new IloIntVar[nrOfFlowVars + nrOfVertices
                + nrOfVertices - 1];
        final double[] values = new double[vars.length];

        final int[] routeSuccessors = new int[nrOfVertices - 1];
        for (int i = 0; i < nrOfVertices - 1; i++) {
            final int v1 = initSolution.route[i];
            final int v2 = initSolution.route[i + 1];
            routeSuccessors[v1] = v2;
        }

        final int offset = 0;

        // Initialize the Flow Vars:
        for (int i = 0; i < nrOfVertices; i++) {
            for (int j = 0; j < nrOfVertices; j++) {
                if (flowVars[i][j] == null) {
                    continue;
                }

                if (routeSuccessors[i] == j) {
                    flowVars[i][j].setLB(1);
                } else {
                    flowVars[i][j].setUB(0);
                }
            }
        }

        // Initialize the startTimeVars and tardinessVars:
        for (int i = 0; i < nrOfVertices; i++) {
            final int arrivalTime = initSolution.arrivalTimes[i];
            startTimeVars[i].setLB(arrivalTime);
            startTimeVars[i].setUB(arrivalTime);

            if (i == source) {
                continue;
            }

            final int tardiness = Math.max(0, (arrivalTime + serviceTime[i])
                    - dueDates[i]);
            tardinessVars[i].setLB(tardiness);
            tardinessVars[i].setUB(tardiness);
        }

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
        cplex.setOut(null);

        // build the variables
        flowVars = new IloIntVar[nrOfVertices][nrOfVertices];
        nrOfFlowVars = 0;
        startTimeVars = new IloIntVar[nrOfVertices];
        tardinessVars = new IloIntVar[nrOfVertices];

        // Flow variables between customers
        final int[] predecessors = new int[nrOfVertices];
        final int[] successors = new int[nrOfVertices];
        final boolean[] isSuccessor = new boolean[nrOfVertices];
        final boolean[] isPredecessor = new boolean[nrOfVertices];
        Arrays.fill(predecessors, -1);
        Arrays.fill(successors, -1);
        Arrays.fill(isSuccessor, false);
        Arrays.fill(isPredecessor, false);
        for (final int[] servicePair : servicePairs) {
            predecessors[servicePair[1]] = servicePair[0];
            successors[servicePair[0]] = servicePair[1];
            isSuccessor[servicePair[1]] = true;
            isPredecessor[servicePair[0]] = true;
        }

        for (int i = 1; i < nrOfVertices - 1; i++) {
            for (int j = 1; j < nrOfVertices - 1; j++) {
                if (i == j || successors[j] == i) {
                    continue;
                }
                flowVars[i][j] = cplex.boolVar("x_" + i + "," + j);
                nrOfFlowVars++;
            }
        }
        // Flow variables leaving the source
        for (int j = 1; j < nrOfVertices - 1; j++) {
            if (isSuccessor[j]) {
                continue;
            }
            flowVars[source][j] = cplex.boolVar("x_" + source + "," + j);
            nrOfFlowVars++;
        }
        // Flow variables entering the sink
        for (int i = 1; i < nrOfVertices - 1; i++) {
            if (isPredecessor[i]) {
                continue;
            }
            flowVars[i][sink] = cplex.boolVar("x_" + i + "," + sink);
            nrOfFlowVars++;
        }

        // Start time variables and tardiness variables
        for (int i = 0; i < nrOfVertices; i++) {
            int earliestStart = releaseDates[i];
            if (isSuccessor[i]) {
                final int predecessor = predecessors[i];
                earliestStart = Math
                        .max(earliestStart, releaseDates[predecessor]
                                + serviceTime[predecessor]
                                + travelTime[predecessor][i]);
            }
            startTimeVars[i] = cplex
                    .intVar(earliestStart, Integer.MAX_VALUE, "s" + i);

            final int leastTardiness = Math.max(0, earliestStart
                    + serviceTime[i] - dueDates[i]);
            tardinessVars[i] = cplex
                    .intVar(leastTardiness, Integer.MAX_VALUE, "g" + i);
        }
        startTimeVars[source].setMax(0);
        tardinessVars[source].setMax(0);

        // build the objective: Minimize
        // TRAVEL_TIME_WEIGHT*totalTravelTime+TARDINESS_WEIGHT*totalTardiness
        final IloLinearIntExpr obj = cplex.linearIntExpr();
        // for (int i = 1; i < nrOfVertices - 1; i++) {
        // for (int j = 1; j < nrOfVertices - 1; j++) {
        // if (i == j) {
        // continue;
        // }
        // obj.addTerm(TRAVEL_TIME_WEIGHT * travelTime[i][j], flowVars[i][j]);
        // }
        // }
        // for (int j = 1; j < nrOfVertices - 1; j++) {
        // obj.addTerm(TRAVEL_TIME_WEIGHT * travelTime[source][j],
        // flowVars[source][j]);
        // }
        // for (int i = 1; i < nrOfVertices - 1; i++) {
        // obj.addTerm(TRAVEL_TIME_WEIGHT * travelTime[i][sink],
        // flowVars[i][sink]);
        // }

        for (int i = 0; i < nrOfVertices; i++) {
            for (int j = 0; j < nrOfVertices; j++) {
                if (flowVars[i][j] == null) {
                    continue;
                }
                obj.addTerm(TRAVEL_TIME_WEIGHT * travelTime[i][j], flowVars[i][j]);
            }
        }

        for (int i = 0; i < nrOfVertices; i++) {
            obj.addTerm(TARDINESS_WEIGHT, tardinessVars[i]);
        }
        cplex.addMinimize(obj);

        // build the constraints
        // 1. Vehicle should leave the source depot
        final IloLinearIntExpr exprLeaveSource = cplex.linearIntExpr();
        for (int j = 1; j < nrOfVertices - 1; j++) {
            if (flowVars[source][j] != null) {
                exprLeaveSource.addTerm(1, flowVars[source][j]);
            }
        }
        cplex.addEq(exprLeaveSource, 1, "leaveSource");

        // 2. Vehicle should enter the sink
        final IloLinearIntExpr exprEnterSink = cplex.linearIntExpr();
        for (int i = 1; i < nrOfVertices - 1; i++) {
            if (flowVars[i][sink] != null) {
                exprEnterSink.addTerm(1, flowVars[i][sink]);
            }
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
                if (flowVars[i][j] != null) {
                    expr1.addTerm(1, flowVars[i][j]);
                }
                if (flowVars[j][i] != null) {
                    expr2.addTerm(1, flowVars[j][i]);
                }
            }
            if (flowVars[i][sink] != null) {
                expr1.addTerm(1, flowVars[i][sink]);
            }
            if (flowVars[source][i] != null) {
                expr2.addTerm(1, flowVars[source][i]);
            }
            cplex.addEq(expr1, expr2, "flowPreserv" + i);
        }

        // 4. Each customer must be visited
        for (int i = 1; i < nrOfVertices - 1; i++) {
            final IloLinearIntExpr expr = cplex.linearIntExpr();
            for (int j = 1; j < nrOfVertices; j++) {
                if (flowVars[i][j] == null) {
                    continue;
                }
                expr.addTerm(1, flowVars[i][j]);
            }
            cplex.addEq(expr, 1, "visitCust" + i);
        }

        // 5. Travel and processing times for each vertex pair
        for (int i = 0; i < nrOfVertices - 1; i++) {
            for (int j = 1; j < nrOfVertices; j++) {
                if (flowVars[i][j] == null) {
                    continue;
                }
                final IloLinearIntExpr expr = cplex.linearIntExpr();
                expr.addTerm(1, startTimeVars[i]);
                expr.addTerm(-1, startTimeVars[j]);
                expr.addTerm(M, flowVars[i][j]);

                int time = M;
                time -= serviceTime[i];
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
            cplex.addLe(expr, -serviceTime[i] - travelTime[i][j], "prec_" + i
                    + "," + j);
        }

        // 7. Tardiness constraints
        for (int i = 0; i < nrOfVertices; i++) {
            final IloLinearIntExpr expr = cplex.linearIntExpr();
            expr.addTerm(1, tardinessVars[i]);
            expr.addTerm(-1, startTimeVars[i]);
            int time = serviceTime[i];
            time -= dueDates[i];
            cplex.addGe(expr, time, "tardiness" + i);
        }

        // 8a. Lower bound on start time
        for (int i = 0; i < nrOfVertices; i++) {
            final IloLinearIntExpr expr = cplex.linearIntExpr();
            expr.setConstant(releaseDates[i]);
            for (int j = 0; j < nrOfVertices; j++) {
                if (flowVars[j][i] == null) {
                    continue;
                }
                expr.addTerm(Math.max(0, releaseDates[j] - releaseDates[i]
                        + serviceTime[j] + travelTime[i][j]), flowVars[j][i]);
            }
            cplex.addGe(startTimeVars[i], expr, "lbStartTime" + i);
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
                && (cplex.getStatus() == IloCplex.Status.Feasible || cplex
                        .getStatus() == IloCplex.Status.Optimal)) {
            solution = getSolution();
        } else if (cplex.getStatus() == IloCplex.Status.Unknown) {
            throw new RuntimeException(
                    "No solution could be found in the given amount of time");
        } else {
            // NOTE: when cplex does not find a solution before the default time
            // out, it throws a Status Unknown exception
            // NOTE2: Might be required to extend the default runtime.
            throw new RuntimeException("Cplex solve terminated with status: "
                    + cplex.getStatus());
        }
        // cplex.end();
    }

    public void end() {
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
            if (flowVars[source][j] != null
                    && doubleToBoolean(cplex.getValue(flowVars[source][j]))) {
                successorArray[source] = j;
                // System.out.println("x_"+source+","+j+"=1");
                break;
            }
        }

        for (int i = 1; i < nrOfVertices - 1; i++) {
            for (int j = 1; j < nrOfVertices; j++) {
                if (flowVars[i][j] == null) {
                    continue;
                }
                if (doubleToBoolean(cplex.getValue(flowVars[i][j]))) {
                    successorArray[i] = j;
                    // System.out.println("x_"+i+","+j+"=1");
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
            // System.out.println(startTimeVars[i]+"="+arrivalTimes[i]);
        }

        final SolutionObject sol = new SolutionObject(serviceSequence,
                arrivalTimes, objective);
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
            throw new RuntimeException(
                    "Failed to convert to boolean, not near zero or one: "
                            + value);
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
            throw new RuntimeException(
                    "Failed to convert to int, not near an integer value: "
                            + value);
        }
    }
}
