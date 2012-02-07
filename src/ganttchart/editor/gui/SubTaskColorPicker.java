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


public class SubTaskColorPicker extends JDialog{


	private Task task;
	private TaskPool tasks;
	private ArrayList<TaskColor> colors;
	private DialogActionListener listener;
	private JButton btnOK, btnCancel;
	private JList lstColors;
	private DefaultListModel lstModel;
	private JColorButton but;
	private Color col;
	private SubTaskListListener clistener;

	
	public SubTaskColorPicker(Frame owner, Task task, TaskPool tasks){
		super(owner, "Pick a color", true);
		this.task = task;
		this.tasks = tasks;
		
		colors = getColors(task.getColor());
		initSubColor(colors);
		
		listener = new DialogActionListener();
		clistener = new SubTaskListListener();
		
		this.setResizable(false);
		this.setLocationByPlatform(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
		createOptions(getContentPane());
		
        pack();
        setVisible(true);
	}

	private void initSubColor(ArrayList<TaskColor> colors2) {
		for(int i = 0; i < colors.size(); i++){
			if(colors.get(i).isInUse()){
				colors.remove(i);
			}
		}
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

	private void createOptions(Container contentPane) {
		contentPane.setLayout(new GridBagLayout());

		createList(contentPane);
		createColorOptions();
		
		btnOK = new JButton("OK");
		btnOK.addActionListener(listener);
		btnOK.setActionCommand("Done");
		
		contentPane.add(btnOK, newConstraints(2, 0, 1, false));
		but = new JColorButton(colors.get(0).getColor());
		but.setEnabled(false);
		contentPane.add(but, newConstraints(0, 0, 1, false));
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(listener);
		btnCancel.setActionCommand("Cancel");
		contentPane.add(btnCancel, newConstraints(2, 1, 1, false));
	}
	
	private void createList(Container contentPane) {
		lstModel = new DefaultListModel();
		lstColors = new JList(lstModel);
		lstColors.setPreferredSize(new Dimension(150, 200));
		lstColors.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		lstColors.setLayoutOrientation(JList.VERTICAL_WRAP);
		lstColors.setVisibleRowCount(-1);	
		lstColors.addListSelectionListener(clistener);
		contentPane.add(lstColors, newConstraints(1, 0, 1, false));
		
	}

	/**
	 * Update the sub task list with all the subtask instances in the instance.
	 */
	private void createColorOptions() {
		lstModel.clear();
		for(int i = 0; i < colors.size(); i++){
			if(!colors.get(i).isInUse())
				lstModel.addElement(colors.get(i).getName());
		}
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
	 * returns the set color
	 * @return
	 */
	public Color getColor(){
		return col;
	}
	
	/**
	 * Hide the window.
	 */
	private void dialogDone() {
		this.setVisible(false);
	}
	
	class DialogActionListener extends ChartActionListener {
		
		/**
		 * Sets the chosen color as subtaskcolor
		 * @param arg0
		 */
		public void doDone(ActionEvent arg0){
			int sel = lstColors.getSelectedIndex();
			col = colors.get(sel).getColor();
			colors.get(sel).setInUse(true);
			dialogDone();
		}
		
		/**
		 * Frees up the color and cancels the operation
		 * @param arg0
		 */
		public void doCancel(ActionEvent arg0){
			int sel = lstColors.getSelectedIndex();
			if(sel >= 0 )
				colors.get(sel).setInUse(false);
			dialogDone();			
		}
	}
	
	class SubTaskListListener implements ListSelectionListener{

		@Override
		public void valueChanged(ListSelectionEvent e) {
			but.setColor(colors.get(lstColors.getSelectedIndex()).getColor());
			but.repaint();
		}
	}
}
