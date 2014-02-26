/**
 * 
 */
package reflexactoring.diagram.action.recommend.suboptimal;

import java.util.ArrayList;

/**
 * @author linyun
 *
 */
public class Population extends ArrayList<Genotype>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1030488561271723649L;

	public ArrayList<Genotype> getKBestCandidate(int k){
		if(this.size() < k){
			return this;
		}
		else{
			ArrayList<Genotype> list = new ArrayList<>();
			for(int i=0; i<k; i++){
				list.add(this.get(i));
			}
			
			return  list;
		}
	}
	
	/*private Genotype optimalGene;

	*//**
	 * @return the optimalGene
	 *//*
	public Genotype getOptimalGene() {
		return optimalGene;
	}

	*//**
	 * @param optimalGene the optimalGene to set
	 *//*
	public void setOptimalGene(Genotype optimalGene) {
		this.optimalGene = optimalGene;
	}
	
	public void updateOptimalGene(){
		
		if(this.size() == 0)return;
		
		Genotype optimalGene = (null == this.optimalGene)? this.get(0) : this.optimalGene;
		
		for(int i=0; i<this.size(); i++){
			Genotype gene = this.get(i);
			if(gene.getFitness() > optimalGene.getFitness()){
				optimalGene = gene;
			}
		}
		
		this.optimalGene = optimalGene;
	}*/
}
