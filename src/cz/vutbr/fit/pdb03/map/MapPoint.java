package cz.vutbr.fit.pdb03.map;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;

import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

/**
 * Vlastni upravena implementace MapMarkerDot
 *
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
public class MapPoint extends MapMarkerDot {

	private int id;
	private static int pointSize = 50;
	private static Color color = Color.YELLOW;

	public static int counter = 0;

	public MapPoint(double lat, double lon, int id) {
		super(lat, lon);

		setId(id);
	}

	public static int getPointSize(){
		return pointSize;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	@Override
	public void paint(Graphics g, Point position) {

        int sizeHorizontal = pointSize / 2;
        g.setColor(color);
        g.fillOval(position.x - sizeHorizontal, position.y - sizeHorizontal, pointSize, pointSize);
        g.setColor(Color.BLACK);
        g.drawOval(position.x - sizeHorizontal, position.y - sizeHorizontal, pointSize, pointSize);

        g.setColor(Color.BLACK);
		g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, pointSize-2));
		g.drawString(id+"", position.x-(int)(pointSize/4), position.y+(pointSize/2));
	}

	@Override
	public String toString() {
		return "Marker " + id + " at lat: " + getLat() + ", lon: " + getLon();
	}
}
