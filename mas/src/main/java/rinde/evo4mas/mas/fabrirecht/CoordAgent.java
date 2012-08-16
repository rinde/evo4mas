/**
 * 
 */
package rinde.evo4mas.mas.fabrirecht;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public interface CoordAgent {

	void setCoordModel(CoordModel model);

	/**
	 * 
	 */
	void notifyServiceChange();

}
