package ganttchart.editor.gui.headers;

import ganttchart.tasklib.TaskPool;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.Calendar;
import java.util.Locale;

import javax.swing.JComponent;

/**
 * The chart column header. It will display a chart header for each
 * day.
 * @author Daniel McKenzie
 */
public class ChartColumnHeader extends JComponent {
  private int columnWidth = 0;
  private int adjustedWidth = 0;
  private int height = 40;
  private float zoom, focusZoom;
  private TaskPool tasks;
  private Calendar highLightDate, startFocal, endFocal;
	
  /**
   * Create the column header.
   * @param nColumnWidth The width of each column
   * @param tasks The tasks.
   */
  public ChartColumnHeader(int nColumnWidth, TaskPool tasks) {
    columnWidth = nColumnWidth;
    setPreferredSize(new Dimension(0, height));
    setZoomFactor(1.0f);
    this.tasks = tasks;
  }
	
  /**
   * Update the width to draw.
   * @param width Width of chart
   */
  public void setPreferredWidth(int width) {
    setPreferredSize(new Dimension(width, height));
  }
	
  /**
   * Update the zoom factor and redraw.
   * @param zoom New zoom factor
   */
  public void setZoomFactor(float zoom) {
    this.zoom = zoom;
    adjustedWidth = (int)(columnWidth * this.zoom);
    invalidate();
    repaint();
  }
	
  /**
   * Paint the column header.
   */
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    
    FontMetrics fm =  g.getFontMetrics();
    int asc = fm.getAscent();

    // Get the earliest date
    Calendar date = null;
    if(tasks.getEarliestStart() != null)
      date = (Calendar)tasks.getEarliestStart().clone();
    // Update the width
    setPreferredWidth(tasks.getNumberOfDays() * adjustedWidth);
        
    // Keep a track of the last month painted
    String lastMonth = "";
        
    // For each day
    int x = 0;
    for (int i = 0; i < tasks.getNumberOfDays(); i++) {
      // Update the x position
      if(getStartFocal() != null && getEndFocal() != null && 
         date.compareTo(getStartFocal()) >=0 && date.compareTo(getEndFocal()) <= 0){
        adjustedWidth = (int)(columnWidth * focusZoom);
      }else{
        adjustedWidth = (int)(columnWidth * zoom);
      }
      //int x =  i * adjustedWidth;
      // Set the y position
      int y = height-5;

      // draw vertical guide lines
      g.setColor(Color.BLACK);
      int dom = date.get(Calendar.DAY_OF_MONTH);
      int y2 = dom == 1? (height / 2) - 1 : y-(int)(1.3*asc);
      g.drawLine(x,height,x,y2);

      // If it's the weekend, draw it in red, otherwise, black
      if (date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
          date.get(Calendar.DAY_OF_WEEK) ==  Calendar.SUNDAY) {
        g.setColor(Color.RED);
      } else {
        g.setColor(Color.BLACK);
      }
        	
      if(highLightDate != null && date.compareTo(highLightDate) == 0){
        g.setColor(Color.MAGENTA);
        g.drawRect(x-1, y-asc-2, adjustedWidth+2, height);
      }
        	
      if(startFocal != null && endFocal != null && date.compareTo(startFocal) 
         >= 0 && date.compareTo(endFocal) <= 0){
        g.setColor(Color.BLUE);
      }
      // Draw the day of the month
      String ds = Integer.toString(dom);
      int xf = x+(adjustedWidth-fm.stringWidth(ds))/2;
      g.drawString(ds, xf, y);

        	
      if (dom == 13) { // draw month name more or less in the middle
      // Prepare to draw the month name and year
      String month = date.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US);
      month += " " + date.get(Calendar.YEAR);
      //System.err.println("draw day at ("+x+","+y+") "+ds);
      
      // If this isn't the same as last time then draw it
      //if (!lastMonth.equals(month)) {
      //  System.err.println("draw month at ("+x+","+y+") "+month);
        g.setColor(Color.BLACK);
        y = (height / 2) - 5;
        g.drawString(month, x, y);
        //lastMonth = month;
      }
        	
        	
      // Add one to the date
      date.add(Calendar.DAY_OF_YEAR, 1);
      x += adjustedWidth;
    }
  }
	
  public Calendar getDay(int x){
    Calendar date = null;
    if(tasks.getEarliestStart() != null)
      date = (Calendar)tasks.getEarliestStart().clone();
        
    for(int i = x; i >= 0; i=i){
      date.add(Calendar.DAY_OF_YEAR, 1);

      if(getStartFocal() != null && getEndFocal() != null && date.compareTo(getStartFocal()) >=0 && date.compareTo(getEndFocal()) <= 0){
        adjustedWidth = (int)(columnWidth * 3.0f);
      }else{
        adjustedWidth = (int)(columnWidth * zoom);
      }
      i -= adjustedWidth; 
    }
    date.add(Calendar.DAY_OF_YEAR,-1);
    return date;
  }

  public void setHighlightDay(Calendar day) {
    highLightDate = day;
  }

  public void setEndFocal(Calendar endFocal) {
    this.endFocal = endFocal;
  }

  public Calendar getEndFocal() {
    return endFocal;
  }

  public void setStartFocal(Calendar startFocal) {
    this.startFocal = startFocal;
  }

  public Calendar getStartFocal() {
    return startFocal;
  }

  public void setFocusZoom(float focusZoom) {
    this.focusZoom = focusZoom;
  }

  public float getFocusZoom() {
    return focusZoom;
  }
}
