import java.util.Arrays;

/**
 * 
 */

/**
 * Solution object for single pickup-and-delivery problem with time windows.
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 */
public class SolutionObject {

	/**
	 * Array of locations which have to be serviced in the specified sequence,
	 * starts with the begin location and ends with depot.
	 */
	public final int[] serviceSequence;

	/**
	 * Array of times at which every location servicing starts. The number at
	 * arrivalTimes[i] indicates the arrival time at location i, where i refers
	 * to the location as specified in the <i>input</i>. Always starts with 0.
	 */
	public final int[] arrivalTimes;

	/**
	 * The objective value for this solution as computed by the solver.
	 */
	public final int objectiveValue;

	/**
	 * 
	 * @param serviceSequence {@link #serviceSequence}
	 * @param arrivalTimes {@link #arrivalTimes}
	 * @param objectiveValue {@link #objectiveValue}
	 */
	public SolutionObject(int[] serviceSequence, int[] arrivalTimes, int objectiveValue) {
		this.serviceSequence = serviceSequence;
		this.arrivalTimes = arrivalTimes;
		this.objectiveValue = objectiveValue;
	}

	@Override
	public String toString() {
		String s = "Route: ";
		s += Arrays.toString(serviceSequence) + "\n";
		s += "Arrival times: " + Arrays.toString(arrivalTimes) + "\n";
		s += "Objective: " + objectiveValue;
		return s;
	}

}
