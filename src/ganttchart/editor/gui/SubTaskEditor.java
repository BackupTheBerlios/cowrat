package ganttchart.editor.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ganttchart.tasklib.SubTask;
import ganttchart.tasklib.SubTaskInstance;
import ganttchart.tasklib.TaskInstance;
import ganttchart.tasklib.TaskPool;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;

/**
 * This is the subtask editor. It edits existing subtask instances, or their owner (the subtask itself)
 * It is based on the old subtaskeditor (now NewSubTaskEditor)
 * @author Wim Vanden Broeck
 */
public class SubTaskEditor extends JDialog {
	
	private SubTaskInstance instance;
	private DialogActionListener listener;
	private TaskPool tasks;
	
	private JTextField txtSubtask;
	private JColorButton btnColour;
	private SpinnerDateModel spinEndModel, spinStartModel;
	private JButton addAttribute, clearAttribute;
	private JTextArea txtAttributes, txtAddedAtts;
	private JCheckBox chbattributes;
	private String[] attributeArray;
	
	/**
	 * Constructor will create the dialog window.
	 * @param owner Owner frame.
	 * @param subInst The subtaskinstance that needs to be edited.
	 * @param tasks The taskpool that contains all tasks and subtasks
	 */
	
	public SubTaskEditor(Frame owner, SubTaskInstance subInst, TaskPool tasks) {
		super(owner, "Edit Sub Task", true);
		instance = subInst;
		this.tasks = tasks;
		this.setResizable(false);
		this.setLocationByPlatform(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        listener = new DialogActionListener();
        createOptions(getContentPane());
        
        //Display the window.
        pack();
        setVisible(true);
	}
	
	/**
	 * Helper function to create new grid bag constraints.
	 * @param y The y position
	 * @param x The x position
	 * @param width The width of the position
	 * @param radio If it is a radio, it will apply different insets
	 * @return GridBagConstraints for use with a GridBagLayout
	 */
	private GridBagConstraints newConstraints(int y, int x, int width, boolean radio) {
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = width;
		c.ipadx = 1;
		c.ipady = 1;
		
		if (radio) {
			c.insets = new Insets(4, 4, 0 ,4);
		} else {
			c.insets = new Insets(4,4,4,4);
		}
		c.fill = GridBagConstraints.HORIZONTAL;
		
		return c;
	}
	
	/**
	 * Creates the full window.
	 * @param content The content pane to use.
	 */
	private void createOptions(Container contentPane) {
		// Add the layout
		contentPane.setLayout(new GridBagLayout());
				
		createColorButton(contentPane);
		chbattributes = UIFactory.createToolBarCheckBox(listener, "ShowAttribute", "Add an attribute to the Subtask", "Add attributes (separated by ',')");
		txtSubtask = new JTextField();
		txtSubtask.setText(instance.getDefinition().getName());
		
		contentPane.add(chbattributes, newConstraints(7, 0, 1, true));
		contentPane.add(txtSubtask, newConstraints(1, 1, 1, false));
		createStartDateInput(contentPane);
		createEndDateInput(contentPane);
		createSeperators(contentPane);
		createButtons(contentPane);
		createAttributeOptions(contentPane);
	}
	
	/**
	 * Creates seperators between in the contentPane
	 * @param contentPane The Container
	 * @param contentPane
	 */
	private void createSeperators(Container contentPane) {
		JSeparator sepButtonSep = new JSeparator();
		GridBagConstraints g = newConstraints(6, 0, 4, false);
		g.insets = new Insets(4, 0, 4, 0);
		contentPane.add(sepButtonSep, g);
		
		JSeparator sepButtonSep2 = new JSeparator();
		GridBagConstraints g2 = newConstraints(10, 0, 4, false);
		g.insets = new Insets(4, 0, 4, 0);
		contentPane.add(sepButtonSep2, g2);
	}

	/**
	 * Creates the subtask attributes
	 */
	private void createSubTaskAttributes() {
		SubTask sub = instance.getDefinition();
			String atts = "";
			if(sub.getAttributes().size() > 0){
				atts = sub.getAttributes().get(0);
				for(int i = 1; i < sub.getAttributes().size(); i++){
					atts += "," + sub.getAttributes().get(i);
				}
			}
			txtAddedAtts.setText(atts);
	}

	/**
	 * Creates the buttons and textareas to contain the attributes
	 * @param contentPane
	 */
	private void createAttributeOptions(Container contentPane) {
		txtAttributes = new JTextArea();
		txtAttributes.setColumns(20);
		txtAttributes.setRows(5);
		txtAttributes.setLineWrap(true);
		txtAttributes.setWrapStyleWord(true);
		JScrollPane pane = new JScrollPane(txtAttributes);

		txtAddedAtts = new JTextArea();
		txtAddedAtts.setColumns(20);
		txtAddedAtts.setRows(5);
		txtAddedAtts.setLineWrap(true);
		txtAddedAtts.setWrapStyleWord(true);
		txtAddedAtts.setEditable(false);
		JScrollPane pane2 = new JScrollPane(txtAddedAtts);
		
		addAttribute =  new JButton("OK");
		addAttribute.addActionListener(listener);
		addAttribute.setActionCommand("AddAttribute");
		
		clearAttribute =  new JButton("Clear");
		clearAttribute.addActionListener(listener);
		clearAttribute.setActionCommand("ClearAttributes");
		
		contentPane.add(pane, newConstraints(8, 0, 1, false));
		contentPane.add(pane2, newConstraints(8, 2, 1, false));
		contentPane.add(addAttribute, newConstraints(9, 0, 1, false));
		contentPane.add(clearAttribute, newConstraints(9, 2, 1, false));
		txtAttributes.setEnabled(false); addAttribute.setEnabled(false);
		createSubTaskAttributes();
	}

	/**
	 * Creates the buttons
	 * @param contentPane
	 */
	private void createButtons(Container contentPane) {
		JButton btnOK = new JButton("OK");
		btnOK.addActionListener(listener);
		btnOK.setActionCommand("Done");
		btnOK.setPreferredSize(new Dimension(150, btnOK.getPreferredSize().height));
		contentPane.add(btnOK, newConstraints(11, 1, 1, false));
		
		JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(listener);
		btnDelete.setActionCommand("Delete");
		btnOK.setPreferredSize(new Dimension(150, btnDelete.getPreferredSize().height));
		contentPane.add(btnDelete, newConstraints(11, 2, 1, false));
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(listener);
		btnCancel.setActionCommand("Cancel");
		contentPane.add(btnCancel, newConstraints(11, 3, 1, false));
	}

	/**
	 * Creates the output for the end date
	 * @param contentPane
	 */
	private void createEndDateInput(Container contentPane) {
		JLabel lblEnd = new JLabel("End date:");
		spinEndModel = new SpinnerDateModel(instance.getEnd().getTime(), tasks.getStartDate().getTime(), tasks.getEndDate().getTime(), Calendar.DAY_OF_YEAR);
		JSpinner spinEnd = new JSpinner(spinEndModel);
		spinEnd.setEditor(new JSpinner.DateEditor(spinEnd, "dd/MM/yyyy"));
		lblEnd.setLabelFor(spinEnd);
		contentPane.add(lblEnd, newConstraints(5, 0, 1, false));
		contentPane.add(spinEnd, newConstraints(5, 1, 1, false));		
	}

	/**
	 * Creates the output for the startdate
	 * @param contentPane
	 */
	private void createStartDateInput(Container contentPane) {
		JLabel lblStart = new JLabel("Start date:");
		spinStartModel = new SpinnerDateModel(instance.getStart().getTime(), tasks.getStartDate().getTime(), tasks.getEndDate().getTime(), Calendar.DAY_OF_YEAR);
		JSpinner spinStart = new JSpinner(spinStartModel);
		spinStart.setEditor(new JSpinner.DateEditor(spinStart, "dd/MM/yyyy"));
		lblStart.setLabelFor(spinStart);
		contentPane.add(lblStart, newConstraints(4, 0, 1, false));
		contentPane.add(spinStart, newConstraints(4, 1, 1, false));		
	}

	/**
	 * Creates the color picker
	 * @param contentPanePane
	 */
	private void createColorButton(Container contentPanePane) {
		btnColour = new JColorButton(instance.getDefinition().getColor());
		btnColour.setEnabled(false);
		contentPanePane.add(btnColour, newConstraints(1, 3, 1, false));
	}

	
	/**
	 * Finds if a subtask falls on an existing instance and enlarges the instance if so
	 */
	private boolean checkSubTaskInstance(Calendar start, Calendar end){
		boolean out = false;
		for(int i = 0; i < instance.getDefinition().getOwner().getNumberOfInstances(); i++){
			TaskInstance inst = instance.getDefinition().getOwner().getInstance(i);
			// {-[*-*-*-*-*]-}
			if(start.compareTo(inst.getStart()) >= 0 && end.compareTo(inst.getEnd()) <= 0){
				out = true;
			}// [****{*-]------}
			if(start.compareTo(inst.getStart()) <= 0 && start.compareTo(inst.getEnd()) <=0 && end.compareTo(inst.getStart())>=0){
				inst.setStart(start);
				out = true;
			}// {-------[-*}*******]
			if((start.compareTo(inst.getEnd()) <= 0 && end.compareTo(inst.getEnd()) >=0)){
				inst.setEnd(end);
				out = true;
			}
		}
		return out;
	}
	
	/**
	 * When complete, hide the window.
	 */
	public void dialogDone() {
		this.setVisible(false);
	}
	
	/**
	 * All the actions this provides.
	 * @author Daniel McKenzie
	 */
	class DialogActionListener extends ChartActionListener {
		
		/**
		 * Checks if the attribute checkbox has changed
		 * @param arg0 ActionEvent
		 */
		void doShowAttribute(ActionEvent arg0){
			addAttribute.setEnabled(chbattributes.isSelected());
			txtAttributes.setEnabled(chbattributes.isSelected());		
		}
		
		/**
		 * Adds the attributes in the attribute textarea
		 * @param arg0 ActionEvent
		 */		
		void doAddAttribute(ActionEvent arg0){
			String input = txtAttributes.getText();
			attributeArray = input.split(",");
			txtAttributes.setText("");
			txtAttributes.setEnabled(false);
			addAttribute.setEnabled(false);
			
			if(attributeArray != null){
				for(int i = 0; i < attributeArray.length; i++){
					instance.getDefinition().addAttribute(attributeArray[i]);
				}
			}
			
			attributeArray = new String[0];
			createSubTaskAttributes();
		}
		
		/**
		 * Clears the attributes of a subtask
		 */
		void doClearAttributes(ActionEvent arg0){
			instance.getDefinition().getAttributes().clear();
			createSubTaskAttributes();
		}
		
		/**
		 * When OK'd, edits the subtask and/or instance.
		 * Hide the window when done.
		 * @param arg0 Action args (not used)
		 */
		void doDone(ActionEvent arg0) {
			instance.setName(txtSubtask.getText());
			Calendar start = new GregorianCalendar();
			start.setTime(spinStartModel.getDate());
			instance.setStart(start);
			
			Calendar end = new GregorianCalendar();
			end.setTime(spinEndModel.getDate());
			instance.setEnd(end);
			
			checkSubTaskInstance(start, end);
			tasks.revalidate();
			
			dialogDone();
		}
		
		/**
		 * Deletes the subtask instance
		 */
		void doDelete(ActionEvent arg0){
			instance.getDefinition().removeInstance(instance);
			dialogDone();
		}
		
		/**
		 * When Cancelled, hide the window.
		 * @param arg0 Action args (not used)
		 */
		void doCancel(ActionEvent arg0) {
			dialogDone();
		}
	}
	
}
