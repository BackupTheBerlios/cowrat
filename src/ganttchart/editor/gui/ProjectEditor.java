package ganttchart.editor.gui;

import ganttchart.tasklib.TaskPool;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;

public class ProjectEditor extends JDialog {
	
	DialogActionListener listener;
	private JCheckBox highDetail, lowDetail;
	private boolean detail;
	private JButton btnStart;
	private boolean started;
	private SpinnerDateModel spinEndModel, spinStartModel;
	private TaskPool tasks;
	private JTextField txtName;


	/**
	 * Constructs and sets up the window.
	 * @param owner The owning frame
	 **/
	public ProjectEditor(Frame owner, TaskPool tasks){
		super(owner, "Project information", true);
        listener = new DialogActionListener();
        started = false;
        this.tasks = tasks;
        
		this.setResizable(false);
		this.setLocationByPlatform(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        createOptions(getContentPane());
        
        pack();
        setVisible(true);
	}
	
	public boolean getDetail(){
		return detail;
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
	
	private void dialogDone() {
		this.setVisible(false);
	}
	
	/**
	 * Lays out the window.
	 * @param contentPane The content pane to layout.
	 */
	private void createOptions(Container contentPane) {
		contentPane.setLayout(new GridBagLayout());
		
		createProjectNameOption(contentPane);
		createDetailOptions(contentPane);
		createStartDateInput(contentPane);
		createEndDateInput(contentPane);
		
		if(tasks != null){
			btnStart = createButton("Edit project", "Start");
			btnStart.setEnabled(true);
		}
		else{
			btnStart = createButton("Start project", "Start");
			btnStart.setEnabled(false);
		}
		
		contentPane.add(btnStart, newConstraints(6,0,1,true));
	}
	
	private void createProjectNameOption(Container contentPane) {
		JLabel lblTaskName = new JLabel("Project name:");
		txtName = new JTextField();
		if(tasks != null)
			txtName.setText(tasks.getProjectName());
		
		txtName.setPreferredSize(new Dimension(150, txtName.getPreferredSize().height));
		lblTaskName.setLabelFor(txtName);	
		
		contentPane.add(lblTaskName, newConstraints(1, 0, 1, true));
		contentPane.add(txtName, newConstraints(1, 1, 2, false));
	}

	private void createDetailOptions(Container contentPane) {
		JLabel lbl = new JLabel("Options for the project: ");
		contentPane.add(lbl, newConstraints(0,0,1,true));
		
		highDetail = UIFactory.createToolBarCheckBox(listener, "HighDetail", "Use high detail", "Use mutiple levels (subtasks)");
		lowDetail = UIFactory.createToolBarCheckBox(listener, "LowDetail", "Use low detail", "Use one level (only tasks)");
		
		if(tasks != null){
			if(tasks.getDetail())
				highDetail.setSelected(true);
			else
				lowDetail.setSelected(true);
		}
		contentPane.add(highDetail, newConstraints(2,0,1,true));
		contentPane.add(lowDetail, newConstraints(3,0,1,true));		
	}
	
	/**
	 * Creates the output for the end date
	 * @param contentPane
	 */
	private void createEndDateInput(Container contentPane) {
		JLabel lblEnd = new JLabel("End date:");
		if(tasks != null)
			spinEndModel = new SpinnerDateModel(tasks.getEndDate().getTime(), null, null,Calendar.DAY_OF_YEAR);
		else
			spinEndModel = new SpinnerDateModel();
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
		if(tasks != null)
			spinStartModel = new SpinnerDateModel(tasks.getStartDate().getTime(), null, null,Calendar.DAY_OF_YEAR);
		else
			spinStartModel = new SpinnerDateModel();
		
		JSpinner spinStart = new JSpinner(spinStartModel);
		spinStart.setEditor(new JSpinner.DateEditor(spinStart, "dd/MM/yyyy"));
		lblStart.setLabelFor(spinStart);
		contentPane.add(lblStart, newConstraints(4, 0, 1, false));
		contentPane.add(spinStart, newConstraints(4, 1, 1, false));		
	}

	class DialogActionListener extends ChartActionListener {
		
		void doHighDetail(ActionEvent arg0) {
			if(highDetail.isSelected()){
				lowDetail.setSelected(false);
				btnStart.setEnabled(true);
			}else{
				if(!lowDetail.isSelected())
					btnStart.setEnabled(false);
			}
		}
		
		void doLowDetail(ActionEvent arg0){
			if(lowDetail.isSelected()){
				highDetail.setSelected(false);
				btnStart.setEnabled(true);
			}else{
				if(!highDetail.isSelected())
					btnStart.setEnabled(false);
			}
		}	
		
		void doStart(ActionEvent arg0){
			if(highDetail.isSelected())
				detail = true;
			else if(lowDetail.isSelected())
				detail = false;
			
			if(txtName.getText().compareTo("") != 0 && txtName.getText() != null){
				if(tasks != null){
					tasks.setProjectName(txtName.getText());
					tasks.convertToDetail(detail);
				}
				else{
					tasks = new TaskPool(txtName.getText());
					tasks.setDetail(detail);
				}
				
				Calendar start = new GregorianCalendar();
				
				start.setTime(spinStartModel.getDate());
				tasks.setStartDate(start);
				
				Calendar end = new GregorianCalendar();
				end.setTime(spinEndModel.getDate());
				tasks.setEndDate(end);
				
				started = true;
				dialogDone();
			}else
				JOptionPane.showMessageDialog(null, "Please give a name for the project");

		}
	}

	public TaskPool getTaskPool(){
		return tasks;
	}
	
	public boolean getStarted() {
		return started;
	}
}
