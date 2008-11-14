package net.sourceforge.pmd.eclipse.ui.preferences.br;

import java.util.Map;

import net.sourceforge.pmd.PropertyDescriptor;
import net.sourceforge.pmd.Rule;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * 
 * @author Brian Remedios
 */
public class FormArranger {

	private final Composite parent;
	private final Map<Class<?>, EditorFactory>	editorFactoriesByValueType;
	private final ValueChangeListener changeListener;
	private Rule  rule;

	private Control[][] widgets;
	
	/**
	 * Constructor for FormArranger.
	 * @param theParent Composite
	 * @param factories Map<Class,EditorFactory>
	 */
	public FormArranger(Composite theParent, Map<Class<?>, EditorFactory> factories, ValueChangeListener listener) {
		parent = theParent;
		editorFactoriesByValueType = factories;
		changeListener = listener;
	}

	/**
	 * Method factoryFor.
	 * @param desc PropertyDescriptor
	 * @return EditorFactory
	 */
	private EditorFactory factoryFor(PropertyDescriptor<?> desc) {
		return editorFactoriesByValueType.get(desc.type());
	}

	private void clearChildren() {
		Control[] kids = parent.getChildren();
		for (int i=0; i<kids.length; i++) kids[i].dispose();
	}

	/**
	 * Method arrangeFor.
	 * @param theRule Rule
	 */
	public void arrangeFor(Rule theRule) {

	    if (rule == theRule) return;
	    
		clearChildren();
		rule = theRule;
		
		if (rule == null) return;
		
		Map<PropertyDescriptor<?>, Object> valuesByDescriptor = PMDPreferencePage.filteredPropertiesOf(rule);
		if (valuesByDescriptor.isEmpty()) return;
		
		PropertyDescriptor<?>[] orderedDescs = (PropertyDescriptor[])valuesByDescriptor.keySet().toArray(new PropertyDescriptor[valuesByDescriptor.size()]);
				
		int maxColumns = 2;
		int rowCount = 0;	// count up the actual rows with widgets needed, not all have editors yet
		for (int i=0; i<orderedDescs.length; i++) {
			EditorFactory factory = factoryFor(orderedDescs[i]);
			if (factory == null) {
			    System.out.println("No editor defined for: "  + orderedDescs[i]);
			    continue;
			}
			int colsReqd = factory.columnsRequired();
			maxColumns = Math.max(maxColumns, colsReqd);
			rowCount++;
		}
			
        GridLayout layout = new GridLayout(maxColumns, false);
        parent.setLayout(layout);
		
		widgets = new Control[rowCount][maxColumns];
		if (maxColumns < 1) return;
		
		int rowsAdded = 0;
		for (int i=0; i<orderedDescs.length; i++) {
			if (addRowWidgets(factoryFor(orderedDescs[i]), rowsAdded, orderedDescs[i])) rowsAdded++;
		}
				
		if (rowsAdded > 0) {
		    parent.pack();
			}
	}

	/**
	 * @param factory EditorFactory
	 * @param rowIndex int
	 * @param desc PropertyDescriptor
	 * @return boolean
	 */
	private boolean addRowWidgets(EditorFactory factory, int rowIndex, PropertyDescriptor<?> desc) {

		if (factory == null) return false;
		
		int columns = factory.columnsRequired();
		for (int i=0; i<columns; i++) {	// add all the labels & controls necessary on each row
			widgets[rowIndex][i] = factory.newEditorOn(parent, i, desc, rule, changeListener);
		}
		
		return true;
	}
}