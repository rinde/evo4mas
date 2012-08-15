/**
 * 
 */
package rinde.evo4mas.evo.gp;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class GPComputationJobImpl<C> implements GPComputationJob<C> {

	private static final long serialVersionUID = 3586719916116534106L;
	protected final String computerClassName;
	protected final GPProgram<C> program;

	public GPComputationJobImpl(String className, GPProgram<C> prog) {
		computerClassName = className;
		program = prog;
	}

	public String getComputerClassName() {
		return computerClassName;
	}

	public GPProgram<C> getProgram() {
		return program;
	}

}
