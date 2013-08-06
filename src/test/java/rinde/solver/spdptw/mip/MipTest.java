package rinde.solver.spdptw.mip;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import rinde.solver.spdptw.SolutionObject;
import rinde.solver.spdptw.Solver;
import rinde.solver.spdptw.SolverDebugger;
import rinde.solver.spdptw.SolverValidator;

public class MipTest {

    Solver solver;

    @Before
    public void setUp() {
        solver = SolverDebugger.wrap(SolverValidator.wrap(new MipSolver()));
    }

    @Test
    public void test1() {
        final int[][] travelTime = { { 0, 1, 2, 3, 4 }, { 1, 0, 2, 3, 4 },
                { 2, 2, 0, 4, 4 }, { 3, 3, 4, 0, 4 }, { 4, 4, 4, 4, 0 } };

        final int[] releaseDates = { 0, 1, 2, 3, 0 };
        final int[] dueDates = { 0, 8, 9, 10, 11 };
        final int[][] servicePairs = { { 1, 2 } };
        final int[] serviceTimes = createServiceTimes(2, 5);

        final Solver mip = SolverDebugger.wrap(new MipSolver());
        final SolutionObject solution = mip
                .solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
        System.out.println(solution);

        SolverValidator
                .validate(solution, travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void test2() {
        final int[][] locations = new int[][] { { 1, 2 }, { 3, 4 }, { 8, 6 },
                { 4, 5 }, { 5, 4 }, { 0, 0 } };

        final int[][] travelTime = new int[locations.length][locations.length];
        for (int i = 0; i < locations.length; i++) {
            for (int j = 0; j < locations.length; j++) {
                if (i == j) {
                    continue;
                }
                travelTime[i][j] = (int) Math.ceil(Math.sqrt(Math
                        .pow(locations[i][0] - locations[j][0], 2)
                        + Math.pow(locations[i][1] - locations[j][1], 2)));
            }
        }

        System.out.println(Arrays.deepToString(travelTime));

        final int[] releaseDates = { 0, 1, 2, 3, 4, 0 };
        final int[] dueDates = { 0, 8, 9, 10, 11, 15 };
        final int[][] servicePairs = { { 1, 2 }, { 3, 4 } };
        final int[] serviceTimes = createServiceTimes(2, 6);

        final MipSolver mip = new MipSolver();
        final SolutionObject solution = mip
                .solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
        System.out.println(solution);

        SolverValidator
                .validate(solution, travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testBig3() {
        final int[][] travelTime = { { 0, 275, 135, 266, 79, 0 },
                { 275, 0, 379, 295, 313, 275 }, { 135, 379, 0, 245, 184, 135 },
                { 266, 295, 245, 0, 345, 266 }, { 79, 313, 184, 345, 0, 79 },
                { 0, 275, 135, 266, 79, 0 } };
        final int[] releaseDates = { 0, 3131, 11933, 3627, 11529, 0 };
        final int[] dueDates = { 0, 11712, 12390, 8548, 12446, 12824000 };
        final int[][] servicePairs = { { 1, 2 }, { 3, 4 } };
        final int[] serviceTimes = createServiceTimes(300, dueDates.length);
        final Solver mip = SolverValidator.wrap(SolverDebugger
                .wrap(new MipSolver()));
        final SolutionObject solution = mip
                .solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testBig4() {
        final int[][] travelTime = {
                { 0, 420, 290, 286, 412, 78, 129, 382, 254, 518, 450, 272 },
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
        final int[] releaseDates = { 0, 2564, 3075, 0, 1507, 1383, 2650, 2031,
                3131, 0, 0, 0 };
        final int[] dueDates = { 0, 3458, 3925, 1713, 3349, 3077, 3561, 3287,
                3883, 1840, 3163, 4259 };
        final int[][] servicePairs = { { 1, 2 }, { 3, 4 }, { 5, 6 }, { 7, 8 } };
        final int[] serviceTimes = createServiceTimes(300, dueDates.length);
        final Solver mip = SolverValidator.wrap(SolverDebugger
                .wrap(new MipSolver()));
        final SolutionObject solution = mip
                .solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    // takes about 27 minutes!
    // @Test
    public void testBig5() {
        final int[][] travelTime = {
                { 0, 207, 187, 191, 150, 172, 253, 375, 303, 64, 222, 277, 142 },
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
        final int[] releaseDates = { 0, 104, 1900, 0, 0, 1200, 2133, 0, 0, 459,
                0, 0, 0 };
        final int[] dueDates = { 0, 3195, 3553, 174, 3444, 2935, 3462, 3371,
                2832, 3248, 3514, 3472, 3913 };
        final int[][] servicePairs = { { 1, 2 }, { 3, 4 }, { 5, 6 } };
        final int[] serviceTimes = createServiceTimes(300, dueDates.length);
        final Solver mip = SolverValidator.wrap(SolverDebugger
                .wrap(new MipSolver()));
        final SolutionObject solution = mip
                .solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    // takes more than 2 hours!
    public void testBig6() {
        final int[][] travelTime = {
                { 0, 211, 127, 121, 316, 123, 329, 308, 194, 137, 220, 114, 93,
                        433, 235, 25, 410, 133, 181 },
                { 211, 0, 251, 194, 118, 330, 148, 178, 98, 113, 109, 192, 210,
                        225, 226, 203, 226, 191, 112 },
                { 127, 251, 0, 242, 368, 139, 316, 266, 182, 229, 200, 60, 218,
                        444, 144, 105, 389, 253, 163 },
                { 121, 194, 242, 0, 261, 225, 339, 348, 232, 82, 258, 212, 36,
                        410, 320, 138, 418, 13, 229 },
                { 316, 118, 368, 261, 0, 438, 176, 243, 207, 189, 208, 310,
                        289, 166, 333, 313, 225, 253, 224 },
                { 123, 330, 139, 225, 438, 0, 431, 394, 292, 259, 316, 176,
                        190, 546, 283, 127, 510, 238, 275 },
                { 329, 148, 316, 339, 176, 431, 0, 82, 140, 259, 117, 261, 351,
                        137, 216, 313, 82, 337, 157 },
                { 308, 178, 266, 348, 243, 394, 82, 0, 119, 273, 91, 219, 352,
                        215, 145, 289, 129, 349, 128 },
                { 194, 98, 182, 232, 207, 292, 140, 119, 0, 163, 28, 124, 234,
                        263, 129, 176, 219, 235, 20 },
                { 137, 113, 229, 82, 189, 259, 259, 273, 163, 0, 187, 183, 102,
                        330, 270, 141, 338, 79, 164 },
                { 220, 109, 200, 258, 208, 316, 117, 91, 28, 187, 0, 144, 261,
                        246, 125, 202, 194, 261, 41 },
                { 114, 192, 60, 212, 310, 176, 261, 219, 124, 183, 144, 0, 195,
                        386, 122, 90, 337, 221, 106 },
                { 93, 210, 218, 36, 289, 190, 351, 352, 234, 102, 261, 195, 0,
                        431, 309, 113, 432, 49, 228 },
                { 433, 225, 444, 410, 166, 546, 137, 215, 263, 330, 246, 386,
                        431, 0, 353, 422, 112, 404, 281 },
                { 235, 226, 144, 320, 333, 283, 216, 145, 129, 270, 125, 122,
                        309, 353, 0, 211, 274, 327, 116 },
                { 25, 203, 105, 138, 313, 127, 313, 289, 176, 141, 202, 90,
                        113, 422, 211, 0, 394, 150, 162 },
                { 410, 226, 389, 418, 225, 510, 82, 129, 219, 338, 194, 337,
                        432, 112, 274, 394, 0, 416, 235 },
                { 133, 191, 253, 13, 253, 238, 337, 349, 235, 79, 261, 221, 49,
                        404, 327, 150, 416, 0, 233 },
                { 181, 112, 163, 229, 224, 275, 157, 128, 20, 164, 41, 106,
                        228, 281, 116, 162, 235, 233, 0 } };
        final int[] releaseDates = { 0, 2583, 4392, 0, 2844, 0, 1102, 2288,
                3616, 1199, 3930, 1200, 3887, 0, 3009, 4107, 5475, 848, 0 };
        final int[] dueDates = { 0, 5794, 6344, 3135, 6283, 5620, 6350, 4023,
                6487, 5224, 6466, 5500, 6279, 4574, 6391, 5320, 6272, 4444,
                6806 };
        final int[][] servicePairs = { { 1, 2 }, { 3, 4 }, { 5, 6 }, { 7, 8 },
                { 9, 10 }, { 11, 12 }, { 13, 14 }, { 15, 16 } };
        final int[] serviceTimes = createServiceTimes(300, dueDates.length);
        final Solver mip = SolverValidator.wrap(SolverDebugger
                .wrap(new MipSolver()));
        final SolutionObject solution = mip
                .solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew1() {
        final int[][] travelTime = { { 0, 290, 135, 234, 178, 0 },
                { 290, 0, 275, 515, 371, 290 }, { 135, 275, 0, 269, 312, 135 },
                { 234, 515, 269, 0, 314, 234 }, { 178, 371, 312, 314, 0, 178 },
                { 0, 290, 135, 234, 178, 0 } };
        final int[] releaseDates = { 0, 2042, 583, 8266, 5339, 0 };
        final int[] dueDates = { 0, 6078, 2850, 12842, 11731, 13375 };
        final int[][] servicePairs = { { 1, 3 }, { 2, 4 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew2() {
        final int[][] travelTime = { { 0, 167, 257, 87, 124, 0 },
                { 167, 0, 347, 194, 271, 167 }, { 257, 347, 0, 338, 327, 257 },
                { 87, 194, 338, 0, 85, 87 }, { 124, 271, 327, 85, 0, 124 },
                { 0, 167, 257, 87, 124, 0 } };
        final int[] releaseDates = { 0, 3558, 6765, 5953, 8844, 0 };
        final int[] dueDates = { 0, 12402, 8728, 12895, 12858, 13281 };
        final int[][] servicePairs = { { 1, 3 }, { 2, 4 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew3() {
        final int[][] travelTime = { { 0, 290, 135, 141, 234, 178, 237, 0 },
                { 290, 0, 275, 429, 515, 371, 236, 290 },
                { 135, 275, 0, 213, 269, 312, 328, 135 },
                { 141, 429, 213, 0, 101, 220, 352, 141 },
                { 234, 515, 269, 101, 0, 314, 453, 234 },
                { 178, 371, 312, 220, 314, 0, 184, 178 },
                { 237, 236, 328, 352, 453, 184, 0, 237 },
                { 0, 290, 135, 141, 234, 178, 237, 0 } };
        final int[] releaseDates = { 0, 1746, 287, 10, 7970, 5043, 927, 0 };
        final int[] dueDates = { 0, 5782, 2554, 4139, 12546, 11435, 11132,
                13079 };
        final int[][] servicePairs = { { 1, 4 }, { 2, 5 }, { 3, 6 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 300, 0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew4() {
        final int[][] travelTime = { { 0, 275, 134, 135, 208, 0 },
                { 275, 0, 367, 379, 208, 275 }, { 134, 367, 0, 211, 340, 134 },
                { 135, 379, 211, 0, 235, 135 }, { 208, 208, 340, 235, 0, 208 },
                { 0, 275, 134, 135, 208, 0 } };
        final int[] releaseDates = { 0, 3362, 839, 12164, 5036, 0 };
        final int[] dueDates = { 0, 11943, 2616, 12621, 12548, 13055 };
        final int[][] servicePairs = { { 1, 3 }, { 2, 4 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew5() {
        final int[][] travelTime = { { 0, 402, 146, 270, 63, 0 },
                { 402, 0, 351, 668, 449, 402 }, { 146, 351, 0, 385, 145, 146 },
                { 270, 668, 385, 0, 242, 270 }, { 63, 449, 145, 242, 0, 63 },
                { 0, 402, 146, 270, 63, 0 } };
        final int[] releaseDates = { 0, 5160, 1717, 8243, 11326, 0 };
        final int[] dueDates = { 0, 8568, 3326, 12278, 12485, 12847 };
        final int[][] servicePairs = { { 1, 3 }, { 2, 4 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew6() {
        final int[][] travelTime = { { 0, 266, 303, 79, 279, 0 },
                { 266, 0, 132, 345, 501, 266 }, { 303, 132, 0, 377, 471, 303 },
                { 79, 345, 377, 0, 234, 79 }, { 279, 501, 471, 234, 0, 279 },
                { 0, 266, 303, 79, 279, 0 } };
        final int[] releaseDates = { 0, 3176, 2110, 11078, 7342, 0 };
        final int[] dueDates = { 0, 8097, 5714, 11995, 11163, 12373 };
        final int[][] servicePairs = { { 1, 3 }, { 2, 4 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew7() {
        final int[][] travelTime = { { 0, 275, 134, 257, 135, 208, 19, 0 },
                { 275, 0, 367, 469, 379, 208, 268, 275 },
                { 134, 367, 0, 332, 211, 340, 152, 134 },
                { 257, 469, 332, 0, 126, 286, 248, 257 },
                { 135, 379, 211, 126, 0, 235, 129, 135 },
                { 208, 208, 340, 286, 235, 0, 190, 208 },
                { 19, 268, 152, 248, 129, 190, 0, 19 },
                { 0, 275, 134, 257, 135, 208, 19, 0 } };
        final int[] releaseDates = { 0, 2674, 151, 6834, 11476, 4348, 10123, 0 };
        final int[] dueDates = { 0, 11255, 1928, 11502, 11933, 11860, 12049,
                12367 };
        final int[][] servicePairs = { { 1, 4 }, { 2, 5 }, { 3, 6 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 300, 0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew8() {
        final int[][] travelTime = { { 0, 9, 114, 163, 353, 223, 154 },
                { 9, 0, 116, 156, 349, 219, 153 },
                { 114, 116, 0, 178, 279, 167, 63 },
                { 163, 156, 178, 0, 241, 119, 150 },
                { 353, 349, 279, 241, 0, 132, 216 },
                { 223, 219, 167, 119, 132, 0, 108 },
                { 154, 153, 63, 150, 216, 108, 0 } };
        final int[] releaseDates = { 0, 4701, 1539, 10503, 5615, 6359, 0 };
        final int[] dueDates = { 0, 11257, 3969, 11712, 11224, 11754, 12161 };
        final int[][] servicePairs = { { 1, 3 }, { 2, 4 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew9() {
        final int[][] travelTime = {
                { 0, 367, 332, 61, 199, 211, 152, 178, 354, 340, 134 },
                { 367, 0, 469, 335, 476, 379, 268, 463, 59, 208, 275 },
                { 332, 469, 0, 379, 180, 126, 248, 193, 418, 286, 257 },
                { 61, 335, 379, 0, 257, 255, 167, 237, 331, 343, 149 },
                { 199, 476, 180, 257, 0, 123, 209, 22, 441, 355, 204 },
                { 211, 379, 126, 255, 123, 0, 129, 122, 336, 235, 135 },
                { 152, 268, 248, 167, 209, 129, 0, 195, 236, 190, 19 },
                { 178, 463, 193, 237, 22, 122, 195, 0, 429, 350, 189 },
                { 354, 59, 418, 331, 441, 336, 236, 429, 0, 150, 246 },
                { 340, 208, 286, 343, 355, 235, 190, 350, 150, 0, 208 },
                { 134, 275, 257, 149, 204, 135, 19, 189, 246, 208, 0 } };
        final int[] releaseDates = { 0, 2222, 6382, 22, 397, 11024, 9671, 2168,
                2529, 3896, 0 };
        final int[] dueDates = { 0, 10802, 11049, 8315, 9991, 11480, 11596,
                11426, 9495, 11407, 11914 };
        final int[][] servicePairs = { { 1, 5 }, { 2, 6 }, { 3, 7 }, { 4, 8 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 300, 300, 300,
                300, 0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew10() {
        final int[][] travelTime = { { 0, 402, 146, 299, 270, 63, 164, 0 },
                { 402, 0, 351, 697, 668, 449, 535, 402 },
                { 146, 351, 0, 412, 385, 145, 197, 146 },
                { 299, 697, 412, 0, 30, 270, 242, 299 },
                { 270, 668, 385, 30, 0, 242, 220, 270 },
                { 63, 449, 145, 270, 242, 0, 101, 63 },
                { 164, 535, 197, 242, 220, 101, 0, 164 },
                { 0, 402, 146, 299, 270, 63, 164, 0 } };
        final int[] releaseDates = { 0, 3924, 481, 1024, 7007, 10090, 10415, 0 };
        final int[] dueDates = { 0, 7332, 2090, 5639, 11042, 11249, 11148,
                11611 };
        final int[][] servicePairs = { { 1, 4 }, { 2, 5 }, { 3, 6 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 300, 0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew11() {
        final int[][] travelTime = { { 0, 167, 257, 296, 87, 124, 228, 0 },
                { 167, 0, 347, 397, 194, 271, 386, 167 },
                { 257, 347, 0, 512, 338, 327, 363, 257 },
                { 296, 397, 512, 0, 218, 188, 188, 296 },
                { 87, 194, 338, 218, 0, 85, 200, 87 },
                { 124, 271, 327, 188, 85, 0, 116, 124 },
                { 228, 386, 363, 188, 200, 116, 0, 228 },
                { 0, 167, 257, 296, 87, 124, 228, 0 } };
        final int[] releaseDates = { 0, 1837, 5044, 820, 4232, 7123, 4343, 0 };
        final int[] dueDates = { 0, 10681, 7007, 9788, 11174, 11137, 11033,
                11560 };
        final int[][] servicePairs = { { 1, 4 }, { 2, 5 }, { 3, 6 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 300, 0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew12() {
        final int[][] travelTime = {
                { 0, 167, 257, 296, 241, 87, 124, 228, 77, 0 },
                { 167, 0, 347, 397, 182, 194, 271, 386, 127, 167 },
                { 257, 347, 0, 512, 488, 338, 327, 363, 231, 257 },
                { 296, 397, 512, 0, 298, 218, 188, 188, 369, 296 },
                { 241, 182, 488, 298, 0, 191, 269, 375, 260, 241 },
                { 87, 194, 338, 218, 191, 0, 85, 200, 154, 87 },
                { 124, 271, 327, 188, 269, 85, 0, 116, 200, 124 },
                { 228, 386, 363, 188, 375, 200, 116, 0, 301, 228 },
                { 77, 127, 231, 369, 260, 154, 200, 301, 0, 77 },
                { 0, 167, 257, 296, 241, 87, 124, 228, 77, 0 } };
        final int[] releaseDates = { 0, 1747, 4954, 730, 2604, 4142, 7033,
                4253, 6330, 0 };
        final int[] dueDates = { 0, 10591, 6917, 9698, 10296, 11084, 11047,
                10943, 10715, 11470 };
        final int[][] servicePairs = { { 1, 5 }, { 2, 6 }, { 3, 7 }, { 4, 8 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 300, 300, 300,
                0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew13() {
        final int[][] travelTime = { { 0, 203, 276, 186, 373, 0 },
                { 203, 0, 402, 209, 171, 203 }, { 276, 402, 0, 206, 535, 276 },
                { 186, 209, 206, 0, 329, 186 }, { 373, 171, 535, 329, 0, 373 },
                { 0, 203, 276, 186, 373, 0 } };
        final int[] releaseDates = { 0, 5674, 3561, 7164, 10443, 0 };
        final int[] dueDates = { 0, 10263, 9750, 10771, 10584, 11256 };
        final int[][] servicePairs = { { 1, 3 }, { 2, 4 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew14() {
        final int[][] travelTime = { { 0, 238, 547, 236, 371, 515, 290 },
                { 238, 0, 353, 232, 220, 278, 60 },
                { 547, 353, 0, 580, 494, 227, 345 },
                { 236, 232, 580, 0, 184, 453, 237 },
                { 371, 220, 494, 184, 0, 314, 178 },
                { 515, 278, 227, 453, 314, 0, 234 },
                { 290, 60, 345, 237, 178, 234, 0 } };
        final int[] releaseDates = { 0, 2688, 5635, 0, 2996, 5923, 0 };
        final int[] dueDates = { 0, 5076, 10387, 9085, 9388, 10498, 11031 };
        final int[][] servicePairs = { { 1, 2 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew15() {
        final int[][] travelTime = { { 0, 260, 119, 191, 83, 0 },
                { 260, 0, 174, 70, 343, 260 }, { 119, 174, 0, 112, 194, 119 },
                { 191, 70, 112, 0, 273, 191 }, { 83, 343, 194, 273, 0, 83 },
                { 0, 260, 119, 191, 83, 0 } };
        final int[] releaseDates = { 0, 3372, 2110, 6825, 3248, 0 };
        final int[] dueDates = { 0, 7007, 8224, 10486, 10594, 10976 };
        final int[][] servicePairs = { { 1, 3 }, { 2, 4 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew16() {
        final int[][] travelTime = { { 0, 260, 119, 444, 191, 83, 195, 0 },
                { 260, 0, 174, 201, 70, 343, 107, 260 },
                { 119, 174, 0, 373, 112, 194, 83, 119 },
                { 444, 201, 373, 0, 263, 524, 305, 444 },
                { 191, 70, 112, 263, 0, 273, 72, 191 },
                { 83, 343, 194, 524, 273, 0, 274, 83 },
                { 195, 107, 83, 305, 72, 274, 0, 195 },
                { 0, 260, 119, 444, 191, 83, 195, 0 } };
        final int[] releaseDates = { 0, 3320, 2058, 1364, 6773, 3196, 4203, 0 };
        final int[] dueDates = { 0, 6955, 8172, 7941, 10434, 10542, 10430,
                10924 };
        final int[][] servicePairs = { { 1, 4 }, { 2, 5 }, { 3, 6 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 300, 0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew17() {
        final int[][] travelTime = {
                { 0, 167, 257, 296, 241, 204, 87, 124, 228, 77, 247, 0 },
                { 167, 0, 347, 397, 182, 116, 194, 271, 386, 127, 242, 167 },
                { 257, 347, 0, 512, 488, 437, 338, 327, 363, 231, 172, 257 },
                { 296, 397, 512, 0, 298, 331, 218, 188, 188, 369, 542, 296 },
                { 241, 182, 488, 298, 0, 67, 191, 269, 375, 260, 417, 241 },
                { 204, 116, 437, 331, 67, 0, 177, 261, 375, 206, 354, 204 },
                { 87, 194, 338, 218, 191, 177, 0, 85, 200, 154, 330, 87 },
                { 124, 271, 327, 188, 269, 261, 85, 0, 116, 200, 361, 124 },
                { 228, 386, 363, 188, 375, 375, 200, 116, 0, 301, 441, 228 },
                { 77, 127, 231, 369, 260, 206, 154, 200, 301, 0, 178, 77 },
                { 247, 242, 172, 542, 417, 354, 330, 361, 441, 178, 0, 247 },
                { 0, 167, 257, 296, 241, 204, 87, 124, 228, 77, 247, 0 } };
        final int[] releaseDates = { 0, 1090, 4297, 73, 1947, 1157, 3485, 6376,
                3596, 5673, 8779, 0 };
        final int[] dueDates = { 0, 9934, 6260, 9041, 9639, 7376, 10427, 10390,
                10286, 10058, 10267, 10813 };
        final int[][] servicePairs = { { 1, 6 }, { 2, 7 }, { 3, 8 }, { 4, 9 },
                { 5, 10 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 300, 300, 300,
                300, 300, 0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew18() {
        final int[][] travelTime = {
                { 0, 335, 379, 257, 336, 255, 167, 331, 24, 343, 237, 149 },
                { 335, 0, 469, 476, 291, 379, 268, 59, 358, 208, 463, 275 },
                { 379, 469, 0, 180, 193, 126, 248, 418, 386, 286, 193, 257 },
                { 257, 476, 180, 0, 283, 123, 209, 441, 255, 355, 22, 204 },
                { 336, 291, 193, 283, 0, 161, 170, 235, 354, 94, 282, 188 },
                { 255, 379, 126, 123, 161, 0, 129, 336, 263, 235, 122, 135 },
                { 167, 268, 248, 209, 170, 129, 0, 236, 185, 190, 195, 19 },
                { 331, 59, 418, 441, 235, 336, 236, 0, 354, 150, 429, 246 },
                { 24, 358, 386, 255, 354, 263, 185, 354, 0, 364, 234, 166 },
                { 343, 208, 286, 355, 94, 235, 190, 150, 364, 0, 350, 208 },
                { 237, 463, 193, 22, 282, 122, 195, 429, 234, 350, 0, 189 },
                { 149, 275, 257, 204, 188, 135, 19, 246, 166, 208, 189, 0 } };
        final int[] releaseDates = { 0, 1034, 5194, 0, 5020, 9836, 8483, 1341,
                7791, 2708, 980, 0 };
        final int[] dueDates = { 0, 9615, 9862, 8803, 8433, 10293, 10409, 8308,
                10262, 10220, 10239, 10727 };
        final int[][] servicePairs = { { 1, 5 }, { 2, 6 }, { 3, 7 }, { 4, 8 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 300, 300, 300,
                300, 300, 0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew19() {
        final int[][] travelTime = {
                { 0, 167, 257, 296, 241, 204, 104, 87, 124, 228, 77, 247, 156,
                        0 },
                { 167, 0, 347, 397, 182, 116, 97, 194, 271, 386, 127, 242, 322,
                        167 },
                { 257, 347, 0, 512, 488, 437, 252, 338, 327, 363, 231, 172,
                        275, 257 },
                { 296, 397, 512, 0, 298, 331, 387, 218, 188, 188, 369, 542,
                        248, 296 },
                { 241, 182, 488, 298, 0, 67, 248, 191, 269, 375, 260, 417, 352,
                        241 },
                { 204, 116, 437, 331, 67, 0, 189, 177, 261, 375, 206, 354, 336,
                        204 },
                { 104, 97, 252, 387, 248, 189, 0, 170, 226, 331, 35, 174, 254,
                        104 },
                { 87, 194, 338, 218, 191, 177, 170, 0, 85, 200, 154, 330, 162,
                        87 },
                { 124, 271, 327, 188, 269, 261, 226, 85, 0, 116, 200, 361, 89,
                        124 },
                { 228, 386, 363, 188, 375, 375, 331, 200, 116, 0, 301, 441, 92,
                        228 },
                { 77, 127, 231, 369, 260, 206, 35, 154, 200, 301, 0, 178, 221,
                        77 },
                { 247, 242, 172, 542, 417, 354, 174, 330, 361, 441, 178, 0,
                        350, 247 },
                { 156, 322, 275, 248, 352, 336, 254, 162, 89, 92, 221, 350, 0,
                        156 },
                { 0, 167, 257, 296, 241, 204, 104, 87, 124, 228, 77, 247, 156,
                        0 } };
        final int[] releaseDates = { 0, 901, 4108, 0, 1758, 968, 8728, 3296,
                6187, 3407, 5484, 8590, 9615, 0 };
        final int[] dueDates = { 0, 9745, 6071, 8852, 9450, 7187, 9616, 10238,
                10201, 10097, 9869, 10078, 10169, 10624 };
        final int[][] servicePairs = { { 1, 7 }, { 2, 8 }, { 3, 9 }, { 4, 10 },
                { 5, 11 }, { 6, 12 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 300, 300, 300,
                300, 300, 300, 300, 0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew20() {
        final int[][] travelTime = { { 0, 142, 223, 142, 310, 0 },
                { 142, 0, 88, 277, 173, 142 }, { 223, 88, 0, 362, 132, 223 },
                { 142, 277, 362, 0, 432, 142 }, { 310, 173, 132, 432, 0, 310 },
                { 0, 142, 223, 142, 310, 0 } };
        final int[] releaseDates = { 0, 3266, 1956, 5846, 6117, 0 };
        final int[] dueDates = { 0, 9534, 3561, 10110, 9942, 10551 };
        final int[][] servicePairs = { { 1, 3 }, { 2, 4 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew21() {
        final int[][] travelTime = { { 0, 366, 459, 370, 286 },
                { 366, 0, 96, 52, 145 }, { 459, 96, 0, 98, 212 },
                { 370, 52, 98, 0, 115 }, { 286, 145, 212, 115, 0 } };
        final int[] releaseDates = { 0, 801, 2481, 2148, 0 };
        final int[] dueDates = { 0, 2607, 9969, 10066, 10480 };
        final int[][] servicePairs = { { 1, 2 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew22() {
        final int[][] travelTime = {
                { 0, 351, 412, 330, 385, 197, 71, 145, 146 },
                { 351, 0, 697, 381, 668, 535, 348, 449, 402 },
                { 412, 697, 0, 400, 30, 242, 371, 270, 299 },
                { 330, 381, 400, 0, 375, 373, 263, 279, 220 },
                { 385, 668, 30, 375, 0, 220, 342, 242, 270 },
                { 197, 535, 242, 373, 220, 0, 188, 101, 164 },
                { 71, 348, 371, 263, 342, 188, 0, 105, 82 },
                { 145, 449, 270, 279, 242, 101, 105, 0, 63 },
                { 146, 402, 299, 220, 270, 164, 82, 63, 0 } };
        final int[] releaseDates = { 0, 2660, 0, 3969, 5743, 9151, 6749, 8826,
                0 };
        final int[] dueDates = { 0, 6068, 4375, 9404, 9778, 9884, 9966, 9985,
                10347 };
        final int[][] servicePairs = { { 1, 4 }, { 2, 5 }, { 3, 6 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 300, 300, 0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew23() {
        final int[][] travelTime = { { 0, 366, 450, 459, 444, 370, 286 },
                { 366, 0, 313, 96, 276, 52, 145 },
                { 450, 313, 0, 320, 42, 262, 214 },
                { 459, 96, 320, 0, 279, 98, 212 },
                { 444, 276, 42, 279, 0, 224, 190 },
                { 370, 52, 262, 98, 224, 0, 115 },
                { 286, 145, 214, 212, 190, 115, 0 } };
        final int[] releaseDates = { 0, 534, 1557, 2213, 8357, 1880, 0 };
        final int[] dueDates = { 0, 2339, 9274, 9702, 9724, 9799, 10213 };
        final int[][] servicePairs = { { 1, 3 }, { 2, 4 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew24() {
        final int[][] travelTime = { { 0, 142, 223, 206, 142, 310, 243, 0 },
                { 142, 0, 88, 136, 277, 173, 375, 142 },
                { 223, 88, 0, 188, 362, 132, 461, 223 },
                { 206, 136, 188, 0, 293, 167, 374, 206 },
                { 142, 277, 362, 293, 0, 432, 101, 142 },
                { 310, 173, 132, 167, 432, 0, 524, 310 },
                { 243, 375, 461, 374, 101, 524, 0, 243 },
                { 0, 142, 223, 206, 142, 310, 243, 0 } };
        final int[] releaseDates = { 0, 2771, 1461, 1227, 5351, 5622, 5475, 0 };
        final int[] dueDates = { 0, 9039, 3066, 5547, 9615, 9447, 9514, 10056 };
        final int[][] servicePairs = { { 1, 4 }, { 2, 5 }, { 3, 6 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 300, 0 };
        // 346ms
        // route: [0, 3, 2, 5, 1, 6, 4, 7]
        // arrivalTimes: [0, 6095, 2766, 2278, 7171, 5622, 6770, 10056]
        // objectiveValue: 1317
        // travel time : 1317
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew25() {
        final int[][] travelTime = {
                { 0, 335, 379, 257, 336, 281, 255, 167, 331, 24, 125, 343, 237,
                        149 },
                { 335, 0, 469, 476, 291, 186, 379, 268, 59, 358, 227, 208, 463,
                        275 },
                { 379, 469, 0, 180, 193, 284, 126, 248, 418, 386, 330, 286,
                        193, 257 },
                { 257, 476, 180, 0, 283, 319, 123, 209, 441, 255, 271, 355, 22,
                        204 },
                { 336, 291, 193, 283, 0, 110, 161, 170, 235, 354, 233, 94, 282,
                        188 },
                { 281, 186, 284, 319, 110, 0, 207, 134, 135, 302, 160, 64, 311,
                        151 },
                { 255, 379, 126, 123, 161, 207, 0, 129, 336, 263, 209, 235,
                        122, 135 },
                { 167, 268, 248, 209, 170, 134, 129, 0, 236, 185, 83, 190, 195,
                        19 },
                { 331, 59, 418, 441, 235, 135, 336, 236, 0, 354, 212, 150, 429,
                        246 },
                { 24, 358, 386, 255, 354, 302, 263, 185, 354, 0, 148, 364, 234,
                        166 },
                { 125, 227, 330, 271, 233, 160, 209, 83, 212, 148, 0, 223, 254,
                        75 },
                { 343, 208, 286, 355, 94, 64, 235, 190, 150, 364, 223, 0, 350,
                        208 },
                { 237, 463, 193, 22, 282, 311, 122, 195, 429, 234, 254, 350, 0,
                        189 },
                { 149, 275, 257, 204, 188, 151, 135, 19, 246, 166, 75, 208,
                        189, 0 } };
        final int[] releaseDates = { 0, 357, 4517, 0, 4343, 5020, 9159, 7806,
                664, 7114, 8004, 2031, 303, 0 };
        final int[] dueDates = { 0, 8938, 9185, 8126, 7756, 9217, 9616, 9732,
                7631, 9585, 9676, 9543, 9562, 10050 };
        final int[][] servicePairs = { { 1, 6 }, { 2, 7 }, { 3, 8 }, { 4, 9 },
                { 5, 10 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 300, 300, 300,
                300, 300, 300, 300, 0 };
        // 1752ms
        // route: [0, 12, 3, 2, 4, 11, 5, 8, 1, 9, 10, 7, 6, 13]
        // arrivalTimes: [0, 7241, 5196, 625, 5689, 6447, 9159, 8730, 6882,
        // 7899, 8347, 6083, 303, 10050]
        // objectiveValue: 1837
        // travel time : 1837
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew26() {
        final int[][] travelTime = { { 0, 116, 238, 178, 380, 167, 279, 63 },
                { 116, 0, 130, 156, 424, 219, 349, 153 },
                { 238, 130, 0, 165, 443, 276, 404, 254 },
                { 178, 156, 165, 0, 283, 119, 241, 150 },
                { 380, 424, 443, 283, 0, 214, 129, 318 },
                { 167, 219, 276, 119, 214, 0, 132, 108 },
                { 279, 349, 404, 241, 129, 132, 0, 216 },
                { 63, 153, 254, 150, 318, 108, 216, 0 } };
        final int[] releaseDates = { 0, 2508, 2493, 8310, 3259, 4166, 3422, 0 };
        final int[] dueDates = { 0, 9064, 8494, 9519, 9351, 9561, 9031, 9968 };
        final int[][] servicePairs = { { 1, 3 }, { 2, 4 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 300, 0 };
        // 410ms
        // route: [0, 1, 2, 5, 6, 4, 3, 7]
        // arrivalTimes: [0, 2508, 2938, 8310, 7727, 6866, 7298, 9968]
        // objectiveValue: 1216
        // travel time : 1216
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    // @Test
    public void testNew27() {

        final int[][] travelTime = {
                { 0, 167, 257, 296, 241, 204, 104, 170, 333, 87, 124, 228, 77,
                        247, 156, 246, 246, 0 },
                { 167, 0, 347, 397, 182, 116, 97, 68, 212, 194, 271, 386, 127,
                        242, 322, 153, 408, 167 },
                { 257, 347, 0, 512, 488, 437, 252, 390, 555, 338, 327, 363,
                        231, 172, 275, 480, 351, 257 },
                { 296, 397, 512, 0, 298, 331, 387, 347, 418, 218, 188, 188,
                        369, 542, 248, 342, 224, 296 },
                { 241, 182, 488, 298, 0, 67, 248, 116, 124, 191, 269, 375, 260,
                        417, 352, 49, 409, 241 },
                { 204, 116, 437, 331, 67, 0, 189, 51, 130, 177, 261, 375, 206,
                        354, 336, 44, 406, 204 },
                { 104, 97, 252, 387, 248, 189, 0, 141, 303, 170, 226, 331, 35,
                        174, 254, 232, 347, 104 },
                { 170, 68, 390, 347, 116, 51, 141, 0, 167, 164, 248, 364, 160,
                        303, 314, 92, 392, 170 },
                { 333, 212, 555, 418, 124, 130, 303, 167, 0, 302, 384, 494,
                        326, 449, 463, 91, 528, 333 },
                { 87, 194, 338, 218, 191, 177, 170, 164, 302, 0, 85, 200, 154,
                        330, 162, 212, 230, 87 },
                { 124, 271, 327, 188, 269, 261, 226, 248, 384, 85, 0, 116, 200,
                        361, 89, 294, 145, 124 },
                { 228, 386, 363, 188, 375, 375, 331, 364, 494, 200, 116, 0,
                        301, 441, 92, 405, 39, 228 },
                { 77, 127, 231, 369, 260, 206, 35, 160, 326, 154, 200, 301, 0,
                        178, 221, 250, 315, 77 },
                { 247, 242, 172, 542, 417, 354, 174, 303, 449, 330, 361, 441,
                        178, 0, 350, 394, 443, 247 },
                { 156, 322, 275, 248, 352, 336, 254, 314, 463, 162, 89, 92,
                        221, 350, 0, 373, 96, 156 },
                { 246, 153, 480, 342, 49, 44, 232, 92, 91, 212, 294, 405, 250,
                        394, 373, 0, 437, 246 },
                { 246, 408, 351, 224, 409, 406, 347, 392, 528, 230, 145, 39,
                        315, 443, 96, 437, 0, 246 },
                { 0, 167, 257, 296, 241, 204, 104, 170, 333, 87, 124, 228, 77,
                        247, 156, 246, 246, 0 } };
        final int[] releaseDates = { 0, 0, 2593, 0, 243, 0, 7213, 6426, 692,
                1781, 4672, 1892, 3969, 7075, 8100, 7199, 2593, 0 };
        final int[] dueDates = { 0, 8230, 4556, 7337, 7935, 5672, 8101, 8173,
                5142, 8723, 8686, 8582, 8354, 8563, 8654, 8564, 8564, 9109 };
        final int[][] servicePairs = { { 1, 9 }, { 2, 10 }, { 3, 11 },
                { 4, 12 }, { 5, 13 }, { 6, 14 }, { 7, 15 }, { 8, 16 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 300, 300, 300,
                300, 300, 300, 300, 300, 300, 300, 300, 0 };
        // 343768ms
        // route: [0, 1, 5, 8, 4, 3, 11, 16, 2, 12, 10, 9, 7, 15, 6, 13, 14, 17]
        // arrivalTimes: [0, 167, 4256, 2778, 2180, 583, 7731, 6426, 1013, 5962,
        // 5577, 3266, 4787, 8205, 8855, 7199, 3605, 9311]
        // objectiveValue: 3800
        // travel time : 3097
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew28() {
        final int[][] travelTime = {
                { 0, 201, 373, 489, 485, 556, 263, 524, 503, 578, 416, 305, 444 },
                { 201, 0, 174, 316, 288, 398, 70, 343, 317, 384, 238, 107, 260 },
                { 373, 174, 0, 185, 116, 281, 112, 194, 162, 240, 119, 83, 119 },
                { 489, 316, 185, 0, 132, 98, 247, 48, 59, 329, 78, 259, 66 },
                { 485, 288, 116, 132, 0, 222, 223, 113, 79, 199, 120, 197, 90 },
                { 556, 398, 281, 98, 222, 0, 332, 112, 143, 411, 168, 353, 163 },
                { 263, 70, 112, 247, 223, 332, 0, 273, 248, 341, 169, 72, 191 },
                { 524, 343, 194, 48, 113, 112, 273, 0, 35, 300, 109, 274, 83 },
                { 503, 317, 162, 59, 79, 143, 248, 35, 0, 271, 93, 243, 61 },
                { 578, 384, 240, 329, 199, 411, 341, 300, 271, 0, 312, 279, 286 },
                { 416, 238, 119, 78, 120, 168, 169, 109, 93, 312, 0, 186, 35 },
                { 305, 107, 83, 259, 197, 353, 72, 274, 243, 279, 186, 0, 195 },
                { 444, 260, 119, 66, 90, 163, 191, 83, 61, 286, 35, 195, 0 } };
        final int[] releaseDates = { 0, 1454, 192, 5248, 1449, 7363, 4907,
                1330, 7045, 3437, 7873, 2337, 0 };
        final int[] dueDates = { 0, 5089, 6306, 8340, 4421, 8257, 8568, 8676,
                8698, 7699, 8724, 8564, 9058 };
        final int[][] servicePairs = { { 1, 6 }, { 2, 7 }, { 3, 8 }, { 4, 9 },
                { 5, 10 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 300, 300, 300,
                300, 300, 300, 0 };
        // 484ms
        // route: [0, 1, 2, 4, 9, 11, 6, 3, 8, 7, 5, 10, 12]
        // arrivalTimes: [0, 1454, 3041, 6686, 3457, 7792, 4907, 7380, 7045,
        // 3956, 8260, 4535, 9058]
        // objectiveValue: 1697
        // travel time : 1697
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew29() {
        final int[][] travelTime = {
                { 0, 116, 238, 34, 178, 380, 162, 167, 279, 63 },
                { 116, 0, 130, 147, 156, 424, 225, 219, 349, 153 },
                { 238, 130, 0, 264, 165, 443, 290, 276, 404, 254 },
                { 34, 147, 264, 0, 185, 360, 143, 152, 253, 45 },
                { 178, 156, 165, 185, 0, 283, 137, 119, 241, 150 },
                { 380, 424, 443, 360, 283, 0, 219, 214, 129, 318 },
                { 162, 225, 290, 143, 137, 219, 0, 22, 125, 100 },
                { 167, 219, 276, 152, 119, 214, 22, 0, 132, 108 },
                { 279, 349, 404, 253, 241, 129, 125, 132, 0, 216 },
                { 63, 153, 254, 45, 150, 318, 100, 108, 216, 0 } };
        final int[] releaseDates = { 0, 1554, 1539, 1192, 7356, 2305, 3699,
                3212, 2468, 0 };
        final int[] dueDates = { 0, 8110, 7540, 6751, 8565, 8397, 8615, 8607,
                8077, 9014 };
        final int[][] servicePairs = { { 1, 4 }, { 2, 5 }, { 3, 6 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 300, 300, 300,
                0 };
        // 796ms
        // route: [0, 3, 1, 2, 7, 6, 8, 5, 4, 9]
        // arrivalTimes: [0, 2206, 2636, 1192, 8265, 7682, 6828, 3212, 7253,
        // 9014]
        // objectiveValue: 1296
        // travel time : 1296
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew30() {
        final int[][] travelTime = {
                { 0, 351, 412, 330, 239, 385, 197, 71, 396, 145, 146 },
                { 351, 0, 697, 381, 132, 668, 535, 348, 573, 449, 402 },
                { 412, 697, 0, 400, 566, 30, 242, 371, 223, 270, 299 },
                { 330, 381, 400, 0, 272, 375, 373, 263, 212, 279, 220 },
                { 239, 132, 566, 272, 0, 537, 410, 223, 450, 321, 272 },
                { 385, 668, 30, 375, 537, 0, 220, 342, 206, 242, 270 },
                { 197, 535, 242, 373, 410, 220, 0, 188, 323, 101, 164 },
                { 71, 348, 371, 263, 223, 342, 188, 0, 330, 105, 82 },
                { 396, 573, 223, 212, 450, 206, 323, 330, 0, 272, 250 },
                { 145, 449, 270, 279, 321, 242, 101, 105, 272, 0, 63 },
                { 146, 402, 299, 220, 272, 270, 164, 82, 250, 63, 0 } };
        final int[] releaseDates = { 0, 1104, 0, 2413, 3664, 4187, 7595, 5193,
                4500, 7270, 0 };
        final int[] dueDates = { 0, 4512, 2819, 7848, 5504, 8222, 8328, 8410,
                7694, 8429, 8791 };
        final int[][] servicePairs = { { 1, 5 }, { 2, 6 }, { 3, 7 }, { 4, 8 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 300, 300, 300,
                300, 0 };
        // 357ms
        // route: [0, 2, 3, 1, 4, 7, 8, 5, 6, 9, 10]
        // arrivalTimes: [0, 4212, 1713, 2413, 4670, 7087, 7607, 5193, 5823,
        // 8129, 8791]
        // objectiveValue: 2468
        // travel time : 2468
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew31() {
        final int[][] travelTime = {
                { 0, 203, 276, 184, 229, 186, 373, 151, 127, 0 },
                { 203, 0, 402, 201, 430, 209, 171, 241, 245, 203 },
                { 276, 402, 0, 460, 268, 206, 535, 422, 394, 276 },
                { 184, 201, 460, 0, 360, 332, 340, 74, 100, 184 },
                { 229, 430, 268, 360, 0, 344, 600, 292, 262, 229 },
                { 186, 209, 206, 332, 344, 0, 329, 325, 307, 186 },
                { 373, 171, 535, 340, 600, 329, 0, 397, 406, 373 },
                { 151, 241, 422, 74, 292, 325, 397, 0, 31, 151 },
                { 127, 245, 394, 100, 262, 307, 406, 31, 0, 127 },
                { 0, 203, 276, 184, 229, 186, 373, 151, 127, 0 } };
        final int[] releaseDates = { 0, 2799, 686, 3306, 3696, 4289, 7568,
                4151, 6872, 0 };
        final int[] dueDates = { 0, 7388, 6875, 5286, 4668, 7896, 7709, 7261,
                7955, 8381 };
        final int[][] servicePairs = { { 1, 5 }, { 2, 6 }, { 3, 7 }, { 4, 8 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 300, 300, 300,
                0 };
        // 445ms
        // route: [0, 1, 3, 4, 2, 5, 7, 8, 6, 9]
        // arrivalTimes: [0, 2805, 4534, 3306, 3966, 5040, 7578, 6541, 6872,
        // 8381]
        // objectiveValue: 2542
        // travel time : 2373
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew32() {
        final int[][] travelTime = {
                { 0, 356, 442, 351, 309, 44, 86, 502, 24, 246, 62, 438, 227 },
                { 356, 0, 88, 136, 132, 356, 277, 173, 375, 338, 295, 119, 142 },
                { 442, 88, 0, 188, 200, 444, 362, 132, 461, 414, 381, 109, 223 },
                { 351, 136, 188, 0, 42, 335, 293, 167, 374, 241, 294, 111, 206 },
                { 309, 132, 200, 42, 0, 294, 252, 203, 332, 216, 252, 142, 172 },
                { 44, 356, 444, 335, 294, 0, 108, 492, 62, 206, 72, 429, 238 },
                { 86, 277, 362, 293, 252, 108, 0, 432, 101, 254, 39, 369, 142 },
                { 502, 173, 132, 167, 203, 492, 432, 0, 524, 404, 442, 64, 310 },
                { 24, 375, 461, 374, 332, 62, 101, 524, 0, 266, 82, 460, 243 },
                { 246, 338, 414, 241, 216, 206, 254, 404, 266, 0, 224, 351, 295 },
                { 62, 295, 381, 294, 252, 72, 39, 442, 82, 224, 0, 378, 168 },
                { 438, 119, 109, 111, 142, 429, 369, 64, 460, 351, 378, 0, 250 },
                { 227, 142, 223, 206, 172, 238, 142, 310, 243, 295, 168, 250, 0 } };
        final int[] releaseDates = { 0, 1025, 0, 0, 5924, 814, 3605, 3876,
                3729, 6480, 3290, 1674, 0 };
        final int[] dueDates = { 0, 7292, 1320, 3801, 7200, 5620, 7868, 7700,
                7767, 7715, 6371, 6799, 8309 };
        final int[][] servicePairs = { { 1, 6 }, { 2, 7 }, { 3, 8 }, { 4, 9 },
                { 5, 10 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 300, 300, 300,
                300, 300, 300, 0 };
        // 522ms
        // route: [0, 3, 2, 11, 7, 1, 5, 8, 10, 6, 4, 9, 12]
        // arrivalTimes: [0, 4349, 1020, 351, 6640, 5005, 6088, 3876, 5367,
        // 7415, 5749, 1674, 8309]
        // objectiveValue: 2187
        // travel time : 2187
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);

    }

    @Test
    public void testNew33() {
        final int[][] travelTime = {
                { 0, 203, 276, 184, 229, 93, 186, 373, 151, 127, 289, 0 },
                { 203, 0, 402, 201, 430, 256, 209, 171, 241, 245, 218, 203 },
                { 276, 402, 0, 460, 268, 184, 206, 535, 422, 394, 558, 276 },
                { 184, 201, 460, 0, 360, 277, 332, 340, 74, 100, 119, 184 },
                { 229, 430, 268, 360, 0, 201, 344, 600, 292, 262, 478, 229 },
                { 93, 256, 184, 277, 201, 0, 146, 417, 239, 213, 379, 93 },
                { 186, 209, 206, 332, 344, 146, 0, 329, 325, 307, 402, 186 },
                { 373, 171, 535, 340, 600, 417, 329, 0, 397, 406, 298, 373 },
                { 151, 241, 422, 74, 292, 239, 325, 397, 0, 31, 190, 151 },
                { 127, 245, 394, 100, 262, 213, 307, 406, 31, 0, 217, 127 },
                { 289, 218, 558, 119, 478, 379, 402, 298, 190, 217, 0, 289 },
                { 0, 203, 276, 184, 229, 93, 186, 373, 151, 127, 289, 0 } };
        final int[] releaseDates = { 0, 2721, 608, 3228, 3618, 1163, 4211,
                7490, 4073, 6794, 4347, 0 };
        final int[] dueDates = { 0, 7310, 6797, 5208, 4590, 7037, 7818, 7631,
                7183, 7877, 7715, 8303 };
        final int[][] servicePairs = { { 1, 6 }, { 2, 7 }, { 3, 8 }, { 4, 9 },
                { 5, 10 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 300, 300, 300,
                300, 300, 0 };
        // 567ms
        // route: [0, 5, 2, 4, 3, 10, 1, 6, 8, 9, 7, 11]
        // arrivalTimes: [0, 5329, 3050, 4392, 3618, 1163, 5838, 7500, 6463,
        // 6794, 4811, 8303]
        // objectiveValue: 2755
        // travel time : 2586
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    // @Test
    public void testNew34() {
        final int[][] travelTime = {
                { 0, 437, 331, 67, 189, 51, 130, 42, 261, 375, 206, 336, 44,
                        406, 205, 177, 354, 204 },
                { 437, 0, 512, 488, 252, 390, 555, 456, 327, 363, 231, 275,
                        480, 351, 637, 338, 172, 257 },
                { 331, 512, 0, 298, 387, 347, 418, 371, 188, 188, 369, 248,
                        342, 224, 448, 218, 542, 296 },
                { 67, 488, 298, 0, 248, 116, 124, 89, 269, 375, 260, 352, 49,
                        409, 177, 191, 417, 241 },
                { 189, 252, 387, 248, 0, 141, 303, 205, 226, 331, 35, 254, 232,
                        347, 386, 170, 174, 104 },
                { 51, 390, 347, 116, 141, 0, 167, 68, 248, 364, 160, 314, 92,
                        392, 248, 164, 303, 170 },
                { 130, 555, 418, 124, 303, 167, 0, 100, 384, 494, 326, 463, 91,
                        528, 85, 302, 449, 333 },
                { 42, 456, 371, 89, 205, 68, 100, 0, 301, 416, 227, 374, 44,
                        446, 181, 217, 359, 236 },
                { 261, 327, 188, 269, 226, 248, 384, 301, 0, 116, 200, 89, 294,
                        145, 445, 85, 361, 124 },
                { 375, 363, 188, 375, 331, 364, 494, 416, 116, 0, 301, 92, 405,
                        39, 549, 200, 441, 228 },
                { 206, 231, 369, 260, 35, 160, 326, 227, 200, 301, 0, 221, 250,
                        315, 407, 154, 178, 77 },
                { 336, 275, 248, 352, 254, 314, 463, 374, 89, 92, 221, 0, 373,
                        96, 528, 162, 350, 156 },
                { 44, 480, 342, 49, 232, 92, 91, 44, 294, 405, 250, 373, 0,
                        437, 162, 212, 394, 246 },
                { 406, 351, 224, 409, 347, 392, 528, 446, 145, 39, 315, 96,
                        437, 0, 585, 230, 443, 246 },
                { 205, 637, 448, 177, 386, 248, 85, 181, 445, 549, 407, 528,
                        162, 585, 0, 367, 533, 408 },
                { 177, 338, 218, 191, 170, 164, 302, 217, 85, 200, 154, 162,
                        212, 230, 367, 0, 330, 87 },
                { 354, 172, 542, 417, 174, 303, 449, 359, 361, 441, 178, 350,
                        394, 443, 533, 330, 0, 247 },
                { 204, 257, 296, 241, 104, 170, 333, 236, 124, 228, 77, 156,
                        246, 246, 408, 87, 247, 0 } };
        final int[] releaseDates = { 0, 1710, 0, 0, 6331, 5543, 0, 921, 3789,
                1010, 3086, 7218, 6317, 1710, 2937, 899, 6193, 0 };
        final int[] dueDates = { 0, 3673, 6454, 7053, 7218, 7290, 4259, 2561,
                7803, 7699, 7472, 7771, 7681, 7681, 7145, 7840, 7680, 8226 };
        final int[][] servicePairs = { { 1, 8 }, { 2, 9 }, { 3, 10 },
                { 4, 11 }, { 5, 12 }, { 6, 13 }, { 7, 14 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 300, 300, 300,
                300, 300, 300, 300, 300, 300, 300, 300, 0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew35() {
        final int[][] travelTime = { { 0, 9, 163, 223, 154 },
                { 9, 0, 156, 219, 153 }, { 163, 156, 0, 119, 150 },
                { 223, 219, 119, 0, 108 }, { 154, 153, 150, 108, 0 } };
        final int[] releaseDates = { 0, 4749, 10551, 6407, 0 };
        final int[] dueDates = { 0, 11304, 11759, 11801, 12208 };
        final int[][] servicePairs = { { 1, 2 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 0 };
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    @Test
    public void testNew36() {
        final int[][] travelTime = {
                { 0, 116, 238, 34, 245, 178, 380, 162, 189, 167, 279, 63 },
                { 116, 0, 130, 147, 349, 156, 424, 225, 211, 219, 349, 153 },
                { 238, 130, 0, 264, 477, 165, 443, 290, 242, 276, 404, 254 },
                { 34, 147, 264, 0, 228, 185, 360, 143, 182, 152, 253, 45 },
                { 245, 349, 477, 228, 0, 411, 510, 327, 390, 345, 382, 261 },
                { 178, 156, 165, 185, 411, 0, 283, 137, 78, 119, 241, 150 },
                { 380, 424, 443, 360, 510, 283, 0, 219, 214, 214, 129, 318 },
                { 162, 225, 290, 143, 327, 137, 219, 0, 75, 22, 125, 100 },
                { 189, 211, 242, 182, 390, 78, 214, 75, 0, 54, 164, 138 },
                { 167, 219, 276, 152, 345, 119, 214, 22, 54, 0, 132, 108 },
                { 279, 349, 404, 253, 382, 241, 129, 125, 164, 132, 0, 216 },
                { 63, 153, 254, 45, 261, 150, 318, 100, 138, 108, 216, 0 } };
        final int[] releaseDates = { 0, 1407, 1392, 1045, 6008, 7209, 2158,
                3552, 7528, 3065, 2321, 0 };
        final int[] dueDates = { 0, 7963, 7393, 6604, 7741, 8418, 8250, 8468,
                8430, 8460, 7930, 8867 };
        final int[][] servicePairs = { { 1, 5 }, { 2, 6 }, { 3, 7 }, { 4, 8 } };
        final int[] serviceTimes = { 0, 300, 300, 300, 300, 300, 300, 300, 300,
                300, 300, 0 };
        // 1340ms
        // route: [0, 3, 1, 2, 9, 7, 4, 10, 6, 8, 5, 11]
        // arrivalTimes: [0, 4053, 4483, 3606, 6008, 8118, 7119, 5381, 7740,
        // 5059, 6690, 8867]
        // objectiveValue: 1889
        // travel time : 1889
        solver.solve(travelTime, releaseDates, dueDates, servicePairs, serviceTimes);
    }

    public static int[] createServiceTimes(int val, int length) {
        checkArgument(length > 2);
        final int[] serviceTimes = new int[length];
        Arrays.fill(serviceTimes, val);
        serviceTimes[0] = 0;
        serviceTimes[length - 1] = 0;
        return serviceTimes;
    }

}
