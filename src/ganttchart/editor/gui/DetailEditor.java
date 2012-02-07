package ganttchart.editor.gui;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class DetailEditor extends JDialog {
	
	DialogActionListener listener;
	private JCheckBox highDetail, lowDetail;
	private boolean detail;
	private JButton btnStart;
	private boolean started;


	/**
	 * Constructs and sets up the window.
	 * @param owner The owning frame
	 **/
	public DetailEditor(Frame owner){
		super(owner, "Detail select", true);
        listener = new DialogActionListener();
        started = false;
        
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

		JLabel lbl = new JLabel("Please select the level of detail in your graph: ");
		contentPane.add(lbl, newConstraints(0,0,1,true));
		
		highDetail = UIFactory.createToolBarCheckBox(listener, "HighDetail", "Use high detail", "Use mutiple levels (subtasks)");
		lowDetail = UIFactory.createToolBarCheckBox(listener, "LowDetail", "Use low detail", "Use one level (only tasks)");
		contentPane.add(highDetail, newConstraints(1,0,1,true));
		contentPane.add(lowDetail, newConstraints(2,0,1,true));
		
		btnStart = createButton("Start project", "Start");
		btnStart.setEnabled(false);
		contentPane.add(btnStart, newConstraints(3,0,1,true));
	}
	
	/**
	 * All actions get handled in here
	 * @author Win Vanden Broeck
	 *
	 */
	class DialogActionListener extends ChartActionListener {
		
		/**
		 * Sets the detail to high detail (with subtasks)
		 * @param arg0
		 */
		void doHighDetail(ActionEvent arg0) {
			if(highDetail.isSelected()){
				lowDetail.setSelected(false);
				btnStart.setEnabled(true);
			}else{
				if(!lowDetail.isSelected())
					btnStart.setEnabled(false);
			}
		}
		
		/**
		 * Sets the detail on low detail (just tasks)
		 * @param arg0
		 */
		void doLowDetail(ActionEvent arg0){
			if(lowDetail.isSelected()){
				highDetail.setSelected(false);
				btnStart.setEnabled(true);
			}else{
				if(!highDetail.isSelected())
					btnStart.setEnabled(false);
			}
		}	
		
		/**
		 * Sets the detail and closes the dialog
		 * @param arg0
		 */
		void doStart(ActionEvent arg0){
			if(highDetail.isSelected())
				detail = true;
			else if(lowDetail.isSelected())
				detail = false;
			
			started = true;
			dialogDone();
		}
	}

	/**
	 * Gets a boolean to see whether the detail is selected
	 * @return
	 */
	public boolean getStarted() {
		return started;
	}
}
