package ganttchart.tasklib;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * This is the Task. It contains all its instances and subtask definitions.
 * @author Daniel McKenzie
 *
 */
public class Task {

	private ArrayList<TaskInstance> allInstances;
	private ArrayList<SubTask> allSubTaskDefinitions;
	private Color thisColor;
	private String taskName;
	private boolean folded;
	
	/**
	 * Prepares the Task.
	 */
	public Task() {
		setAllInstances(new ArrayList<TaskInstance>());
		setAllSubTaskDefinitions(new ArrayList<SubTask>());
		thisColor = Color.black;
		folded = true;
	}
	
	/**
	 * Sets the datefields so that all the non-used fields won't give problems
	 * @param date
	 * @return date
	 */
	public Calendar setDateFields(Calendar date){
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		return date;
	}
	
	/**
	 * Adds the instance, making sure it doesn't clash with
	 * any other instance, and makes sure the list of instances is
	 * in order.
	 * @param newInstance
	 * @return False if unsuccessful.
	 */
	//Boolean only used for the cmb input
	public boolean addInstance(TaskInstance newInstance) {
		int locationID = 0;
		if(!checkInstance(newInstance.getStart(), newInstance.getEnd())){
			for (int i = 0; i < allInstances.size(); i++) {
				TaskInstance checkInst = allInstances.get(i);
				if (newInstance.getStart().after(checkInst.getStart())) {
					locationID = i + 1;
				}
			}
			allInstances.add(locationID, newInstance);
		}
		return true;
	}
	
	/**
	 * Get the instance with the ID.
	 * @param index The instance ID.
	 * @return A TaskInstance that matches the ID.
	 */
	public TaskInstance getInstance(int index) {
		return allInstances.get(index);
	}
	
	/**
	 * Get the number of instances.
	 * @return Integer of instances.
	 */
	public int getNumberOfInstances() {
		return allInstances.size();
	}
	
	/**
	 * Get the colour that represents the task.
	 * @return Color object.
	 */
	public Color getColor() {
		return thisColor;
	}
	
	/**
	 * Set the colour.
	 * @param rgb Integer representing the RGB value of the colour.
	 */
	public void setColor(int rgb) {
		thisColor = new Color(rgb);
	}
	
	/**
	 * Change the name of the task.
	 * @param newName Name of the task.
	 */
	public void setName(String newName) {
		taskName = newName;
	}
	
	/**
	 * Get the name of the task.
	 * @return String name
	 */
	public String getName() {
		return taskName;
	}
	
	/**
	 * Change if the task is folded or unfolded in the graph
	 * @param folded Boolean status of folded
	 */
	
	public void setFolded(Boolean folded){
		this.folded = folded;
	}
	
	/**
	 * Gets the folded status of a task
	 * @return folded Boolean representation of folded status
	 */
	
	public Boolean getFolded(){
		return folded;
	}
	
	/**
	 * Add a subtask definition to the task.
	 * @param newTask The Subtask definition.
	 */
	public void addSubTask(SubTask newTask) {
		getAllSubTaskDefinitions().add(newTask);
	}
	
	/**
	 * Add a subtask definition to the task.
	 * @param name Name of the subtask.
	 * @param scolor Colour to represent the subtask.
	 */
	public void addSubTask(String name, Color scolor) {
		SubTask newst = new SubTask(this);
		newst.setName(name);
		newst.setColor(scolor);
		getAllSubTaskDefinitions().add(newst);
	}
	
	/**
	 * Add a subtask definition to the task, then add an instance of it.
	 * @param name Name of subtask.
	 * @param scolor Colour representing the subtask.
	 * @param start Start time.
	 * @param end End time.
	 * @param attributeArray 
	 * @param ownerInstance The contained task instance.
	 * @return True on success, false on failure.
	 */
	public void addSubTaskWithInstance(String name, Color scolor, Calendar start, 
		Calendar end, String[] attributeArray) {
		SubTask newst = new SubTask(this);
		newst.setName(name);
		newst.setColor(scolor);
		getAllSubTaskDefinitions().add(newst);
		newst.addSubTaskInstance(start, end);
		if(attributeArray != null){
			for(int i = 0; i < attributeArray.length; i++){
				newst.addAttribute(attributeArray[i]);
			}
		}
	}
	
	/**
	 * Get the subtask definition with the ID.
	 * @param index The sub task definition ID
	 * @return The Subtask definition.
	 */
	public SubTask getSubTask(int index) {
		return allSubTaskDefinitions.get(index);
	}
	
	/**
	 * Get number of all defined subtasks.
	 * @return Number of subtasks.
	 */
	public int getNumberOfSubTasks() {
		return allSubTaskDefinitions.size();
	}
	
	/**
	 * Remove a subtask definition completely. This does not safely remove all instances first, so
	 * first make sure all instances are removed.
	 * @param index Definition ID to remove.
	 */
	public void removeSubTask(int index) {
		allSubTaskDefinitions.remove(index);
	}

	/**
	 * Sets all the subTask definitions of a task
	 * @param allSubTaskDefinitions
	 */
	public void setAllSubTaskDefinitions(ArrayList<SubTask> allSubTaskDefinitions) {
		this.allSubTaskDefinitions = allSubTaskDefinitions;
	}

	/**
	 * Gets all the subtask definitions of a task
	 * @return ArrayList<SubTask> An array of subtasks
	 */
	public ArrayList<SubTask> getAllSubTaskDefinitions() {
		return allSubTaskDefinitions;
	}

	/**
	 * Sets all instances of a task
	 * @param allInstances
	 */
	public void setAllInstances(ArrayList<TaskInstance> allInstances) {
		this.allInstances = allInstances;
	}

	/**
	 * gets all instances of a task
	 * @return ArrayList<TaskInstance> An array of instances
	 */
	public ArrayList<TaskInstance> getAllInstances() {
		return allInstances;
	}

	/**
	 * Returns the size of subtask instances
	 * @return int The amount of subtask instances
	 */
	public int getNumberOfSubTaskInstances() {
		int out = 0;
		for(int i = 0; i < allSubTaskDefinitions.size(); i++){
			SubTask sub = getSubTask(i);
			for(int j = 0; j < sub.getNumberOfSubTaskInstances(); j++){
				out++;
			}
		}
		return out;
	}
	
	/**
	 * Checks whether a TaskInstance already exists on that place in time
	 * @param start
	 * @param end
	 * @return true or false
	 */
	public boolean checkInstance(Calendar start, Calendar end){
		boolean out = false;
		for(int i = 0; i < getNumberOfInstances(); i++){
			TaskInstance inst = getInstance(i);
			// {-[*-*-*-*-*]-}
			if(start.compareTo(inst.getStart()) >= 0 && end.compareTo(inst.getEnd()) <= 0){
				out = true;
			}// [****{*-]------}
			if(start.compareTo(inst.getStart()) <= 0 && start.compareTo(inst.getEnd()) <=0 && end.compareTo(inst.getStart())>=0){
				inst.setStart(start);
				out = true;
			}// {-------[-*}*******]
			if((start.compareTo(inst.getEnd()) <= 0 && end.compareTo(inst.getEnd()) >=0)){
				inst.setEnd(end);
				out = true;
			}
		}
		return out;
	}

	/**
	 * Removes the subTasks, subTaskInstances and instances
	 */
	public void clear() {
		allInstances.clear();
		for(int i = 0; i < allSubTaskDefinitions.size(); i++){
			allSubTaskDefinitions.get(i).clear();
		}
		allSubTaskDefinitions.clear();
	}

	/**
	 * Removes a given TaskInstance fron the Task
	 * @param taskInstance
	 */
	public void removeInstance(TaskInstance taskInstance) {
		taskInstance.remove(); 
		allInstances.remove(taskInstance);
	}
	
	/**
	 * Removes the instance that falls on a certain date
	 * @param date
	 */
	public void removeInstanceOn(Calendar date) {
		for(int i = 0; i < getNumberOfInstances(); i++){
			TaskInstance inst = getInstance(i);
			if(inst.getStart().compareTo(date) <= 0 && inst.getEnd().compareTo(date) >=0 ){
				removeInstance(inst);
			}
		}
	}

	/**
	 * Removes all SubTasks from the task
	 */
	public void removeAllSubTasks() {
		allSubTaskDefinitions = new ArrayList<SubTask>();
	}
	
	/**
	 * Sets the Color of the task
	 * @param color
	 */
	public void setColor(Color color) {
		thisColor = color;		
	}
	
	/**
	 * Revalidates the instances, after a change has been made in the chart
	 * It will combine two instances if they have a date in common
	 */
	public void revalidateInstances(){
		for(int i = 0; i < getNumberOfInstances()-1; i++){
			int j = i + 1;
			Calendar startCheck = (Calendar) getInstance(j).getStart().clone();
			startCheck.add(Calendar.DAY_OF_YEAR, -1);
			if(getInstance(i).getEnd().compareTo(startCheck) >= 0 &&
					getInstance(i).getStart().compareTo(getInstance(j).getEnd()) <= 0){
				getInstance(j).setStart(getInstance(i).getStart());
				allInstances.remove(getInstance(i));
				break;
			}
			Calendar checkEnd = (Calendar) getInstance(i).getEnd().clone();
			checkEnd.add(Calendar.DAY_OF_YEAR, 1);
			if(getInstance(j).getStart().compareTo(checkEnd) <= 0 &&
					getInstance(j).getEnd().compareTo(getInstance(i).getStart()) >= 0){
				getInstance(i).setEnd(getInstance(j).getEnd());
				allInstances.remove(getInstance(j));
				break;
			}
		}
	}

	/**
	 * Revalidates the SubTasks and instances
	 */
	public void revalidate() {
		for(int i = 0; i < getNumberOfSubTasks(); i++){
			SubTask sub = getSubTask(i);
			for(int j = 0; j < sub.getNumberOfSubTaskInstances(); j++){
				sub.getSubTaskInstance(j).revalidate();
			}
		}
		revalidateInstances();
	}

	/**
	 * Gets an instance that falls on a certain date
	 * @param date
	 * @return
	 */
	public TaskInstance getInstance(Calendar date) {
		for(int i = 0; i < getNumberOfInstances(); i++){
			if(getInstance(i).getStart().compareTo(date) <= 0 && getInstance(i).getEnd().compareTo(date) >= 0){
				return getInstance(i);
			}
		}
		return null;
	}
	
	/**
	 * Moves the subTaskInstance up or down in the array
	 * @param sub The instance that should be moved
	 * @param direction true-up, false-down
	 */
	public void moveSubTaskInArray(SubTaskInstance sub, boolean direction){
		SubTask aid;
		for(int i = 0; i < allSubTaskDefinitions.size(); i++){
			if(allSubTaskDefinitions.get(i).equals((sub.getDefinition()))){
				if(direction){
					if( i > 0 ){
						aid = allSubTaskDefinitions.get(i);
						allSubTaskDefinitions.set(i, allSubTaskDefinitions.get(i-1));
						allSubTaskDefinitions.set(i-1, aid);
						break;
					}
				}else{
					if(i < allSubTaskDefinitions.size()-1){
						aid = allSubTaskDefinitions.get(i);
						allSubTaskDefinitions.set(i, allSubTaskDefinitions.get(i+1));
						allSubTaskDefinitions.set(i+1, aid);
						break;
					}
				}
			}
		}
	}

	public int getSubTaskID(SubTask subTask) {
		int out = -1;
		for(int i = 0; i < allSubTaskDefinitions.size(); i++){
			if(allSubTaskDefinitions.get(i).equals(subTask))
				out = i;
		}
		return out;
	}
}