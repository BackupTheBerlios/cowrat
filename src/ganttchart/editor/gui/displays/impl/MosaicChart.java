package ganttchart.editor.gui.displays.impl;

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

import javax.swing.BorderFactory;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

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
public class MosaicChart extends ChartDisplay {

  private MosaicChartActionListener listener;
  // Reference to itself
  private MosaicChart chart = this;

  //references to its map
  private MosaicChart map;
  private MosaicChart parent = null;
	
  //When true a semi-transparent Ganttchart will be drawn over the Mosaic
  private boolean gantt = false;
  // The height of the bar
  static int BAR_THICKNESS = 20;

  JSlider blrSlider = new JSlider(0,255,blurLevel);
	
  /**
   * Makes a new mosaic chart and initializes everything.
   * @param theTasks The tasks to draw.
   */
  public MosaicChart(TaskPool theTasks, JDesktopPane parentFrame, JFrame mainWindow) {
    tasks = theTasks;
    listener = new MosaicChartActionListener();
    setBackground(Color.WHITE);
    this.setFocusable(true);

    addMouseListener(new MosaicMouseListener());
    addKeyListener(new MosaicKeyListener());
    addMouseMotionListener(new MosaicMotionListener());

    blurType = 0;
    pane = parentFrame;
    this.mainWindow = mainWindow;
    this.setFocusCycleRoot(true);
  }
	
  /**
   * The chart name.
   * @return "Mosaic Chart"
   */
  public static String getChartName() {
    return "MosaicChart";
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
   * Creates a menu called "Mosaic" with the actions this chart has.
   */
  public JMenu getContextMenu() {
    JMenu context = new JMenu("Mosaic");
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
    
    
    blrSlider.addChangeListener(new ChangeListener(){ 
        public void stateChanged(ChangeEvent e)  {
          blurLevel = blrSlider.getValue();
          repaintAll();
        }});
    JPanel p = new JPanel();
    //p.setBorder(BorderFactory.createEtchedBorder());
    tools.add(new JLabel("     Blur: "));
    tools.add(blrSlider);
    blrSlider.setEnabled(false);
    //tools.add(p);

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
    repaint();
  }
	
  /**
   * Binds the map to an owner
   * @param chart The parent chart
   */
  public void setMapParent(MosaicChart chart) {
    parent = chart;
  }
	
  /**
   * Gets the parent of the chart
   * @return parent The parent of the chart (is null when the chart isn't a map)
   */
  public MosaicChart getMapParent(){
    return parent;
  }

  /**
   * Scales the graph to the its smallest size
   */
  private void scaleMinimum(){
    zoom = 0.6f;
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
    colHead.setFont(new Font("Arial", Font.PLAIN, 10));
    container.setColumnHeaderView(colHead);
  }
	
  /**
   * Will start drawing!
   */
  public void startDrawing() {
    drawing = true;
  }
	
  /**
   * Gets the Task that is under the mouse cursor
   * @param arg0
   * @return
   */
  public Task getMouseTask(MouseEvent arg0){
    Calendar date = colHead.getDay(arg0.getX());
    ArrayList<Task> taskDaily = new ArrayList<Task>();
    if(parent == null){
      taskDaily = getTasksOnDate(tasks,date);
      if(taskDaily.size() != 0){
        int bHeight = height / taskDaily.size();
        int taskNumber = (int) Math.floor(arg0.getY() / bHeight); 
        return taskDaily.get(taskNumber);
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
        int bHeight = height / taskDaily.size();
        int taskNumber = (int) Math.floor(arg0.getY() / bHeight); 
					
        ArrayList<SubTask> subs = getSubs(taskDaily.get(taskNumber), date);
        if(subs.size() != 0){
          int sHeight = bHeight / subs.size();
          int subNumber = (int) Math.floor((arg0.getY()) /sHeight) - (taskNumber*subs.size());
          if(subNumber < 0)
            subNumber = 0;
          if(!(subNumber >= subs.size()))
            return subs.get(subNumber).getInstanceOnDate(date);
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
             date.compareTo(startFocalDay) >=0 && date.compareTo(endFocalDay) <= 0){
            zoom = getFocusZoom();
          }else{
            zoom = 0.6f;
            //zoom = 1.0f;
          }
        }
        int bwidth = (int)(DAY_SIZE * zoom );
        // Get all the tasks on this date
        ArrayList<Task> daystasks = new ArrayList<Task>();
        daystasks = getTasksOnDate(tasks,date);
        if (daystasks.size() > 0) {
          // Calculate the height of each block
          int bheight = height / daystasks.size();
          // Start drawing it;
          for (int i = 0; i < daystasks.size(); i++) {
            // Position based on the day and block
            //int x = daynumber * bwidth;
            int y = bheight * i;

            // Draw the block
            //check for blanked out
            if(checkBlur(daystasks.get(i)))
              g.setColor(new Color(daystasks.get(i).getColor().getRed(),
                                   daystasks.get(i).getColor().getGreen(),
                                   daystasks.get(i).getColor().getBlue(),blurLevel));
            else
              g.setColor(daystasks.get(i).getColor());
                                        
            if(gantt)
              g.setColor(new Color(daystasks.get(i).getColor().getRed(),
                                   daystasks.get(i).getColor().getGreen(),
                                   daystasks.get(i).getColor().getBlue(),50));

            g.fillRect(x, y, bwidth, bheight+i);
            if(focus.isSelected()){
              if(startFocalDay != null && endFocalDay != null && 
                 date.compareTo(startFocalDay) >=0 && date.compareTo(endFocalDay) <= 0){
                if( MULTILABEL_DRAW || daystasks.get(i).getInstance(date).getStart().compareTo(date) == 0)
                {
                  g.setColor(Color.WHITE);
                  g.drawString(daystasks.get(i).getName(), x+(bwidth/5), y+20);
                }
              }
            }
            if(instClicked != null && instClicked.getStart().compareTo(date) == 0 && 
               i == daystasks.size()-1 && !detail.isSelected()){
              drawArrows(g, x, 0, height, 0);
            }
            Calendar taskCheck = (Calendar) date.clone();
            taskCheck.add(Calendar.DAY_OF_YEAR, -1);
            if(instClicked != null && instClicked.getEnd().compareTo(taskCheck) == 0 && 
               i == daystasks.size()-1 && !detail.isSelected()){
              drawArrows(g, x-1, 0, height, 0);
            }	        			
            if(shouldShowSubs(daystasks.get(i)) || detail.isSelected() )
              {// If there are subtasks involved
                // Get all the subtask colors
                ArrayList<SubTask> subs = getSubTasks(daystasks.get(i), date);
                if (subs.size() > 0) {
                  // Break up the height of this block
                  int subHeight = bheight / subs.size();
                  for (int k = 0; k < subs.size(); k++) {
                    // Recalculate the y position and draw the block
                    int sy = (bheight * i) + (subHeight * k);
                    
                    if(checkBlur(subs.get(k)))
                      g.setColor(new Color(subs.get(k).getColor().getRed(),
                                           subs.get(k).getColor().getGreen(),
                                           subs.get(k).getColor().getBlue(),blurLevel));
                    else
                      g.setColor(subs.get(k).getColor());
                    
                    if(gantt)
                      g.setColor(new Color(subs.get(k).getColor().getRed(),
                                           subs.get(k).getColor().getGreen(),
                                           subs.get(k).getColor().getBlue(),50));
                    
                    g.fillRect(x, sy, bwidth, subHeight+k+i);
                    if(focus.isSelected()){
                      if(startFocalDay != null && endFocalDay != null && 
                         date.compareTo(startFocalDay) >=0 && date.compareTo(endFocalDay) <= 0){
                        if(MULTILABEL_DRAW || 
                           subs.get(k).getInstanceOnDate(date).getStart().compareTo(date) == 0)
                          {
                            g.setColor(Color.WHITE);
                            g.drawString(subs.get(k).getName(), x+(bwidth/5), sy+20);
                          }
                      }
                    }
                    if(subInstClicked != null){	
                      Calendar check = (Calendar) subInstClicked.getEnd().clone();
                      check.add(Calendar.DAY_OF_YEAR, 1);
                      if(check.compareTo(date) == 0 && i == daystasks.size()-1){
                        drawArrows(g,x,0,height,1);			
                      }
                      if(date.compareTo(subInstClicked.getStart()) == 0 && i == daystasks.size()-1){
                        drawArrows(g,x,0,height,1);			
                      }		 
                    }
                  }
                }else{
                  //Draw the arrows to highlight an empty TaskInstance
                  if(!focus.isSelected()){
                    int drawX = getXFromDate(date);
                    drawArrows(g, drawX, y, bheight, 3);
                  }
                }
              }
          }
        }
        if(focus.isSelected() && startFocalDay != null && 
           date.compareTo(startFocalDay) == 0)
          {
            drawArrows(g, x, 0, height, 2);
          }
        Calendar taskCheck = (Calendar) date.clone();
        taskCheck.add(Calendar.DAY_OF_YEAR, -1);
        if(focus.isSelected() && endFocalDay != null && taskCheck.compareTo(endFocalDay) == 0){
          drawArrows(g, x-1, 0, height, 2);
        }	
        // Increment the day by one
        x += bwidth;
        date.add(Calendar.DAY_OF_YEAR, 1);
        daynumber++;
      }
	        
      if(gantt)
        drawGantt(g);
    }
  }
    
  private void drawGantt(Graphics g) {
    // Get the start of the chart
    Calendar date = (Calendar)tasks.getEarliestStart().clone();
    Calendar end = tasks.getLatestEnd();

    // Keep a counter of how many days we have drawn
    int daynumber = 0;
    // Precalculate each days width
       
    // Until we reach the very end of time
    int x = 0;
        
    while (date.compareTo(end) <= 0) {
      int bwidth = (int)(DAY_SIZE * zoom );

      // Get all the tasks on this date
      ArrayList<Task> daystasks = getTasksOnDate(tasks,date);
      int barStart = 0;
      if (daystasks.size() > 0) {
        // Start drawing it;

        for (int i = 0; i < tasks.getNumberOfTasks(); i++) {  
          int bHeight;
          if(focus.isSelected()){
            bHeight = (int)(BAR_THICKNESS) + tasks.getTaskID(tasks.getTask(i));
            if(startFocalDay != null && endFocalDay != null && 
               date.compareTo(startFocalDay) >=0 && date.compareTo(endFocalDay) <= 0)
              {
                zoom = getFocusZoom();
              }else{
              zoom = 1.0f;
            }
          }else{
            bHeight = (int)(zoom * BAR_THICKNESS) + tasks.getTaskID(tasks.getTask(i));
          }

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
            // Draw the block
            g.setColor(new Color(tasks.getTask(i).getColor().getRed(),
                                 tasks.getTask(i).getColor().getGreen(),
                                 tasks.getTask(i).getColor().getBlue()));

			        		
            g.fillRect(x, y, bwidth, bHeight);
            if(focus.isSelected()){
              if(startFocalDay != null && endFocalDay != null && 
                 date.compareTo(startFocalDay) >=0 && date.compareTo(endFocalDay) <= 0)
                {
                  if(tasks.getTask(i).getInstance(date).getStart().compareTo(date) == 0){
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
            if(detail.isSelected()){// If there are subtasks involved
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
							        	
                    g.setColor(new Color(tasks.getTask(i).getSubTask(k).getColor().getRed(),
                                         tasks.getTask(i).getSubTask(k).getColor().getGreen(),
                                         tasks.getTask(i).getSubTask(k).getColor().getBlue()));
						        		
                    g.fillRect(x, sy, bwidth, subHeight+k+tasks.getTaskID(tasks.getTask(i)));
                    if(subInstClicked != null && 
                       subInstClicked == tasks.getTask(i).getSubTask(k).getInstanceOnDate(date))
                      {
                        if(date.compareTo(subInstClicked.getStart()) == 0){
                          drawArrows(g,x,sy,(int)(BAR_THICKNESS/2*zoom)+i+k,1);			
                        }
                        if(date.compareTo(subInstClicked.getEnd()) == 0){
                          drawArrows(g,x+bwidth,sy,(int)(BAR_THICKNESS/2*zoom)+i+k,1);			
                        }
                      }	
                  }
                }
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

  private int getXFromDate(Calendar date) {
    int i = 0;
    Calendar start = (Calendar) tasks.getStartDate().clone();
    while(start.compareTo(date) < 0){
      i++;
      start.add(Calendar.DAY_OF_YEAR, 1);
    }
    return (int)(DAY_SIZE * zoom * i);
  }

  /**
   * Draws the lines when a subtask is selected
   * @param g
   * @param x
   * @param y
   * @param height
   * @param b, 0 for Task, 1 for subTask, 2 for Focus, 3 for empty taskInstances
   * @param dir
   */
  public void drawArrows(Graphics g, int x, int y, int height, int b){	
    boolean colored = true;
    if(b == 0)
      g.setColor(new Color(238,232,170));
    else if(b == 1)
      g.setColor(new Color(255,235,205));
    		
    if(movable)
      g.setColor(new Color(132,112,255));
    		
    if(b == 2){
      g.setColor(new Color(198,226,255));
      for(int i = y; i < height; i = i+2){
        g.fillRect(x, i, 2, 2);
      }
    }else if(b == 3){
      g.setColor(Color.WHITE);     	
      for(int i = y; i < height; i = i + 15){
        g.drawLine(x,i,x+(int)(DAY_SIZE*zoom),i+10);
      }	
    }else{
      for(int i = y; i < height; i = i+2){
        if(colored){
          g.fillRect(x, i, 2, 2);
          colored = false;
        }else{
          colored = true;
        }
      }	
    }
  }
    	
  // now in ChartDisplay.java
//   /**
//    * Decides if a task should show its subtasks
//    * @param t The Task
//    * @return true or false
//    */
//   protected boolean shouldShowSubs(Task t) {
//     boolean out = false;
//     if(parent == null){
//       if(tasks.getDetail()){
//         if(!t.getFolded())
//           out = true;
//       }
//     }
//     return out;
//   }

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
   * @return "MosaicChart"
   */
  public String getChartIdentifier() {
    if(parent == null)
      return "Mosaic Chart";
    else
      return "Mosaic Overview";
  }
    

  /**
   * Contains the actions that this mosaic chart can do.
   * @author Daniel McKenzie
   *
   */
  public class MosaicChartActionListener extends ChartActionListener {
    	
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
      map = new MosaicChart(tasks, pane, mainWindow);
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
      if (blurType > 0)
        blrSlider.setEnabled(true);
      else
        blrSlider.setEnabled(false);
      repaintAll();		
    }

    /**
     * Calls the repaint to change the detail
     * @param arg0
     */
    public void doDetail(ActionEvent arg0){
      if(!detail.isSelected()){
        tasks.setFolded(true);
      }else
        tasks.setFolded(false);
			
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
  class MosaicKeyListener implements KeyListener{

    @Override
    public void keyPressed(KeyEvent e) {
      //Checking for tasks to be moved or not
      if(e.getKeyCode() == 18){
        movable = true;
      }
			
      //Draw a semi-transparent Gantt-chart
      if(e.getKeyCode() == 16){
        gantt = true;
        repaintAll();
        clicked =  null;
        subInstClicked = null;
      }
			
      //Up and Down keys to move the (sub)Tasks in their own array
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
      if(e.getKeyCode() == 18)
        movable = false;
			
			
      if(e.getKeyCode() == 16)
        gantt = false;
			
      repaintAll();
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
  class MosaicMotionListener implements MouseMotionListener{

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
        if(getMouseSubTask(e) != null)
          setToolTipText(getMouseSubTask(e).getDefinition().getName() + " - " + getMouseSubTask(e).getNumberOfDays());
      }else
        if(getMouseTask(e) != null)
          setToolTipText(getMouseTask(e).getName());
    }
  }
    
  /**
   * MouseListener
   * @author Wim Vanden Broeck
   *
   */
  class MosaicMouseListener implements MouseListener {
		
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
          if(task != null){
            rowHead.setClicked(task);
            clicked = task;
            instClicked = clicked.getInstance(date);
            SubTaskInstance sub = getMouseSubTask(arg0);
            if(sub != null && detail.isSelected()){
              rowHead.setSubClicked(sub.getDefinition());
              subInstClicked = sub;
              if(detail.isSelected())
                clicked.setFolded(false);
            }else{
              rowHead.setSubClicked(null);
              subInstClicked = null;
            }
          }else{
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
