package ganttchart.editor.gui;

import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

/**
 * The UI factory is a helper object to create UI elements
 * with ease. This is to help avoid having lots of
 * createXXX functions through out code that all do the same
 * thing. All functions are static.
 * @author Daniel
 *
 */
public class UIFactory {
	
  /**
   * Creates a new menu item.
   * @param name The name of the item.
   * @param actionListener The action listener to use.
   * @param command The action command.
   * @return A menu item.
   */
  public static JMenuItem createMenuItem(String name, ActionListener actionListener, String command) {
    JMenuItem newItem = new JMenuItem(name);
    newItem.addActionListener(actionListener);
    newItem.setActionCommand(command);
    return newItem;
  }
	
  /**
   * Creates a new button for use on a JToolBar.
   * @param imageName The name of the image. The images need to be placed in the root.
   * @param actionListener The action listener to use.
   * @param actionCommand The action command to use.
   * @param toolTipText The tooltip to appear.
   * @param altText Text to use if no image is available.
   * @return A JButton.
   */
  public static JButton createToolBarButton(String imageName,
                                            ActionListener actionListener,
                                            String actionCommand,
                                            String toolTipText,
                                            String altText) 
  {
    //Look for the image.
    URL imageURL = null;
    if(imageName != null){
      String imgLocation = "/Images/"
        + imageName
        + ".png";
      imageURL = Main.class.getResource(imgLocation);
    }
    //Create and initialize the button.
    JButton button = new JButton();
    button.setActionCommand(actionCommand);
    button.setToolTipText(toolTipText);
    button.addActionListener(actionListener);
    button.setDefaultCapable(false);
    if (imageURL != null) {                      //image found
      button.setIcon(new ImageIcon(imageURL, altText));
    } else {                                     //no image found
      button.setText(altText);
      System.err.println("Resource not found: " + imageName);
    }
    button.setFocusable(false);
    button.setFocusCycleRoot(false);
    button.setFocusTraversalKeysEnabled(false);
    return button;
  }
	
  public static JCheckBox createToolBarCheckBox(ActionListener actionListener,
                                                String actionCommand,
                                                String toolTipText,
                                                String altText){
		
    /*String imgLocation = "/Images/"
      + imageName
      + ".png";
      URL imageURL = Main.class.getResource(imgLocation);*/
			
    //Create and initialize the button.
    JCheckBox check = new JCheckBox();
    check.setActionCommand(actionCommand);
    check.setToolTipText(toolTipText);
    check.addActionListener(actionListener);
    /*if (imageURL != null) {                      //image found
      check.setIcon(new ImageIcon(imageURL, altText));
      } else {*/                                     //no image found
    check.setText(altText);
    //System.err.println("Resource not found: " + imgLocation);
    //}
    check.setFocusable(false);
    check.setFocusCycleRoot(false);
    check.setFocusTraversalKeysEnabled(false);
    return check;
  }
	
	
}
