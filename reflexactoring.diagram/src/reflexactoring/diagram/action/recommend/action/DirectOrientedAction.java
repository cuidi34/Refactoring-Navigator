/**
 * 
 */
package reflexactoring.diagram.action.recommend.action;

import reflexactoring.diagram.bean.ModuleWrapper;

/**
 * @author linyun
 *
 */
public abstract class DirectOrientedAction extends RefactoringAction {
	private ModuleWrapper origin;
	private ModuleWrapper destination;
	
	/**
	 * @return the origin
	 */
	public ModuleWrapper getOrigin() {
		return origin;
	}

	/**
	 * @param origin the origin to set
	 */
	public void setOrigin(ModuleWrapper origin) {
		this.origin = origin;
	}

	/**
	 * @return the destination
	 */
	public ModuleWrapper getDestination() {
		return destination;
	}

	/**
	 * @param destination the destination to set
	 */
	public void setDestination(ModuleWrapper destination) {
		this.destination = destination;
	}
	
	/* (non-Javadoc)
	 * @see reflexactoring.diagram.action.recommend.RefactoringAction#getDetailedDescription()
	 */
	@Override
	public String getDetailedDescription() {
		
		String from = (origin == null)? "canvas" : origin.getNameWithTag(); 
		
		String desc = "from " + from + " to " + destination.getNameWithTag();
		return desc;
	}
}
