package ganttchart.tasklib;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * A <strong>SubTask</strong> is a task that belongs to, or is dependent on, a
 * <strong>Task</strong>.
 * @author Daniel McKenzie
 *
 */
public class SubTask {

	private Color taskColor;
	private String name;
	private Task owner;
	private ArrayList<SubTaskInstance> instances;
	private ArrayList<String> attributes;

	
	/**
	 * Constructor.
	 * @param theOwner Owning task.
	 */
	public SubTask(Task theOwner) {
		taskColor = Color.black;
		owner = theOwner;
		instances = new ArrayList<SubTaskInstance>();
		attributes = new ArrayList<String>();
	}
	
	/**
	 * Set the colour.
	 * @param r Red component.
	 * @param g Green component.
	 * @param b Blue component.
	 */
	public void setColor(int r, int g, int b) {
		taskColor = new Color(r, g, b);
	}
	
	/**
	 * Set the colour.
	 * @param newColor Colour object.
	 */
	public void setColor(Color newColor) {
		taskColor = newColor;
	}
	
	/**
	 * Set the name.
	 * @param newName The new name.
	 */
	public void setName(String newName) {
		name = newName;
	}
	
	/**
	 * Get the colour.
	 * @return Colour object.
	 */
	public Color getColor() {
		return taskColor;
	}
	
	/**
	 * Get the name.
	 * @return String name.
	 */
	public String getName() {
		return name;
	}
	
	public Task getOwner(){
		return owner;
	}
	
	/**
	 * Get the ID of this subtask.
	 * @return The ID.
	 */
	public int getID() {
		int theID = owner.getAllSubTaskDefinitions().indexOf(this);
		return theID;
	}
	
	/**
	 * Remove a subtask from this instance.
	 * @param index Index of the subtask.
	 */
	public void removeSubTask(int index) {
		instances.remove(index);
	}
	
	/**
	 * Checks whether a subTaskInstance already exists on that place in time
	 * @param start
	 * @param end
	 * @return true or false
	 */
	private boolean checkSubTaskInstance(Calendar start, Calendar end){
		boolean out = false;
		for(int i = 0; i < getNumberOfSubTaskInstances(); i++){
			SubTaskInstance inst = getSubTaskInstance(i);
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
	 * Add a new subtask instance and checks if it already contains one on that date.
	 * @param start Start time.
	 * @param end End time.
	 * @return True on success, false on failure.
	 */
	public boolean addSubTaskInstance(Calendar start, Calendar end) {
		SubTaskInstance newinst = new SubTaskInstance(this);

		if(!checkSubTaskInstance(start, end)){
			newinst.setStart(start); newinst.setEnd(end);
			
		int locationID = 0;

		for (int i = 0; i < instances.size(); i++) {
			SubTaskInstance checkInst = instances.get(i);
			if (newinst.getStart().after(checkInst.getStart())) {
				locationID = i + 1;
			}
		}
			instances.add(locationID, newinst);
			return true;
		}
		return false;
	}
	
	/**
	 * Add a new subtask instance and checks if it already contains one on that date.
	 * @param start Start time in milliseconds.
	 * @param end End time in milliseconds.
	 * @return True on success, false on failure.
	 */
	public boolean addSubTaskInstance(Long start, Long end) {
		Calendar calStart = new GregorianCalendar();
		calStart.setTimeInMillis(start);
		Calendar calEnd = new GregorianCalendar();
		calEnd.setTimeInMillis(end);
		return addSubTaskInstance(calStart, calEnd);
	}
	
	/**
	 * Get the number of instances of subtasks in this instance.
	 * @return Number of instances.
	 */
	public int getNumberOfSubTaskInstances() {
		return instances.size();
	}
	
	/**
	 * Get a subtask instance.
	 * @param index The ID of the instance.
	 * @return SubTaskInstance object.
	 */
	public SubTaskInstance getSubTaskInstance(int index) {
		return instances.get(index);
	}
	
	/**
	 * Get all the instances that belong to a subtask.
	 * @param definition The definition to find.
	 * @return An array of Sub Task Instances.
	 */
	public SubTaskInstance[] getSubTaskInstance() {
		SubTaskInstance[] subinsts = new SubTaskInstance[instances.size()];
		return instances.toArray(subinsts);
	}

	/**
	 * Sets the attributes using an arraylist
	 * @param attributes An arraylist of attributes
	 */
	public void setAttributes(ArrayList<String> attributes) {
		this.attributes = attributes;
	}

	/**
	 * Gets the attributes for this subtask
	 * @return attributes An arraylist of attributes
	 */
	public ArrayList<String> getAttributes() {
		return attributes;
	}
	
	/**
	 * Adds an attribute for this subtask
	 * @param att A string attribute
	 */
	public void addAttribute(String att){
		attributes.add(att);
	}
	
	/**
	 * Removes an attribute for this subtask
	 * @param att The string that had to be removed
	 */
	public void removeAttribute(String att){
		for(int i = 0; i < attributes.size(); i++){
			if(attributes.get(i) == att){
				attributes.remove(i);			
			}
		}
	}
	
	/**
	 * Checks whether the subtask has a certain attribute
	 * @param att The attribute that we want to check
	 * @return true or false
	 */
	public boolean hasAttribute(String att){
		if(attributes.contains(att))
			return true;
		
		return false;
	}

	/**
	 * Clears the instances and attributes for this subtask
	 */
	public void clear() {
		attributes.clear();
		instances.clear();
	}

	/**
	 * Removes a subtask instance given that instance 
	 * @param subTaskInstance
	 */
	public void removeInstance(SubTaskInstance subTaskInstance) {
		for(int i = 0; i < instances.size(); i++){
			if(instances.get(i) == subTaskInstance){
				instances.remove(i);
			}
		}
	}
	
	/**
	 * If two instances of a subtask fall at the same date they become one subtaskinstance
	 */
	public void revalidateInstances(){
		for(int i = 0; i < getNumberOfSubTaskInstances()-1; i++){
			int j = i + 1;
			Calendar startCheck = (Calendar) getSubTaskInstance(j).getStart().clone();
			startCheck.add(Calendar.DAY_OF_YEAR, -1);

			if(getSubTaskInstance(i).getEnd().compareTo(startCheck) >= 0 &&
					getSubTaskInstance(i).getStart().compareTo(getSubTaskInstance(j).getEnd()) <= 0){
				getSubTaskInstance(j).setStart(getSubTaskInstance(i).getStart());
				instances.remove(getSubTaskInstance(i));
				break;
			}
			
			Calendar checkEnd = (Calendar) getSubTaskInstance(i).getEnd().clone();
			checkEnd.add(Calendar.DAY_OF_YEAR, 1);
			if(getSubTaskInstance(j).getStart().compareTo(checkEnd) <= 0 &&
					getSubTaskInstance(j).getEnd().compareTo(getSubTaskInstance(i).getStart()) >= 0){
				getSubTaskInstance(i).setEnd(getSubTaskInstance(j).getEnd());
				instances.remove(getSubTaskInstance(j));
				break;
			}
		}
	}
	
	/**
	 * Gets a subtask instance using day
	 * @param date The date of the instance
	 * @return a subtask instance that contains the date
	 */
	
	public SubTaskInstance getInstanceOnDate(Calendar date){
		for(int i = 0; i < getNumberOfSubTaskInstances(); i++){
			if(getSubTaskInstance(i).contains(date)){
				return getSubTaskInstance(i);
			}
		}
		return null;
	}
}
