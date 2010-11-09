package cz.vutbr.fit.pdb03.map;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import org.openstreetmap.gui.jmapviewer.Coordinate;

/**
 * Trida implementujici obdelnik, pro nasledne zobrazeni do mapy
 *
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
public class MapRectangle implements
		org.openstreetmap.gui.jmapviewer.interfaces.MapRectangle {

	double topLeftLat, topLeftLon, bottomRightLat, bottomRightLon;

	/**
	 * Zakladni konstruktor vytvarejici obdelnik v WGS84 souracnicich
	 *
	 * @param topLeftLat bod x pro levy horni roh
	 * @param topLeftLon bod y pro levy horni roh
	 * @param bottomRightLat bod x pro pravy dolni roh
	 * @param bottomRightLon bod y pro pravy dolni roh
	 */
	public MapRectangle(double topLeftLat, double topLeftLon, double bottomRightLat, double bottomRightLon){
		this.topLeftLat = topLeftLat;
		this.topLeftLon = topLeftLon;
		this.bottomRightLat = bottomRightLat;
		this.bottomRightLon = bottomRightLon;
	}

	@Override
	public Coordinate getTopLeft() {
		return new Coordinate(topLeftLat, topLeftLon);
	}

	@Override
	public Coordinate getBottomRight() {
		return new Coordinate(bottomRightLat, bottomRightLon);
	}

	@Override
	public void paint(Graphics g, Point topLeft, Point bottomRight) {
		g.setColor(Color.BLACK);
		g.drawRect(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y);
	}

}
