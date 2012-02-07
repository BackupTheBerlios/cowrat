package ganttchart.editor.gui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JButton;

/**
 * JColorButton is a JButton extended to show a colour on its face.
 * It adds two new functions to the original JButton.
 * @author Daniel McKenzie
 *
 */
public class JColorButton extends JButton {
	private Color repColor;
	
	/**
	 * Constructor for the JColorButton, to create a new button.
	 * @param theColor The colour to be represented by this button.
	 */
	public JColorButton(Color theColor) {
		super("  "); // Create the original button with blankness
		repColor = theColor; // Set the colour
	}
	
	/**
	 * Set the colour to a new one.
	 * @param newColor The new colour to use.
	 */
	public void setColor(Color newColor) {
		repColor = newColor;
	}
	
	/**
	 * Get the colour that this button represents.
	 * @return The colour in this button.
	 */
	public Color getColor() {
		return repColor;
	}
	
	/**
	 * Overrides the original paintComponent. It paints the colour
	 * on top of the button.
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		int x = 5;
		int y = 5;
		int width = this.getSize().width - (x*2);
		int height = this.getSize().height - (y*2);
		
		g.setColor(repColor);
		g.fillRect(x, y, width, height);
		
	}
	
}
