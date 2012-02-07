package ganttchart.editor.gui.headers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.SystemColor;
import ganttchart.editor.gui.displays.ChartDisplay;
import ganttchart.tasklib.*;

import javax.swing.JComponent;

/**
 * This is a row header for a JScrollPane. It will display a header
 * for each task.
 * @author Daniel McKenzie and Wim Vanden Broeck
 *
 */
public class ChartRowHeader extends JComponent {

  private int rowHeight = 0;
  private int adjustedHeight = 0;
  private int width = 0;
	
  //The Chart which has this heading
  private ChartDisplay parent;
	
  private TaskPool tasks;
  private Task hovered;
  private Task clicked;
  private SubTask subClicked;
  private double zoom;
	
  /**
   * Will construct a ChartRowHeader
   * @param nRowHeight The height of each row.
   * @param nWidth The width of the header.
   * @param tasks The task pool.
   */
  public ChartRowHeader(int nRowHeight, int nWidth, TaskPool tasks, ChartDisplay parent) {
    rowHeight = nRowHeight;
    width = nWidth;
    setZoomFactor(1.0f);
    this.tasks = tasks;
    this.parent = parent;
		
    this.addMouseListener(new RowMouseListener());
    this.addMouseMotionListener(new RowMotionListener());
		
  }
	
  /**
   * Gets the clicked Task
   * @return hovered
   */
	
  public Task getClicked(){
    return clicked;
  }
	
  /**
   * Set the height of this to a variable value.
   * @param height The height to set this to.
   */
  public void setPreferredHeight(int height) {
    setPreferredSize(new Dimension(width, height));
  }
	
  /**
   * Call to change the zoom factor, and then repaint.
   * @param zoom The new zoom factor to use.
   */
  public void setZoomFactor(double zoom) {
    this.zoom = zoom;
    adjustedHeight = (int)(rowHeight * this.zoom);
    invalidate();
    repaint();
  }
	
  /**
   * Paints the header.
   */
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
        
    // Update the height of the drawing space
    setPreferredHeight(tasks.getNumberOfTasksAndSubTasks() * adjustedHeight);
    // Keep a count of the number of tasks we have drawn
    int barnum = 0;
    // The width of the colour swatch
    int width = (rowHeight/2)+2;
        
    // For each of the tasks
    for (int i = 0; i < tasks.getNumberOfTasks(); i++) {
        	
      Task thisTask = tasks.getTask(i);
        	
      Color textColor = SystemColor.textText;
        	
      // Is this hovered over?
      boolean drawHover = false;
      if (hovered == thisTask) {
        drawHover = true;
        		
        // Draw operating systems selected background colour
        int height = adjustedHeight;
        if((!thisTask.getFolded() ) &&  parent.getMapParent() == null)
          height += thisTask.getNumberOfSubTasks() * adjustedHeight;
        		
        int y = barnum * adjustedHeight;
        if(thisTask == clicked)
          g.setColor(SystemColor.textInactiveText);
        else
          g.setColor(SystemColor.textHighlight);

        g.fillRect(0, y, this.width, height);
        		
        // Set the text color to the OS' hovered text colour.
        textColor = SystemColor.textHighlightText;
      }
        	
      // Set the position
      int y = barnum * adjustedHeight + g.getFontMetrics().getHeight();
      int x = 8 + width;
      // Draw the text
      g.setColor(textColor);
      if(thisTask == clicked)
        g.drawString(thisTask.getName(), x+5+8, y);
      else
        g.drawString(thisTask.getName(), x+5+8, y);
        	
      // Update the position for drawing the swatch
      y = (barnum * adjustedHeight) + width;
      x = 4;
        	
      // If we are hovered, draw a text hover colour square
      if (drawHover) {
        g.drawRect(x-1, y-1-6, width*2+1, width+1);
      }        	
        	
      // Draw the swatch
      g.setColor(thisTask.getColor());
      g.fillRect(x, y-1-6, width*2 , width );
        	

      // For each of the sub tasks
      if(!thisTask.getFolded() && parent.getMapParent() == null){
        for (int j = 0; j < thisTask.getNumberOfSubTasks(); j++) {
          SubTask sub = thisTask.getSubTask(j);
	        		
          // Set the position
          int sy = (barnum+j + 1) * adjustedHeight + g.getFontMetrics().getHeight();
	            	
          int sx = 8 + (width*2);
          if(sub == subClicked)
            sx += 10;
          // Draw the name using the current colour (text)
          g.setColor(textColor);
          g.drawString(sub.getName(), sx+12, sy);
	            	
          // Swatch position update
          sy = ((barnum+j+1) * adjustedHeight) + width;
          sx = 5 + width;
          if(sub == subClicked)
            sx += 10;
	            	
          // Draw the hover squares if needed
          if (drawHover) {
            g.drawRect(sx-1, sy-1-6, width*2+1, width+1);
          }
	            	            	
          // Draw the subtask colour swatch
          g.setColor(sub.getColor());
          g.fillRect(sx, sy-6, width*2  , width );
        }
        barnum += thisTask.getNumberOfSubTasks();
      }
      barnum++;
    }
        
  }
	
  /**
   * Find what task the mouse is hovering over and then
   * force a redraw.
   * @param x The x position of the mouse
   * @param y The y position of the mouse
   */
  private void followMouse(int x, int y) {
    // Now some difficulty, we need to find the barriers of where
    // a task actually starts and ends.
		
    // Go through each task, calculate if y falls in its area
    // then update.
    int curY = 0;
    for (int i = 0; i < tasks.getNumberOfTasks(); i++) {
      int newY = curY + adjustedHeight;
      if( !tasks.getTask(i).getFolded())
        newY += (adjustedHeight * tasks.getTask(i).getNumberOfSubTasks()) - 1;
			
      if (y > curY && y < newY) {
        // This is our task
        if (tasks.getTask(i) != hovered) {
          hovered = tasks.getTask(i);
          repaint();
        }
        return;
      }
      curY = newY + 1;
    }
    // We haven't found it, so set to null and be done
    hovered = null;
    repaint();
  }
	
  /**
   * This class allows for the task editor to be invoked when a task
   * is clicked on.
   * @author Daniel McKenzie
   */
  class RowMouseListener implements MouseListener {

    /**
     * Invoke the task editor on the currently hovered task.
     */
    public void mouseClicked(MouseEvent arg0) {
      if(parent.getMapParent() == null){
        if (hovered != null) {
          clicked = hovered;
          clicked.setFolded(!clicked.getFolded());
          if(clicked.getFolded()){
            hovered = null;
            clicked = null;
          }
          repaint();
          if(parent != null)
            parent.repaint();
        }
      }
    }

    // The below methods aren't used, but have to be here.
		
    public void mouseEntered(MouseEvent arg0) { }

    public void mouseExited(MouseEvent arg0) { }

    public void mousePressed(MouseEvent arg0) { }

    public void mouseReleased(MouseEvent arg0) { }
		
  }
	
  /**
   * This class enables the hover effect when the user moves the mouse
   * over the header.
   * @author Daniel McKenzie
   */
  class RowMotionListener implements MouseMotionListener {
		
    /**
     * Just calls followMouse.
     * @see ChartRowHeader#followMouse(int, int)
     */
    public void mouseMoved(MouseEvent arg0) {
      followMouse(arg0.getX(), arg0.getY());
    }
		
    /**
     * Not used.
     */
    public void mouseDragged(MouseEvent arg0) { }
		
  }

  /**
   * Sets the clicked Task
   * @param task
   */
  public void setClicked(Task task) {
    clicked = task;
    hovered = task;
    //if(clicked != null)
    //clicked.setFolded(false);
    repaint();
  }
	
  /**
   * Sets the clicked SubTask
   * @param subTask
   */

  public void setSubClicked(SubTask subTask) {
    subClicked = subTask;
    repaint();
  }
	
}
