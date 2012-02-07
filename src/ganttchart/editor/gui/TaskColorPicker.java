package ganttchart.editor.gui;

import ganttchart.editor.gui.colors.TaskColor;
import ganttchart.tasklib.Task;
import ganttchart.tasklib.TaskPool;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class TaskColorPicker extends JDialog{

	private ArrayList<TaskColor> colors;
	
	private Task task;
	private TaskPool tasks;
	
	private DialogActionListener listener;
	private TaskListListener tListener;
	
	private JList lstColors;
	private DefaultListModel lstModel;
	private JButton btnOK, btnCancel;
	private JColorButton btnColour;
	
	public TaskColorPicker(Frame owner, TaskPool tasks, Task task){
		super(owner, "Pick a color", true);
		this.task = task;
		this.tasks = tasks;
		initColors();

		listener = new DialogActionListener();
		tListener = new TaskListListener();
		
		this.setResizable(false);
		this.setLocationByPlatform(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		createOptions(getContentPane());
		pack();
        setVisible(true);
	}

	private void createOptions(Container contentPane) {
		contentPane.setLayout(new GridBagLayout());
		createColorButton(contentPane);
		createColorList(contentPane);
		createColorOptions();
		createButtons(contentPane);
	}

	private void createButtons(Container contentPane) {
		btnOK = new JButton("OK");
		btnOK.addActionListener(listener);
		btnOK.setActionCommand("Done");
		contentPane.add(btnOK, newConstraints(2, 0, 1, false));
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(listener);
		btnCancel.setActionCommand("Cancel");
		contentPane.add(btnCancel, newConstraints(3, 0, 1, false));		
	}

	private void createColorList(Container contentPane) {
		lstModel = new DefaultListModel();
		lstColors = new JList(lstModel);
		lstColors.setPreferredSize(new Dimension(150, 200));
		lstColors.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		lstColors.setLayoutOrientation(JList.VERTICAL_WRAP);
		lstColors.setVisibleRowCount(-1);	
		lstColors.addListSelectionListener(tListener);
		contentPane.add(lstColors, newConstraints(1, 0, 1, false));		
	}

	private void initColors() {
		colors = new ArrayList<TaskColor>();
		for(int i = 0; i < tasks.getTaskColors().size(); i++){
			if(!tasks.getTaskColors().get(i).isInUse())
				colors.add(tasks.getTaskColors().get(i));
		}
	} 
	
	/**
	 * Update the color list with available colors.
	 */
	private void createColorOptions() {
		lstModel.clear();
		for(int i = 0; i < colors.size(); i++){
				lstModel.addElement(colors.get(i).getName());
		}
	}
	
	/**
	 * Creates the color picker
	 * @param contentPanePane
	 */
	private void createColorButton(Container contentPanePane) {
		if(task != null)
			btnColour = new JColorButton(task.getColor());
		else
			btnColour = new JColorButton(colors.get(0).getColor());

		btnColour.setEnabled(false);
		contentPanePane.add(btnColour, newConstraints(1, 3, 1, false));
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
	 * returns the colors for the task color
	 * @param color
	 * @return
	 */
	private ArrayList<TaskColor> getColors(Color color) {
		ArrayList<TaskColor> tasksColors = tasks.getTaskColors();
		for(int i = 0; i < tasksColors.size(); i++){
			if(tasksColors.get(i).getColor().equals(color)){
				return tasksColors.get(i).getSubColors();
			}
		}
		return null;
	}

	/**
	 * Hide the window.
	 */
	private void dialogDone() {
		this.setVisible(false);
	}
	
	class DialogActionListener extends ChartActionListener {
		
		/**
		 * Enters the color as choisen value
		 * @param arg0
		 */
		public void doDone(ActionEvent arg0){
			if(task.getColor() != null){
				TaskColor col = tasks.getTaskColor(task.getColor());
				if(col != null)
					col.setInUse(false);
			}
			int sel = lstColors.getSelectedIndex();
			task.setColor(colors.get(sel).getColor());
			
			ArrayList<TaskColor> subColors = getColors(task.getColor());

			for(int i = 0; i < task.getNumberOfSubTasks(); i++){
				task.getSubTask(i).setColor(subColors.get(i).getColor());
			}
			colors.get(sel).setInUse(true);
			dialogDone();
		}
		
		/**
		 * Cancels the operation and frees the color
		 * @param arg0
		 */
		public void doCancel(ActionEvent arg0){
			int sel = lstColors.getSelectedIndex();
			if(sel >= 0 )
				colors.get(sel).setInUse(false);
			dialogDone();
		}
	}
	
	class TaskListListener implements ListSelectionListener{

		@Override
		public void valueChanged(ListSelectionEvent e) {
			btnColour.setColor(colors.get(lstColors.getSelectedIndex()).getColor());
			btnColour.repaint();
		}
	}
}
