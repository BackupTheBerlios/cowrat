package ganttchart.editor.gui.displays.impl;

import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;

import ganttchart.editor.gui.*;
import ganttchart.editor.gui.Main.ChartWindowListener;
import ganttchart.editor.gui.Main.MenuActionListener;
import ganttchart.editor.gui.displays.*;
import ganttchart.editor.gui.headers.*;
import ganttchart.tasklib.*;

/**
 * Implementation of the Mosaic Chart.
 * @author Daniel McKenzie
 */
public class CombiChart extends ChartDisplay {


  private static int BAR_THICKNESS = 20;
		
  private CombiChartActionListener listener;
  // Reference to itself
  private CombiChart chart = this;
	
  //references to its map
  private CombiChart map;
  private CombiChart parent = null;

	
  /**
   * Makes a new mosaic chart and initializes everything.
   * @param theTasks The tasks to draw.
   */
  public CombiChart(TaskPool theTasks, JDesktopPane parentFrame, JFrame mainWindow) {
    tasks = theTasks;
    listener = new CombiChartActionListener();
    setBackground(Color.WHITE);
    this.setFocusable(true);
		
    addMouseListener(new CombiMouseListener());
    addKeyListener(new CombiKeyListener());
    addMouseMotionListener(new CombiMotionListener());
		
    blurType = 0;
    pane = parentFrame;
    this.mainWindow = mainWindow;
    this.setFocusCycleRoot(true);
  }
	
  /**
   * The chart name.
   * @return "CombiChart"
   */
  public static String getChartName() {
    return "CombiChart";
  }
	
  /**
   * Recalculates the available height, and also the width
   * of the chart.
   */
  public Dimension getPreferredSize() {
    height = container.getViewport().getHeight();
    int width = tasks.getNumberOfDays() * (int)(DAY_SIZE * zoom);

    return new Dimension(width,height);
  }
           
  /**
   * Creates a menu called "Combi" with the actions this chart has.
   */
  public JMenu getContextMenu() {
    JMenu context = new JMenu("Combi");
    context.add(UIFactory.createMenuItem("Zoom In", listener, "ZoomIn"));
    context.add(UIFactory.createMenuItem("Zoom Out", listener, "ZoomOut"));
    context.add(UIFactory.createMenuItem("Edit Task", listener, "EditTask"));
    context.add(UIFactory.createMenuItem("Overview", listener, "OverView"));
    return context;
  }

  /**
   * Sets up the toolbar.
   */
  public JToolBar getContextToolbar() {

    tools = new JToolBar();
    tools.add(UIFactory.createToolBarButton("zoomin", listener, "ZoomIn", "Zoom in on the chart", "Zoom In"));
    tools.add(UIFactory.createToolBarButton("zoomout", listener, "ZoomOut", "Zoom out from the chart", "Zoom Out"));
    tools.add(UIFactory.createToolBarButton("blurout", listener, "BlurOut","Blur out all other tasks", "Blur Out"));
    detail = UIFactory.createToolBarCheckBox(listener, "Detail","Show more detail", "Detail");
    if(tasks.getDetail())
      setDetail(true);
    else{
      setDetail(false);
      detail.setEnabled(false);
    }
    tools.add(detail);
    tools.add(UIFactory.createToolBarButton("edittask", listener, "EditTask", "Edit selected task", "Edit Task"));
    tools.add(UIFactory.createToolBarButton("overview", listener, "OverView", "Show overview", "Show Overview"));
    focus = UIFactory.createToolBarCheckBox(listener, "Focus", "Enter Focus mode", "Focus");
    tools.add(focus);
    tools.setFocusCycleRoot(false);
    tools.setFocusable(false);
    tools.setFocusTraversalKeysEnabled(false);
    return tools;
  }

  /**
   * Sets the detail level of the graph
   * @param b True if the detail should be shown, false if only task colors are needed
   */
  private void setDetail(boolean b) {
    detail.setSelected(b);
    tasks.setFolded(!b);
    repaint();
  }
	
  /**
   * Binds the map to an owner
   * @param chart The parent chart
   */
  public void setMapParent(CombiChart chart) {
    parent = chart;
  }
	
  /**
   * Gets the parent of the chart
   * @return parent The parent of the chart (is null when the chart isn't a map)
   */
  public CombiChart getMapParent(){
    return parent;
  }

  /**
   * Scales the graph to the its smallest size
   */
  private void scaleMinimum(){
    zoom = 1.0f;
    colHead.setZoomFactor(zoom);
    chart.revalidate();
    chart.repaint();
    container.revalidate();
  }

  /**
   * Takes in the scroll pane and adds the row and column headers.
   */
  public void setScrollPane(JScrollPane scrollPane) {
    container = scrollPane;
    container.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    rowHead = new ChartRowHeader(20, 150, tasks, this);
    container.setRowHeaderView(rowHead);
    colHead = new ChartColumnHeader(DAY_SIZE, tasks);
    container.setColumnHeaderView(colHead);
  }
	
  /**
   * Will start drawing!
   */
  public void startDrawing() {
    drawing = true;
  }
	
  /**
   * Gets the Task that the mouse is hovered/clicked over
   * @param arg0
   * @return
   */
  public Task getMouseTask(MouseEvent arg0){
    Calendar date = colHead.getDay(arg0.getX());
    ArrayList<Task> taskDaily = new ArrayList<Task>();
    if(parent == null){
      taskDaily = getTasksOnDate(tasks,date);
      if(taskDaily.size() != 0){
        int i = arg0.getY();
        int id = 0;
        while(i >= 0){
          if(i == id){
            break;
          }
          i = i - (int)(BAR_THICKNESS * zoom) - (int)(zoom*id*7);
						
          if(detail.isSelected()){
            if(i > 0){
              if(id < tasks.getNumberOfTasks())
                i = i - (BAR_THICKNESS * tasks.getTask(id).getNumberOfSubTasks());
            }
          }
          id++;
        }
        id--;
        if(id < 0)
          id = 0;
        if(id < tasks.getNumberOfTasks())
          return tasks.getTask(id);
      }
    }
    return null;
  }
	
  /**
   * Gets the SubTaskInstace that is under the mouse cursor
   * @param arg0
   * @return
   */
  public SubTaskInstance getMouseSubTask(MouseEvent arg0){
    Calendar date = colHead.getDay(arg0.getX());
    int y = arg0.getY();
    if(parent == null){
      Task task = getMouseTask(arg0);
      if(task != null){
        if(super.toggle || task != clicked){
          //calculate Mosaic subTask
          y = y - (int) (BAR_THICKNESS * zoom);
          y = y - (int)(zoom*7);
					
          for(int i  = 0; i < tasks.getTaskID(task); i++){
            y = y - (int) (BAR_THICKNESS * zoom);
            y = y - (int)(zoom*i*7);
            y = y - (BAR_THICKNESS/6);
            y = y - (tasks.getTask(i).getNumberOfSubTasks() * BAR_THICKNESS );
          }
          ArrayList<SubTask> subs = getSubs(task, date);
          if(subs.size() != 0){
            int sHeight = (int) (task.getNumberOfSubTasks() * BAR_THICKNESS ) / subs.size();
            int subNumber = (int) Math.floor(y / sHeight);
		
            if(!(subNumber >= subs.size())){
              return subs.get(subNumber).getInstanceOnDate(date);
            }
          }
        }else{
          //calculate Gantt subTask
          y = y - (int) (BAR_THICKNESS * zoom);
          y = y - (int)(zoom*7);
					
          for(int i  = 0; i < tasks.getTaskID(task); i++){
            y = y - (int) (BAR_THICKNESS * zoom);
            y = y - (int)(zoom*i*7);
            y = y - (BAR_THICKNESS/6);
            y = y - (tasks.getTask(i).getNumberOfSubTasks() * BAR_THICKNESS );
          }
          ArrayList<SubTask> subs = getSubs(task, date);
          if(subs.size() != 0){
            int number = y/BAR_THICKNESS;
            if(number >= 0)
              return task.getSubTask(y/BAR_THICKNESS).getInstanceOnDate(date);
          }
					
        }
      }
    }
    return null;
  }
					

						
	
  /**
   * Does all the drawing of the mosaic chart.
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
      while (date.compareTo(end) <= 0) {
        if(focus.isSelected()){
          if(startFocalDay != null && endFocalDay != null && 
             date.compareTo(startFocalDay) >=0 && 
             date.compareTo(endFocalDay) <= 0)
            {
              zoom = getFocusZoom();
            }
          else
            {
              zoom = 1.0f;
          }
        } 
        int bwidth = (int)(DAY_SIZE * zoom );
        // Get all the tasks on this date
        ArrayList<Task> daystasks = new ArrayList<Task>();
        int barStart = 0;

        daystasks = getTasksOnDate(tasks,date);
        if (daystasks.size() > 0) {
          // Calculate the height of each block
          // Start drawing it;
          for (int i = 0; i <  tasks.getNumberOfTasks(); i++) {       		
            // Position based on the day and block
            if(daystasks.contains(tasks.getTask(i))){
              int y;
              if(detail.isSelected()){			      
                y = (int) (barStart * (BAR_THICKNESS * zoom));
              }else
                y = (int) (BAR_THICKNESS * zoom) * tasks.getTaskID(tasks.getTask(i));
              if(focus.isSelected()){
                y = (int) (barStart * (BAR_THICKNESS));
              }
			        		
              if(barStart != 0){
                y = y + (i*7);
              }
              // Draw the block
              //check for blanked out
              if(checkBlur(tasks.getTask(i)))
                g.setColor(new Color(tasks.getTask(i).getColor().getRed(),
                                     tasks.getTask(i).getColor().getGreen(),
                                     tasks.getTask(i).getColor().getBlue(),25));
              else
                g.setColor(tasks.getTask(i).getColor());
				        		
              g.fillRect(x, y, bwidth, BAR_THICKNESS+i);
				        		
              if(instClicked != null && instClicked == tasks.getTask(i).getInstance(colHead.getDay(x))){
                if(date.compareTo(instClicked.getStart()) == 0){
                  drawArrows(g,x,y,height,1);	
                }
                if(date.compareTo(instClicked.getEnd()) == 0){
                  drawArrows(g,x+bwidth,y,height,1);
                }
              }	
              y = y + (BAR_THICKNESS/6);
              //draw subTasks
              if(detail.isSelected()){
                if(super.toggle || tasks.getTask(i) != clicked){
                  drawMosaicSubTasks(g, date, x, bwidth, i, y);
                }else if(tasks.getTask(i) == clicked){
                  drawGanttSubTasks(g, date, x, bwidth, i, y, barStart);
                }
              }
            }
            if(detail.isSelected() && parent == null)
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
   * Draws the SubTasks using the Gantt system
   * @param g
   * @param date
   * @param x
   * @param bwidth
   * @param i
   * @param y
   * @param barStart
   */
  private void drawGanttSubTasks(Graphics g, Calendar date, int x, int bwidth, int i, int y, int barStart) {
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
            g.setColor(new Color(tasks.getTask(i).getSubTask(k).getColor().getRed(),tasks.getTask(i).getSubTask(k).getColor().getGreen(),tasks.getTask(i).getSubTask(k).getColor().getBlue(),25));
          else
            g.setColor(tasks.getTask(i).getSubTask(k).getColor());
          g.fillRect(x, sy+(i*(BAR_THICKNESS/6)) + (i*7), bwidth, subHeight+k+tasks.getTaskID(tasks.getTask(i)));
          if(subInstClicked != null && subInstClicked == tasks.getTask(i).getSubTask(k).getInstanceOnDate(date)){
            if(date.compareTo(subInstClicked.getStart()) == 0){
              //drawArrows(g,x,sy,(int)(BAR_THICKNESS/2*zoom)+i+k,1);
              drawArrows(g,x,0,height,1);
            }
            if(date.compareTo(subInstClicked.getEnd()) == 0){
              drawArrows(g,x+bwidth,0,height,1);			
            }
          }	
        }
      }
    }		
  }

  /**
   * Draws the SubTasks using the Mosaic System
   * @param g
   * @param date
   * @param x
   * @param bwidth
   * @param i
   * @param y
   */
  private void drawMosaicSubTasks(Graphics g, Calendar date, int x, int bwidth, int i, int y) {
    ArrayList<SubTask> subs = getSubTasks(tasks.getTask(i), date);
    if (subs.size() > 0) {			        					
      int subHeight = (BAR_THICKNESS * tasks.getTask(i).getNumberOfSubTasks()) / subs.size();
      for (int k = 0; k < subs.size(); k++) {
        // Recalculate the y position and draw the block
        int sy ;
        if(focus.isSelected())
          sy = y + (int)BAR_THICKNESS +  (subHeight * k);
        else
          sy =y + (int)(BAR_THICKNESS*zoom) +  (subHeight * k);
				
				
        if(checkBlur(subs.get(k)))
          g.setColor(new Color(subs.get(k).getColor().getRed(),subs.get(k).getColor().getGreen(),subs.get(k).getColor().getBlue(),25));
        else
          g.setColor(subs.get(k).getColor());
 
        g.fillRect(x, sy, bwidth, subHeight);
        if(focus.isSelected()){
          if(startFocalDay != null && endFocalDay != null && date.compareTo(startFocalDay) >=0 && date.compareTo(endFocalDay) <= 0){
            if(subs.get(k).getInstanceOnDate(date).getStart().compareTo(date) == 0){
              g.setColor(Color.WHITE);
              g.drawString(subs.get(k).getName(), x+(bwidth/3), sy+20);
            }
          }
        }
        if(subInstClicked != null){	
          Calendar check = (Calendar) subInstClicked.getEnd().clone();
          check.add(Calendar.DAY_OF_YEAR, 1);
          if(check.compareTo(date) == 0 && k == subs.size()-1){
            drawArrows(g,x,0,height,1);			
          }
          if(date.compareTo(subInstClicked.getStart()) == 0 && k == subs.size()-1){
            drawArrows(g,x,0,height,1);			
          }		 
        }
      }
    }
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
   * @return "CombiChart"
   */
  public String getChartIdentifier() {
    if(parent == null)
      return "Combi Chart";
    else
      return "Combi Overview";
  }
	
  /**
   * Contains the actions that this mosaic chart can do.
   * @author Daniel McKenzie
   *
   */
  public class CombiChartActionListener extends ChartActionListener {
    	
    /**
     * Zooms in. Adds 0.1 to the factor and calls for a redraw.
     * @param arg0 Arguments (not used)
     */
    public void doZoomIn(ActionEvent arg0) {
      zoom += 0.1f;
      colHead.setZoomFactor(zoom);
      chart.revalidate();
      chart.repaint();
      container.revalidate();
    }
		
    /**
     * Zooms out. Subtracts 0.1 from the factor and calls for a redraw.
     * @param arg0
     */
    public void doZoomOut(ActionEvent arg0) {
      if(zoom >= 0.6f){
        zoom -= 0.1f;
        colHead.setZoomFactor(zoom);
        chart.revalidate();
        chart.repaint();
        container.revalidate();
      }else {
        JOptionPane.showMessageDialog(null, "You have zoomed out as far as possible.","Warning",JOptionPane.WARNING_MESSAGE);
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
        repaint();
        rowHead.repaint();
        colHead.repaint();
      }else
        JOptionPane.showMessageDialog(null, "You haven't selected a task.","Warning",JOptionPane.WARNING_MESSAGE);			
    }
		
    /**
     * Shows the overview of the chart - not fully implemented
     * @param arg0
     */
    public void doOverView(ActionEvent arg0){
      map = new CombiChart(tasks, pane, mainWindow);
      map.setMapParent(chart);

      ChartInternalFrame newchart = new ChartInternalFrame(mainWindow, map, new MenuActionListener());
      newchart.addInternalFrameListener(new ChartWindowListener());
      newchart.setSize(800, 250);
      map.setMenuVisible(false);
      map.scaleMinimum();
      map.setDetail(false);
      pane.add(newchart);
      newchart.toFront();
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
     * Calls the repaint to change the detail
     * @param arg0
     */
    public void doDetail(ActionEvent arg0){
      if(!detail.isSelected()){
        tasks.setFolded(true);
      }else{
        tasks.setFolded(false);
      }
      repaintAll();
    }
		
    /**
     * Goes into Bi-Focal zoom mode
     * @param arg0
     */
    public void doFocus(ActionEvent arg0){
      if(focus.isSelected()){
        scaleMinimum();
        new FocusEditor(null, chart);
      }else{
        zoom = 1.0f;
        colHead.setStartFocal(null);
        colHead.setEndFocal(null);
        colHead.setZoomFactor(zoom);
        chart.revalidate();
        chart.repaint();
        container.revalidate();
      }
			
      repaint();
    }
  }
    
  /**
   * Listens for the alt key
   * @author Wim Vanden Broeck
   */
  class CombiKeyListener implements KeyListener{

    @Override
    public void keyPressed(KeyEvent e) {
      if(e.getKeyCode() == 18){
        movable = true;
      }		
      if(subInstClicked != null && detail.isSelected()){
        if(e.getKeyCode() == 38)
          subInstClicked.getDefinition().getOwner().moveSubTaskInArray(subInstClicked, true);
        if(e.getKeyCode() == 40)
          subInstClicked.getDefinition().getOwner().moveSubTaskInArray(subInstClicked, false);
				
        repaintAll();
      }else if(clicked != null && !detail.isSelected()){
        if(e.getKeyCode() == 38)
          tasks.moveTaskInArray(clicked, true);
        if(e.getKeyCode() == 40)
          tasks.moveTaskInArray(clicked, false);
				
        repaintAll();				
      }
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
   * MouseMotionListener, used for drag/move action
   * @author Wim Vanden Broeck
   *
   */
  class CombiMotionListener implements MouseMotionListener{

    @Override
    /**
     * If the mouse is dragged move/drag actions of tasks will possibly occur
     * @param e MouseEvent
     */
      public void mouseDragged(MouseEvent e) {
      colHead.setHighlightDay(colHead.getDay(e.getX()));
      colHead.repaint();
      if(parent == null){
        if(movable){
          endDrag = colHead.getDay(e.getX());
          if((clicked != null)){
            if(instClicked != null && !detail.isSelected())
              moveTaskInstance(instClicked, endDrag);
            else if(subInstClicked != null)
              moveSubTaskInstance(endDrag, subInstClicked);

          }
        }else{
          endDrag = colHead.getDay(e.getX());
          if(subInstClicked != null && detail.isSelected()){							
            dragSubTaskInstance(startDrag, endDrag, dragDirection, subInstClicked);
          }
          else if(instClicked != null)
            dragTaskInstance(dragDirection, instClicked, startDrag, endDrag);
        }
      }
    }

    /**
     * Moved event, not used but needs to be implemented
     * @param e MouseEvent
     */
    @Override
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
      }
      else if(detail.isSelected()){
        if(getMouseTask(e) != null)
          setToolTipText(getMouseTask(e).getName());
        else if(getMouseSubTask(e) != null)
          setToolTipText(getMouseSubTask(e).getDefinition().getName() + " - " + getMouseSubTask(e).getNumberOfDays());			}
    }
  }
    
  /**
   * MouseListener
   * @author Wim Vanden Broeck
   *
   */
  class CombiMouseListener implements MouseListener {
		
    /**
     * Invoke the task editor on the currently hovered task.
     * (dis)abling of clicked (sub)tasks, (un)folding tasks
     * @param arg0 MouseEvent
     */
    public void mouseClicked(MouseEvent arg0) {
      Calendar date = colHead.getDay(arg0.getX());
      ChartPopUpMenu popup = new ChartPopUpMenu(chart,date);
      if((arg0.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)
        popup.show(arg0.getComponent(),
                   arg0.getX(), arg0.getY());
      else{
        if(parent == null){
          Task task = getMouseTask(arg0);
          clicked = task;
          SubTaskInstance sub = null;
          if(detail.isSelected())
            sub = getMouseSubTask(arg0);
					
          if(sub != null){
            rowHead.setSubClicked(sub.getDefinition());
            subInstClicked = sub;
            rowHead.setClicked(task);

            if(detail.isSelected())
              clicked.setFolded(false);	
						
          }else if(task != null){
            rowHead.setClicked(task);
            instClicked = clicked.getInstance(date);	
          }else {
            clicked = null;
            subInstClicked = null;
            instClicked = null;
            rowHead.setClicked(null);
            rowHead.setSubClicked(null);
            repaint();
          }
          repaint();
        }else
          parent.setXAxis(arg0.getX());
      }
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
								
        if(instClicked != null && !detail.isSelected()){
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
}
