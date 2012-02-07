package ganttchart.editor.gui;

import ganttchart.editor.gui.displays.ChartDisplay;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;

public class FocusEditor extends JDialog {
	
  DialogActionListener listener;
  private ChartDisplay chart;
  private JSlider zoom, range;


  /**
   * Constructs and sets up the window.
   * @param owner The owning frame
   **/
  public FocusEditor(Frame owner, ChartDisplay chart){
    super(owner, "BiFocal select", true);
    listener = new DialogActionListener();
        
    this.chart = chart;
    this.setResizable(false);
    this.setLocationByPlatform(true);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    createOptions(getContentPane());
        
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
	
  private void dialogDone() {
    this.setVisible(false);
  }
	
  /**
   * Lays out the window.
   * @param contentPane The content pane to layout.
   */
  private void createOptions(Container contentPane) {
    contentPane.setLayout(new GridBagLayout());

    JLabel lbl = new JLabel("Please select how you want to BiFocal zoom: ");
    contentPane.add(lbl, newConstraints(0,0,1,true));
		
    createZoomOptions(contentPane);
    createRangeOptions(contentPane);
    JButton start = createButton("Done", "Start");
    contentPane.add(start, newConstraints(3,1,0,false));
		
  }

  private void createRangeOptions(Container contentPane) {


    JLabel label = new JLabel("How many days does the BiFocal contain: ");
    range = new JSlider(3,7);
    range.setValue(5);
		
    Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
    labelTable.put( new Integer( 3 ), new JLabel("3") );
    labelTable.put( new Integer( 4 ), new JLabel("4") );
    labelTable.put( new Integer( 5 ), new JLabel("5") );
    labelTable.put( new Integer( 6 ), new JLabel("6") );
    labelTable.put( new Integer( 7 ), new JLabel("7") );
    range.setLabelTable( labelTable );
    range.setPaintLabels(true);
		
    contentPane.add(label, newConstraints(2,0,1,true));
    contentPane.add(range, newConstraints(2,1,1,false));
  }



  private void createZoomOptions(Container contentPane) {
    zoom = new JSlider(2,5);
    zoom.setValue(3);
		
    Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
    labelTable.put( new Integer( 2 ), new JLabel("2") );
    labelTable.put( new Integer( 3 ), new JLabel("3") );
    labelTable.put( new Integer( 4 ), new JLabel("4") );
    labelTable.put( new Integer( 5 ), new JLabel("5") );
    zoom.setLabelTable(labelTable);
    zoom.setPaintLabels(true);
		 
    JLabel label = new JLabel("Define the zoom factor: ");
		
    contentPane.add(label, newConstraints(1,0,1,true));
    contentPane.add(zoom, newConstraints(1,1,1,false));
  }

  /**
   * All actions get handled in here
   * @author Win Vanden Broeck
   *
   */
  class DialogActionListener extends ChartActionListener {
		
    /**
     * Sets the detail and closes the dialog
     * @param arg0
     */
    void doStart(ActionEvent arg0){
      chart.setFocasSize(range.getValue());
      chart.setFocusZoom(zoom.getValue());
      dialogDone();
    }
  }
}
