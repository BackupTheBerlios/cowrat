package ganttchart.editor.gui;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ganttchart.tasklib.Task;
import ganttchart.tasklib.TaskInstance;
import ganttchart.tasklib.TaskPool;

import javax.swing.*;

/**
 * This is the instance editor (when you add an instance). It will ask
 * for a start date and time, create the instance and add it to the
 * task.
 * @author Daniel McKenzie
 *
 */
public class InstanceEditor extends JDialog {

	TaskInstance instance;
	DialogActionListener listener;
	SpinnerDateModel spinEndModel;
	SpinnerDateModel spinStartModel; 
	Task task;
	TaskPool tasks;
	InstanceEditor selfRef = this;
	
	/**
	 * Constructs and sets up the window.
	 * @param owner The owning frame
	 * @param task The task we are adding this instance to
	 */
	public InstanceEditor(Frame owner, Task task, TaskPool tasks) {
		// Run the original constructor 
		super(owner, "New Instance", true);
		// Set some basics
		this.setResizable(false);
		this.setLocationByPlatform(true);
        //Create and set up the window.
		this.task = task;
		this.tasks = tasks;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        listener = new DialogActionListener();
        createOptions(getContentPane());
        instance = new TaskInstance(this.task);

        
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
		c.fill = GridBagConstraints.HORIZONTAL;
		
		if (label) {
			c.anchor = GridBagConstraints.FIRST_LINE_END;
		}
		
		return c;
	}
	
	/**
	 * Helper function to create a button.
	 * @param label The text label on the button
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
	 * Lays out the window.
	 * @param contentPane The content pane to layout.
	 */
	private void createOptions(Container contentPane) {
		contentPane.setLayout(new GridBagLayout());
		
		createStartDateInput(contentPane);
		createEndDateInput(contentPane);
		
		// Separate between main controls and dialog controls
		JSeparator sepButtonSep = new JSeparator();
		GridBagConstraints g = newConstraints(2, 0, 3, false);
		g.insets = new Insets(4, 0, 4, 0);
		contentPane.add(sepButtonSep, g);
		
		// OK button
		JButton btnOK = createButton("OK", "Done");
		contentPane.add(btnOK, newConstraints(3, 0, 1, false));
		
		// Cancel button
		JButton btnCancel = createButton("Cancel", "Cancel");
		contentPane.add(btnCancel, newConstraints(3, 1, 1, false));
	}
	
	private void createEndDateInput(Container contentPane) {
		JLabel lblEnd = new JLabel("End date:");
		spinEndModel = new SpinnerDateModel(tasks.getEndDate().getTime(), tasks.getStartDate().getTime(), tasks.getEndDate().getTime(), Calendar.DAY_OF_YEAR);
		JSpinner spinEnd = new JSpinner(spinEndModel);
		spinEnd.setEditor(new JSpinner.DateEditor(spinEnd, "dd/MM/yyyy"));
		lblEnd.setLabelFor(spinEnd);
		contentPane.add(lblEnd, newConstraints(1, 0, 1, true));
		contentPane.add(spinEnd, newConstraints(1, 1, 1, false));		
	}

	private void createStartDateInput(Container contentPane) {
		JLabel lblStart = new JLabel("Start date:");
		spinStartModel = new SpinnerDateModel(tasks.getStartDate().getTime(), tasks.getStartDate().getTime(), tasks.getEndDate().getTime(), Calendar.DAY_OF_YEAR);
		JSpinner spinStart = new JSpinner(spinStartModel);
		spinStart.setEditor(new JSpinner.DateEditor(spinStart, "dd/MM/yyyy"));
		lblStart.setLabelFor(spinStart);
		contentPane.add(lblStart, newConstraints(0, 0, 1, true));
		contentPane.add(spinStart, newConstraints(0, 1, 1, false));
	}

	/**
	 * Hides the window to dispose of it.
	 */
	public void dialogDone() {
		this.setVisible(false);
	}
	
	/**
	 * Performs all the actions in this window
	 * @author Daniel McKenzie
	 *
	 */
	class DialogActionListener extends ChartActionListener {
		
		/**
		 * When the window is done, create a new instance and add
		 * it to the task. Then close the window.
		 * @param arg0 Action arguments (not used)
		 */
		public void doDone(ActionEvent arg0) {
			Calendar start = new GregorianCalendar();
			start.setTime(spinStartModel.getDate());
			
			Calendar end = new GregorianCalendar();
			end.setTime(spinEndModel.getDate());
			
			instance.setStart(start); instance.setEnd(end);
			task.addInstance(instance);
			dialogDone();
		}
		
		/**
		 * Just close the window.
		 * @param arg0 Action arguments (not used)
		 */
		public void doCancel(ActionEvent arg0) {
			dialogDone();
		}
		
	}
}
