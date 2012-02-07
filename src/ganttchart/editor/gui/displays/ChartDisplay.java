package ganttchart.editor.gui.displays;

import ganttchart.editor.gui.headers.ChartColumnHeader;
import ganttchart.editor.gui.headers.ChartRowHeader;
import ganttchart.tasklib.SubTask;
import ganttchart.tasklib.SubTaskInstance;
import ganttchart.tasklib.Task;
import ganttchart.tasklib.TaskInstance;
import ganttchart.tasklib.TaskPool;

import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.JCheckBox;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

/**
 * An abstract class that defines the basics of a chart display.
 * All charts must extend this class.
 * @author Daniel McKenzie
 *
 */
public abstract class ChartDisplay extends JPanel {
	
  // References to its parent frame
  protected JDesktopPane pane;
  protected JFrame mainWindow;
  protected JScrollPane container;
	
  //The size of one day, horizontally
  //Height of the chart
  protected static int DAY_SIZE = 24;
  // draw task labels on focus view for each step 
  protected static boolean MULTILABEL_DRAW = true;
  protected int height = 0;

  protected int blurLevel = 25;
	
  //The tasks that make up the chart
  protected TaskPool tasks;
	
  //Row and Column header
  protected ChartColumnHeader colHead;
  protected ChartRowHeader rowHead;
	
  // References to what is currently selected 
  protected Task clicked;
  protected SubTaskInstance subInstClicked;
  protected TaskInstance instClicked;
	
  //true; drag happens from start date, false; Drag happens from end date
  protected boolean dragDirection;
  protected Calendar startDrag, endDrag;
	
  //Information about the focusZoom
  protected int focalSize = 5;
  protected Calendar startFocalDay, endFocalDay;
  protected float focusZoom = 3.0f;
	
  // To prevent breakage, don't start drawing immediately
  protected boolean drawing = false;
	
  //The zoom factor
  protected float zoom = 1.0f;
	
  //toggle; only used for CombiChart, swaps between Mosaic & Gantt
  protected boolean toggle = true;
	
  //Checks whether the tasks are allowed to be moved
  protected boolean movable = false;
	
  //state of blur
  protected int blurType;
  protected String keyWord;
	
  //The toolbar
  protected JToolBar tools;
	
  //true for high detail, false for low detail
  protected JCheckBox detail, focus;  
	
  protected ChartDisplay parent = null;

  /**
   * Static chart name.
   * @return The chart name.
   */
  public static String getChartName() {
    return "Chart";
  }
	
  /**
   * Set a context menu. This will appear after the Charts
   * menu on the main program menu bar.
   * @return JMenu with all user options.
   */
  public abstract JMenu getContextMenu();
	
  /**
   * Set a toolbar. This will appear in the chart window
   * above the chart.
   * @return JToolBar with user options.
   */
  public abstract JToolBar getContextToolbar();
	
  /**
   * Called by ChartInternalFrame to let the chart know
   * what scroll pane it is in. The chart can then use this scroll
   * pane to set up headers, and chart specific options.
   * @param scrollPane The scroll pane this chart is in.
   */
  public abstract void setScrollPane(JScrollPane scrollPane);
	
  /**
   * ChartInternalFrame will call this when it is done initialising
   * all requirements.
   */
  public abstract void startDrawing();
	
  /**
   * Chart identifier. Should be unique.
   * @return String identifier.
   */
  public abstract String getChartIdentifier();
	
  /**
   * Get the maps parent 
   * @return parentMap The map's parent
   */
  public abstract ChartDisplay getMapParent();
	
  //GETTERS AND SETTERS
  /**
   * gets the clicked subtaskInstance
   * @return subInstClicked The clicked SubTaskInstance
   */
  public SubTaskInstance getClickedSubTaskInstance() {
    return subInstClicked;
  }
	
  /**
   * Sets the blurtype
   * @param type 
   */
  public void setBlurType(int type){
    blurType = type;
  }
	
  /**
   * Return the blurtype
   * @return blurType
   */
  public int getBlurType() {
    return blurType;
  }
	
  public boolean getToggle() {
    return toggle;
  }

  public void setToggle(boolean b) {
    toggle = b;
  }
	
  /**
   * Returns the clicked task
   * @return
   */
  public Task getClickedTask(){
    return clicked;
  }
	
  /**
   * Sets the clicked task
   */
  public void setClickedTask(Task task){
    clicked = task;
  }
	
  /**
   * Sets the x-axis of the parent graph to the map's x-coordinate
   * @param x The x-coordinate
   */
  public void setXAxis(int x){
    container.getHorizontalScrollBar().setValue(roundToDay(x));
    repaint();
  }
    
  public void setFocusZoom(float focusZoom) {
    this.focusZoom = focusZoom;
    colHead.setFocusZoom(focusZoom);
  }

  public float getFocusZoom() {
    return focusZoom;
  }
	
  public void setFocasSize(int focalSize) {
    this.focalSize = focalSize;
  }

  public int getFocasSize() {
    return focalSize;
  }
	
  public boolean getMovable() {
    return movable;
  }

  public void setMovable(boolean moveable) {
    this.movable = moveable;
  }
	
  /**
   * Sets the direction of the drag
   */
  public void setDragDirection(boolean drag) {
    dragDirection = drag;
  }
	
  /**
   * Gets the TaskPool that forms this chart
   * @return tasks The TaskPool
   */
  public TaskPool getTasks() {
    return tasks;
  }
	
  //END OF GETTERS AND SETTERS
	
  /**
   * Get all the tasks that fall on this date.
   * @param tasks The TaskPool from which to find the tasks
   * @param theDate The date to pinpoint.
   * @return An array of tasks that fall on this date.
   */
  public  ArrayList<Task> getTasksOnDate(TaskPool tasks, Calendar theDate){
    ArrayList<Task> daystasks = new ArrayList<Task>();
    	    	
    // For every task
    for (int i = 0; i < tasks.getNumberOfTasks(); i++) {
      Task thisTask = tasks.getTask(i);
      // and for every instance of that task
      for (int j = 0; j < thisTask.getNumberOfInstances(); j++) {
        TaskInstance inst = thisTask.getInstance(j);
        // Check that the instance falls on theDate
        if (theDate.compareTo(inst.getStart()) >= 0 && theDate.getTime().compareTo(inst.getEnd().getTime()) <= 0) {
          /**
           * I have commented this piece of code because it apparently led to errors 
           * when the previous day didn't have a task.
           * It would skip the second (or third,...) task, and only show one (or two,...).
           * This would become a problem when we try to add an actionEvent to the graph and 
           * attempt to have a click event involving getting the task/subtask on said day.
           * - Wim Vanden Broeck  
    				 
           if (lastList == null) { // We don't have a previous list to check on
           daystasks.add(thisTask);
           } else { // we do have a previous list
           int pos = lastList.indexOf(thisTask);
    					
           if (pos == 0) { // this is the first task
           daystasks.add(0, thisTask);
           } else if (pos == (lastList.size() - 1)) {
           lastTask = thisTask; // add this task later when we are finished
           } else {
           daystasks.add(thisTask); // add this task to the end of the list now
           }
    					
           }
          */
          daystasks.add(thisTask);
        }    			
      }
    }
    	
    // Add the last task if available
    /**
     * This is also part of the previously commented code, 
     * and as such no longer used due to the previous mentioned error
     * - Wim Vanden Broeck
     * 
     * if (lastTask != null) {
     daystasks.add(lastTask);
     }
    	
     // Keep a reference to this list, we'll need it later
     lastList = daystasks;
    */
    return daystasks;
  }
    
  /**
   * Get all the subtasks that fall on this date and are part of this task
   * @param theDate The date to pinpoint
   * @param theTask The task to pinpoint
   * @return An array of subtasks that falls on this date and is part of the task 
   */
  public ArrayList<SubTask> getSubs(Task theTask, Calendar theDate){
    ArrayList<SubTask> subs = new ArrayList<SubTask>();
    	
    for (int i = 0; i < theTask.getNumberOfSubTasks(); i++) {
      SubTask sub = theTask.getSubTask(i);
      // Find all the subtasks in this instance that fall on this date
      for (int k = 0; k < sub.getNumberOfSubTaskInstances(); k++) {
        SubTaskInstance subInst = sub.getSubTaskInstance(k);
        if (theDate.compareTo(subInst.getStart()) >= 0 && theDate.compareTo(subInst.getEnd()) <= 0) {
          // Add the subtask
          subs.add(subInst.getDefinition());
        }
      }
    }
    return subs;
  }
    
  /**
   * Moves a taskInstance and all its subTasksInstances to a given startDate
   * @param date
   */
  public void moveTaskInstance(TaskInstance instClicked, Calendar endDrag) {
    if(instClicked != null){
      Calendar aide = (Calendar) endDrag.clone();
      aide.add(Calendar.DAY_OF_YEAR, instClicked.getNumberOfDays()-1);
			
      int ammount = instClicked.getSubTaskInstances().size();
      for(int i = 0; i < ammount; i++){
        if(i < instClicked.getSubTaskInstances().size()){
          int diff = differenceInDates(instClicked.getStart(), instClicked.getSubTaskInstance(i).getStart());
          Calendar stStart = (Calendar) endDrag.clone();
          stStart.add(Calendar.DAY_OF_YEAR, diff);
          Calendar stEnd = (Calendar) stStart.clone();
          int days= instClicked.getSubTaskInstance(i).getNumberOfDays();
          stEnd.add(Calendar.DAY_OF_YEAR, days-1);
          instClicked.getSubTaskInstance(i).setEnd(stEnd);
          instClicked.getSubTaskInstance(i).setStart(stStart);
        }
      }
      instClicked.setStart(endDrag);
      instClicked.setEnd(aide);
      repaint();		
    }
  }
	
  /**
   * Moves a subTaskInstace to the given startdate
   * @param date
   */
  public void moveSubTaskInstance(Calendar endDrag, SubTaskInstance subInstClicked) {
    Calendar aide = (Calendar) endDrag.clone();
    aide.add(Calendar.DAY_OF_YEAR, subInstClicked.getNumberOfDays()-1);
    subInstClicked.setStart(endDrag);
    subInstClicked.setEnd(aide);
    repaint();				
  }
	
  /**
   * Drags a SubTaskInstance, enlonging it or shortening it
   */
  public void dragSubTaskInstance(Calendar startDrag, Calendar endDrag, boolean dragDirection, SubTaskInstance subInstClicked){
    if(!(startDrag.compareTo(endDrag) == 0)){
      if(subInstClicked.getStart().compareTo(subInstClicked.getEnd()) == 0){
        adjustSTInstance(endDrag, subInstClicked);
      }else{
        if(!dragDirection){
          adjustSTInstanceEnd(endDrag, subInstClicked);
        }
        else{ 
          adjustSTInstanceStart(endDrag, subInstClicked);
        }
      }
    }
  }
		
  /**
   * Adjust the subTaskInstance when its size is one day
   * @param endDrag 
   */
	
  private void adjustSTInstance(Calendar endDrag, SubTaskInstance subInstClicked){
    if(endDrag.compareTo(subInstClicked.getEnd()) > 0){
      adjustSTInstanceEnd(endDrag, subInstClicked);
      setDragDirection(false);
    }
    else if(endDrag.compareTo(subInstClicked.getStart()) < 0){
      adjustSTInstanceStart(endDrag, subInstClicked);
      setDragDirection(true);
    }
  }
  /**
   * Adjusts the start if a subtaskInstance
   */
  private void adjustSTInstanceStart(Calendar endDrag, SubTaskInstance subInstClicked) {
    subInstClicked.setStart(endDrag);
    repaint();
  }
	
  /**
   * Adjusts the end of a subtaskInstance
   */
  private void adjustSTInstanceEnd(Calendar endDrag, SubTaskInstance subInstClicked) {
    subInstClicked.setEnd(endDrag);
    repaint();
  }
	
  /**
   * Drags a TaskInstance, enlonging it or shortening it
   */
  public void dragTaskInstance(boolean dragDirection, TaskInstance instClicked, Calendar startDrag, Calendar endDrag){
    if(!(startDrag.compareTo(endDrag) == 0)){
      if(instClicked.getStart().compareTo(instClicked.getEnd()) == 0){
        adjustInstance(endDrag, instClicked);
      }else{
        if(!dragDirection){
          adjustInstanceEnd(instClicked, endDrag);
        }
        else{ 
          adjustInstanceStart(instClicked, endDrag);
        }
      }
    }
  }
	
  /**
   * Drags a TaskInstance if its size equals one day
   * @param endDrag
   * @param instClicked
   */
  private void adjustInstance(Calendar endDrag, TaskInstance instClicked) {
    if(endDrag.compareTo(instClicked.getEnd()) > 0){
      adjustInstanceEnd(instClicked,endDrag);
      setDragDirection(false);
    }
    else if(endDrag.compareTo(instClicked.getStart()) < 0){
      adjustInstanceStart(instClicked, endDrag);
      setDragDirection(true);
    }		
  }

  /**
   * Adjusts the end of a TaskInstance
   */
  private void adjustInstanceEnd(TaskInstance instClicked, Calendar endDrag) {
    instClicked.setEnd(endDrag);
    repaint();			
  }

  /**
   * Adjusts the start of a TaskInstance
   */
  private void adjustInstanceStart(TaskInstance instClicked, Calendar endDrag) {
    instClicked.setStart(endDrag);
    repaint();			
  }
	
  /**
   * Calculates day difference between two dates
   */
  public int differenceInDates(Calendar start, Calendar end){
    int out = 0;
    Calendar check = (Calendar) start.clone();
    while(check.compareTo(end) < 0){
      out++;
      check.add(Calendar.DAY_OF_YEAR, 1);
    }
    return out;
  }
	
  /**
   * Rounds an integer to a day value
   * @param value
   * @return
   */
  public int roundToDay(int value){
    int day = (int)value / (int)(DAY_SIZE * zoom);
    return (int) (day * DAY_SIZE * zoom);
  }
    
  /**
   * Sets the menu visible or invisible
   * @param menu
   */
  public void setMenuVisible(Boolean menu){
    tools.setVisible(menu);
  }
	
  /**
   * Repaints the chart and its components
   */
  public void repaintAll() {
    colHead.repaint();
    rowHead.repaint();
    repaint();
  }
	
  /**
   * Checks if the given SubTask has to be blurred out
   * @param definition The SubTask that needs to be checked
   * @return true or false
   */
  public boolean checkBlur(SubTask definition) {
    switch(blurType){
    case 0:
      return false;
    case 1:
      if(definition.getOwner() != clicked | (subInstClicked != null && definition != subInstClicked.getDefinition()))
        return true;
      break;
    case 2:
      if(!definition.hasAttribute(keyWord))
        return true;
      break;
    }
    return false;
  }

  /**
   * Checks if the given Task needs to be blurred
   * @param thisTask The given Task
   * @return true or false
   */
  public boolean checkBlur(Task thisTask) {
    switch(blurType){
    case 0: 
      return false;
    case 1:
      if(subInstClicked != null || (thisTask != clicked && clicked != null))
        return true;
      break;
    case 2:
      return true;
    default: 
      JOptionPane.showMessageDialog(null, "Error resolving blurType");
      break;
    }
    return false;
  }
  
  /**
   * Decides if a task should show its subtasks
   * @param t The Task
   * @return true or false
   */
  protected boolean shouldShowSubs(Task t) {
    boolean out = false;
    if(parent == null){
      if(tasks.getDetail()){
        if(!t.getFolded())
          out = true;
      }
    }
    return out;
  }

}

