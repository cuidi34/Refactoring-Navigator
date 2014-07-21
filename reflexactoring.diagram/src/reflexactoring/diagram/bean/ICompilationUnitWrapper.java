/**
 * 
 */
package reflexactoring.diagram.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;

import reflexactoring.diagram.action.semantic.TokenExtractor;

/**
 * @author linyun
 *
 */
public class ICompilationUnitWrapper extends Document implements LowLevelSuggestionObject, LowLevelGraphNode{
	private ICompilationUnit compilationUnit; 
	private ModuleWrapper mappingModule;
	private CompilationUnit javaUnit;
	
	private boolean isInterface;
	
	private ICompilationUnitWrapper superClass;
	private ArrayList<ICompilationUnitWrapper> superInterfaceList = new ArrayList<>();
	
	private ArrayList<ICompilationUnitWrapper> parentList = new ArrayList<>();
	private ArrayList<ICompilationUnitWrapper> childList = new ArrayList<>();
	
	private ArrayList<ICompilationUnitWrapper> calleeCompilationUnitList = new ArrayList<>();
	private ArrayList<ICompilationUnitWrapper> callerCompilationUnitList = new ArrayList<>();
	
	private HashMap<ICompilationUnitWrapper, ArrayList<ASTNode>> referingDetails
		= new HashMap<>();
	
	private ArrayList<UnitMemberWrapper> members = new ArrayList<>();
	
	/**
	 * @param compilationUnit
	 */
	public ICompilationUnitWrapper(ICompilationUnit compilationUnit) {
		super();
		this.compilationUnit = compilationUnit;
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		
		Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
		parser.setCompilerOptions(options);
		
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);

		parser.setSource(compilationUnit);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		setJavaUnit(cu);
		
		TypeDeclaration typeDeclar = (TypeDeclaration) this.javaUnit.types().get(0);
		this.isInterface = typeDeclar.isInterface();
		
		String content = new TokenExtractor(this).extractTokens(cu);
		content = content + generateTitle();
		
		this.setDescription(content);
		this.extractTermFrequency(content);
	}
	
	public boolean isInterface(){
		return isInterface;
	}
	
	public void setInterface(boolean isInterface){
		this.isInterface = isInterface;
	}
	
	public String toString(){
		return this.compilationUnit.getElementName();
	}
	
	public void openInEditor(){
		IEditorPart javaEditor;
		try {
			javaEditor = JavaUI.openInEditor(this.compilationUnit);
			JavaUI.revealInEditor(javaEditor,
					(IJavaElement) this.compilationUnit);
		} catch (PartInitException e) {
			e.printStackTrace();
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}
	
	public boolean hasSuperCompilationUnit(ICompilationUnitWrapper unit){
		if(superClass != null && superClass.equals(unit)){
			return true;
		}
		else{
			for(ICompilationUnitWrapper interf: superInterfaceList){
				if(interf.equals(unit)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean hasCalleeCompilationUnit(ICompilationUnitWrapper unit){
		for(ICompilationUnitWrapper calleeUnit: this.calleeCompilationUnitList){
			if(calleeUnit.equals(unit)){
				return true;
			}
		}
		
		return false;
	}
	
	public String getSimpleName(){
		String uniqueName = this.compilationUnit.getElementName();
		uniqueName = uniqueName.substring(0, uniqueName.indexOf(".java"));
		return uniqueName;
	}
	
	public String getPackageName(){
		try {
			return this.compilationUnit.getPackageDeclarations()[0].getElementName();
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	public String getFullQualifiedName(){
		return getPackageName() + "." + getSimpleName();
	}
	
	public int hashCode(){
		return getSimpleName().hashCode();
	}
	
	public boolean equals(Object obj){
		
		if(obj == null){
			return false;
		}
		
		if(obj instanceof ICompilationUnitWrapper){
			ICompilationUnitWrapper type = (ICompilationUnitWrapper)obj;
			return type.getFullQualifiedName().equals(this.getFullQualifiedName());
		}
		
		return false;
	}
	
	/**
	 * @return the compilationUnit
	 */
	public ICompilationUnit getCompilationUnit() {
		return compilationUnit;
	}
	/**
	 * @param compilationUnit the compilationUnit to set
	 */
	public void setCompilationUnit(ICompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}
	
	@Override
	/**
	 * @return the mappingModule
	 */
	public ModuleWrapper getMappingModule() {
		return mappingModule;
	}
	/**
	 * @param mappingModule the mappingModule to set
	 */
	public void setMappingModule(ModuleWrapper mappingModule) {
		this.mappingModule = mappingModule;
	}

	/**
	 * @return the javaUnit
	 */
	public CompilationUnit getJavaUnit() {
		return javaUnit;
	}

	/**
	 * @param javaUnit the javaUnit to set
	 */
	public void setJavaUnit(CompilationUnit javaUnit) {
		this.javaUnit = javaUnit;
	}

	/**
	 * @return the calleeCompilationUnitList
	 */
	public ArrayList<ICompilationUnitWrapper> getCalleeCompilationUnitList() {
		return calleeCompilationUnitList;
	}

	/**
	 * @param calleeCompilationUnitList the calleeCompilationUnitList to set
	 */
	public void setCalleeCompilationUnitList(
			ArrayList<ICompilationUnitWrapper> calleeCompilationUnitList) {
		this.calleeCompilationUnitList = calleeCompilationUnitList;
	}

	/**
	 * @return the callerCompilationUnitList
	 */
	public ArrayList<ICompilationUnitWrapper> getCallerCompilationUnitList() {
		return callerCompilationUnitList;
	}

	/**
	 * @param callerCompilationUnitList the callerCompilationUnitList to set
	 */
	public void setCallerCompilationUnitList(
			ArrayList<ICompilationUnitWrapper> callerCompilationUnitList) {
		this.callerCompilationUnitList = callerCompilationUnitList;
	}
	
	public void addParent(ICompilationUnitWrapper unit){
		if(hasSuperCompilationUnit(unit) && !this.parentList.contains(unit)){
			this.parentList.add(unit);
		}
	}
	
	public void addChild(ICompilationUnitWrapper unit){
		if(!this.childList.contains(unit)){
			this.childList.add(unit);
		}
	}
	
	public void addCaller(ICompilationUnitWrapper unit){
		if(!this.callerCompilationUnitList.contains(unit)){
			this.callerCompilationUnitList.add(unit);
		}
	}
	
	public void addCallee(ICompilationUnitWrapper unit){
		if(this.calleeCompilationUnitList.contains(unit)){
			this.calleeCompilationUnitList.add(unit);
		}
	}
	
	public void addMember(UnitMemberWrapper member){
		if(!this.members.contains(member)){
			this.members.add(member);
		}
	}

	/* (non-Javadoc)
	 * @see reflexactoring.diagram.bean.SuggestionObject#getName()
	 */
	@Override
	public String getName() {
		return this.getSimpleName();
	}
	
	/** 
	 * (non-Javadoc)
	 * @see reflexactoring.diagram.bean.GraphNode#getCallerList()
	 */
	@Override
	public List<? extends GraphNode> getCallerList() {
		return callerCompilationUnitList;
	}

	/** (non-Javadoc)
	 * @see reflexactoring.diagram.bean.GraphNode#getCalleeList()
	 */
	@Override
	public List<? extends GraphNode> getCalleeList() {
		return calleeCompilationUnitList;
	}
	

	/** (non-Javadoc)
	 * @see reflexactoring.diagram.bean.GraphNode#getParentList()
	 */
	@Override
	public List<? extends GraphNode> getParentList() {
		return this.parentList;
	}

	/** (non-Javadoc)
	 * @see reflexactoring.diagram.bean.GraphNode#getChildList()
	 */
	@Override
	public List<? extends GraphNode> getChildList() {
		return this.childList;
	}
	
	private ArrayList<ICompilationUnitWrapper> convertToList(HashMap<ICompilationUnitWrapper, Integer> map){
		ArrayList<ICompilationUnitWrapper> list = new ArrayList<>();
		for(ICompilationUnitWrapper wrapper: map.keySet()){
			list.add(wrapper);
		}
		
		return list;
	}

	/* (non-Javadoc)
	 * @see reflexactoring.diagram.bean.SuggestionObject#getType()
	 */
	@Override
	public String getTypeName() {
		return "Java type";
	}

	/* (non-Javadoc)
	 * @see reflexactoring.diagram.bean.SuggestionObject#getNameWithTag()
	 */
	@Override
	public String getNameWithTag() {
		return "<a href=\"Type\" value=\"test\">" + getName() + "</a>";
	}

	/**
	 * @return the members
	 */
	public ArrayList<UnitMemberWrapper> getMembers() {
		return members;
	}

	/**
	 * @param members the members to set
	 */
	public void setMembers(ArrayList<UnitMemberWrapper> members) {
		this.members = members;
	}

	/**
	 * @return the referingDetails
	 */
	public HashMap<ICompilationUnitWrapper, ArrayList<ASTNode>> getReferingDetails() {
		return referingDetails;
	}

	/**
	 * @param refereeCompilationUnit
	 * @param type
	 */
	public void putReferringDetail(
			ICompilationUnitWrapper refereeCompilationUnit, ASTNode node) {
		ArrayList<ASTNode> nodeList = this.referingDetails.get(refereeCompilationUnit);
		if(nodeList == null){
			nodeList = new ArrayList<>();
		}
		if(!nodeList.contains(node)){
			nodeList.add(node);			
		}
		this.referingDetails.put(refereeCompilationUnit, nodeList);
	}

	@Override
	protected String getDocName() {
		return getName();
	}


	/**
	 * @return the superClass
	 */
	public ICompilationUnitWrapper getSuperClass() {
		return superClass;
	}


	/**
	 * @param superClass the superClass to set
	 */
	public void setSuperClass(ICompilationUnitWrapper superClass) {
		this.superClass = superClass;
	}


	/**
	 * @return the superInterfaceList
	 */
	public ArrayList<ICompilationUnitWrapper> getSuperInterfaceList() {
		return superInterfaceList;
	}


	/**
	 * @param superInterfaceList the superInterfaceList to set
	 */
	public void setSuperInterfaceList(ArrayList<ICompilationUnitWrapper> superInterfaceList) {
		this.superInterfaceList = superInterfaceList;
	}
	
	public void addSuperInterface(ICompilationUnitWrapper superInterface){
		this.superInterfaceList.add(superInterface);
	}

}
