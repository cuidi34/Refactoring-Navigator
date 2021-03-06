package reflexactoring.diagram.edit.parts;

import java.util.Iterator;

import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeCompartmentEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.DragDropEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.FlowLayoutEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.ResizableCompartmentEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.figures.ResizableCompartmentFigure;
import org.eclipse.gmf.runtime.diagram.ui.requests.RequestConstants;
import org.eclipse.gmf.runtime.draw2d.ui.figures.ConstrainedToolbarLayout;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.gmf.tooling.runtime.edit.policies.reparent.CreationEditPolicyWithCustomReparent;

import reflexactoring.diagram.edit.parts.Class2EditPart.ClassFigure;
import reflexactoring.diagram.edit.policies.ModuleTypeContainerDropEditPolicy;
import reflexactoring.diagram.edit.policies.ModuleTypeContainerCompartmentCanonicalEditPolicy;
import reflexactoring.diagram.edit.policies.ModuleTypeContainerCompartmentItemSemanticEditPolicy;
import reflexactoring.diagram.part.Messages;
import reflexactoring.diagram.part.ReflexactoringVisualIDRegistry;

/**
 * @generated
 */
public class ModuleTypeContainerCompartmentEditPart extends
		ShapeCompartmentEditPart {

	/**
	 * @generated
	 */
	public static final int VISUAL_ID = 7001;

	/**
	 * @generated
	 */
	public ModuleTypeContainerCompartmentEditPart(View view) {
		super(view);
	}

	/**
	 * @generated
	 */
	public String getCompartmentName() {
		return Messages.ModuleTypeContainerCompartmentEditPart_title;
	}

	/**
	 * @not generated
	 */
	public IFigure createFigure() {
		ResizableCompartmentFigure result = (ResizableCompartmentFigure) super
				.createFigure();
		result.setTitleVisibility(false);
		result.getContentPane().setLayoutManager(new CustomLayout());
		//result.getContentPane().setLayoutManager(new XYLayout());
		
		return result;
	}
	
	/**
	 * @not generated
	 */
	private static class CustomLayout extends XYLayout
	{
		@Override
		public void layout(IFigure parent){
			Iterator<IFigure> fIterator = parent.getChildren().iterator();
			int count = 1;
			int x = 10;
			int y = 10;
			while (fIterator.hasNext()){
				IFigure figure = fIterator.next();
				int height = figure.getPreferredSize().height;
				int width = figure.getPreferredSize().width;
				figure.setBounds(new Rectangle(x, y, width,height));
				if(count % 3 == 0){
					x = 10;
					y = y + height + 20;
				}else{
					x = x + width + 20;
				}
				count++;
			}
		}
		
		/*public void layout(IFigure parent) {
			Iterator children = parent.getChildren().iterator();
			Point offset = getOrigin(parent);
			IFigure f;
			while (children.hasNext()) {
				f = (IFigure) children.next();
				Rectangle bounds = (Rectangle) getConstraint(f);
				if (bounds == null)
					continue;

				if (bounds.width == -1 || bounds.height == -1) {
					Dimension preferredSize = f.getPreferredSize(bounds.width,
							bounds.height);
					bounds = bounds.getCopy();
					if (bounds.width == -1)
						bounds.width = preferredSize.width;
					if (bounds.height == -1)
						bounds.height = preferredSize.height;
				}
				bounds = bounds.getTranslated(offset);
				f.setBounds(bounds);
			}
		}*/
	}

	
	/**
	 * @not generated
	 */
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE,
				new ResizableCompartmentEditPolicy());
		installEditPolicy(EditPolicyRoles.SEMANTIC_ROLE,
				new ModuleTypeContainerCompartmentItemSemanticEditPolicy());
		installEditPolicy(EditPolicyRoles.CREATION_ROLE,
				new CreationEditPolicyWithCustomReparent(
						ReflexactoringVisualIDRegistry.TYPED_INSTANCE));
		//installEditPolicy(EditPolicyRoles.DRAG_DROP_ROLE, new DragDropEditPolicy());
		installEditPolicy(EditPolicyRoles.DRAG_DROP_ROLE, new ModuleTypeContainerDropEditPolicy());
		installEditPolicy(EditPolicyRoles.CANONICAL_ROLE,
				new ModuleTypeContainerCompartmentCanonicalEditPolicy());
		//removeEditPolicy(EditPolicy.LAYOUT_ROLE);
		//installEditPolicy(EditPolicy.LAYOUT_ROLE, new CustomFlowLayoutEditPolicy());
		//installEditPolicy(EditPolicy.LAYOUT_ROLE, new XYLayoutEditPolicy());
	}
	
/*	private class CustomFlowLayoutEditPolicy extends FlowLayoutEditPolicy{

		 (non-Javadoc)
		 * @see org.eclipse.gef.editpolicies.OrderedLayoutEditPolicy#createAddCommand(org.eclipse.gef.EditPart, org.eclipse.gef.EditPart)
		 
		@Override
		protected Command createAddCommand(EditPart child, EditPart after) {
			// TODO Auto-generated method stub
			return null;
		}

		 (non-Javadoc)
		 * @see org.eclipse.gef.editpolicies.OrderedLayoutEditPolicy#createMoveChildCommand(org.eclipse.gef.EditPart, org.eclipse.gef.EditPart)
		 
		@Override
		protected Command createMoveChildCommand(EditPart child, EditPart after) {
			// TODO Auto-generated method stub
			return null;
		}

		 (non-Javadoc)
		 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getCreateCommand(org.eclipse.gef.requests.CreateRequest)
		 
		@Override
		protected Command getCreateCommand(CreateRequest request) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}*/

	/**
	 * @generated
	 */
	protected void setRatio(Double ratio) {
		if (getFigure().getParent().getLayoutManager() instanceof ConstrainedToolbarLayout) {
			super.setRatio(ratio);
		}
	}

}
