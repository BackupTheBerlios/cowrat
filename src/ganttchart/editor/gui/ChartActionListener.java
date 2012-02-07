package ganttchart.editor.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

/**
 * As the ActionListeners pile up, the actionPerformed code was the same
 * throughout. This is a single class that can be subclassed so then there
 * is a single point for actionPerformed.
 * 
 * The actionPerformed has been implemented to take the ActionEvent's command,
 * find the function named do(Command) and invoke it with the ActionEvent.
 * 
 * This is much nicer than having a giant actionPerformed function with one
 * giant if..else statement.
 * 
 * @author Daniel McKenzie
 *
 */
public class ChartActionListener implements ActionListener {

  public void actionPerformed(ActionEvent arg0) {
    try {
      Class[] arr = {ActionEvent.class};
      getClass().getDeclaredMethod("do" + arg0.getActionCommand(), arr).invoke(this, arg0);
    } catch (NoSuchMethodException e) {
      JOptionPane.showMessageDialog(null, "That command hasn't be created.");
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, "Exception : " + e.getCause() + " at " + arg0.getActionCommand());
    }
  }
}
