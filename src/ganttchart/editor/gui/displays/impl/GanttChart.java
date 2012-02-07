package ganttchart.editor.gui.displays.impl;

import ganttchart.editor.gui.BlurEditor;
import ganttchart.editor.gui.ChartActionListener;
import ganttchart.editor.gui.ChartInternalFrame;
import ganttchart.editor.gui.ChartPopUpMenu;
import ganttchart.editor.gui.FocusEditor;
import ganttchart.editor.gui.TaskEditor;
import ganttchart.editor.gui.UIFactory;
import ganttchart.editor.gui.Main.ChartWindowListener;
import ganttchart.editor.gui.Main.MenuActionListener;
import ganttchart.editor.gui.headers.*;
import ganttchart.tasklib.SubTask;
import ganttchart.tasklib.SubTaskInstance;
import ganttchart.tasklib.Task;
import ganttchart.tasklib.TaskPool;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

/**
 * An implementation of a Gantt Chart. This extends ChartDisplay.
 * @author Daniel McKenzie 
 * @see ganttchart.editor.gui.displays.ChartDisplay
 */
public class GanttChart extends ganttchart.editor.gui.displays.ChartDisplay {

  // The height of the bar
  static int BAR_THICKNESS = 20;
  // The size of the row header
  // This used to be called the gutter, and was drawn inside this class
  static int GUTTER_WIDTH = 150;	

  private GanttChartActionListener listener;
	
  // A reference to itself for the actionlistener
  GanttChart chart = this;
	
  //references to its map
  private GanttChart map;
  private GanttChart parent = null;

	
  /**
   * Constructor. Makes a new Gantt Chart, initialises all
   * necessary variables.
   * @param taskset The task pool to draw.
   * @param mainWindow2 
   * @param pane2 
   */
  public GanttChart(TaskPool taskset, JDesktopPane pane2, JFrame mainWindow2) {
    tasks = taskset;
    setBackground(Color.WHITE);
    listener = new GanttChartActionListener();
        
    this.addMouseListener(new GanttMouseListener());
    this.addKeyListener(new GanttKeyListener());
    this.addMouseMotionListener(new GanttMotionListener());
        
    this.setFocusable(true);
    this.setFocusCycleRoot(true);
        
    mainWindow = mainWindow2;
    pane = pane2;
  }

  /**
   * Gets the size based on the
   * time and height of the bars.
   */
  public Dimension getPreferredSize() {
    height = container.getViewport().getHeight(); //tasks.getNumberOfTasksAndSubTasks() * (int)(BAR_THICKNESS * zoom);
    int width = tasks.getNumberOfDays() * (int)(DAY_SIZE * zoom);
    	
    return new Dimension(width,height);
  }
    
  /**
   * gets the Task that is under the mouse cursor
   * @param arg0
   * @return
   */
  public Task getMouseTask(MouseEvent arg0){
    Calendar date = colHead.getDay(arg0.getX());
    ArrayList<Task> taskDaily = new ArrayList<Task>();
    if(parent == null){
      taskDaily = getTasksOnDate(tasks,date);
      if(taskDaily.size() != 0){
        int test = arg0.getY() / (int)(BAR_THICKNESS*zoom);
        return getTask(test, date);
      }
    }
    return null;
  }
	
  /**
   * Gets the SubTaskInstance that is under the mouse cursor
   * @param arg0
   * @return
   */
  public SubTaskInstance getMouseSubTask(MouseEvent arg0){
    Calendar date = colHead.getDay(arg0.getX());
    ArrayList<Task> taskDaily = new ArrayList<Task>();
    if(parent == null){
      taskDaily = getTasksOnDate(tasks,date);
      if(taskDaily.size() != 0){
        int test = arg0.getY() / (int)(BAR_THICKNESS*zoom);
        SubTask sub = getSubtask(test, date);
        if(sub != null)
          return sub.getInstanceOnDate(date);
      }
    }
    return null;
  }
    
  /**
   * Does all the drawing of the chart.
   */
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
        	
    // Only if we are actually drawing
    if (drawing) {
      // Get the start of the chart
      Calendar date = (Calendar)tasks.getEarliestStart().clone();
      Calendar end = tasks.getLatestEnd();

      // Keep a counter of how many days we have drawn
      int daynumber = 0;
      // Precalculate each days width
	       
      // Until we reach the very end of time
      int x = 0;
      int bwidth = (int)(DAY_SIZE * zoom );
      while (date.compareTo(end) <= 0) {
        // Get all the tasks on this date
        ArrayList<Task> daystasks = getTasksOnDate(tasks,date);
        int barStart = 0;
        if (daystasks.size() > 0) {
          // Start drawing it;
          for (int i = 0; i < tasks.getNumberOfTasks(); i++) {  
            int bHeight;
            if(focus.isSelected()){
              bHeight = (int)(BAR_THICKNESS) + tasks.getTaskID(tasks.getTask(i));
              if(startFocalDay != null && 
                 endFocalDay != null && 
                 date.compareTo(startFocalDay) >=0 && 
                 date.compareTo(endFocalDay) <= 0){
                zoom = getFocusZoom();
              }else{
                zoom = 0.6f;
                //zoom = 1.0f;
              }
            }else{
              bHeight = (int)(zoom * BAR_THICKNESS) + tasks.getTaskID(tasks.getTask(i));
            }
            bwidth = (int)(DAY_SIZE * zoom );
            boolean showsubts = shouldShowSubs(tasks.getTask(i));
            // Position based on the day and block
            if(daystasks.contains(tasks.getTask(i))){
              int y;
              
              if(showsubts || detail.isSelected()){
                y = (int) (barStart * (BAR_THICKNESS * zoom));
              }else
                y = (int) (BAR_THICKNESS * zoom) * tasks.getTaskID(tasks.getTask(i));
              if(focus.isSelected()){
                y = (int) (barStart * (BAR_THICKNESS));
              }
              // SL: bypass all the above tests (to be removed later)
              // in order to allow correct placement of unfolded
              // subtasks)
              y = (int) (barStart * (BAR_THICKNESS));
              // Draw the block
              //check for blanked out
              if(checkBlur(tasks.getTask(i)))
                g.setColor(new Color(tasks.getTask(i).getColor().getRed(),
                                     tasks.getTask(i).getColor().getGreen(),
                                     tasks.getTask(i).getColor().getBlue(),blurLevel));
              else
                g.setColor(tasks.getTask(i).getColor());
				        		
              g.fillRect(x, y, bwidth, bHeight);
              if(focus.isSelected()){
                if(startFocalDay != null && endFocalDay != null && 
                   date.compareTo(startFocalDay) >=0 && date.compareTo(endFocalDay) <= 0)
                  {
                    if(MULTILABEL_DRAW || 
                       tasks.getTask(i).getInstance(date).getStart().compareTo(date) == 0)
                      {
                        g.setColor(Color.WHITE);
                        g.drawString(tasks.getTask(i).getName(), x+(bwidth/3), y+10);
                      }
                  }
              }
              if(instClicked != null && 
                 instClicked == tasks.getTask(i).getInstance(colHead.getDay(x)))
                {
                  if(date.compareTo(instClicked.getStart()) == 0){
                    drawArrows(g,x,y,(int)(BAR_THICKNESS*zoom),1);
                  }
                  if(date.compareTo(instClicked.getEnd()) == 0){
                    drawArrows(g,x+bwidth,y,(int)(BAR_THICKNESS*zoom),1);
                  }
                }
              if( showsubts || detail.isSelected()){// If there are subtasks involved
                // Get all the subtask colors
                ArrayList<SubTask> subs = getSubTasks(tasks.getTask(i), date);
                if (subs.size() > 0) {
                  // Break up the height of this block
                  int subHeight = BAR_THICKNESS/2;
                  for (int k = 0; k < tasks.getTask(i).getNumberOfSubTasks(); k++) {
                    if(subs.contains(tasks.getTask(i).getSubTask(k))){
                      // Recalculate the y position and draw the block
                      int sy; 
                      if(focus.isSelected())
                        sy = (int) (barStart+k+1) * (BAR_THICKNESS);
                      else
                        sy = (int) ((barStart+k+1) * (BAR_THICKNESS * zoom));
								        	
                      if(checkBlur(tasks.getTask(i).getSubTask(k)))
                        g.setColor(new Color(tasks.getTask(i).getSubTask(k).getColor().getRed(),
                                             tasks.getTask(i).getSubTask(k).getColor().getGreen(),
                                             tasks.getTask(i).getSubTask(k).getColor().getBlue(),
                                             blurLevel));
                      else
                        g.setColor(tasks.getTask(i).getSubTask(k).getColor());
							        		
                      g.fillRect(x, sy, bwidth, subHeight+k+tasks.getTaskID(tasks.getTask(i)));
                      if(subInstClicked != null && 
                         subInstClicked == tasks.getTask(i).getSubTask(k).getInstanceOnDate(date)){
                        if(date.compareTo(subInstClicked.getStart()) == 0)
                          drawArrows(g,x,sy,(int)(BAR_THICKNESS/2*zoom)+i+k,1);
                        if(date.compareTo(subInstClicked.getEnd()) == 0)
                          drawArrows(g,x+bwidth,sy,(int)(BAR_THICKNESS/2*zoom)+i+k,1);
                      } // end if (clicked subtask bar)
                    } // end if  (contains subtask)
                  } // end for (draw subtasks)
                  //barStart = barStart + tasks.getTask(i).getNumberOfSubTasks();
                } 
                //barStart = barStart + tasks.getTask(i).getNumberOfSubTasks();
              } 
            } 
            if((showsubts || detail.isSelected()) && parent == null)
              barStart = barStart + tasks.getTask(i).getNumberOfSubTasks();
            barStart++;
            }
          }
          if(focus.isSelected() && startFocalDay != null && date.compareTo(startFocalDay) == 0){
            drawArrows(g, x, 0, height, 2);
          }
          Calendar taskCheck = (Calendar) date.clone();
          taskCheck.add(Calendar.DAY_OF_YEAR, -2);
          if(focus.isSelected() && endFocalDay != null && taskCheck.compareTo(endFocalDay) == 0){
            drawArrows(g, x-25, 0, height, 2);
          }
          // Increment the day by one
          x += bwidth;
          date.add(Calendar.DAY_OF_YEAR, 1);
          daynumber++;
        }
      }
    }
    
  /**
   * Will find all the subtasks that have an instance on this day
   * in the task.
   * @param theTask The task to look into.
   * @param theDate The date to pinpoint.
   * @return An array list of colors.
   */
  private ArrayList<SubTask> getSubTasks(Task theTask, Calendar theDate) {
    ArrayList<SubTask> subs = new ArrayList<SubTask>();
    // For each subtask
    for (int i = 0; i < theTask.getAllSubTaskDefinitions().size(); i++) {
      SubTask sub = theTask.getSubTask(i);
      for (int k = 0; k < sub.getNumberOfSubTaskInstances(); k++) {
        SubTaskInstance subInst = sub.getSubTaskInstance(k);
        if (theDate.compareTo(subInst.getStart()) >= 0 && theDate.compareTo(subInst.getEnd()) <= 0) {
          subs.add(subInst.getDefinition());
        }
      }
    }
    return subs;
  }
    
  /**
   * The chart name
   * @return Chart name
   */
  public static String getChartName() {
    return "Gantt Chart";
  }

  /**
   * This will create a menu called "Gantt" which contains the options
   * that this gantt chart can do.
   */
  public JMenu getContextMenu() {
		
    JMenu options = new JMenu("Gantt");
		
    JMenuItem zoomIn = UIFactory.createMenuItem("Zoom In", listener, "ZoomIn");
    options.add(zoomIn);
		
    JMenuItem zoomOut = UIFactory.createMenuItem("Zoom Out", listener, "ZoomOut");
    options.add(zoomOut);
    options.add(UIFactory.createMenuItem("Edit Task", listener, "EditTask"));
		
    return options;
  }
	
  /**
   * This will create a toolbar with all the options this can do.
   */
  public JToolBar getContextToolbar() {
		
    tools = new JToolBar();
    tools.add(UIFactory.createToolBarButton("zoomin", listener, "ZoomIn", "Zoom in on the chart", "Zoom In"));
    tools.add(UIFactory.createToolBarButton("zoomout", listener, "ZoomOut", "Zoom out from the chart", "Zoom Out"));
    tools.add(UIFactory.createToolBarButton("blurout", listener, "BlurOut","Blur out all other tasks", "Blur Out"));
    detail = UIFactory.createToolBarCheckBox(listener, "Detail","Show more detail", "Detail");
    if(tasks.getDetail())
      setDetail(false);
    else{
      setDetail(false);
      detail.setEnabled(false);
    }
    tools.add(detail);
    tools.add(UIFactory.createToolBarButton("edittask", listener, "EditTask", "Edit selected task", "Edit Task"));
    tools.add(UIFactory.createToolBarButton("overview", listener, "Overview", "Show overview", "Show Overview"));
    focus = UIFactory.createToolBarCheckBox(listener, "Focus", "Enter Focus mode", "Focus");
    tools.add(focus);
		
    return tools;
  }
	
  /**
   * Sets the detail level of the Chart
   * @param b
   */
  private void setDetail(boolean b) {
    detail.setSelected(b);
    repaint();
  }

  /**
   * Will set the scroll pane and then create a row header and column
   * header on it.
   */
  public void setScrollPane(JScrollPane scrollPane) {
    container = scrollPane;
    rowHead = new ChartRowHeader(BAR_THICKNESS, GUTTER_WIDTH, tasks, this);
    colHead = new ChartColumnHeader(DAY_SIZE, tasks);
    //colHead.setFont(new Font(Font.SANS_SERIF, Font.PLAIN,10));
    container.setRowHeaderView(rowHead);
    container.setColumnHeaderView(colHead);
  }

  /**
   * Will start drawing!
   */
  public void startDrawing() {
    drawing = true;
    repaint();
  }
	
  /**
   * @return string "GanttChart"
   */
  public String getChartIdentifier() {
    if(parent == null)
      return "Gantt Chart";
    else
      return "Gantt Overview";
  }
	
  /**
   * Get a SubTask with an id and a date
   * @param id
   * @param date
   * @return subTask
   */
  public SubTask getSubtask(int id, Calendar date){
    int curId = 0;
    SubTask out = null;
    for(int i = 0; i < tasks.getNumberOfTasks(); i++){
      Task task = tasks.getTask(i);
      if(!task.getFolded()){
        for(int j = 0; j < task.getNumberOfSubTasks(); j++){
          curId++;
          if(curId == id){
            if(getSubs(task, date).contains(task.getSubTask(j))){
              out = task.getSubTask(j);
              clicked = task;
            }
          }
        }
      }
      curId++;
    }
    return out;
  }
	
  /**
   * Get a Task with an id and a date
   * @param test
   * @param date
   * @return task
   */
  private Task getTask(int id, Calendar date) {
    int curId = 0;
    Task out = null;
    for(int i = 0; i < tasks.getNumberOfTasks(); i++){
      Task task = tasks.getTask(i);
      if(curId == id){
        out = task;
      }
      if(!task.getFolded())
        curId += task.getNumberOfSubTasks();
			
      curId++;
    }
    clicked = out;
    return out;
  }
	
  /**
   * Draws the lines when a subtask is selected
   * @param g
   * @param x
   * @param y
   * @param height
   * @param dir
   */
  public void drawArrows(Graphics g, int x, int y, int height, int b){	
    if(b == 0)
      g.setColor(new Color(238,232,170));
    else if(b == 1)
      g.setColor(new Color(255,235,205));
    else if(b == 2)
      g.setColor(new Color(198,226,255));

    if(movable)
      g.setColor(new Color(132,112,255));
		
    g.fillRect(x, y, 2, height);
  }

  /**
   * The Gantt Keylistener, listens for the shift key
   * @author Wim Vanden Broeck
   *
   */
  class GanttKeyListener implements KeyListener{

    @Override
    public void keyPressed(KeyEvent e) {
      if(e.getKeyCode() == 18){
        movable = true;
      } 
			
      if(detail.isSelected()){
        if(e.getKeyCode() == 38 && subInstClicked != null)
          subInstClicked.getDefinition().getOwner().moveSubTaskInArray(subInstClicked, true);
        if(e.getKeyCode() == 40 && subInstClicked != null)
          subInstClicked.getDefinition().getOwner().moveSubTaskInArray(subInstClicked, false);
      }else{
        if(e.getKeyCode() == 38 && clicked != null){
          tasks.moveTaskInArray(clicked, true);
        }
					
        if(e.getKeyCode() == 40 && clicked != null){
          tasks.moveTaskInArray(clicked, false);
        }
      }
			
      repaintAll();				
    }

    @Override
    public void keyReleased(KeyEvent e) {
      if(e.getKeyCode() == 18){
        movable = false;
      }		
    }

    @Override
    //Not used
      public void keyTyped(KeyEvent e) {
    }
  }

  /**
   * This does the actions that GanttChart has.
   * @author Daniel McKenzie
   *
   */
  public class GanttChartActionListener extends ChartActionListener {
		
    /**
     * Zooms in, adds 0.1 to the zoom factor, then calls for a 
     * complete redraw.
     * @param arg0 The action arguments (not used)
     */
    public void doZoomIn(ActionEvent arg0) {
      zoom += 0.5;
      rowHead.setZoomFactor(zoom);
      colHead.setZoomFactor(zoom);
      chart.revalidate();
      chart.repaint();
      container.revalidate();
    }
		
    /**
     * Zooms out, subtracts 0.1 from the zoom factor, then calls
     * for a complete redraw.
     * @param arg0 The action arguments (not used)
     */
    public void doZoomOut(ActionEvent arg0) {
      if(zoom >= 0.6){
        zoom -= 0.5;
        rowHead.setZoomFactor(zoom);
        colHead.setZoomFactor(zoom);
        chart.revalidate();
        chart.repaint();
        container.revalidate();
      }else{
        JOptionPane.showMessageDialog(null, "You have zoomed out as far as possible.",
                                      "Warning",JOptionPane.WARNING_MESSAGE);			
      }
    }
		
    /**
     * Edits the clicked task.
     * @param arg0
     */
    public void doEditTask(ActionEvent arg0){
      if(rowHead.getClicked() != null){
        clicked = rowHead.getClicked();
        new TaskEditor(null, clicked, tasks);
        repaintAll();
      }else{
        JOptionPane.showMessageDialog(null, "You haven't selected a task.",
                                      "Warning",JOptionPane.WARNING_MESSAGE);			
      }
    }
		
    /**
     * Calls the repaint to change the detail
     * @param arg0
     */
    public void doDetail(ActionEvent arg0){
      for(int i = 0; i < tasks.getNumberOfTasks(); i++){
        tasks.getTask(i).setFolded(!detail.isSelected());
      }
      repaintAll();
    }
		
    /**
     * Handles the blur editor's result
     * @param arg0
     */
		
    public void doBlurOut(ActionEvent arg0){
      JFrame frame = new JFrame("Blur");
      BlurEditor blurEditor = new BlurEditor(frame);
      blurType = blurEditor.getBlurType();
      if(blurType == 2){
        keyWord = blurEditor.getKeyWord();
        tasks.setFolded(false);
      }
      repaintAll();		
    }
		
    /**
     * Shows the overview of the graph
     * @param arg0
     */
		
    public void doOverview(ActionEvent arg0){
      map = new GanttChart(tasks, pane, mainWindow);
      map.setMapParent(chart);
      ChartInternalFrame newchart = new ChartInternalFrame(mainWindow, map, new MenuActionListener());
      newchart.addInternalFrameListener(new ChartWindowListener());
      newchart.setSize(800, 250);
      map.setMenuVisible(false);
      map.scaleMinimum();
			

      newchart.setSize(800, 250);
      map.setMenuVisible(false);
      map.scaleMinimum();
      map.setDetail(false);
      map.setMapParent(chart);

      pane.add(newchart);
      newchart.toFront();
    }
		
    /**
     * Goes into Bi-Focal zoom mode
     * @param arg0
     */
    public void doFocus(ActionEvent arg0){
      if(focus.isSelected()){
        scaleMinimum();
        //        rowHead.setZoomFactor(1.0f);
        //colHead.setZoomFactor(1.0f);
        new FocusEditor(null, chart);
      }else{
        zoom = 1.0f;
        colHead.setStartFocal(null);
        colHead.setEndFocal(null);
        colHead.setZoomFactor(zoom);
        //rowHead.setZoomFactor(zoom);
        chart.revalidate();
        chart.repaint();
        container.revalidate();
      }
			
      repaint();
    }
  }
	
  /**
   * Mouse Listener, used for dragging and moving (sub)tasks
   * @author Wim Vanden Broeck
   *
   */
  class GanttMotionListener implements MouseMotionListener{

    @Override
    /**
     * Drag event 
     * @param e MouseEvent
     */
      public void mouseDragged(MouseEvent e) {
      colHead.setHighlightDay(colHead.getDay(e.getX()));
      colHead.repaint();
      if(parent == null){
        if(movable){
          endDrag = colHead.getDay(e.getX());
          if((clicked != null)){
            if(instClicked != null)
              moveTaskInstance(instClicked, endDrag);
            else if(subInstClicked != null)
              moveSubTaskInstance(endDrag, subInstClicked);

          }
        }else{
          endDrag = colHead.getDay(e.getX());
          if(subInstClicked != null)
            dragSubTaskInstance(startDrag, endDrag, dragDirection, subInstClicked);
          else if(instClicked != null)
            dragTaskInstance(dragDirection, instClicked, startDrag, endDrag);
        }
      }
    }

    @Override
    /**
     * Move event
     * @param e MouseEvent
     */
      public void mouseMoved(MouseEvent e) {
      if(focus.isSelected()){
        startFocalDay = colHead.getDay(e.getX());
        endFocalDay = (Calendar) startFocalDay.clone();
        int days = focalSize/2;
        startFocalDay.add(Calendar.DAY_OF_YEAR,-1*days);

        endFocalDay.add(Calendar.DAY_OF_YEAR, days);
        colHead.setStartFocal(startFocalDay);
        colHead.setEndFocal(endFocalDay);
        repaintAll();
      }else{
        if(detail.isSelected()){
          if(getMouseSubTask(e) != null)
            setToolTipText(getMouseSubTask(e).getDefinition().getName() + " - " + getMouseSubTask(e).getNumberOfDays());
          else if(getMouseTask(e) != null)
            setToolTipText(getMouseTask(e).getName());
        }
        else if(getMouseTask(e) != null)
          setToolTipText(getMouseTask(e).getName());
      }
    }
  }
	
  class GanttMouseListener implements MouseListener {

    /**
     * Invoke the task editor on the currently hovered task.
     * (dis)abling of clicked (sub)tasks, (un)folding tasks
     * @param arg0 MouseEvent
     */
    public void mouseClicked(MouseEvent arg0) {			
      Calendar date = colHead.getDay(arg0.getX());

      ChartPopUpMenu popup = new ChartPopUpMenu(chart, date);
      if((arg0.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)
        popup.show(arg0.getComponent(),
                   arg0.getX(), arg0.getY());
			
      ArrayList<Task> taskDaily = new ArrayList<Task>();
			
      if(parent == null){
        taskDaily = getTasksOnDate(tasks,date);
        if(taskDaily.size() != 0){
          int test = arg0.getY() / (int)(BAR_THICKNESS*zoom);
          Task task = getTask(test, date);
          SubTask sub = getSubtask(test, date);
					
          //checking if you clicked on a task
          if(task != null){
            clicked = task;
            instClicked = clicked.getInstance(date);
            rowHead.setClicked(task);
            subInstClicked = null;
            rowHead.setSubClicked(null);
            repaintAll();
            //checking if you clicked on a subtask
          }else{
            if(sub != null){
              rowHead.setSubClicked(sub);
              subInstClicked = sub.getInstanceOnDate(date);
              clicked = sub.getOwner();
              rowHead.setClicked(clicked);
              instClicked = null;
              repaintAll();
            }
            else{
              rowHead.setSubClicked(null);
              subInstClicked = null;
              instClicked = null;
            }
          }
        }else{
          rowHead.setSubClicked(null);
          subInstClicked = null;
          clicked = null;
          rowHead.setClicked(null);
          instClicked = null;
        }
      }else
        parent.setXAxis(arg0.getX());

    }

    // The below methods aren't used, but have to be here.
    public void mouseEntered(MouseEvent arg0) { }

    public void mouseExited(MouseEvent arg0) { }

    /**
     * MousePressed event used for starting the drag action
     * @param arg0 MouseEvent
     */
    public void mousePressed(MouseEvent arg0) { 
      if(parent == null){
        if(!movable)
          startDrag = colHead.getDay(arg0.getX());
				
        if(instClicked != null){
          if(startDrag.compareTo(instClicked.getStart()) == 0){
            dragDirection = true;
          }else if(startDrag.compareTo(instClicked.getEnd()) == 0){
            dragDirection = false;
          }
        }else if(subInstClicked != null){
          if(startDrag.compareTo(subInstClicked.getStart()) == 0){
            dragDirection = true;
          }else if(startDrag.compareTo(subInstClicked.getEnd()) == 0){
            dragDirection = false;
          }				
        }
      }
    }

    /**
     * MouseReleased event Used for finishing the drag/move action
     * @param arg0 MouseEvent
     */
    public void mouseReleased(MouseEvent arg0) { 
      tasks.revalidate();
      colHead.setHighlightDay(null);
      colHead.repaint();
      repaint();
    }		
  }

  /**
   * Sets the parent of the chart (only occurs when this chart is an overview)
   * @param chart
   */
  public void setMapParent(GanttChart chart) {
    parent = chart;		
  }
	
  /**
   * Scales the chart to the smallest possible size
   */
  public void scaleMinimum() {
    zoom = 0.6f;
    colHead.setZoomFactor(zoom);
    //rowHead.setZoomFactor(zoom);
    chart.revalidate();
    chart.repaint();
    container.revalidate();
  }

  /**
   * Gets the map's parent
   */
  @Override
  public GanttChart getMapParent() {
    return parent;
  }
}
