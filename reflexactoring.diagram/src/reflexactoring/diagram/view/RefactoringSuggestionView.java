package reflexactoring.diagram.view;

import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;

import reflexactoring.Activator;
import reflexactoring.diagram.action.recommend.Suggestion;
import reflexactoring.diagram.util.Settings;

public class RefactoringSuggestionView extends ViewPart {

	private FormToolkit toolkit;
	private ScrolledForm form;
	private FormText text; 
	
	private static final String initialDesc = "Refactoring with Fun! ^_^";
	private static final String refactoringDesc = "The following are suggestions to improve the consistency:";
	
	
	
	public RefactoringSuggestionView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.setText("Suggestions");
		form.getBody().setLayout(new TableWrapLayout());
		text = toolkit.createFormText(form.getBody(), true);
		text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));
		text.setText("<form>" + initialDesc +"</form>", true, false);
		
		//hookActionsOnToolBar();
	}
	
	public void refreshSuggestionsOnUI(ArrayList<Suggestion> suggestions){
		for(Control control: form.getBody().getChildren()){
			control.dispose();
		}
		
		FormText headerText = toolkit.createFormText(form.getBody(), true);
		headerText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));
		headerText.setText(refactoringDesc, false, false);
		
		
		if(suggestions == null){
			return;
		}
		
		generateSuggestionsOnUI(suggestions);
		
		form.reflow(false);
	}
	
	private void generateSuggestionsOnUI(ArrayList<Suggestion> list){
		for(Suggestion suggestion: list){
			final FormText text = toolkit.createFormText(form.getBody(), true);
			text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));
			
			StringBuffer buffer = new StringBuffer();
			buffer.append("<form>");
			buffer.append("<li>");
			buffer.append(suggestion.generateTagedText());
			buffer.append("<b>[</b><a href=\"Exec\">Execute</a>, ");
			buffer.append("<a href=\"Undo\">Undo</a><b>]</b>");
			buffer.append("</li>");			
			
			buffer.append("</form>");
			
			text.setText(buffer.toString(), true, false);
			text.setData(suggestion);
			text.addHyperlinkListener(new HyperlinkAdapter() {
				public void linkActivated(HyperlinkEvent e) {
					Suggestion suggestion = (Suggestion) text.getData();
					if(e.getHref().equals("Module")){
						System.out.println(suggestion);
					}
					else if(e.getHref().equals("Type")){
						System.out.println(suggestion);
					}
					else if(e.getHref().equals("Method")){
						System.out.println(suggestion);
					}
					else if(e.getHref().equals("Field")){
						System.out.println(suggestion);
					}
					else if(e.getHref().equals("Exec")){
						suggestion.apply();
					}
					else if(e.getHref().equals("Undo")){
						suggestion.undoApply();
					}
				}
			});
			text.getParent().layout();
			text.getParent().redraw();
		}
	}

	@SuppressWarnings("restriction")
	private void hookActionsOnToolBar(){
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		
		Action skipUnmappedTypeAction = new Action("Ignore Unmapped Types", IAction.AS_CHECK_BOX){
			public void run(){
				if(this.isChecked()){
					Settings.isSkipUnMappedTypes = true;
				}
				else{
					Settings.isSkipUnMappedTypes = false;
				}
			}
		};
		
		skipUnmappedTypeAction.setImageDescriptor(JavaPluginImages.DESC_OBJS_NLS_SKIP);
		
		toolBar.add(skipUnmappedTypeAction);
	}
	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
