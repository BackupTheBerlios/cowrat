 package ganttchart.tasklib;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * A <strong>TaskInstance</strong> contains a set time that the task runs
 * for.
 * @author Daniel McKenzie
 *
 */
public class TaskInstance {

	private Calendar startDay;
	private Calendar endDay;
	private Task owner;
	
	/**
	 * Constructor, sets up a task instance.
	 * @param theOwner The task that owns this instance.
	 */
	public TaskInstance(Task theOwner) {
		startDay = new GregorianCalendar();
		endDay = new GregorianCalendar();
		owner = theOwner;
	}
	
	/**
	 * Set the start day.
	 * @param day The numerical day value (0-30)
	 * @param month The numerical month value (0-11)
	 * @param year The numerical year value.
	 */
	public void setStart(int day, int month, int year) {
		Calendar cal = new GregorianCalendar();
		cal.set(year, month, day, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		setStart(cal);	
	}
	
	/**
	 * Set the start date using a date from Unix EPOCH.
	 * @param unix The date in milliseconds.
	 */
	public void setStart(long unix) {
		startDay.setTimeInMillis(unix);
	}
	
	/**
	 * Set the start date using a predefined Calendar.
	 * @param date The calendar representing the date.
	 */
	public void setStart(Calendar date) {
		startDay = owner.setDateFields(date);
	}
	
	/**
	 * Set the end day.
	 * @param day The numerical day (0-30)
	 * @param month The numerical month (0-11)
	 * @param year The year
	 */
	public void setEnd(int day, int month, int year) {
		Calendar cal = new GregorianCalendar();
		cal.set(year, month, day, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		setEnd(cal);
	}
	
	/**
	 * Set the end date, with time from Unix EPOCH.
	 * @param unix Time in milliseconds.
	 */
	public void setEnd(long unix) {
		endDay.setTimeInMillis(unix);
	}
	
	/**
	 * Set the date with predefined Calendar object.
	 * @param date Calendar object.
	 */
	public void setEnd(Calendar date) {
		endDay = owner.setDateFields(date);
	}
	
	/**
	 * Get the start day.
	 * @return Calendar start date.
	 */
	public Calendar getStart() {
		return startDay;
	}
	
	/**
	 * Get the end day.
	 * @return Calendar end date.
	 */
	public Calendar getEnd() {
		return endDay;
	}
	
	/**
	 * Get the number of days this instance lasts for.
	 * @return Number of days.
	 */
	public int getNumberOfDays() {
		int days = 0;
		
		Calendar start = (Calendar)getStart().clone();
		while (start.compareTo(getEnd()) <= 0) {
			start.add(Calendar.DAY_OF_YEAR, 1);
			days++;
		}
		
		return days;
	}
	
	/**
	 * Get the owner of the instance
	 * @return
	 */
	public Task getOwner(){
		return owner;
	}
	
	/**
	 * Returns a subtaskinstance that falls within the instance
	 * @param index
	 * @return SubTaskInstance
	 */
	public SubTaskInstance getSubTaskInstance(int index) {
		return getSubTaskInstances().get(index); 
	}

	/**
	 * Checks whether a subtaskinstance falls in a instance
	 * @param subTask
	 * @return true or false
	 */
	public boolean includes(SubTaskInstance subTask) {
		if((subTask.getStart().compareTo(this.getStart()) >=0 &&
				subTask.getStart().compareTo(this.getEnd()) <= 0) ||
				subTask.getEnd().compareTo(this.getStart()) >= 0 &&
						subTask.getEnd().compareTo(this.getEnd()) <= 0){
			return true;
		}		
		return false;
	}

	/**
	 * Get all the subtaskinstances that fall in this instance 
	 * @return
	 */
	public ArrayList <SubTaskInstance> getSubTaskInstances() {
		ArrayList <SubTaskInstance> out = new ArrayList<SubTaskInstance>();
		for(int i = 0; i < owner.getNumberOfSubTasks(); i++){
			SubTask sub = owner.getSubTask(i);
			for(int j = 0; j < sub.getNumberOfSubTaskInstances(); j++){
				if(includes(sub.getSubTaskInstance(j))){
					out.add(sub.getSubTaskInstance(j));
				}
			}
		}
		return out;
	}
	
	/**
	 * Returns the count for subtaskinstances within this instance
	 * @return
	 */
	public int getNumberOfSubTaskInstances() {
		return getSubTaskInstances().size();
	}

	
	/**
	 * Removes all subTask Instances from the taskInstace
	 */
	public void remove() {
		for(int i = 0; i < owner.getNumberOfSubTasks(); i++){
			SubTask sub = owner.getSubTask(i);
			for(int j = 0; j < sub.getNumberOfSubTaskInstances(); j++){
				SubTaskInstance subInst = sub.getSubTaskInstance(j);
				if(includes(subInst)){
					sub.removeInstance(subInst);
				}
			}
		}
	}
}
