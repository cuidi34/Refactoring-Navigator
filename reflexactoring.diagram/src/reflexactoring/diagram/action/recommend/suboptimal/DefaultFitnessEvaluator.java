package reflexactoring.diagram.action.recommend.suboptimal;

import java.util.ArrayList;

import reflexactoring.diagram.util.Settings;

/**
 * @author linyun
 *
 */
public abstract class DefaultFitnessEvaluator implements FitnessEvaluator {
	/**
	 * this violation list is a temporary variable, after evaluator computing the fitness value for
	 * certain genotype, the genotype should also read this variable to retrieve the violations it
	 * generates.
	 */
	protected ArrayList<Violation> violationList = new ArrayList<>();
	
	protected double[][] similarityTable;
	
	protected double[][] highLevelNodeDependencyMatrix;
	protected double[][] lowLevelNodeDependencyMatrix;
	
	protected double[][] highLevelNodeCreationMatrix;
	protected double[][] lowLevelNodeCreationMatrix;
	
	protected double[][] highLevelNodeInheritanceMatrix;
	protected double[][] lowLevelNodeInheritanceMatrix;

	/**
	 * @param similarityTable
	 * @param highLevelNodeDependencyMatrix
	 * @param lowLevelNodeDependencyMatrix
	 * @param highLevelNodeCreationMatrix
	 * @param lowLevelNodeCreationMatrix
	 * @param highLevelNodeInheritanceMatrix
	 * @param lowLevelNodeInheritanceMatrix
	 */
	public DefaultFitnessEvaluator(double[][] similarityTable,
			double[][] highLevelNodeDependencyMatrix,
			double[][] lowLevelNodeDependencyMatrix,
			double[][] highLevelNodeInheritanceMatrix,
			double[][] lowLevelNodeInheritanceMatrix,
			double[][] highLevelNodeCreationMatrix,
			double[][] lowLevelNodeCreationMatrix) {
		super();
		this.similarityTable = similarityTable;
		this.highLevelNodeDependencyMatrix = highLevelNodeDependencyMatrix;
		this.lowLevelNodeDependencyMatrix = lowLevelNodeDependencyMatrix;
		this.highLevelNodeCreationMatrix = highLevelNodeCreationMatrix;
		this.lowLevelNodeCreationMatrix = lowLevelNodeCreationMatrix;
		this.highLevelNodeInheritanceMatrix = highLevelNodeInheritanceMatrix;
		this.lowLevelNodeInheritanceMatrix = lowLevelNodeInheritanceMatrix;
	}

	public abstract double computeFitness(Genotype gene);
	
	protected double computeStructureDependencyViolation(Genotype gene){
		double result = 0;
		
		double[][] confidenceTable = Settings.dependencyConfidenceTable.convertToRawTable();
		
		for(int i=0; i<highLevelNodeDependencyMatrix.length; i++){
			for(int j=0; j<highLevelNodeDependencyMatrix.length; j++){
				if(i != j){
					/**
					 * Detect divergence violation
					 */
					if(highLevelNodeDependencyMatrix[i][j] == 0){
						int violationNum = countDivergenceViolation(gene, i, j, lowLevelNodeDependencyMatrix);
						if(violationNum != 0){
							Violation violation = new Violation(i, j, Violation.DEPENDENCY_DIVERGENCE);
							violationList.add(violation);
						}
						result += confidenceTable[i][j] * violationNum;
					}
					/**
					 * Detect absence violation
					 */
					else{
						int violationNum = countAbsenceViolation(gene, i, j, lowLevelNodeDependencyMatrix);
						if(violationNum != 0){
							Violation violation = new Violation(i, j, Violation.DEPENDENCY_ABSENCE);
							violationList.add(violation);
						}
						result += confidenceTable[i][j] * violationNum;
					}
				}
			}
		}
		
		return result;
	}
	
	protected double computeStructureInheritanceViolation(Genotype gene){
		double result = 0;
		
		double[][] confidenceTable = Settings.extendConfidenceTable.convertToRawTable();
		
		for(int i=0; i<highLevelNodeInheritanceMatrix.length; i++){
			for(int j=0; j<highLevelNodeInheritanceMatrix.length; j++){
				if(i != j){
					/**
					 * Detect divergence violation
					 */
					if(highLevelNodeInheritanceMatrix[i][j] == 0){
						int violationNum = countDivergenceViolation(gene, i, j, lowLevelNodeInheritanceMatrix);
						if(violationNum != 0){
							Violation violation = new Violation(i, j, Violation.INHERITANCE_DIVERGENCE);
							violationList.add(violation);
						}
						result += confidenceTable[i][j] * violationNum;
					}
					/**
					 * Detect absence violation
					 */
					else{
						int violationNum = countAbsenceViolation(gene, i, j, lowLevelNodeInheritanceMatrix);
						if(violationNum != 0){
							Violation violation = new Violation(i, j, Violation.INHERITANCE_ABSENCE);
							violationList.add(violation);
						}
						result += confidenceTable[i][j] * violationNum;
					}
				}
			}
		}
		
		return result;
	}
	
	protected double computeStructureCreationViolation(Genotype gene){
		double result = 0;
		
		double[][] confidenceTable = Settings.extendConfidenceTable.convertToRawTable();
		
		for(int i=0; i<highLevelNodeCreationMatrix.length; i++){
			for(int j=0; j<highLevelNodeCreationMatrix.length; j++){
				if(i != j){
					/**
					 * Detect divergence violation
					 */
					if(highLevelNodeCreationMatrix[i][j] == 0){
						int violationNum = countDivergenceViolation(gene, i, j, lowLevelNodeCreationMatrix);
						if(violationNum != 0){
							Violation violation = new Violation(i, j, Violation.CREATION_DIVERGENCE);
							violationList.add(violation);
						}
						result += confidenceTable[i][j] * violationNum;
					}
					/**
					 * Detect absence violation
					 */
					else{
						int violationNum = countAbsenceViolation(gene, i, j, lowLevelNodeCreationMatrix);
						if(violationNum != 0){
							Violation violation = new Violation(i, j, Violation.CREATION_ABSENCE);
							violationList.add(violation);
						}
						result += confidenceTable[i][j] * violationNum;
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * There should be some dependency between caller module and callee module.
	 * 
	 * @param gene
	 * @param i
	 * @param j
	 * @return
	 */
	private int countAbsenceViolation(Genotype gene, int callerModuleIndex, int calleeModuleIndex, double[][] lowLevelNodeMatrix) {
		for(int i=0; i<gene.getLength(); i++){
			/**
			 * find any low level node i, which is mapped to caller module.
			 */
			if(gene.getDNA()[i] == callerModuleIndex){
				/**
				 * find any the low level node, j, called by node i.
				 */
				for(int j=0; j<lowLevelNodeMatrix.length; j++){
					if(lowLevelNodeMatrix[i][j] != 0){
						/**
						 * if node j is mapped to callee module, it means there is no violation.
						 */
						if(gene.getDNA()[j] == calleeModuleIndex){
							return 0;
						}
					}
				}
			}
		}
		/**
		 * find all the possible maps, there does not exsit such dependency, return 1 to mean a violatoin.
		 */
		return 1;
	}

	/**
	 * There should not be any dependency between caller module and callee module
	 * @param gene 
	 * @return
	 */
	private int countDivergenceViolation(Genotype gene, int callerModuleIndex, 
			int calleeModuleIndex, double[][] lowLevelNodeMatrix) {
		int num = 0;
		for(int i=0; i<gene.getLength(); i++){
			/**
			 * find any low level node i, which is mapped to caller module.
			 */
			if(gene.getDNA()[i] == callerModuleIndex){
				/**
				 * find any the low level node, j, called by node i.
				 */
				for(int j=0; j<lowLevelNodeMatrix.length; j++){
					if(lowLevelNodeMatrix[i][j] != 0){
						/**
						 * if node j is mapped to callee module, there is a violation.
						 */
						if(gene.getDNA()[j] == calleeModuleIndex){
							num += lowLevelNodeMatrix[i][j];
							//num++;
						}
					}
				}
			}
		}
		return num;
	}

	protected double computeLexicalSimilarity(Genotype gene){
		double result = 0;
		
		for(int lowLevelNodeIndex=0; lowLevelNodeIndex<gene.getLength(); lowLevelNodeIndex++){
			int highLevelNodeIndex = gene.getDNA()[lowLevelNodeIndex];
			/**
			 * here, if a new type has no similarity with any module.
			 */
			if(lowLevelNodeIndex < similarityTable[0].length){
				result += similarityTable[highLevelNodeIndex][lowLevelNodeIndex];				
			}
		}
		
		return result/gene.getLength();
	}

	@Override
	public boolean isFeasible() {
		return this.violationList.size() == 0;
	}

	/**
	 * @return the violationList
	 */
	public ArrayList<Violation> getViolationList() {
		return violationList;
	}

	/**
	 * @return the similarityTable
	 */
	public double[][] getSimilarityTable() {
		return similarityTable;
	}

	/**
	 * @param similarityTable the similarityTable to set
	 */
	public void setSimilarityTable(double[][] similarityTable) {
		this.similarityTable = similarityTable;
	}

	/**
	 * @return the highLevelNodeDependencyMatrix
	 */
	public double[][] getHighLevelNodeDependencyMatrix() {
		return highLevelNodeDependencyMatrix;
	}

	/**
	 * @param highLevelNodeDependencyMatrix the highLevelNodeDependencyMatrix to set
	 */
	public void setHighLevelNodeDependencyMatrix(
			double[][] highLevelNodeDependencyMatrix) {
		this.highLevelNodeDependencyMatrix = highLevelNodeDependencyMatrix;
	}

	/**
	 * @return the lowLevelNodeDependencyMatrix
	 */
	public double[][] getLowLevelNodeDependencyMatrix() {
		return lowLevelNodeDependencyMatrix;
	}

	/**
	 * @param lowLevelNodeDependencyMatrix the lowLevelNodeDependencyMatrix to set
	 */
	public void setLowLevelNodeDependencyMatrix(
			double[][] lowLevelNodeDependencyMatrix) {
		this.lowLevelNodeDependencyMatrix = lowLevelNodeDependencyMatrix;
	}

	/**
	 * @return the highLevelNodeInheritanceMatrix
	 */
	public double[][] getHighLevelNodeInheritanceMatrix() {
		return highLevelNodeInheritanceMatrix;
	}

	/**
	 * @param highLevelNodeInheritanceMatrix the highLevelNodeInheritanceMatrix to set
	 */
	public void setHighLevelNodeInheritanceMatrix(
			double[][] highLevelNodeInheritanceMatrix) {
		this.highLevelNodeInheritanceMatrix = highLevelNodeInheritanceMatrix;
	}

	/**
	 * @return the lowLevelNodeInheritanceMatrix
	 */
	public double[][] getLowLevelNodeInheritanceMatrix() {
		return lowLevelNodeInheritanceMatrix;
	}

	/**
	 * @param lowLevelNodeInheritanceMatrix the lowLevelNodeInheritanceMatrix to set
	 */
	public void setLowLevelNodeInheritanceMatrix(
			double[][] lowLevelNodeInheritanceMatrix) {
		this.lowLevelNodeInheritanceMatrix = lowLevelNodeInheritanceMatrix;
	}

	/**
	 * @return the highLevelNodeCreationMatrix
	 */
	public double[][] getHighLevelNodeCreationMatrix() {
		return highLevelNodeCreationMatrix;
	}

	/**
	 * @param highLevelNodeCreationMatrix the highLevelNodeCreationMatrix to set
	 */
	public void setHighLevelNodeCreationMatrix(
			double[][] highLevelNodeCreationMatrix) {
		this.highLevelNodeCreationMatrix = highLevelNodeCreationMatrix;
	}

	/**
	 * @return the lowLevelNodeCreationMatrix
	 */
	public double[][] getLowLevelNodeCreationMatrix() {
		return lowLevelNodeCreationMatrix;
	}

	/**
	 * @param lowLevelNodeCreationMatrix the lowLevelNodeCreationMatrix to set
	 */
	public void setLowLevelNodeCreationMatrix(double[][] lowLevelNodeCreationMatrix) {
		this.lowLevelNodeCreationMatrix = lowLevelNodeCreationMatrix;
	}

}
