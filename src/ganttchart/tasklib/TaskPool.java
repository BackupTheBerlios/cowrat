package ganttchart.tasklib;

import ganttchart.editor.gui.colors.TaskColor;

import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * A <strong>task pool</strong> contains everything, tasks, subtasks and instances.
 * It also performs saving and loading functions to and from XML format.
 * @author Daniel McKenzie
 */
public class TaskPool {

	private ArrayList<TaskInstance> failedTasks = new ArrayList<TaskInstance>();
	private ArrayList<Task> allTasks;
	
	private ArrayList<TaskColor> taskColors;
	private int dateStyle;
	private boolean detail;
	
	//Project information
	private String projectName;
	private Calendar startDate, endDate;
	
	
	static final int BY_HOUR = 1;
	static final int BY_DAY  = 2;
	
	/**
	 * This must be updated whenever changes are made to the XML.
	 */
	private final int XML_VERSION = 3;
	
	/**
	 * Prepares the pool for use.
	 * @param b 
	 */
	public TaskPool() {
		allTasks = new ArrayList<Task>();
		dateStyle = BY_DAY;
		setProjectName("Untitled");
		startDate = new GregorianCalendar();
		endDate = new GregorianCalendar();
		startDate.set(1900, 01, 01, 0, 0, 0);
		startDate.set(Calendar.MILLISECOND, 0);
		endDate.set(2500, 01, 01, 0, 0, 0);
		endDate.set(Calendar.MILLISECOND, 0);
		initTaskColors();
	}
	
	/**
	 * Prepares pool for use, and sets its name
	 * @param name
	 */
	public TaskPool(String name) {
		allTasks = new ArrayList<Task>();
		dateStyle = BY_DAY;
		setProjectName(name);
		startDate = new GregorianCalendar();
		endDate = new GregorianCalendar();
		startDate.set(1900, 01, 01, 0, 0, 0);
		startDate.set(Calendar.MILLISECOND, 0);
		endDate.set(2500, 01, 01, 0, 0, 0);
		endDate.set(Calendar.MILLISECOND, 0);
		initTaskColors();
	}
	
	/**
	 * Creates the colors used in the project
	 * Could be stored in XML or database, and is easily expanded
	 */
	private void initTaskColors() {
		taskColors = new ArrayList<TaskColor>();
		TaskColor black = new TaskColor(Color.BLACK, "Black");
		TaskColor blue = new TaskColor(Color.BLUE, "Blue");
		TaskColor green = new TaskColor(Color.GREEN,"Green"); 
		TaskColor yellow = new TaskColor(Color.YELLOW, "Yellow");
		TaskColor red = new TaskColor(Color.RED, "Red");
		TaskColor pink = new TaskColor(Color.PINK, "Pink");
		TaskColor orange = new TaskColor(Color.ORANGE, "Orange");
		
		black = initBlackSubs(black);
		blue = initBlueSubs(blue);
		green = initGreenSubs(green);
		yellow = initYellowSubs(yellow);
		red = initRedSubs(red);
		pink = initPinkSubs(pink);
		orange = initOrangeSubs(orange);
		
		taskColors.add(black);
		taskColors.add(blue);
		taskColors.add(green);
		taskColors.add(yellow);
		taskColors.add(red);
		taskColors.add(pink);
		taskColors.add(orange);
	}

	/**
	 * Creates the sub colors for Orange
	 * @param TaskColor orange
	 * @return orange
	 */
	private TaskColor initOrangeSubs(TaskColor orange) {
		orange.addSubTaskColor(new TaskColor(new Color(139,69,0),"Very Dark Orange"));
		orange.addSubTaskColor(new TaskColor(new Color(205,102,0),"Dark Orange"));
		orange.addSubTaskColor(new TaskColor(new Color(255,165,79),"Pale Orange"));
		orange.addSubTaskColor(new TaskColor(new Color(255,130,71),"Sienne Orange"));
		orange.addSubTaskColor(new TaskColor(new Color(255,97,3),"Bright Orange"));
		return orange;
	}

	/**
	 * Creates the sub colors for Pink
	 * @param TaskColor pink
	 * @return pink
	 */
	private TaskColor initPinkSubs(TaskColor pink) {
		pink.addSubTaskColor(new TaskColor(new Color(255,182,193),"Very Light Pink"));
		pink.addSubTaskColor(new TaskColor(new Color(139,95,101),"Dark Pink"));
		pink.addSubTaskColor(new TaskColor(new Color(255,62,150),"Bright Pink"));
		pink.addSubTaskColor(new TaskColor(new Color(139,10,80),"Deep Pink"));
		pink.addSubTaskColor(new TaskColor(new Color(255,131,250),"Light Pink"));
		return pink;
	}

	/**
	 * Creates the sub colors for Red
	 * @param red
	 * @return red
	 */
	private TaskColor initRedSubs(TaskColor red) {
		red.addSubTaskColor(new TaskColor(new Color(128,0,0),"Maroon Red"));
		red.addSubTaskColor(new TaskColor(new Color(238,0,0),"Dull Red"));
		red.addSubTaskColor(new TaskColor(new Color(255,48,48),"Pale Red"));
		red.addSubTaskColor(new TaskColor(new Color(255,99,71),"Light Red"));
		red.addSubTaskColor(new TaskColor(new Color(240,128,128),"Very Light Red"));
		return red;
	}

	/**
	 * Creates the sub colors for yellow
	 * @param yellow
	 * @return yellow
	 */
	private TaskColor initYellowSubs(TaskColor yellow) {
		yellow.addSubTaskColor(new TaskColor(new Color(238,238,0),"Pale Yellow"));
		yellow.addSubTaskColor(new TaskColor(new Color(205,205,0),"Dark Yellow"));
		yellow.addSubTaskColor(new TaskColor(new Color(139,139,0),"Gold Yellow"));
		yellow.addSubTaskColor(new TaskColor(new Color(128,128,0),"Olive"));
		yellow.addSubTaskColor(new TaskColor(new Color(255,246,143),"Light Yellow"));
		return yellow;	
	}

	/**
	 * Creates the sub colors for green
	 * @param green
	 * @return green
	 */
	private TaskColor initGreenSubs(TaskColor green) {
		green.addSubTaskColor(new TaskColor(new Color(0,100,0),"Dark Green"));
		green.addSubTaskColor(new TaskColor(new Color(162,205,90),"Olive Green"));
		green.addSubTaskColor(new TaskColor(new Color(152,251,152),"Pale Green"));
		green.addSubTaskColor(new TaskColor(new Color(124,252,0),"Light Green"));
		green.addSubTaskColor(new TaskColor(new Color(69,139,116),"Green-Blue"));
		return green;
	}

	/**
	 * Creates the sub colors for blue
	 * @param blue
	 * @return blue
	 */
	private TaskColor initBlueSubs(TaskColor blue) {
		blue.addSubTaskColor(new TaskColor(new Color(0,0,128),"Navy Blue"));
		blue.addSubTaskColor(new TaskColor(new Color(100,149,237),"Blue"));
		blue.addSubTaskColor(new TaskColor(new Color(61,89,171),"Cobolt Blue"));
		blue.addSubTaskColor(new TaskColor(new Color(30,144,255),"Light Blue"));
		blue.addSubTaskColor(new TaskColor(new Color(135,206,250),"very Light Blue"));
		return blue;
	}

	/**
	 * Creates the sub colors for black
	 * @param black
	 * @return black
	 */
	private TaskColor initBlackSubs(TaskColor black) {
		black.addSubTaskColor(new TaskColor(new Color(32,32,32),"Very Dark Grey"));
		black.addSubTaskColor(new TaskColor(new Color(88,88,88),"Dark Grey"));
		black.addSubTaskColor(new TaskColor(new Color(136,136,136),"Grey"));
		black.addSubTaskColor(new TaskColor(new Color(184,184,184),"Light Grey"));
		black.addSubTaskColor(new TaskColor(new Color(232,232,232),"Very Light Grey"));
		return black;
	}
	
	/**
	 * Adds a task if the task as been defined.
	 * @param newTask
	 */
	public void addTask(Task newTask) {
		allTasks.add(newTask);
	}
	
	/**
	 * Sets the field of a Calendar to 0, except year, month and day
	 * @param date Calendar
	 */
	private Calendar setCalendarFields(Calendar date){
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE,0);
		date.set(Calendar.SECOND,0);
		date.set(Calendar.MILLISECOND,0);
		return date;
	}
	
	/**
	 * Creates a task, gives it the name specified and
	 * adds it to the list.
	 * @param name
	 */
	public void addTask(String name) {
		Task newTask = new Task();
		newTask.setName(name);
		allTasks.add(newTask);
	}
	
	/**
	 * Get the task at the index.
	 * @param index
	 * @return The task at the index.
	 */
	public Task getTask(int index) {
		return allTasks.get(index);
	}
	
	/**
	 * Returns the number of tasks in the list.
	 * @return Number of tasks.
	 */
	public int getNumberOfTasks() {
		return allTasks.size();
	}
	
	/**
	 * Returns the number of tasks and subtasks they contain.
	 * @return Number of tasks and subtasks.
	 */
	public int getNumberOfTasksAndSubTasks() {
		int numTasks = getNumberOfTasks();
		
		int numAll = numTasks;
		
		for (int i = 0; i < numTasks; i++) {
			numAll += getTask(i).getNumberOfSubTasks();
		}
		return numAll;
	}
	
	/**
	 * Returns the earliest date in the entire pool
	 * @return The earliest date.
	 */
	public Calendar getEarliestStart() {
		/**
		 * This bit of code is changed when the project itself got a start and end date
		 */
		/*Calendar earliest = null;
		for (int i = 0; i < getNumberOfTasks(); i++) {
			// The first instance is always going to be the earliest for that
			// task.
			if(getTask(i).getNumberOfInstances() > 0){
				
				if (earliest == null) {
					
					earliest = getTask(i).getInstance(0).getStart();
				} else {
					if (getTask(i).getInstance(0).getStart().before(earliest)) {
						earliest = getTask(i).getInstance(0).getStart();
					}
				}
			}
		}*/
		return startDate;
	}
	
	/**
	 * Returns the latest date in the entire pool
	 * @return The latest date.
	 */
	public Calendar getLatestEnd() {
		/*Calendar latest = null;
		for (int i = 0; i < getNumberOfTasks(); i++) {
			// The last instance is always going to be the latest
			// for that task.
			if (latest == null) {
				latest = getTask(i).getInstance(getTask(i).getNumberOfInstances() - 1).getEnd();
			} else {
				Task task = getTask(i);
				if (task.getInstance(task.getNumberOfInstances() -1).getEnd().after(latest)) {
					latest = task.getInstance(task.getNumberOfInstances() -1).getEnd();
				}
			}
		}*/
		return endDate;
	}
	
	/**
	 * Returns the number of days this pool lasts for.
	 * @return Number of days.
	 */
	public int getNumberOfDays() {
		
		Calendar start = null;
		Calendar end = null;
		if(getEarliestStart() != null){
			start = (Calendar)getEarliestStart().clone();
			end = (Calendar)getLatestEnd().clone();
		}
		int days = 0;
		// Keep adding a day until start reaches end.
		if(start != null){
			while (start.compareTo(end) <= 0) {
				start.add(Calendar.DAY_OF_YEAR, 1);
				days++;
			}
		}
		return days;
	}
	
	/**
	 * Returns the number of days between the earliest date represented
	 * in this pool, and the date provided.
	 * @param check A Calendar representing the date to check to.
	 * @return Number of days.
	 */
	public int getNumberOfDaysFromEarliest(Calendar check) {
		int days = 0;
		
		Calendar start = (Calendar)getEarliestStart().clone();
		while (start.compareTo(check) <= 0) {
			start.add(Calendar.DAY_OF_YEAR, 1);
			days++;
		}
		return days;
	}
	
	/**
	 * Set the date style.
	 * @param style Use contsants, either BY_HOUR or BY_DAY.
	 */
	public void setTaskDateStyle(int style) {
		if (style == BY_HOUR || style == BY_DAY) {
			dateStyle = style;
		}
	}
	
	/**
	 * Get the current date style.
	 * @return BY_HOUR or BY_DAY.
	 */
	public int getTaskDateStyle() {
		return dateStyle;
	}
	
	/**
	 * This will save the task pool to an XML file at the name and path specified.
	 * 
	 * Help in developing this function was found at
	 * http://www.rgagnon.com/javadetails/java-0530.html
	 * @param fileName The name of the file to save to
	 * @return True always
	 * @throws ParserConfigurationException
	 * @throws TransformerFactoryConfigurationError
	 * @throws IOException
	 * @throws TransformerException
	 */
	public boolean saveToFile(String fileName) throws ParserConfigurationException, TransformerFactoryConfigurationError, IOException, TransformerException {

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder xmlBuilder = builderFactory.newDocumentBuilder();
		DOMImplementation implementation = xmlBuilder.getDOMImplementation();
		
		Document theDoc = implementation.createDocument(null, null, null);
		
		// Create the root element
		Element root = theDoc.createElement("pool");
		root.setAttribute("version", Integer.toString(XML_VERSION));
		root.setAttribute("datestyle", Integer.toString(dateStyle));
		root.setAttribute("detail", String.valueOf(detail));
		root.setAttribute("name", projectName);
		root.setAttribute("start", Long.toString(getStartDate().getTimeInMillis()));
		root.setAttribute("end", Long.toString(getEndDate().getTimeInMillis()));
		
		theDoc.appendChild(root);
		// For each task
		for (Task thisTask : allTasks) {
			// Create the task element
			Element taskElement = theDoc.createElement("task");
			Color itsColor = thisTask.getColor();
			taskElement.setAttribute("color", Integer.toString(itsColor.getRGB()));
			taskElement.setAttribute("name", thisTask.getName());
			root.appendChild(taskElement);
			
			// Create nodes for subtasks and instances to hang off
			Element subTaskNode = theDoc.createElement("definitions");
			taskElement.appendChild(subTaskNode);
			Element instanceNode = theDoc.createElement("instances");
			taskElement.appendChild(instanceNode);
			
			// For each subtask definition
			for (Iterator<SubTask> iSubTask = thisTask.getAllSubTaskDefinitions().iterator(); iSubTask.hasNext(); ) {
				SubTask thisSubTask = iSubTask.next();
				// Create the subtask element
				Element subTaskElement = theDoc.createElement("subtaskdefinition");
				subTaskElement.setAttribute("name", thisSubTask.getName());
				subTaskElement.setAttribute("color", Integer.toString(thisSubTask.getColor().getRGB()));
				subTaskNode.appendChild(subTaskElement);
				// For each sub task instance
				for (SubTaskInstance thisSTInstance : thisSubTask.getSubTaskInstance()) {
					// Create the sub task instance element
					Element stElement = theDoc.createElement("subtask");
					stElement.setAttribute("start", Long.toString(thisSTInstance.getStart().getTimeInMillis()));
					stElement.setAttribute("end", Long.toString(thisSTInstance.getEnd().getTimeInMillis()));
					subTaskElement.appendChild(stElement);
				}
				//creates attributes node to hang off
				Element stAttributes = theDoc.createElement("attributes");
				subTaskElement.appendChild(stAttributes);
				for(int i = 0; i < thisSubTask.getAttributes().size(); i++){
					Element stAttribute = theDoc.createElement("attribute");
					stAttribute.setAttribute("name", thisSubTask.getAttributes().get(i));
					stAttributes.appendChild(stAttribute);
				}
			}
			// For each instance
			for (TaskInstance thisInstance : thisTask.getAllInstances()) {
				// Create the instance element
				Element instanceElement = theDoc.createElement("instance");
				instanceElement.setAttribute("start", Long.toString(thisInstance.getStart().getTimeInMillis()));
				instanceElement.setAttribute("end", Long.toString(thisInstance.getEnd().getTimeInMillis()));
				instanceNode.appendChild(instanceElement);
			}
		}
		
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		// initialise StreamResult with File object to save to file
		StreamResult result = new StreamResult(new FileWriter(fileName));
		DOMSource source = new DOMSource(theDoc);
		transformer.transform(source, result);

		return true;
		
	}
	
	/**
	 * Creates the Task Pool from scratch with the data in the XML file.
	 * @param fileName The name of the file to load from
	 * @return True always
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public boolean loadFromFile(String fileName) throws ParserConfigurationException, SAXException, IOException {
		// Reset the list
		allTasks = new ArrayList<Task>();
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder xmlBuilder = builderFactory.newDocumentBuilder();
		
		// Read in the file
		Document dom = xmlBuilder.parse(fileName);
		
		// Get the root node and make sure the versions match
		Element root = dom.getDocumentElement();
		if (Integer.parseInt(root.getAttribute("version")) != XML_VERSION) {
			return false;
		}
		
		// Get the date style (if it is set).
		if (!root.getAttribute("datestyle").equals("")) {
			setTaskDateStyle(Integer.parseInt(root.getAttribute("datestyle")));
		} else {
			setTaskDateStyle(BY_DAY);
		}
		
		// Get the detail level
		if(!root.getAttribute("detail").equals("")){
			setDetail(root.getAttribute("detail"));
		}else{
			setDetail(false);
		}
		
		//Get the project name
		if(!root.getAttribute("name").equals(""))
			projectName = root.getAttribute("name");
		else
			projectName = "Untitled";

		//Get the start and end date of the project
		setStartDate(Long.parseLong(root.getAttribute("start")));
		setEndDate(Long.parseLong(root.getAttribute("end")));

		
		// For each of the child nodes
		for (int i = 0; i < root.getChildNodes().getLength(); i++) {
			// Skip if it isn't a task node
			if (root.getChildNodes().item(i).getNodeName() != "task") {
				continue;
			} else {
				// Create a new task item and add it to the pool
				Element taskElem = (Element) root.getChildNodes().item(i);
				Task newTask = new Task();
				newTask.setName(taskElem.getAttribute("name"));
				newTask.setColor(Integer.parseInt(taskElem.getAttribute("color")));
				TaskColor col = getTaskColor(newTask.getColor());
				col.setInUse(true);
				this.addTask(newTask);
				// For each of the task child nodes
				for (int j = 0; j < taskElem.getChildNodes().getLength(); j++) {
					// Skip it if it is a text node
					if (taskElem.getChildNodes().item(j).getNodeType() == Node.TEXT_NODE) {
						continue;
					}
					// Get the element
					Element observedElem = (Element) taskElem.getChildNodes().item(j);
					boolean isDefinitions = false;
					// Check if it is a definitions block, otherwise it is an instances block
					if (observedElem.getNodeName().equals("definitions")) {
						isDefinitions = true;
					}
					
					// For each of these child nodes
					for (int k = 0; k < observedElem.getChildNodes().getLength(); k++) {
						// If it is a text node, skip it
						if (observedElem.getChildNodes().item(k).getNodeType() == Node.TEXT_NODE) {
							continue;
						}
						// Get this node
						Element childElement = (Element) observedElem.getChildNodes().item(k);
						
						// If it is a subtask definition
						if (isDefinitions && childElement.getNodeName().equals("subtaskdefinition")) {
							// Add the definition
							String defName = childElement.getAttribute("name");
							Color defColor = new Color(Integer.parseInt(childElement.getAttribute("color")));
							SubTask sub = new SubTask(newTask);
							sub.setName(defName); sub.setColor(defColor);
							
							TaskColor subCol = getSubTaskColor(col, defColor);
							if(subCol == null)
								System.out.println("Error @ " + sub.getName() + " " + newTask.getName());
							else
								subCol.setInUse(true);
							
							newTask.addSubTask(sub);
							// Get the nodes to check for any subtask elements
							for (int l = 0; l < childElement.getChildNodes().getLength(); l++) {
								// If it's a text node, skip it
								if (childElement.getChildNodes().item(l).getNodeType() == Node.TEXT_NODE) {
									continue;
								}
								// Get the node
								Element stiElement = (Element) childElement.getChildNodes().item(l);
								// If it is a subtask
								if (stiElement.getNodeName().equals("subtask")) {
									// Add the subtask
									Long startILong = Long.parseLong(stiElement.getAttribute("start"));
									Long endILong = Long.parseLong(stiElement.getAttribute("end"));
									sub.addSubTaskInstance(startILong, endILong);					
								}else if(stiElement.getNodeName().equals("attributes")){
										for(int m = 0; m < stiElement.getChildNodes().getLength(); m++){
											if (stiElement.getChildNodes().item(m).getNodeType() == Node.TEXT_NODE) {
												continue;
											}
											Element stAttribute = (Element) stiElement.getChildNodes().item(m);
											sub.addAttribute(stAttribute.getAttribute("name"));
										}
								}
							}
						} else if (!isDefinitions && childElement.getNodeName().equals("instance")) {
							// If it is an instance
							// add the instance
							TaskInstance newinst = new TaskInstance(newTask);
							Long startLong = Long.parseLong(childElement.getAttribute("start"));
							Long endLong = Long.parseLong(childElement.getAttribute("end"));
							newinst.setStart(startLong);
							newinst.setEnd(endLong);
							if(!newTask.addInstance(newinst)){
								failedTasks.add(newinst);
							}		
						}
					}
				}
			}
		}

		if(failedTasks.size() > 0){
			String out = "";
			for(TaskInstance inst:failedTasks){
				out += "Failed: " + inst.getOwner().getName() + " " + inst.getStart().getTime().toString() + "-" + inst.getEnd().getTime().toString() + "\n";
			}
			JOptionPane.showMessageDialog(null, "There were " + failedTasks.size() + " failed entries: " + out);
		}
		return true;
	}

	/**
	 * Sets the endDate of the project
	 * @param parseLong
	 */
	private void setEndDate(long parseLong) {
		endDate.setTimeInMillis(parseLong);
	}

	/**
	 * Sets the starting date of the project
	 * @param parseLong
	 */
	private void setStartDate(long parseLong) {
		startDate.setTimeInMillis(parseLong);
	}

	/**
	 * Sets the detail level of the project
	 * @param attribute String representation of true or false
	 */
	private void setDetail(String attribute) {
		if(attribute.toLowerCase().equals("true")){
			detail = true;
		}else if (attribute.toLowerCase().equals("false")){
			detail = false;
		}else
			detail = false;
	}

	/**
	 * Sets the detail level of the project
	 * @param detail Boolean
	 */
	public void setDetail(boolean detail) {
		this.detail = detail;
	}
	
	/**
	 * Returns the detail level, true for high detail, false for low detail
	 * @return detail A boolean representation
	 */
	public boolean getDetail(){
		return detail;
	}

	/**
	 * Checks whether the TaskPool contains a task using its name
	 * @param text The task name
	 * @return true or false
	 */
	public boolean contain(String text) {
		for(int i = 0; i < getNumberOfTasks(); i++){
			if(getTask(i).getName().equals(text))
				return true;
		}
		return false;
	}

	/**
	 * Sets the project name
	 * @param projectName
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	/**
	 * Returns the project name
	 * @return projectName The project's name
	 */
	public String getProjectName() {
		return projectName;
	}

	/**
	 * Sets the endDate for the project
	 * @param endDate Calendar
	 */
	public void setEndDate(Calendar endDate) {
		this.endDate = setCalendarFields(endDate);
	}

	/**
	 * Gets the endDate of the project
	 * @return endDate Calendar
	 */
	public Calendar getEndDate() {
		return endDate;
	}

	/**
	 * Sets the startDate of the project
	 * @param startDate Calendar
	 */
	public void setStartDate(Calendar startDate) {
		this.startDate = setCalendarFields(startDate);
	}

	/**
	 * Gets the startDate of the project
	 * @return startDate Calendar
	 */
	public Calendar getStartDate() {
		return startDate;
	}

	/**
	 * Sets all the tasks (un)folded
	 * @param b Boolean, true for folded, false for unfolded
	 */
	public void setFolded(boolean b) {
		for(int i = 0; i < getNumberOfTasks(); i++){
			getTask(i).setFolded(b);
		}
	}

	/**
	 * Completely removes a task and its children
	 * @param theTask
	 */
	public void delete(Task theTask) {
		theTask.clear();
		allTasks.remove(theTask);	
	}

	/**
	 * Get a task from the TaskPool giving a task
	 * @param clickedTask
	 * @return task
	 */
	public Task getTask(Task clickedTask) {
		for(int i = 0; i < getNumberOfTasks(); i++){
			if(getTask(i).getName() == clickedTask.getName()){
				return getTask(i);
			}
		}
		return null;
	}

	/**
	 * Converts to the detail mode given
	 * @param detail The detail mode, true-high, false-low
	 */
	public void convertToDetail(boolean detail) {
		if(this.detail){
			if(detail == false){
				lowerDetail();
			}
		}else{
			if(detail == true){
				higherDetail();
			}
		}
	}

	/**
	 * Sets the Pool on high Detail (subtasks)
	 */
	private void higherDetail() {
		detail = true;
		for(int i = 0; i < allTasks.size(); i++){
			SubTask sub = new SubTask(allTasks.get(i));
			sub.setName("SubTask");
			sub.setColor(Color.BLUE);
			allTasks.get(i).addSubTask(sub);
			for(int j = 0; j < allTasks.get(i).getAllInstances().size(); j++){
				sub.addSubTaskInstance(allTasks.get(i).getInstance(j).getStart(), allTasks.get(i).getInstance(j).getEnd());
			}
		}
	}

	/**
	 * Sets the Pool on low detail (only tasks)
	 */
	private void lowerDetail() {
		detail = false;
		for(int i = 0; i < allTasks.size(); i++){
			allTasks.get(i).removeAllSubTasks();
		}
	}

	/**
	 * Set the TaskColors
	 * @param taskColors
	 */
	public void setTaskColors(ArrayList<TaskColor> taskColors) {
		this.taskColors = taskColors;
	}

	/**
	 * returns the TaskColors
	 * @return taskColors
	 */
	public ArrayList<TaskColor> getTaskColors() {
		return taskColors;
	}
	
	/**
	 * Gets the color of a subtask
	 * @param col The TaskColor of the owning Task
	 * @param subCol The rgb representation of the TaskColor
	 * @return TaskColor out
	 */
	public TaskColor getSubTaskColor(TaskColor col, Color subCol){
		TaskColor out = null;
		for(int i = 0; i < col.getSubColors().size(); i++){
			if(subCol.equals(col.getSubColors().get(i).getColor())){
				out = col.getSubColors().get(i);
			}
		}
		return out;
	}

	/**
	 * Returns the TaskColor given a color
	 * @param color
	 * @return taskColor
	 */
	public TaskColor getTaskColor(Color color) {
		TaskColor taskColor = null;
		for(int i = 0; i < taskColors.size(); i++){
			if(color.equals(taskColors.get(i).getColor())){
				taskColor = taskColors.get(i);
			}
		}
		return taskColor;
	}

	/**
	 * Revalidates the Pool after changes
	 */
	public void revalidate() {
		for (Task t : allTasks) {
			t.revalidate();			
		}
	}

	public void moveTaskInArray(Task clicked, boolean direction) {
		Task aid;
		for(int i = 0; i < allTasks.size(); i++){
			if(allTasks.get(i).equals((clicked))){
				if(direction){
					if( i > 0 ){
						aid = allTasks.get(i);
						allTasks.set(i, allTasks.get(i-1));
						allTasks.set(i-1, aid);
						break;
					}
				}else{
					if(i < allTasks.size()-1){
						aid = allTasks.get(i);
						allTasks.set(i, allTasks.get(i+1));
						allTasks.set(i+1, aid);
						break;
					}
				}
			}
		}		
	}

	public int getTaskID(Task task) {
		int out = -1;
		for(int i = 0; i < allTasks.size(); i++){
			if(allTasks.get(i).equals(task))
				out = i;
		}
		return out;
	}
}
