/**
 * 
 */
package reflexactoring.diagram.action.smelldetection.refactoringopportunities;

import java.util.ArrayList;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.operations.IWorkbenchOperationSupport;

import reflexactoring.diagram.action.smelldetection.AdvanceEvaluatorAdapter;
import reflexactoring.diagram.bean.ICompilationUnitWrapper;
import reflexactoring.diagram.bean.ModuleWrapper;
import reflexactoring.diagram.bean.ProgramModel;



/**
 * @author linyun
 *
 */
public abstract class RefactoringOpportunity {
	
	/**
	 * this API (getModuleList) is very time consuming.
	 */
	protected ArrayList<ModuleWrapper> moduleList;
	
	/**
	 * Given a program model, this method is used to simulate the effect of applying
	 * the specific refactoring.
	 * @param model
	 * @return
	 */
	public abstract ProgramModel simulate(ProgramModel model);
	
	public abstract String getRefactoringName();
	
	public String getRefactoringDescription(){
		return toString();
	};
	
	public abstract ArrayList<String> getRefactoringDetails();
	
	public abstract ArrayList<ASTNode> getHints();
	
	public abstract double computeSimilarityWith(RefactoringOpportunity opp);
	/**
	 * this method is used to apply refactoring on real code
	 */
	public abstract void apply();
	
	/**
	 * this method is used to undo apply refactoring on real code, simulate user press ctrl+Z in default
	 */
	public void undoApply(){
		try {
			IWorkbenchOperationSupport operationSupport = PlatformUI.getWorkbench().getOperationSupport();
			IUndoContext context = operationSupport.getUndoContext();
			IOperationHistory operationHistory = operationSupport.getOperationHistory();  
			IStatus status = operationHistory.undo(context, null, null);
		} catch (ExecutionException ee) {
			ee.printStackTrace();
		}
	}
	
	
	/**
	 * this method is used to check whether a refactoring opportunity still hold true, on the process of checking
	 * validity, some opportunity might be updated and then get validated.
	 * @param model
	 * @return
	 */
	protected abstract boolean checkLegal(ProgramModel model);
	/**
	 * @return the moduleList
	 */
	public ArrayList<ModuleWrapper> getModuleList() {
		return moduleList;
	}
	/**
	 * @param moduleList the moduleList to set
	 */
	public void setModuleList(ArrayList<ModuleWrapper> moduleList) {
		this.moduleList = moduleList;
	}
	
	/**
	 * @param newUnit
	 * @return
	 */
	protected ModuleWrapper calculateBestMappingModule(ProgramModel model,
			ICompilationUnitWrapper newUnit) {
		
		ModuleWrapper module = null;
		double fitness = 0;
		
		for(ModuleWrapper m: moduleList){
			newUnit.setMappingModule(m);
			double f = new AdvanceEvaluatorAdapter().computeFitness(model, moduleList);
			if(module == null){
				module = m;
				fitness = f;
			}
			else{
				if(f > fitness){
					module = m;
					fitness = f;
				}
			}
		}
		
		return module;
	}
}
