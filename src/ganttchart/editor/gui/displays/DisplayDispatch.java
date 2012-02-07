package ganttchart.editor.gui.displays;

/**
 * This class contains an array of all the class names of charts. All
 * the charts should belong in ganttchart.editor.gui.displays.impl.
 * This is used to load in the charts correctly.
 * 
 * When you have a new chart, simply add its class name to the displays
 * static array.
 * 
 * @author Daniel McKenzie
 *
 */
public class DisplayDispatch {
	static public String[] displays = {
			"GanttChart",
			"MosaicChart",
			"CombiChart"
	};
}
