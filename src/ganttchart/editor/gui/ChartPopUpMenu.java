package ganttchart.editor.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;

import ganttchart.editor.gui.displays.ChartDisplay;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

public class ChartPopUpMenu extends JPopupMenu implements ActionListener{
	
	private JMenuItem editMenu, deleteMenu, blurMenu, moveMenu, toggleMenu;
	private ChartDisplay chart;
	private Calendar date;
	
	public ChartPopUpMenu(ChartDisplay chart, Calendar date){
		this.chart = chart;
		this.date = date;
		initComponents();
	}

	private void initComponents() {
		editMenu = new JMenuItem("Edit");
		deleteMenu = new JMenuItem("Delete");
		
		
		editMenu.addActionListener(this);
		deleteMenu.addActionListener(this);
		
		add(editMenu); add(deleteMenu); 
		
		if(chart.getBlurType() > 0){
			blurMenu = new JMenuItem("Show all");
			blurMenu.addActionListener(this);
			add(blurMenu);
		}
		
		if(!chart.getMovable()){
			moveMenu = new JMenuItem("Move");
			moveMenu.addActionListener(this);
			add(moveMenu);
		}else{
			moveMenu = new JMenuItem("Stop Move");
			moveMenu.addActionListener(this);
			add(moveMenu);	
		}
		
		if(chart.getChartIdentifier() == "Combi Chart"){
			if(chart.getToggle()){
				toggleMenu = new JMenuItem("Toggle Gantt");
				toggleMenu.addActionListener(this);
				add(toggleMenu);
			}else{
				toggleMenu = new JMenuItem("Toggle Mosaic");
				toggleMenu.addActionListener(this);
				add(toggleMenu);
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());
        if(source.getText() == "Edit"){
        	doEdit();
        }else 
        if(source.getText() == "Delete"){
        	doDelete();
        }else
        if(source.getText() == "Show all"){
        	doBlur();
        }else
        if(source.getText() == "Move"){
        	doMove();
        }else 
        if(source.getText() == "Stop Move"){
        	stopMove();
        }else 
        if(source.getText() == "Toggle Gantt" || source.getText() == "Toggle Mosaic"){
        	doToggle();
        }
	}

	private void stopMove() {
		chart.setMovable(false);
	}

	private void doMove() {
		chart.setMovable(true);
	}

	private void doBlur() {
		chart.setBlurType(0);
		chart.repaintAll();
	}

	private void doDelete() {
		if(chart.getClickedSubTaskInstance() != null)
			chart.getClickedSubTaskInstance().getDefinition().removeInstance(chart.getClickedSubTaskInstance());
		else if(chart.getClickedTask() != null){
			chart.getTasks().getTask(chart.getClickedTask()).removeInstanceOn(date);
		}
		else
			JOptionPane.showMessageDialog(null, "Nothing was deleted");
		
		if(chart.getTasks().getDetail()){
			if(chart.getClickedTask() != null || chart.getClickedSubTaskInstance() != null){
				for(int i = 0; i < chart.getClickedTask().getNumberOfSubTasks(); i++){
					for(int j = 0; j < chart.getClickedTask().getSubTask(i).getNumberOfSubTaskInstances(); j++){
						chart.getClickedTask().getSubTask(i).getSubTaskInstance(j).revalidate();
					}
				}
			}
		}
		chart.repaintAll();

	}

	private void doEdit() {
		if(chart.getClickedSubTaskInstance() != null)
			new SubTaskEditor(null, chart.getClickedSubTaskInstance(), chart.getTasks());
		else if(chart.getClickedTask() != null){
			new TaskEditor(null,chart.getClickedTask(), chart.getTasks());
		}else
			JOptionPane.showMessageDialog(null, "Nothing was selected to edit");
			
		chart.repaintAll();
	}	
	
	private void doToggle(){
		chart.setToggle(!chart.getToggle());
		chart.repaintAll();
	}
}
