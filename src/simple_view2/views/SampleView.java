package simple_view2.views;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;

import java.util.Arrays;

import javax.inject.Inject;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class SampleView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "simple_view2.views.SampleView";

	@Inject IWorkbench workbench;

	private TableViewer viewer;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;
	private Text expressionText;
	private Label resultLabel;

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}
		@Override
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		@Override
		public Image getImage(Object obj) {
			return workbench.getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(3, false));

		Label expressionLabel = new Label(container, SWT.NONE);
		expressionLabel.setText("Expression:");

		expressionText = new Text(container, SWT.BORDER);
		expressionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Button calculateButton = new Button(container, SWT.PUSH);
		calculateButton.setText("Calculate");

		resultLabel = new Label(container, SWT.NONE);
        resultLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

        calculateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String expression = expressionText.getText();
                try {
                    double result = Calculator.evaluate(expression);
                    System.out.println(result);
                    resultLabel.setText("Result: " + result);
                } catch (IllegalArgumentException ex) {
                    resultLabel.setText(ex.toString());
                }
                container.layout(); // Update the layout to show the result label
            }
        });

		Button clearButton = new Button(container, SWT.PUSH);
		clearButton.setText("Clear");
		clearButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				expressionText.setText("");
				resultLabel.setText("");
				container.layout(); // Update the layout to hide the result label
			}
		});}

		private void hookContextMenu() {
			MenuManager menuMgr = new MenuManager("#PopupMenu");
			menuMgr.setRemoveAllWhenShown(true);
			menuMgr.addMenuListener(new IMenuListener() {
				public void menuAboutToShow(IMenuManager manager) {
					SampleView.this.fillContextMenu(manager);
				}
			});
			Menu menu = menuMgr.createContextMenu(viewer.getControl());
			viewer.getControl().setMenu(menu);
			getSite().registerContextMenu(menuMgr, viewer);
		}

		private void contributeToActionBars() {
			IActionBars bars = getViewSite().getActionBars();
			fillLocalPullDown(bars.getMenuManager());
			fillLocalToolBar(bars.getToolBarManager());
		}

		private void fillLocalPullDown(IMenuManager manager) {
			manager.add(action1);
			manager.add(new Separator());
			manager.add(action2);
		}

		private void fillContextMenu(IMenuManager manager) {
			manager.add(action1);
			manager.add(action2);
			// Other plug-ins can contribute there actions here
			manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}

		private void fillLocalToolBar(IToolBarManager manager) {
			manager.add(action1);
			manager.add(action2);
		}

		private void makeActions() {
			action1 = new Action() {
				public void run() {
					showMessage("Action 1 executed");
				}
			};
			action1.setText("Action 1");
			action1.setToolTipText("Action 1 tooltip");
			action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
					getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

			action2 = new Action() {
				public void run() {
					showMessage("Action 2 executed");
				}
			};
			action2.setText("Action 2");
			action2.setToolTipText("Action 2 tooltip");
			action2.setImageDescriptor(workbench.getSharedImages().
					getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
			doubleClickAction = new Action() {
				public void run() {
					IStructuredSelection selection = viewer.getStructuredSelection();
					Object obj = selection.getFirstElement();
					showMessage("Double-click detected on "+obj.toString());
				}
			};
		}

		private void hookDoubleClickAction() {
			viewer.addDoubleClickListener(new IDoubleClickListener() {
				public void doubleClick(DoubleClickEvent event) {
					doubleClickAction.run();
				}
			});
		}
		private void showMessage(String message) {
			MessageDialog.openInformation(
					viewer.getControl().getShell(),
					"Sample View",
					message);
		}


		public class Calculator {

			private static double evaluate(String expression) {
//			    String regex = "\\+|\\-|\\*|\\/";
				String regex = "(?<=[-+*/()])|(?=[-+*/()])";
			    String[] tokens = expression.split(regex);
			    System.out.println(Arrays.deepToString(tokens)
			    		);

			    try {
			        double leftOperand = Double.parseDouble(tokens[0]);
			        double rightOperand = Double.parseDouble(tokens[2]);
			        String operator = tokens[1];

			        switch (operator) {
			            case "+":
			                return leftOperand + rightOperand;
			            case "-":
			                return leftOperand - rightOperand;
			            case "*":
			                return leftOperand * rightOperand;
			            case "/":
			                if (rightOperand == 0) {
			                    throw new IllegalArgumentException("Division by zero");
			                }
			                return leftOperand / rightOperand;
			            default:
			                throw new IllegalArgumentException("Invalid operator: " + operator);
			        }
			    } catch (NumberFormatException e) {
			        throw new IllegalArgumentException("Invalid number in expression");
			    }
			}

		}

		
		@Override
		public void setFocus() {
			viewer.getControl().setFocus();
		}
	}
