/**
 * 
 */
package reflexactoring.diagram.action.smelldetection.refactoringopportunities;

import java.util.ArrayList;

import reflexactoring.diagram.action.smelldetection.NameGernationCounter;
import reflexactoring.diagram.action.smelldetection.bean.CloneInstance;
import reflexactoring.diagram.action.smelldetection.bean.CloneSet;
import reflexactoring.diagram.bean.ICompilationUnitWrapper;
import reflexactoring.diagram.bean.MethodWrapper;
import reflexactoring.diagram.bean.ModuleWrapper;
import reflexactoring.diagram.bean.ProgramModel;
import reflexactoring.diagram.bean.ProgramReference;
import reflexactoring.diagram.bean.UnitMemberWrapper;

/**
 * @author linyun
 *
 */
public class ExtractUtilityClassOpportunity extends RefactoringOpportunity{

	private CloneSet cloneSet;
	
	public ExtractUtilityClassOpportunity(CloneSet cloneSet, ArrayList<ModuleWrapper> moduleList){
		this.cloneSet = cloneSet;
		this.moduleList = moduleList;
	}
	
	@Override
	public ProgramModel simulate(ProgramModel model) {
		ProgramModel newModel = model.clone();
		/**
		 * create a new utility class
		 */
		String className = "UtilityClass" + NameGernationCounter.retrieveNumber();
		String packageName = cloneSet.getInstances().get(0).getMember().getUnitWrapper().getPackageName();
		ICompilationUnitWrapper utilityClass = new ICompilationUnitWrapper(null, false, className, packageName);
		newModel.getScopeCompilationUnitList().add(utilityClass);
		/**
		 * create a new utility method
		 */
		String methodName = "utilityMethod" + NameGernationCounter.retrieveNumber();
		MethodWrapper utilityMethod = new MethodWrapper(methodName, new ArrayList<String>(), false, utilityClass);
		newModel.getScopeMemberList().add(utilityMethod);
		/**
		 * build declaring relation between utility class and utility method
		 */
		utilityClass.addMember(utilityMethod);
		
		/**
		 * change references
		 */
		for(CloneInstance instance: this.cloneSet.getInstances()){
			UnitMemberWrapper member = newModel.findMember(instance.getMember());
			for(ProgramReference reference: instance.getCoveringReferenceList()){
				/**
				 * member's referee
				 */
				if(reference.getReferer().equals(member)){
					member.getRefereePointList().remove(reference);
					reference.setReferer(utilityMethod);
					utilityMethod.addProgramReferee(reference);
				}
				/**
				 * member's referer
				 */
				else{
					member.getRefererPointList().remove(reference);
					reference.setReferee(utilityMethod);
					utilityMethod.addProgramReferer(reference);
				}
				
				ProgramReference newReference = new ProgramReference(member, utilityMethod, member.getJavaElement());
				newModel.getReferenceList().add(newReference);
				member.addProgramReferee(newReference);
				utilityMethod.addProgramReferer(newReference);
			}
		}
		
		/**
		 * update new model
		 */
		newModel.updateUnitCallingRelationByMemberRelations();
		
		/**
		 * may calculate which module is proper to hold the newly created super class
		 */
		ModuleWrapper bestMappingModule = calculateBestMappingModule(newModel, utilityClass);
		utilityClass.setMappingModule(bestMappingModule);
		
		return newModel;
	}

	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
	}
	
}
