package ganttchart.editor.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import ganttchart.editor.gui.displays.*;

/**
 * This is the window containing the chart. When passed a chart, it
 * will set up the window and display it.
 * @author Daniel McKenzie
 *
 */
public class ChartInternalFrame extends JInternalFrame {

	private ChartDisplay chart;
	private Main.MenuActionListener actions;
	private JFrame parent;
	private JScrollPane scroller;
	
	/**
	 * Creates the window, initialises the chart.
	 * @param owner The owning frame.
	 * @param theChart Chart to display
	 * @param actionListener The main program action listener
	 */
	public ChartInternalFrame(JFrame owner, ChartDisplay theChart, Main.MenuActionListener actionListener) {
		// Run the original constructor
		super(theChart.getChartIdentifier());
		parent = owner;
		actions = actionListener;
		
		// Set up the scroll pane and the chart
		// Add the scroll pane to the chart
		scroller = new JScrollPane((JPanel)theChart);
		chart = theChart;
		chart.setScrollPane(scroller);
		setLayout(new BorderLayout());
		
		// Get the toolbar
		JToolBar tb = chart.getContextToolbar();
		if (tb != null) {
			add(tb, BorderLayout.NORTH);
		}
		
		// Set up the window controls and start drawing
		add(scroller, BorderLayout.CENTER);
		setMaximizable(true);
		setClosable(true);
		setResizable(true);
		setVisible(true);
		chart.startDrawing();
	}
	
	/**
	 * This function passes through to the chart
	 * @return The charts context menu
	 * @see ganttchart.editor.gui.displays.ChartDisplay#getContextMenu()
	 */
	public JMenu getContextMenu() {
		return chart.getContextMenu();
	}
	
	/**
	 * Called from the main program when something is changed that the
	 * whole window should be redrawn.
	 */
	public void inform() {
		scroller.invalidate();
		chart.revalidate();
	}
	
	/**
	 * Get the chart instance this contains.
	 * @return The chart
	 */
	public ChartDisplay getChart() {
		return chart;
	}

}
