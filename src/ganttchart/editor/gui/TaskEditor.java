package ganttchart.editor.gui;

import ganttchart.tasklib.*;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.text.DateFormat;

import javax.swing.*;

/**
 * This is the task editor. It is the main interface for creating and or editing
 * a task.
 * @author Daniel McKenzie
 */
public class TaskEditor extends JDialog {
	
	private static final long serialVersionUID = 1L;
	private Task theTask;
	private Task finalisedTask = null;
	
	private TaskPool tasks;
	
	DialogActionListener listener;
	private boolean isNew = false;
	
	
	private JTextField txtTaskName;
	private JComboBox comInstance;
	private JList lstSubTasks;
	private DefaultListModel lstModel;
	private JButton btnOK;
	private JColorButton btnPickColour;
	
	/**
	 * Create a new task editor. Set up the window and get it all ready.
	 * @param owner The owning frame.
	 * @param task The task to edit. If null, will be a new task dialog.
	 */
	public TaskEditor(Frame owner, Task task, TaskPool tasks) {
		super(owner, "Task Editor", true);
		this.setResizable(false);
		this.setLocationByPlatform(true);
		this.tasks = tasks;
		this.setPreferredSize(new Dimension(450,400));

		//Create and set up the window.
		listener = new DialogActionListener();
		
		// If null, create a new task
		if (task == null) {
			theTask = new Task();
			isNew = true;
		} else {
			theTask = task;
		}
		
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        createOptions(getContentPane());
		createInstanceOptions();
		
        //Display the window.
        pack();

        setVisible(true);		
	}
	
	/**
	 * Helper function to create new grid bag constraints.
	 * @param y The y position
	 * @param x The x position
	 * @param width The width of the position
	 * @param label If it is a label, its anchor will be changed to FIRST_LINE_END
	 * @return GridBagConstraints for use with a GridBagLayout
	 */
	private GridBagConstraints newConstraints(int y, int x, int width, boolean label) {
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = width;
		c.ipadx = 1;
		c.ipady = 1;
		c.insets = new Insets(4,4,4,4);
		if (label) {
			c.fill = GridBagConstraints.NONE;
		} else {
			c.fill = GridBagConstraints.HORIZONTAL;
		}
		
		if (label) {
			c.anchor = GridBagConstraints.FIRST_LINE_END;
		}
		
		return c;
	}
	
	/**
	 * Creates a new button.
	 * @param label The label to show
	 * @param action The action command
	 * @return A new button
	 */
	private JButton createButton(String label, String action) {
		JButton btn = new JButton(label);
		btn.addActionListener(listener);
		btn.setActionCommand(action);
		return btn;
	}
	
	/**
	 * Creates all the window options.
	 * @param contentPane Content pane to add the options to.
	 */
	private void createOptions(Container contentPane) {
		contentPane.setLayout(new GridBagLayout());
		
		createTaskInput(contentPane);
		createTaskInstanceWindow(contentPane);
		if(tasks.getDetail())
			createSubTaskList(contentPane);
		
		// If this is a new task, the button is "Cancel"
		// If it is an old task, the button is "Delete"
		String delButtonLabel = "Delete";
		if (isNew) {
			delButtonLabel = "Cancel";
		}
		JButton btnDelete = createButton(delButtonLabel, "Delete");
		btnOK = createButton("Done", "Done");
		if(isNew)
			btnOK.setEnabled(false);

		// Change the Delete button place if it is new or not
		int doneBtnPos = 3;
		if (isNew) {
			doneBtnPos = 2;
		}
		int delBtnPos = 0;
		if (isNew) {
			delBtnPos = 3;
		}
		
		// Add the buttons
		GridBagConstraints k = new GridBagConstraints(doneBtnPos, 5, 1, 1, 0.5, 0.5, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(4,4,4,4), 1, 1);
		contentPane.add(btnOK, k);
		GridBagConstraints d = (GridBagConstraints) k.clone();
		d.gridx = delBtnPos;
		d.anchor = GridBagConstraints.LINE_START;
		contentPane.add(btnDelete, d);
	}
	
	/**
	 * Creates the subtask list
	 * @param contentPane
	 */
	private void createSubTaskList(Container contentPane) {
		JLabel lblSubTasks = new JLabel("Subtasks:");
		lstModel = new DefaultListModel();
		lstSubTasks = new JList(lstModel);
		lstSubTasks.setPreferredSize(new Dimension(150, 200));
		lstSubTasks.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		lstSubTasks.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		lstSubTasks.setVisibleRowCount(-1);

		lblSubTasks.setLabelFor(lstSubTasks);
		
		contentPane.add(lblSubTasks, newConstraints(2, 0, 1, true));
		contentPane.add(lstSubTasks, newConstraints(2, 1, 3, false));
		
		// Subtask buttons
		JButton btnNewSubtask = createButton("New", "NewSubTask");
		contentPane.add(btnNewSubtask, newConstraints(3, 3, 1, true));
		
		// Separator
		JSeparator sepButtonSep = new JSeparator();
		GridBagConstraints g = newConstraints(4, 0, 4, false);
		g.insets = new Insets(4, 0, 4, 0);
		contentPane.add(sepButtonSep, g);
		
	}

	private void createTaskInstanceWindow(Container contentPane) {
		JLabel lblInstance = new JLabel("Instance:");
		comInstance = new JComboBox();
		comInstance.setPreferredSize(new Dimension(170, comInstance.getPreferredSize().height));
		lblInstance.setLabelFor(comInstance);
		comInstance.addActionListener(listener);
		comInstance.setActionCommand("InstanceChange");
		contentPane.add(lblInstance, newConstraints(1, 0, 1, true));
		contentPane.add(comInstance, newConstraints(1, 1, 1, false));
		
		// Instance buttons
		if(!tasks.getDetail()){
			JButton btnNewInstance = createButton("New", "NewInstance");
			contentPane.add(btnNewInstance, newConstraints(1, 2, 1, true));
			JButton btnDelInstance = createButton("Delete", "DeleteInstance");
			contentPane.add(btnDelInstance, newConstraints(1, 3, 1, true));
		}
	}

	/**
	 * Creates the input for a task creation
	 * @param contentPane
	 */
	private void createTaskInput(Container contentPane) {
		JLabel lblTaskName = new JLabel("Task Name:");
		txtTaskName = new JTextField();
		txtTaskName.setText(theTask.getName());
		txtTaskName.setPreferredSize(new Dimension(150, txtTaskName.getPreferredSize().height));
		lblTaskName.setLabelFor(txtTaskName);
		
		contentPane.add(lblTaskName, newConstraints(0, 0, 1, true));
		contentPane.add(txtTaskName, newConstraints(0, 1, 2, false));
		
		// This is for the color button
		btnPickColour = new JColorButton(theTask.getColor());
		btnPickColour.addActionListener(listener);
		btnPickColour.setActionCommand("PickColour");
		contentPane.add(btnPickColour, newConstraints(0, 3, 1, false));
	}

	/**
	 * Update the combo box with all the instance options.
	 */
	private void createInstanceOptions() {
		comInstance.removeAllItems();
		DateFormat dfmt = DateFormat.getDateInstance();
		for (int i = 0; i < theTask.getNumberOfInstances(); i++) {
			TaskInstance inst = theTask.getInstance(i);
			String fmt = dfmt.format(inst.getStart().getTime()) + " - " + dfmt.format(inst.getEnd().getTime());
			comInstance.addItem(fmt);
		}
	}
	
	/**
	 * Update the sub task list with all the subtask instances in the instance.
	 */
	private void createSubtaskOptions() {
		lstModel.clear();
		if (theTask.getNumberOfInstances() > 0) {
			int selectedInstance = comInstance.getSelectedIndex();
			if (selectedInstance < 0)
				selectedInstance = 0;
			TaskInstance inst = theTask.getInstance(selectedInstance);
			for (int i = 0; i < inst.getNumberOfSubTaskInstances(); i++) {
				SubTaskInstance subTask = inst.getSubTaskInstance(i);
				String line = "SubTask: " + subTask.getDefinitionName();
				DateFormat datefmt = DateFormat.getDateInstance();
				line = line + " - From: " + datefmt.format(subTask.getStart().getTime());
				line = line + " -  Till: " + datefmt.format(subTask.getEnd().getTime());
				lstModel.addElement(line);
			}
		}
	}
	
	/**
	 * Getter and setter for finalisedtask
	 */
	
	public void setFinalisedTask(Task t){
		finalisedTask = t;
	}
	
	public Task getFinalisedTask(){
		return finalisedTask;
	}
	
	
	/**
	 * Hide the window.
	 */
	private void dialogDone() {
		this.setVisible(false);
	}
	
	/**
	 * All the options in this dialog.
	 * @author Daniel McKenzie
	 */
	class DialogActionListener extends ChartActionListener {

		/**
		 * Open a pick colour dialog and update the colour.
		 * @param arg0 Action args, gets the button that loaded it
		 */
		void doPickColour(ActionEvent arg0) {
		/*	JComponent source = (JComponent) arg0.getSource();
			Color newColour = JColorChooser.showDialog(null, "Pick Task Colour", ((JColorButton) source).getColor());
			if (newColour != null) {
				((JColorButton) source).setColor(newColour);
				theTask.setColor(newColour.getRGB());
			}*/
			new TaskColorPicker(null, tasks, theTask);
			btnPickColour.setColor(theTask.getColor());
			btnPickColour.repaint();
		}
		
		/**
		 * Opens the new instance dialog box.
		 * @param arg0 Action args (not used)
		 */
		void doNewInstance(ActionEvent arg0) {
			if(!txtTaskName.getText().equals("")){
				if(isNew && !tasks.contain(txtTaskName.getText())){
					theTask.setName(txtTaskName.getText());
					tasks.addTask(theTask);
				}
				new InstanceEditor(null, theTask, tasks);
				createInstanceOptions();
				btnOK.setEnabled(true);
			}else
				JOptionPane.showMessageDialog(null, "Please specify the task name");
		}
				
		/**
		 * When done, get the task ready to add to the pool and hide the window.
		 * @param arg0 Action args (not used)
		 */
		void doDone(ActionEvent arg0) {
			theTask.setName(txtTaskName.getText());
			finalisedTask = theTask;
			if(!tasks.getDetail()){
				if(!txtTaskName.getText().equals("")){
						if(isNew && !tasks.contain(txtTaskName.getText())){
							theTask.setName(txtTaskName.getText());
							tasks.addTask(theTask);
					}
				}
			}
			dialogDone();
		}
		
		/**
		 * This hides the window. It should delete the task, but at the moment
		 * it doesn't.
		 * @param arg0 Action args (not used)
		 */
		void doDelete(ActionEvent arg0) {
			tasks.delete(theTask);
			dialogDone();
		}
		
		/**
		 * Opens the sub task dialog and updates the subtask options.
		 * @param arg0 Action args (not used)
		 */
		void doNewSubTask(ActionEvent arg0) {
			if(!txtTaskName.getText().equals("")){
				if(isNew && !tasks.contain(txtTaskName.getText())){
					theTask.setName(txtTaskName.getText());
					tasks.addTask(theTask);
				}
				new NewSubTaskEditor(null, theTask, tasks);
				createInstanceOptions();
				btnOK.setEnabled(true);
				createSubtaskOptions();
			}else
				JOptionPane.showMessageDialog(null, "Please specify the task name");

		}
		
		/**
		 * If the instance combo is changed, change the subtask options.
		 * @param arg0 Action args (not used)
		 */
		void doInstanceChange(ActionEvent arg0) {
			if(tasks.getDetail()){
				createSubtaskOptions();
			}
		}
	}
}
