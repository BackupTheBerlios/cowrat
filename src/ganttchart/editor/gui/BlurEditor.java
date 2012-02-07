package ganttchart.editor.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class BlurEditor extends JDialog {
	
	private DialogActionListener listener;
	private JCheckBox showAll, normalBlur, keywordBlur;
	private int blurType;
	private JButton btnStart;
	private JTextField txtAttribute;


	/**
	 * Constructs and sets up the window.
	 * @param owner The owning frame
	 **/
	public BlurEditor(Frame owner){
		super(owner, "Blur select", true);
        listener = new DialogActionListener();
        
		this.setResizable(false);
		this.setLocationByPlatform(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        createOptions(getContentPane());
        
        pack();
        setVisible(true);
	}
	
	/**
	 * Returns the blur type: 0-Show all, 1-Normal blur, 2-Keyword blur
	 * @return blurType The code for which blur to apply
	 */
	public int getBlurType(){
		return blurType;
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
	 * Closes the dialog when finished
	 */
	private void dialogDone() {
		this.setVisible(false);
	}
	
	/**
	 * Lays out the window.
	 * @param contentPane The content pane to layout.
	 */
	private void createOptions(Container contentPane) {
		contentPane.setLayout(new GridBagLayout());

		JLabel lbl = new JLabel("Please select what type of blurring you would like to apply: ");
		contentPane.add(lbl, newConstraints(0,0,1,true));
		createCheckBoxOptions(contentPane);
		createTextFieldOption(contentPane);
		btnStart = createButton("Blur out", "Blur");
		btnStart.setEnabled(false);
		contentPane.add(btnStart, newConstraints(5,0,1,true));
	}
	
	/**
	 * Creates the textfields in the contentpane
	 * @param contentPane The content pane to layout.
	 */
	private void createTextFieldOption(Container contentPane) {
		JLabel lblAttribute = new JLabel("Keyword: ");
		txtAttribute = new JTextField();
		txtAttribute.setPreferredSize(new Dimension(150, txtAttribute.getPreferredSize().height));
		lblAttribute.setLabelFor(txtAttribute);
		txtAttribute.setEnabled(false);
		contentPane.add(lblAttribute, newConstraints(4, 0, 1, true));
		contentPane.add(txtAttribute, newConstraints(4, 1, 2, false));
	}

	/**
	 * Creates the checkBox option
	 * @param contentPane The content pane to layout.
	 */
	private void createCheckBoxOptions(Container contentPane) {
		ButtonGroup grp = new ButtonGroup();
		showAll = UIFactory.createToolBarCheckBox(listener, "ShowAll", "Show All", "Show everything in its normal state");
		normalBlur = UIFactory.createToolBarCheckBox(listener, "NormalBlur", "Blur Normal", "Blurs out all other (sub)tasks");
		keywordBlur = UIFactory.createToolBarCheckBox(listener, "KeyWordBlur", "Blur Keyword", "Blurs out using keywords");
		grp.add(keywordBlur); grp.add(normalBlur); grp.add(showAll);
		contentPane.add(showAll, newConstraints(1, 0, 1, true));
		contentPane.add(normalBlur, newConstraints(2,0,1,true));
		contentPane.add(keywordBlur, newConstraints(3,0,1,true));		
	}

	/**
	 * All actions are directed here
	 * @author Wim Vanden Broeck
	 *
	 */
	class DialogActionListener extends ChartActionListener {
		
		/**
		 * Shows all tasks and subtasks normally
		 * @param arg0
		 */
		void doShowAll(ActionEvent arg0){
			if(showAll.isSelected()){
				btnStart.setEnabled(true);
				txtAttribute.setText("");
				txtAttribute.setEnabled(false);
			}			
		}
		
		/**
		 * Standard blur, blurs out everything except the selected subtask
		 * @param arg0
		 */
		void doNormalBlur(ActionEvent arg0) {
			if(normalBlur.isSelected()){
				btnStart.setEnabled(true);
				txtAttribute.setText("");
				txtAttribute.setEnabled(false);
			}
		}
		
		/**
		 * Blurs using keyword
		 * @param arg0
		 */
		void doKeyWordBlur(ActionEvent arg0){
			if(keywordBlur.isSelected()){
				txtAttribute.setEnabled(true);
				btnStart.setEnabled(true);
			}
		}	
		
		/**
		 * Executes the blur and closes the dialog
		 * @param arg0
		 */
		void doBlur(ActionEvent arg0){
			if(keywordBlur.isSelected())
				blurType = 2;
			else if(normalBlur.isSelected())
				blurType = 1;
			else if(showAll.isSelected())
				blurType = 0;
			
			dialogDone();
		}
	}

	/**
	 * Gets the keyword
	 * @return
	 */
	public String getKeyWord() {
		return txtAttribute.getText();
	}
}
