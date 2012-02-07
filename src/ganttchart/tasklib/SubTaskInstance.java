package ganttchart.tasklib;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * This <strong>Sub Task Instance</strong>, like a <strong>Task Instance</strong>
 * represents an instance that a sub task runs for.
 * @author Daniel McKenzie
 *
 */
public class SubTaskInstance {

	private Calendar startDay;
	private Calendar endDay;
	private SubTask definition;
	
	/**
	 * Create a new subtask instance.
	 * @param theOwner The owning subtask.
	 * @param theContainedInstance The owning task instance.
	 */
	public SubTaskInstance(SubTask theOwner) {
		startDay = Calendar.getInstance();
		endDay = Calendar.getInstance();
		definition = theOwner;
	}
	
	/**
	 * Set the start day.
	 * @param day Day (0-30)
	 * @param month Month (0-11)
	 * @param year Year value
	 */
	public void setStart(int day, int month, int year) {
		GregorianCalendar newDate = new GregorianCalendar(year, month, day);
		startDay = newDate;
	}
	
	/**
	 * Set the start time.
	 * @param date Calendar object with time set.
	 */
	public void setStart(Calendar date) {
			startDay = date;
	}
	
	/**
	 * Set the end day.
	 * @param day Day (0-30)
	 * @param month Month (0-11)
	 * @param year Year value
	 */
	public void setEnd(int day, int month, int year) {
		endDay.set(year, month, day);
	}
	
	/**
	 * Set the end time.
	 * @param date Calendar with time set.
	 */
	public void setEnd(Calendar date) {
		endDay = date;
	}

	/**
	 * Get the start time.
	 * @return Calendar start time.
	 */
	public Calendar getStart() {
		return startDay;
	}
	
	/**
	 * Get the end time.
	 * @return Calendar end time.
	 */
	public Calendar getEnd() {
		return endDay;
	}
	
	/**
	 * Get the ID of the subtask.
	 * @return ID of the subtask.
	 */
	public int getSubTaskDefinitionID() {
		int id = definition.getID();
		return id;
	}
	
	/**
	 * Get the name of the definition.
	 * @return Name of the definition.
	 */
	public String getDefinitionName() {
		return definition.getName();
	}
	
	/**
	 * Get the sub task definition.
	 * @return Definition.
	 */
	public SubTask getDefinition() {
		return definition;
	}
	
	/**
	 * Gets the number of days this lasts for.
	 * @deprecated
	 * @return 0, always
	 * @see #getNumberOfDays
	 */
	public int getLength() {
		return 0;
	}
	
	/**
	 * Gets the number of days this lasts for.
	 * @return Number of days.
	 */
	public int getNumberOfDays() {
		int days = 0;
		
		Calendar start = (Calendar)startDay.clone();
		while (start.compareTo(endDay) <= 0) {
			start.add(Calendar.DAY_OF_YEAR, 1);
			days++;
		}
		
		return days;
	}

	/**
	 * Removes this SubTaskInstance
	 */
	public void remove() {
		definition.removeInstance(this);
	}

	/**
	 * Checks whether this instance contains a given date
	 * @param date
	 * @return
	 */
	public boolean contains(Calendar date) {
		if(startDay.compareTo(date) <= 0 && endDay.compareTo(date) >= 0){
			return true;
		}
		return false;
	}

	/**
	 * Sets the name of the SubTask definition
	 * @param text
	 */
	public void setName(String text) {
		definition.setName(text);
	}
	
	/**
	 * Checks after deleting a subtaskinstance of the instance is still alive
	 */
	public void revalidate() {	
		boolean includes = false;
		for(int i = 0; i < definition.getOwner().getNumberOfInstances(); i++){
			TaskInstance inst = definition.getOwner().getInstance(i);

			if(inst.includes(this)){
				includes = true;
				if(this.getStart().compareTo(inst.getStart()) < 0)
					setStart(inst.getStart());
				
				if(this.getEnd().compareTo(inst.getEnd()) > 0)
					setEnd(inst.getEnd());
			}
		}
		if(!includes){
			this.remove();
		}
		
		definition.revalidateInstances();
	}
	
}
