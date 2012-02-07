package ganttchart.editor.gui;

import javax.swing.JMenuItem;

/**
 * This expands on a standard JMenuItem to add a single field
 * containing the chart clas sname.
 * @author Daniel McKenzie
 *
 */
public class DisplayMenuItem extends JMenuItem {
	
	private String theDisplay;
	
	/**
	 * Create the menu option
	 * @param name The text to display
	 * @param display The ChartDisplay classname
	 */
	public DisplayMenuItem(String name, String display) {
		super(name);
		theDisplay = display;
	}
	
	/**
	 * Gets the name of the class
	 * @return Chart class name
	 */
	public String getDisplayName() {
		return theDisplay;
	}
	
}
