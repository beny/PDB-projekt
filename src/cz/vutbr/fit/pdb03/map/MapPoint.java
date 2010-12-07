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
	private static int pointSize = 5;
	private static Color color = Color.BLUE;

	public static int counter = 0;

	private boolean selected = false;

	public MapPoint(double lat, double lon, int id) {
		super(lat, lon);

		setId(id);
	}

	public static int getPointSize() {
		return pointSize;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean getSelected() {
		return selected;
	}

	@Override
	public void paint(Graphics g, Point position) {

		int sizeHorizontal = pointSize / 2;
		g.setColor(color);

		// zda je bod vybran ci ne
		if (selected) {
			g.setColor(Color.RED);
		} else {
			g.setColor(Color.BLACK);
		}
		g.fillOval(position.x - sizeHorizontal, position.y - sizeHorizontal,
				pointSize, pointSize);
		g.setColor(color);
		g.drawOval(position.x - sizeHorizontal, position.y - sizeHorizontal,
				pointSize, pointSize);
		g.setColor(Color.BLACK);
	}

	@Override
	public String toString() {
		return "Marker " + id + " at lat: " + getLat() + ", lon: " + getLon();
	}
}
