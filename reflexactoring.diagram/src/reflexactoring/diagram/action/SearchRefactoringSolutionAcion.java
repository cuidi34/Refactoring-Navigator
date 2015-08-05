package reflexactoring.diagram.action;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import reflexactoring.diagram.action.recommend.RefactoringRecommender;
import reflexactoring.diagram.action.recommend.SuggestionMove;
import reflexactoring.diagram.action.recommend.action.MoveMemberAction;
import reflexactoring.diagram.action.recommend.suboptimal.Violation;
import reflexactoring.diagram.action.smelldetection.AdvanceEvaluatorAdapter;
import reflexactoring.diagram.action.smelldetection.BadSmellDetector;
import reflexactoring.diagram.action.smelldetection.PenaltyAndRewardCalulator;
import reflexactoring.diagram.action.smelldetection.bean.RefactoringSequence;
import reflexactoring.diagram.action.smelldetection.bean.RefactoringSequenceElement;
import reflexactoring.diagram.action.smelldetection.refactoringopportunities.ExtractClassOpportunity;
import reflexactoring.diagram.action.smelldetection.refactoringopportunities.MoveMethodOpportunity;
import reflexactoring.diagram.action.smelldetection.refactoringopportunities.RefactoringOpportunity;
import reflexactoring.diagram.bean.ModuleWrapper;
import reflexactoring.diagram.bean.programmodel.ProgramModel;
import reflexactoring.diagram.perspective.ReflexactoringPerspective;
import reflexactoring.diagram.util.RecordParameters;
import reflexactoring.diagram.util.ReflexactoringUtil;
import reflexactoring.diagram.util.Settings;
import reflexactoring.diagram.view.RefactoringSuggestionsView;

public class SearchRefactoringSolutionAcion implements
		IWorkbenchWindowActionDelegate {
	
	@Override
	public void run(IAction action) {
		RecordParameters.proceedTime++;
		
		Job job = new Job("Searching for refactoring solutions"){
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				int totalWork = Double.valueOf(ReflexactoringUtil.getClimbIterationNumber()).intValue();
				monitor.beginTask("Searching Solution...", totalWork);
				
				final ArrayList<RefactoringSequence> suggestionList = new ArrayList<>();
				ArrayList<ModuleWrapper> moduleList = ReflexactoringUtil.getModuleList(Settings.diagramPath);
				ProgramModel model = Settings.scope;
				BadSmellDetector smellDetector = new BadSmellDetector(moduleList);
				
				RefactoringSequence sequence = new RefactoringSequence(model, moduleList);
				ArrayList<RefactoringOpportunity> oppList = smellDetector.detect(model);
				
				System.currentTimeMillis();
				
				for(int i=0; i<Double.valueOf(ReflexactoringUtil.getClimbIterationNumber()) && oppList.size() != 0; i++){				
					
					monitor.worked(1);
					
					if(i==5){
						System.currentTimeMillis();
					}
					
					long t1 = System.currentTimeMillis();
					RefactoringSequenceElement element = findBestOpportunity(oppList, model, moduleList);
					long t2 = System.currentTimeMillis();
					System.out.println(t2-t1);
					
					PenaltyAndRewardCalulator calculator = new PenaltyAndRewardCalulator();
					if(sequence.isAnImprovement(element) ||
							(calculator.isConformToUserFeedback(element.getOpportunity()) &&
							 !sequence.contains(element))
							){
						
						System.out.println("iteration " + i + ":");
						System.out.println(element.getFitnessValue());
						System.out.println(element.getViolationList());
						System.out.println("====================");
						
						element.setPosition(i);
						sequence.addElement(element);
						model = element.getConsequenceModel();
						oppList = smellDetector.detect(model);
					}
					else{
						System.out.println("break in iteration " + i);
						break;
					}
				}
				
				if(sequence.size() > 0){
					RefactoringSequenceElement element = sequence.get(sequence.size()-1); 
					System.out.println("The final fitness value is: " + element.getFitnessValue());					
				}
				
				/**
				 * prepare prerequisite
				 */
				if(sequence.size() != 0){
					ArrayList<Violation> violations = sequence.get(sequence.size()-1).getViolationList();
					RefactoringRecommender recommender = new RefactoringRecommender();
					ArrayList<SuggestionMove> prerequisite = recommender.findHighLevelModificationSuggestion(violations, moduleList);
					
					sequence.setPrerequisite(prerequisite);
				}
				suggestionList.add(sequence);
				
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						RefactoringSuggestionsView view = (RefactoringSuggestionsView)PlatformUI.getWorkbench().
								getActiveWorkbenchWindow().getActivePage().findView(ReflexactoringPerspective.REFACTORING_SUGGESTIONS);
						view.refreshSuggestionsOnUI(suggestionList);
						
					}
				});
				
				return Status.OK_STATUS;
			}
			
		};
		job.schedule();
	}

	/**
	 * @param oppList
	 * @param model
	 * @return
	 */
	private RefactoringSequenceElement findBestOpportunity(ArrayList<RefactoringOpportunity> oppList, ProgramModel model, ArrayList<ModuleWrapper> moduleList) {
		AdvanceEvaluatorAdapter evaluator = new AdvanceEvaluatorAdapter();
		Double bestfitnessValue = null;
		Double bestFeedbackValue = null;
		RefactoringOpportunity bestOpp = null;
		ProgramModel consequenceModel = null;
		ArrayList<Violation> violationList = null;
		
		//ArrayList<RefactoringSequenceElement> candidateList = new ArrayList<>();
		
		for(RefactoringOpportunity opp: oppList){
			if(Settings.forbiddenOpps.contains(opp)){
				continue;
			}
			
			if(opp instanceof ExtractClassOpportunity){
				System.currentTimeMillis();
			}
			
			if(opp instanceof MoveMethodOpportunity){
				MoveMethodOpportunity opp0 = (MoveMethodOpportunity)opp;
				if(opp0.getObjectMethod().getName().contains("moveBack") && opp0.getTargetUnit().getName().contains("Modifier")){
					System.currentTimeMillis();
				}
				
				if(opp0.getObjectMethod().getName().contains("addSuppress") && opp0.getTargetUnit().getName().contains("Extracted")){
					System.currentTimeMillis();
				}
			}
			
			//long t1 = System.currentTimeMillis();
			
			ProgramModel testModel = opp.simulate(model);
			
			//long t2 = System.currentTimeMillis();
			//System.out.println("Simluated Model Time: " + (t2-t1));
			/*if(t2-t1 > 20){
				System.currentTimeMillis(); 
				opp.simulate(model);
			}*/
			
			double fitnessValue = evaluator.computeFitness(testModel, moduleList);
			
			//long t3 = System.currentTimeMillis();
			//System.out.println("Fitness Time: " + (t3-t2));
			/**
			 * Merging user's feedback.
			 */
			double feedbackValue = new PenaltyAndRewardCalulator().calculate(fitnessValue, opp);
			//double feedbackValue = fitnessValue;
			
			//long t4 = System.currentTimeMillis();
			//System.out.println("Penalty Caluation Used Time: " + (t4-t3));
			/*if(t4-t3 > 20){
				System.currentTimeMillis(); 
				new PenaltyAndRewardCalulator().calculate(value, opp);
			}*/
			/*RefactoringSequenceElement element = new RefactoringSequenceElement(opp, testModel, value, evaluator.getViolationList());
			candidateList.add(element);*/
			
			if(bestFeedbackValue == null){
				bestFeedbackValue = feedbackValue;
				bestfitnessValue = fitnessValue;
				bestOpp = opp;
				consequenceModel = testModel;
				violationList = evaluator.getViolationList();
				
			}
			else{
				if(feedbackValue > bestFeedbackValue){
					bestFeedbackValue = feedbackValue;
					bestfitnessValue = fitnessValue;
					bestOpp = opp;
					consequenceModel = testModel;
					violationList = evaluator.getViolationList();
				}
			}
		}
		
		return new RefactoringSequenceElement(bestOpp, consequenceModel, bestfitnessValue, violationList);
	}
	
	/*private RefactoringSequenceElement breakTie(ArrayList<RefactoringSequenceElement> candidateList, double bestValue){
		ArrayList<RefactoringSequenceElement> bestCandidateElements = new ArrayList<>();
		for(RefactoringSequenceElement element: candidateList){
			if(element.getFitnessValue() == bestValue){
				bestCandidateElements.add(element);
			}
		}
		
		double bestNameSim = 0;
		RefactoringSequenceElement bestElement = null;
		
		for(RefactoringSequenceElement element: bestCandidateElements){
			element.getOpportunity().
		}
	}*/

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		
	}

	@Override
	public void dispose() {
		
	}

	@Override
	public void init(IWorkbenchWindow window) {

	}

}
