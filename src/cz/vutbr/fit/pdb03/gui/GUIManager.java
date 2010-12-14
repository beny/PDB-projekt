package cz.vutbr.fit.pdb03.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;

/**
 * Trida pro jednotnou praci s GUI
 * @author Pavel Srnec <xsrnec01@stud.fit.vutbr.cz>
 *
 */
public class GUIManager {

	/**
	 * Metoda ktera centruje jednu komponentu vuci druhe
	 * @param c komponenta kterou chceme centrovat
	 * @param centerTo vuci ktere budeme komponentu c centrovat
	 */
	public static void moveToCenter(Component c, Component centerTo){
		Point location = centerTo.getLocationOnScreen();
		Dimension size = centerTo.getSize();

		int x = size.width / 2;
		int y = size.height / 2;

		c.setLocation(new Point(location.x + x - c.getWidth()/2, location.y + y - c.getHeight()/2));
	}
}
