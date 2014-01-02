package reflexactoring.diagram.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.emf.core.GMFEditingDomainFactory;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;

import reflexactoring.Module;
import reflexactoring.Reflexactoring;
import reflexactoring.diagram.action.semantic.TFIDF;
import reflexactoring.diagram.bean.ICompilationUnitWrapper;
import reflexactoring.diagram.bean.ModuleWrapper;
import reflexactoring.diagram.util.ReflexactoringUtil;
import reflexactoring.diagram.util.Settings;

public class AutoMappingAction implements IWorkbenchWindowActionDelegate {

	private String diagramName = "/refactoring/default.reflexactoring";
	
	@Override
	public void run(IAction action) {
		try {
			ArrayList<ModuleWrapper> moduleList = getModuleList();
			ArrayList<ICompilationUnitWrapper> compilationUnitWrapperList = new ArrayList<>();
			for(ICompilationUnit unit: Settings.scope.getScopeCompilationUnitList()){
				compilationUnitWrapperList.add(new ICompilationUnitWrapper(unit));
			}
			
			generateMappingRelation(moduleList, compilationUnitWrapperList);
			
			//System.out.println();
			
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * The result should be that: for each module in moduleList, the module wrapper class
	 * will have a list of mapping compilationUnit; in contrast, for each compilationUnit
	 * in compilationUnit, the compilation unit wrapper will have a corresponding mapped module.
	 * 
	 * @param moduleList
	 * @param compilationUnitList
	 */
	private void generateMappingRelation(ArrayList<ModuleWrapper> moduleList,
			ArrayList<ICompilationUnitWrapper> compilationUnitList) {
		double[][] overallSimilarityTable = initializeOverallSimilarityTable(moduleList, compilationUnitList);
		
		/**
		 * should take care that one compilation unit can be mapped to only one module,
		 * on the contrary, one module can be mapped to many compilation unit.
		 */
		for(int j=0; j<compilationUnitList.size(); j++){
			int index = 0;
			double maxValue = -1.0;
			for(int i=0; i<moduleList.size();i++){
				if(overallSimilarityTable[i][j] >= Double.valueOf(ReflexactoringUtil.getMappingThreshold())
						&& overallSimilarityTable[i][j] >= maxValue){
					index = i;
					maxValue = overallSimilarityTable[i][j];
				}
			}
			
			if(maxValue != -1.0){
				ICompilationUnitWrapper unit = compilationUnitList.get(j);
				ModuleWrapper module = moduleList.get(index);
				unit.setMappingModule(module);
				module.getMappingList().add(unit);
			}
		}
	}
	
	/**
	 * generate the overall similarity table in which the overall similarity bewteen
	 * each module and each compilation unit is computed. 
	 * @param moduleList
	 * @param compilationUnitList
	 * @return
	 */
	private double[][] initializeOverallSimilarityTable(ArrayList<ModuleWrapper> moduleList,
			ArrayList<ICompilationUnitWrapper> compilationUnitList){
		double[][] semanticSimilarityTable = generateSemanticSimilarityTable(moduleList, compilationUnitList);
		double[][] structuralSimilarityTable = generateStructuralSimilarityTable(semanticSimilarityTable, moduleList, compilationUnitList);
		
		/**
		 * after the above calculation, the dimensions of both semantic similarity table and
		 * structural similarity table should be the same.
		 */
		int m = moduleList.size();
		int n = compilationUnitList.size();
		double[][] overallSimilarity = new double[m][n];
		for(int i=0; i<m; i++){
			for(int j=0; j<n; j++){
				overallSimilarity[i][j] = (semanticSimilarityTable[i][j] + structuralSimilarityTable[i][j])/2;
			}
		}
		
		return overallSimilarity;
	}
	
	/**
	 * calculate the semantic (lexical) similarities between modules and compilation units.
	 * @param moduleList
	 * @param compilationUnitList
	 * @return
	 */
	private double[][] generateSemanticSimilarityTable(ArrayList<ModuleWrapper> moduleList,
			ArrayList<ICompilationUnitWrapper> compilationUnitList){
		double[][] similarityTable = new double[moduleList.size()][compilationUnitList.size()];
		
		for(int i=0; i<moduleList.size(); i++){
			ModuleWrapper module = moduleList.get(i);
			/**
			 * The first vector represents module description while the following vectors represent
			 * compilation unit description.
			 */
			List<double[]> vectorList = generateTFIDFTable(module, compilationUnitList);
			double[] moduleVector = vectorList.get(0);
			for(int k=1; k<vectorList.size(); k++){
				double[] compilationUnitVector = vectorList.get(k);
				double similarity = ReflexactoringUtil.computeEuclideanSimilarity(moduleVector, compilationUnitVector);
				similarityTable[i][k-1] = similarity;
			}
		}
		
		return similarityTable;
	}
	
	/**
	 * The first vector represents module description while the following vectors represent
	 * compilation unit description.
	 * @param module
	 * @param compilationUnitList
	 * @return
	 */
	private List<double[]> generateTFIDFTable(ModuleWrapper module,
			ArrayList<ICompilationUnitWrapper> compilationUnitList){
		TFIDF tfidf = new TFIDF(false);
		
		ArrayList<String> documentList = new ArrayList<>();
		documentList.add(ReflexactoringUtil.performStemming(module.getDescription().toLowerCase()));
		
		for(ICompilationUnitWrapper compiltionUnit: compilationUnitList){
			documentList.add(compiltionUnit.getDescription());
		}
		
		return tfidf.getTFIDFfromString(documentList);
	}
	
	/**
	 * transmit the semantic similarity with page ranking algorithm.
	 * @param semanticSimilarityTable
	 * @param moduleList
	 * @param compilationUnitList
	 * @return
	 */
	private double[][] generateStructuralSimilarityTable(double[][] semanticSimilarityTable, 
			ArrayList<ModuleWrapper> moduleList, ArrayList<ICompilationUnitWrapper> compilationUnitList){
		double[][] similarityTable = new double[moduleList.size()][compilationUnitList.size()];
		
		return similarityTable;
	}

	private ArrayList<ModuleWrapper> getModuleList() throws PartInitException{
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(ReflexactoringUtil.getTargetProjectName());
		
		if(project != null){
			IFile file = project.getFile(diagramName);
			if(file.exists()){
				IPath path = file.getFullPath();
				TransactionalEditingDomain editingDomain = GMFEditingDomainFactory.INSTANCE.createEditingDomain();
				
				URI targetURI = URI.createFileURI(path.toFile().toString());
				ResourceSet resourceSet = editingDomain.getResourceSet();
				Resource diagramResource = resourceSet.getResource(targetURI, true);
				
				Reflexactoring reflexactoring = (Reflexactoring)diagramResource.getContents().get(0);
				
				ArrayList<ModuleWrapper> moduleList = new ArrayList<>();
				for(Module module: reflexactoring.getModules()){
					moduleList.add(new ModuleWrapper(module));
				}
				
				return moduleList;
			}
		}
		
		return null;
	}
	
	
	

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}

}
