package ganttchart.editor.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.Constructor;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import ganttchart.tasklib.*;
import ganttchart.editor.gui.displays.*;

/**
 * This is the main window and contains most of the drivers for this program.
 * @author Daniel McKenzie
 *
 */
public class Main {
	
  static JDesktopPane desktop;
  static TaskPool tasks;
  static JFrame mainWindow;
  static MenuActionListener menuListener;
  static ChronosKeyListener keyListener;
	
  /**
   * Change this to a different look and feel if needed.
   * @see UIManager
   */
  final static String LOOKANDFEEL = "System";
	
  /**
   * The main window title.
   */
  static final String PROGRAM_NAME = "Chronos";
  static final String VERSION = "0.0.3";
	
  static String currentFileName;
	
  private static JMenu menTasks, menDisplay;
  private static boolean enabled = false;;
	
  /**
   * Constructor to start the window.
   * @param args Command line arguments
   */
  public static void main(String[] args) {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          createAndShowGUI();
        }
      });
  }

  /**
   * Creates the entire window.
   */
  private static void createAndShowGUI() {
    //Create and set up the window.
    initLookAndFeel();
          
    // Add the desktop to contain all windows
    desktop = new JDesktopPane();
    desktop.setBackground(Color.DARK_GRAY);
    // Create the main window
    mainWindow = new JFrame(PROGRAM_NAME + " - Untitled");
    mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mainWindow.setSize(850,750);
        
    // Set up the border layout
    mainWindow.setLayout(new BorderLayout());
    mainWindow.add(desktop, BorderLayout.CENTER);
        
    // Create the menu bar, toolbar and action listener
    menuListener = new MenuActionListener();
    keyListener = new ChronosKeyListener();
    mainWindow.setJMenuBar(createMenu());
          
    mainWindow.addKeyListener(keyListener);
    //Display the window.
    mainWindow.setVisible(true);
  }
	
  /**
   * Create the main menu bar.
   * @return The main menu.
   */
  private static JMenuBar createMenu() {
    JMenuBar menu = new JMenuBar();
          
    createFileMenu(menu);
    createTaskMenu(menu);
    createChartMenu(menu);
    createHelpMenu(menu);
          
    return menu;
  }
	
  /**
   * Creates the help menu
   * @param menu
   */
  private static void createHelpMenu(JMenuBar menu) {
    JMenu menHelp = new JMenu("Help");
    menHelp.add(UIFactory.createMenuItem("About", menuListener, "About"));
    menu.add(menHelp);		
  }

  /**
   * Creates the chart menu
   * @param menu
   */
  private static void createChartMenu(JMenuBar menu) {
    menDisplay = new JMenu("Charts");
    // For each of the tasks in DisplayDispatch, create a menu
    // entry containing the classname.
    for (int i = 0; i < DisplayDispatch.displays.length; i++) {
      String name = "";
      try {
        // Get the name. Will throw an exception if it can't find the class.
        name = (String)Class.forName("ganttchart.editor.gui.displays.impl." + 
                                     DisplayDispatch.displays[i]).getMethod("getChartName").invoke(null);
      } catch (Exception e) {
        e.printStackTrace();
      }
      // Add the menu item
      DisplayMenuItem newItem = new DisplayMenuItem(name, DisplayDispatch.displays[i]);
      newItem.addActionListener(menuListener);
      newItem.setActionCommand("ShowChart");
      menDisplay.add(newItem);
            
    }
          
    // Add the window options
    menDisplay.add(new JSeparator());
    menDisplay.add(UIFactory.createMenuItem("Show Charts Side by Side", menuListener, "WindowsSideBySide"));
    menDisplay.add(UIFactory.createMenuItem("Close All Charts", menuListener, "CloseAll"));
    menDisplay.setEnabled(enabled);
    menu.add(menDisplay);
  }
  
  /**
   * Creates the task menu
   * @param menu
   */
  private static void createTaskMenu(JMenuBar menu) {
    menTasks = new JMenu("Tasks");
    menTasks.add(UIFactory.createMenuItem("New Task", menuListener, "NewTask"));
    menTasks.setEnabled(enabled);
    menu.add(menTasks);
  }
  
  /**
   * Creates the file menu
   * @param menu The menu bar where to add the file menu
   */
  private static void createFileMenu(JMenuBar menu) {
    JMenu menFile = new JMenu("File");
    menFile.add(UIFactory.createMenuItem("New Project", menuListener, "New"));
    menFile.add(UIFactory.createMenuItem("Open Project", menuListener, "Open"));
    menFile.add(new JSeparator());
    menFile.add(UIFactory.createMenuItem("Edit Project", menuListener, "Edit"));
    menFile.add(new JSeparator());
    menFile.add(UIFactory.createMenuItem("Save Project", menuListener, "Save"));
    menFile.add(UIFactory.createMenuItem("Save As...", menuListener, "SaveAs"));
    menFile.add(new JSeparator());
    menFile.add(UIFactory.createMenuItem("Quit", menuListener, "Quit"));
    
    menu.add(menFile);
  }
  
  /**
   * Set the Java look and feel.
   * http://java.sun.com/docs/books/tutorial/uiswing/examples/lookandfeel/LookAndFeelDemoProject/src/lookandfeel/LookAndFeelDemo.java
   * @see #LOOKANDFEEL
   */
  private static void initLookAndFeel() {
    String lookAndFeel = null;
    lookAndFeel = UIManager.getSystemLookAndFeelClassName();
    
    try {
      UIManager.setLookAndFeel(lookAndFeel);      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Update the window title with the current project name
   */
  private static void updateProgramName() {
    String projectName = currentFileName.substring(currentFileName.lastIndexOf('\\')+1);
    mainWindow.setTitle(PROGRAM_NAME + " - " + projectName);
  }
  
  /**
   * Contains all the actions this window performs.
   * @author Daniel McKenzie
   */
  public static class MenuActionListener extends ChartActionListener {
    
    /**
     * Quit the program.
     * @param arg0 Action args (not used)
     */
    void doQuit(ActionEvent arg0) {
      System.exit(0);
    }
    
    /**
     * Add a new task by loading the task editor. Then update the current
     * chart.
     * @param arg0 Action args (not used)
     */
    void doNewTask(ActionEvent arg0) {
      menDisplay.setEnabled(true);
      TaskEditor editor = new TaskEditor(mainWindow, null, tasks);
      if (editor.getFinalisedTask() != null) {
        ChartInternalFrame topFrame = (ChartInternalFrame)desktop.getSelectedFrame();
        if(topFrame != null){
          topFrame.inform();
          topFrame.repaint();
          mainWindow.repaint();
        }
      }
    }
    
    /**
     * Show a chart in the program. Will first identify if the chart is open
     * first, and bring it to front. Otherwise will create a new window.
     * @param arg0 Action arguments, contains the display menu item which we need.
     */
    void doShowChart(ActionEvent arg0) {
      // Get the item that called this and get the name of the chart
      DisplayMenuItem item = (DisplayMenuItem)arg0.getSource();
      String name = item.getDisplayName();
      
      // First try and see if we already have a chart like this open
      for (int i = 0; i < desktop.getAllFrames().length; i++) {
        ChartInternalFrame chkFrame = (ChartInternalFrame)desktop.getAllFrames()[i];
        if (chkFrame.getChart().getChartIdentifier().equals(name)) {
          chkFrame.toFront();
          return;
        }
      }
      
      // No chart found? Then create it
      ChartDisplay disp = null;
      try {
        // Find the constructor
        Constructor<ChartDisplay> dispConst = (Constructor<ChartDisplay>) Class.forName("ganttchart.editor.gui.displays.impl." + name).getConstructors()[0];
        Object[] initArgs = { tasks, desktop, mainWindow };
        // Create it
        disp = dispConst.newInstance(initArgs);
      } catch (Exception e) {
        System.err.println(e.getClass().getName() + " " + e.getMessage());
        return;
      }
      
      // Create the new internal frame and show it
      
      ChartInternalFrame newchart = new ChartInternalFrame(mainWindow, disp, menuListener);
      newchart.addInternalFrameListener(new ChartWindowListener());
      newchart.setSize(desktop.getWidth(), desktop.getHeight());
      desktop.add(newchart);
      newchart.toFront();
    }
    
    /**
     * Create a new task pool.
     * @param arg0 Action args (not used)
     */
    void doNew(ActionEvent arg0) {
      tasks = null;
      ProjectEditor editor = new ProjectEditor(mainWindow, tasks);
      if(editor.getStarted()){
        tasks = editor.getTaskPool();
        enabled = true;
        menTasks.setEnabled(true);
        mainWindow.setTitle(PROGRAM_NAME + " - " + tasks.getProjectName());
      }
      
    }
    
    /**
     * Edits the project
     * @param arg0 ActionEvent
     */
    
    void doEdit(ActionEvent arg0){
      if(tasks != null){
        new ProjectEditor(mainWindow, tasks);
        mainWindow.setTitle(PROGRAM_NAME + " - " + tasks.getProjectName());
        mainWindow.repaint();
      }else
        JOptionPane.showMessageDialog(null, "You currently have no open project.");
      
    }
    
    /**
     * Open up a project.
     * @param arg0 Action args (not used)
     */
    void doOpen(ActionEvent arg0) {
      open();
    }
    
    /**
     * Save the current file. If this is a new one, then run doSaveAs.
     * @param arg0 Action args (not used)
     * @see #doSaveAs(ActionEvent)
     */
    void doSave(ActionEvent arg0) {
      if(tasks != null)
        save();
      else
        JOptionPane.showMessageDialog(null, "You currently have no open project.");
      
    }
    
    /**
     * Open up a save file dialog, and then save the file to that.
     * @param arg0 Action args (not used)
     */
    void doSaveAs(ActionEvent arg0) {
      if(tasks != null)
        saveAs();
      else
        JOptionPane.showMessageDialog(null, "You currently have no open project.");
    }
    
    /**
     * The Show Charts Side by Side option. It will resize the charts so they
     * all fit nicely horizontally across the screen.
     * @param arg0 Action args (not used)
     */
    void doWindowsSideBySide(ActionEvent arg0) {
      int maxSpace = desktop.getWidth();
      
      // Divide the number of windows by the available space
      // then maximise the windows vertically
      int numberOfWindows = desktop.getAllFrames().length;
      int widthOfWins = maxSpace / numberOfWindows;
      int heightOfWins = desktop.getHeight();
      
      // Resize each window
      for (int i = 0; i < numberOfWindows; i++) {
        ChartInternalFrame thisFrm = (ChartInternalFrame)desktop.getAllFrames()[i];
        // Size the window
        thisFrm.setSize(widthOfWins, heightOfWins);
        thisFrm.setLocation(widthOfWins * i, 0);
      }
      
    }
    
    /**
     * Forces all windows closed.
     * @param arg0 Action args (not used)
     */
    void doCloseAll(ActionEvent arg0) {
      for (int i = 0; i < desktop.getAllFrames().length; i++) {
        desktop.getAllFrames()[i].dispose();
      }
    }
    
    /**
     * Open a little About dialog.
     * @param arg0 Action args (not used)
     */
    void doAbout(ActionEvent arg0) {
      JOptionPane.showMessageDialog(null, "<html><font size=+2>Chronos v."+VERSION+"<font size=+0>\nDevelloped at The University of Waikato & Trinity College Dublin\nDaniel McKenzie, Wim Vanden Broeck, Masood Masoodian, Saturnino Luz",
                                    "About", JOptionPane.INFORMATION_MESSAGE);
    }
  }
  
  /**
   * This is for the internal frame listener. This allows us to update the menu
   * bar whenever a frame is activated.
   * @author Daniel McKenzie
   */
  public static class ChartWindowListener implements InternalFrameListener {
    
    /**
     * Update the menu bar when frame is activated.
     */
    public void internalFrameActivated(InternalFrameEvent arg0) {
      mainWindow.setJMenuBar(createMenu());
    }
    
    // The below is not used, but required
    
    public void internalFrameClosed(InternalFrameEvent arg0) { }
    
    public void internalFrameClosing(InternalFrameEvent arg0) { }
    
    public void internalFrameDeactivated(InternalFrameEvent arg0) { }
    
    public void internalFrameDeiconified(InternalFrameEvent arg0) { }
    
    public void internalFrameIconified(InternalFrameEvent arg0) { }
    
    public void internalFrameOpened(InternalFrameEvent arg0) { }
    
  }
  
  public static class ChronosKeyListener implements KeyListener {
    
    int prevKey = -1;
    int curKey = -1;
    
    @Override
    public void keyPressed(KeyEvent arg0) {
      curKey = arg0.getKeyCode();
      if(prevKey == 17){
        if(curKey == 83)
          save();
        if(curKey == 79)
          open();
      }
      prevKey = curKey;
    }
    
    @Override
    public void keyReleased(KeyEvent arg0) {
      // TODO Auto-generated method stub
      
    }
    
    @Override
    public void keyTyped(KeyEvent arg0) {
      // TODO Auto-generated method stub
      
    }
    
  }
  
  public static void save() {
    // If there is already name, save as is, otherwise, open save as dialog
    if (currentFileName != "" && currentFileName != null) {
      try {
        tasks.saveToFile(currentFileName);
      } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "There was a problem saving the file,\n\n" + e.getCause());
      }
    } else {
      saveAs();
    }		
  }
  
  private static void saveAs() {
    // Create the file chooser
    JFileChooser fc = new JFileChooser();
    //FileNameExtensionFilter xtfFilter = new FileNameExtensionFilter("XML Task Format (*.xtf)", "xtf");
    FileNameExtensionFilter xmlFilter = new FileNameExtensionFilter("XML Format (*.xml)", "xml");
    fc.addChoosableFileFilter(xmlFilter);
    
    fc.setDialogTitle("Save Task File");
    // Open the dialog, if OK, then save the file
    int result = fc.showSaveDialog(null);
    if (result == JFileChooser.APPROVE_OPTION) {
      
      String path = fc.getSelectedFile().getAbsolutePath();
      if (!path.contains(".")) {
        path = path + ".xml";
      }
      try {
        tasks.saveToFile(path);
      } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "There was a problem saving the file as,\n\n" + e.getCause());
      }
      currentFileName = path;
    }		
  }
	
  private static void open() {
    tasks = new TaskPool();
    enabled = true;
    // Create the chooser
    JFileChooser fx = new JFileChooser();
    //FileNameExtensionFilter xtfFilter = new FileNameExtensionFilter("XML Task Format (*.xtf)", "xtf");
    FileNameExtensionFilter xmlFilter = new FileNameExtensionFilter("XML Format (*.xml)", "xml");
          
    fx.addChoosableFileFilter(xmlFilter);
    //fx.addChoosableFileFilter(xtfFilter);
          
    fx.setDialogTitle("Open Task File");
          
    // Open it, and if OK'd load the file
    int result = fx.showOpenDialog(null);
    if (result == JFileChooser.APPROVE_OPTION) {
      // To open a file, first check if there are any windows open
      // and close them all.
      for (int i = 0; i < desktop.getAllFrames().length; i++) {
        desktop.getAllFrames()[i].dispose();
      }
      try {
        tasks.loadFromFile(fx.getSelectedFile().toURI().toString());
        currentFileName = fx.getSelectedFile().getAbsolutePath();
        updateProgramName();
        menDisplay.setEnabled(true);
        menTasks.setEnabled(true);
        mainWindow.setTitle(PROGRAM_NAME + " - " + tasks.getProjectName());
      } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "There was a problem opening the file,\n\n" + e.getCause());
      }
    }			
  }
  
}

