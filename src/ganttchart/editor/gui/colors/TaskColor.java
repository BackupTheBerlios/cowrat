package ganttchart.editor.gui.colors;

import java.awt.Color;
import java.util.ArrayList;

public class TaskColor {

	private Color color;
	private String name;
	private boolean inUse;
	private ArrayList<TaskColor> subColors;
	
	public TaskColor(Color color, String name){
		this.setColor(color);
		this.setName(name);
		setInUse(false);
		subColors = new ArrayList<TaskColor>();
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setInUse(boolean inUse) {
		this.inUse = inUse;
	}

	public boolean isInUse() {
		return inUse;
	}
	
	public boolean equals(TaskColor color){
		return color.getName() == name;
	}

	public void setSubColors(ArrayList<TaskColor> subColors) {
		this.subColors = subColors;
	}

	public ArrayList<TaskColor> getSubColors() {
		return subColors;
	}
	
	public void addSubTaskColor(TaskColor color){
		subColors.add(color);
	}
	
}
