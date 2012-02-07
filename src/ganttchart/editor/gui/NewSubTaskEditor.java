package ganttchart.editor.gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ganttchart.tasklib.SubTask;
import ganttchart.tasklib.Task;
import ganttchart.tasklib.TaskInstance;
import ganttchart.tasklib.TaskPool;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

/**
 * This is the subtask editor. It will ask for a subtask, start date and time.
 * It will create all this if necessary and then apply it to the task.
 * @author Daniel McKenzie
 */
public class NewSubTaskEditor extends JDialog {
	
	private TaskInstance instance;
	private Task task;
	private TaskPool tasks;
	private DialogActionListener listener;
	private TextFieldFocusListener flistener;
	
	private boolean attChanged = false;
	private JComboBox comDefinitions;
	private JTextField txtSubtask;
	private JColorButton btnNewColour, btnOldColour;
	private JRadioButton radOld, radNew;
	private SpinnerDateModel spinEndModel, spinStartModel;
	private JButton addAttribute, clearAttribute;
	private JTextArea txtAttributes, txtAddedAtts;
	private JCheckBox chbattributes;
	private String[] attributeArray;
	

	/**
	 * Constructor will create the dialog window.
	 * @param owner Owner frame.
	 * @param theInstance The task instance this is going into.
	 * @param theTask The task this is going into.
	 */
	
	public NewSubTaskEditor(Frame owner, Task theTask, TaskPool tasks) {
		super(owner, "New Sub Task", true);
		this.setResizable(false);
		this.setLocationByPlatform(true);
		task = theTask;
		this.tasks = tasks;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        listener = new DialogActionListener();
        flistener = new TextFieldFocusListener();
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

		createPreviousTaskOption(contentPane);
		createNewTaskOption(contentPane);
		chbattributes = UIFactory.createToolBarCheckBox(listener, "ShowAttribute", "Add an attribute to the Subtask", "Add attributes (separated by ',')");

		contentPane.add(chbattributes, newConstraints(7, 0, 1, true));
		// Add the radios to a button group
		ButtonGroup grp = new ButtonGroup();
		grp.add(radOld);
		grp.add(radNew);
		createStartDateInput(contentPane);
		createEndDateInput(contentPane);

		// Separator
		JSeparator sepButtonSep = new JSeparator();
		GridBagConstraints g = newConstraints(6, 0, 4, false);
		g.insets = new Insets(4, 0, 4, 0);
		contentPane.add(sepButtonSep, g);
		
		createButtons(contentPane);
		createAttributeOptions(contentPane);
		// Fill out the subtasks options, otherwise disable the combobox
		if (task.getNumberOfSubTasks() > 0) {
			setComboSubtasks(comDefinitions);
			radOld.setSelected(true);
			createSubTaskAttributes();
		} else {
			comDefinitions.setEnabled(false);
			radOld.setEnabled(false);
			radNew.setSelected(true);
			txtAttributes.setText("");
		}

	}
	
	/**
	 * Creates the subtask attributes
	 */
	private void createSubTaskAttributes() {
		if(radOld.isSelected() || attChanged){
			SubTask sub = null;
			if(comDefinitions.getSelectedIndex() >= 0)
				sub = task.getSubTask(comDefinitions.getSelectedIndex());
				String atts = "";
				if(sub != null){
					if(sub.getAttributes().size() > 0){
						atts = sub.getAttributes().get(0);
						for(int i = 1; i < sub.getAttributes().size(); i++){
							atts += "," + sub.getAttributes().get(i);
						}
					}
				}else{
					if(attributeArray.length > 0){
						atts = attributeArray[0];
						for(int i = 1; i < attributeArray.length; i++){
							atts += "," + attributeArray[i];
						}
					}				
				}
				txtAddedAtts.setText(atts);
		}else if(radNew.isSelected() && !attChanged)
			txtAddedAtts.setText("");
	}
	
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
		
		clearAttribute = new JButton("Clear");
		clearAttribute.addActionListener(listener);
		clearAttribute.setActionCommand("ClearAttributes");
		
		contentPane.add(pane, newConstraints(8, 0, 1, false));
		contentPane.add(pane2, newConstraints(8, 2, 1, false));
		contentPane.add(addAttribute, newConstraints(8, 1, 1, false));
		contentPane.add(clearAttribute, newConstraints(9, 1, 1, false));
		txtAttributes.setEnabled(false); addAttribute.setEnabled(false);
	}

	private void createButtons(Container contentPane) {
		JButton btnOK = new JButton("OK");
		btnOK.addActionListener(listener);
		btnOK.setActionCommand("Done");
		btnOK.setPreferredSize(new Dimension(150, btnOK.getPreferredSize().height));
		contentPane.add(btnOK, newConstraints(10, 2, 1, false));
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(listener);
		btnCancel.setActionCommand("Cancel");
		contentPane.add(btnCancel, newConstraints(10, 3, 1, false));
	}

	private void createEndDateInput(Container contentPane) {
		JLabel lblEnd = new JLabel("End date:");
		spinEndModel = new SpinnerDateModel(tasks.getEndDate().getTime(), tasks.getStartDate().getTime(), tasks.getEndDate().getTime(), Calendar.DAY_OF_YEAR);
		JSpinner spinEnd = new JSpinner(spinEndModel);
		spinEnd.setEditor(new JSpinner.DateEditor(spinEnd, "dd/MM/yyyy"));
		lblEnd.setLabelFor(spinEnd);
		contentPane.add(lblEnd, newConstraints(5, 0, 1, false));
		contentPane.add(spinEnd, newConstraints(5, 1, 1, false));		
	}

	private void createStartDateInput(Container contentPane) {
		JLabel lblStart = new JLabel("Start date:");
		spinStartModel = new SpinnerDateModel(tasks.getStartDate().getTime(), tasks.getStartDate().getTime(), tasks.getEndDate().getTime(), Calendar.DAY_OF_YEAR);
		JSpinner spinStart = new JSpinner(spinStartModel);
		spinStart.setEditor(new JSpinner.DateEditor(spinStart, "dd/MM/yyyy"));
		lblStart.setLabelFor(spinStart);
		contentPane.add(lblStart, newConstraints(4, 0, 1, false));
		contentPane.add(spinStart, newConstraints(4, 1, 1, false));		
	}

	private void createNewTaskOption(Container contentPane) {
		radNew = new JRadioButton("Create a new subtask");
		radNew.addActionListener(listener);
		radNew.setActionCommand("RadNewChanged");

		txtSubtask = new JTextField();
		txtSubtask.addFocusListener(flistener);
		btnNewColour = new JColorButton(task.getColor());
		btnNewColour.addActionListener(listener);
		btnNewColour.setActionCommand("NewColour");
		
		contentPane.add(radNew, newConstraints(2, 0, 3, true));
		contentPane.add(txtSubtask, newConstraints(3, 1, 2, false));
		contentPane.add(btnNewColour, newConstraints(3, 3, 1, false));
	}

	private void createPreviousTaskOption(Container contentPanePane) {
		radOld = new JRadioButton("Use a previous subtask");
		radOld.addActionListener(listener);
		radOld.setActionCommand("RadOldChanged");
		
		comDefinitions = new JComboBox();
		comDefinitions.addActionListener(listener);
		comDefinitions.setActionCommand("ComboChanged");
		btnOldColour = new JColorButton(Color.WHITE);
		btnOldColour.setEnabled(false);
		
		contentPanePane.add(radOld, newConstraints(0, 0, 3, true));
		contentPanePane.add(comDefinitions, newConstraints(1, 1, 2, false));
		contentPanePane.add(btnOldColour, newConstraints(1, 3, 1, false));
	}

	/**
	 * Add all the subtask options to the combo box.
	 * @param combo The combo box to add these options to.
	 */
	private void setComboSubtasks(JComboBox combo) {
		for (int i = 0; i < task.getNumberOfSubTasks(); i++) {
			combo.addItem(task.getSubTask(i).getName());
		}
	}
	
	/**
	 * Finds if a subtask falls on an existing instance and enlarges the instance if so
	 */
	private TaskInstance checkSubtaskInstance(Calendar start, Calendar end){
		TaskInstance out = null;
		for(int i = 0; i < task.getNumberOfInstances(); i++){
			TaskInstance inst = task.getInstance(i);
			// {-[*-*-*-*-*]-}
			if(start.compareTo(inst.getStart()) >= 0 && end.compareTo(inst.getEnd()) <= 0){
				out = inst;
			}// [****{*-]------}
			if(start.compareTo(inst.getStart()) <= 0 && start.compareTo(inst.getEnd()) <= 0 && end.compareTo(inst.getEnd()) >= 0){
				inst.setStart(start);
				out = inst;
			}// {-------[-*}*******]
			if((start.compareTo(inst.getEnd()) <= 0 && end.compareTo(inst.getEnd()) >=0)){
				inst.setEnd(end);
				out = inst;
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
			attChanged = true;
			createSubTaskAttributes();
		}

		/**
		 * Update the button colour when the combo box is changed.
		 * @param arg0 Action args (not used)
		 */
		void doComboChanged(ActionEvent arg0) {
			SubTask def = task.getSubTask(comDefinitions.getSelectedIndex());
			btnOldColour.setColor(def.getColor());
			btnOldColour.repaint();
			createSubTaskAttributes();
		}
		
		/**
		 * These 2 methods react to the subtask option old/new
		 * @param arg0 ActionEvent
		 */
		void doRadOldChanged(ActionEvent arg0){
			createSubTaskAttributes();
		}
		
		void doRadNewChanged(ActionEvent arg0){
			createSubTaskAttributes();
		}
		
		/**
		 * When the colour button is clicked, open up a colour chooser and update it.
		 * @param arg0 Action args (not used)
		 */
		void doNewColour(ActionEvent arg0) {
			SubTaskColorPicker picker = new SubTaskColorPicker(null, task, tasks);
			btnNewColour.setColor(picker.getColor());
		}
				
		/**
		 * When OK'd, create the subtask and/or instance and add it to the task.
		 * Hide the window when done.
		 * @param arg0 Action args (not used)
		 */
		void doDone(ActionEvent arg0) {
				Calendar start = new GregorianCalendar();
				start.setTime(spinStartModel.getDate());
				task.setDateFields(start);
				Calendar end = new GregorianCalendar();
				end.setTime(spinEndModel.getDate());
				task.setDateFields(end);
				instance = checkSubtaskInstance(start, end);
				
				if(instance == null){
					instance = new TaskInstance(task);
					instance.setStart(start); instance.setEnd(end);
					task.addInstance(instance);
				}
				
				if (radOld.isSelected()) {
					SubTask sub = task.getSubTask(comDefinitions.getSelectedIndex());
					sub.addSubTaskInstance(start, end);
					if(attributeArray != null){
						sub.getAttributes().clear();
						for(int i = 0; i < attributeArray.length; i++){
							sub.addAttribute(attributeArray[i]);
						}
					}
					dialogDone();
				} else {
					if(!txtSubtask.getText().equals("")){
						if(btnNewColour.getColor().equals(task.getColor()))
							JOptionPane.showMessageDialog(null, "Please select a SubTask color");
						else{
							task.addSubTaskWithInstance(txtSubtask.getText(), btnNewColour.getColor(), start, end, attributeArray);
							dialogDone();
						}
					}else
						JOptionPane.showMessageDialog(null, "Please specify a SubTask name");
				}
		}
		
		/**
		 * When Cancelled, hide the window.
		 * @param arg0 Action args (not used)
		 */
		void doCancel(ActionEvent arg0) {
			dialogDone();
		}
	}	
	
	class TextFieldFocusListener implements FocusListener{

		@Override
		public void focusGained(FocusEvent e) {
			radOld.setSelected(false);
			radNew.setSelected(true);
			repaint();
		}

		@Override
		public void focusLost(FocusEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
