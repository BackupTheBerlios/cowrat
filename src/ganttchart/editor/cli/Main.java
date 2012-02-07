package ganttchart.editor.cli;

import ganttchart.tasklib.SubTaskInstance;
import ganttchart.tasklib.Task;
import ganttchart.tasklib.TaskInstance;
import ganttchart.tasklib.TaskPool;

import java.awt.Color;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Main {

	// This contains everything
	private static TaskPool taskPool;
	
	/**
	 * This contains all the commands that someone can enter.
	 * The idea for this came from
	 * http://www.xefer.com/2006/12/switchonstring
	 */
	public enum Actions { 
		NV, HELP, QUIT, NEW, SHOW, SAVE, LOAD;
		
	    public static Actions thisAction(String str)
	    {
	        try {
	            return valueOf(str);
	        } 
	        catch (Exception ex) {
	            return NV;
	        }
	    }   

	};
	
	/**
	 * The main function, continously loops until the user types QUIT.
	 * All the commands are controlled by the Actions enum. It will execute
	 * the function specified in the switch statement.
	 * @param args
	 * @throws IOException 
	 */	
	public static void main(String[] args) throws IOException {
		// Create a new pool
		taskPool = new TaskPool();
		
		// Create the relevant inputs
		InputStreamReader inReader = new InputStreamReader(System.in);
		BufferedReader stdin = new BufferedReader(inReader);
		System.out.println("Welcome to the CLI editor");
		while (true) { // Loop forever
			System.out.print("Do: ");
			String action = "";
			try {
				action = stdin.readLine();
			} catch(Exception e) {
				
			}
			// Actions are all case sensitive
			action = action.toUpperCase();
			
			String[] allParams = action.split(" ");
			switch (Actions.thisAction(allParams[0])) {
			case HELP:
				showHelp();
				break;
			case QUIT:
				System.exit(0);
				break;
			case NEW:
				try {
					createNew(stdin, allParams[1]);
				} catch (ArrayIndexOutOfBoundsException e) {
					System.out.println("new [task]");
				}
				break;
			case SHOW:
				walkTree(stdin);
				break;
			case SAVE:
				saveToFile(stdin);
				break;
			case LOAD:
				loadFromFile(stdin);
				break;
			default:
				System.out.println("That isn't an option, type 'help' for help.");
			}
		}
	}

	/**
	 * Requests the filename to be saved to and calls the saveToFile command
	 * in the task pool.
	 * @param stdin
	 * @throws IOException
	 */
	private static void saveToFile(BufferedReader stdin) throws IOException {
		System.out.println("==SAVE TO FILE==");
		System.out.print("Path and name: ");
		String filename = stdin.readLine();
		try {
			taskPool.saveToFile(filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done!");
	}
	
	/**
	 * Requests for the file path to load and calls the task pool
	 * to load it in. Outputs an error if there are file version
	 * differences.
	 * @param stdin
	 */
	private static void loadFromFile(BufferedReader stdin) {
		System.out.println("==LOAD FROM FILE==");
		System.out.print("Path: ");
		String filename = "";
		try {
			filename = stdin.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			boolean result = taskPool.loadFromFile(filename);
			if (!result) {
				System.out.println("Nothing was loaded. This could be due to version differences.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * A function that passes on to other functions to perform its specified
	 * duties. inparam only accepts TASK, INSTANCE and SUBTASK.
	 * @param stdin
	 * @param inparam
	 * @throws IOException
	 */
	private static void createNew(BufferedReader stdin, String inparam) throws IOException {
		if (inparam.equals("TASK")) {
			createNewTask(stdin);
		} else if (inparam.equals("INSTANCE")) {
			createNewInstance(stdin);
		} else if (inparam.equals("SUBTASK")) {
			createNewSubTask(stdin);
		}
	}

	/**
	 * Will request the name of the task and add it to the pool.
	 * @param stdin
	 * @throws IOException
	 */
	private static void createNewTask(BufferedReader stdin) throws IOException {
		System.out.println("==CREATE NEW TASK==");
		System.out.print("Name of Task: ");
		String taskName = stdin.readLine();
		if (taskName.equals("cancel")) {
			return;
		}
		taskPool.addTask(taskName);
		System.out.println("Done!");
	}
	
	/**
	 * First requests a task ID to add the instance to, then the start date,
	 * and end date then create a new instance in the task.
	 * @param stdin
	 * @throws IOException
	 */
	private static void createNewInstance(BufferedReader stdin) throws IOException {
		System.out.println("==CREATE NEW INSTANCE==");
		// Get the ID
		System.out.print("Task ID: ");
		String taskID = stdin.readLine();
		if (taskID.equals("cancel")) {
			return;
		}
		
		// Ask for the start date
		System.out.print("Start Day (dd/mm/yyyy): ");
		String instStart = stdin.readLine();
		if (instStart.equals("cancel")) {
			return;
		}
		
		// Construct a calendar object
		String[] dateParts = instStart.split("/");
		if (dateParts.length != 3) {
			System.out.println("Not enough date parts.");
			return;
		}
		Calendar startDate = new GregorianCalendar(Integer.valueOf(dateParts[2]),
				Integer.valueOf(dateParts[1]) - 1,
				Integer.valueOf(dateParts[0]));
		
		// Ask for the end date
		System.out.print("End Day (dd/mm/yyyy): ");
		String instEnd = stdin.readLine();
		if (instEnd.equals("cancel")) {
			return;
		}
		
		// Construct the calendar object
		String[] datePartsE = instEnd.split("/");
		if (datePartsE.length != 3) {
			System.out.println("Not enough date parts.");
			return;
		}
		Calendar endDate = new GregorianCalendar(Integer.valueOf(datePartsE[2]),
				Integer.valueOf(datePartsE[1]) - 1,
				Integer.valueOf(datePartsE[0]));
		
		// Add the date, output an error if failed.
		TaskInstance inst = new TaskInstance(taskPool.getTask(Integer.valueOf(taskID)));
		inst.setStart(startDate); inst.setEnd(endDate);
		if (taskPool.getTask(Integer.valueOf(taskID)).addInstance(inst)) {
			System.out.println("Done!");
		} else {
			System.out.println("Dates clash with another instance.");
		}
	}
	
	/**
	 * Asks for a task ID and an instance ID. It will then provide a list of subtasks
	 * already created, which the user can type the id number for or write a new subtask name.
	 * It will then ask for the start and end dates. It will pass all this to:
	 * * The Task if it is new
	 * * The instance if it is old.
	 * @param stdin
	 * @throws IOException
	 */
	private static void createNewSubTask(BufferedReader stdin) throws IOException {
		System.out.println("==CREATE NEW SUBTASK==");
		// Get the task ID to use
		System.out.print("Task ID: ");
		String taskID = stdin.readLine();
		if (taskID.equals("cancel")) {
			return;
		}
		
		// Get the instance ID to use
		System.out.print("Instance ID: ");
		String instanceID = stdin.readLine();
		if (instanceID.equals("cancel")) {
			return;
		}

		Task theTask = taskPool.getTask(Integer.valueOf(taskID));
		
		// Get a list of subtask definitions and output them
		System.out.println("This task contains:");
		for(int i = 0; i < theTask.getNumberOfSubTasks(); i++) {
			System.out.println(Integer.toString(i) + ": " + theTask.getSubTask(i).getName());
		}
		System.out.println("Use a current definition by entering its number, otherwise, choose a name.");
		System.out.print("Name: ");
		// Get the name. If we have a number, then assume it's one of
		// the definitions this person wants. Otherwise, it's new.
		String subName = stdin.readLine();
		int curDefID;
		try {
			curDefID = Integer.parseInt(subName);
		} catch (NumberFormatException e) {
			curDefID = -1;
		}
		if (subName.equals("cancel")) {
			return;
		}
		
		// Get the start and end dates
		System.out.print("Start Day (dd/mm/yyyy): ");
		String subStart = stdin.readLine();
		if (subStart.equals("cancel")) {
			return;
		}
		
		String[] dateParts = subStart.split("/");
		if (dateParts.length != 3) {
			System.out.println("Not enough date parts.");
			return;
		}
		Calendar startDate = new GregorianCalendar(Integer.valueOf(dateParts[2]),
				Integer.valueOf(dateParts[1]) - 1,
				Integer.valueOf(dateParts[0]));
		
		System.out.print("End Day (dd/mm/yyyy): ");
		String subEnd = stdin.readLine();
		if (subEnd.equals("cancel")) {
			return;
		}
		
		String[] datePartsE = subEnd.split("/");
		if (datePartsE.length != 3) {
			System.out.println("Not enough date parts.");
			return;
		}
		
		Calendar endDate = new GregorianCalendar(Integer.valueOf(datePartsE[2]),
				Integer.valueOf(datePartsE[1]) - 1,
				Integer.valueOf(datePartsE[0]));
		
		// Add the instance
		if (curDefID >= 0) { // Use a definition
			TaskInstance theTaskInstance = theTask.getInstance(Integer.parseInt(instanceID));
			//theTaskInstance.addSubTaskInstance(curDefID, startDate, endDate);
		} else { // Create a new definition
//			taskPool.getTask(Integer.valueOf(taskID)).addSubTaskWithInstance(subName, Color.black, startDate, endDate,
					taskPool.getTask(Integer.parseInt(taskID)).getInstance(Integer.parseInt(instanceID));
		}
		
		//newSubTask.addInstance(startDate, endDate);
		//taskPool.allTasks.get(Integer.valueOf(taskID)).allInstances.get(Integer.valueOf(instanceID)).addSubTask(newSubTask);
		System.out.println("Done!");
	}
	
	/**
	 * Will go through the entire task pool and output
	 * all the data it knows about the pool.
	 * @param stdin
	 */
	private static void walkTree(BufferedReader stdin) {
		System.out.println("==WALKING==");
		
		// For each task
		for (int i = 0; i < taskPool.getNumberOfTasks(); i++) {
			Task thisTask = taskPool.getTask(i);
			System.out.println("Task " + Integer.toString(i) + ": " + thisTask.getName());
			
			// For each instance
			for (int j = 0; j < thisTask.getNumberOfInstances(); j++) {
				TaskInstance thisInstance = thisTask.getInstance(j);
				System.out.print("\tInstance " + Integer.toString(j) + ": ");
				
				SimpleDateFormat dtFormatter = new SimpleDateFormat("dd/MM/yyyy");
				System.out.print("Start: " + dtFormatter.format(thisInstance.getStart().getTime()));
				System.out.println(" End: " + dtFormatter.format(thisInstance.getEnd().getTime()));
				
				// For each sub task instance
				/*for (int k = 0; k < thisInstance.getNumberOfSubTaskInstances(); k++) {
					SubTaskInstance thisSubTask = thisInstance.getSubTaskInstance(k);
					System.out.print("\t\tSubtask " + Integer.toString(thisSubTask.getSubTaskDefinitionID()));
					System.out.println(": " + thisSubTask.getDefinitionName());
					System.out.print("\t\t\tStart: " + dtFormatter.format(thisSubTask.getStart()));
					System.out.println(" End: " + dtFormatter.format(thisSubTask.getEnd()));
				}*/
			}
		}
		System.out.println("End of Tree");
	}

	/**
	 * Outputs a helpful message with the commands available.
	 */
	private static void showHelp() {
		System.out.println("==HELP==");
		System.out.println("The following actions are available:");
		System.out.println("new [task|instance|subtask], show, save, load, help, quit");
		System.out.println("Use word 'cancel' to cancel.");
		
	}

}
