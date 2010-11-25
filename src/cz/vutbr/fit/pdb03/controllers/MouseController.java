package cz.vutbr.fit.pdb03.controllers;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.D;
import cz.vutbr.fit.pdb03.map.JMapPane;
import cz.vutbr.fit.pdb03.map.MapPoint;

/**
 * Trida zajistujici odchyceni klikani do mapy
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
public class MouseController extends MouseAdapter {

	/**
	 * Reference na mapu (pro funkce pocitani pozice a pod)
	 */
	JMapPane map;
	AnimalsDatabase frame;

	public MouseController(AnimalsDatabase frame) {
		this.frame = frame;

		// pridani listeneru
		map = frame.getMap();
		map.addMouseListener(this);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Coordinate pointClicked = map.getPosition(e.getPoint());

		// pro leve tlacitko mysi
		if (e.getButton() == MouseEvent.BUTTON1) {

			// nakresli pouze bod pokud jsou body viditelne
			if (map.getMapMarkersVisible()) {
				MapPoint.counter = MapPoint.counter + 1;
				map.addMapMarker(new MapPoint(pointClicked.getLat(),
						pointClicked.getLon(), MapPoint.counter));

				D.log("Pridavam bod do mapy na souradnice: " + pointClicked);
			}
		}
		// pro prave tlacitko mysi
		else if (e.getButton() == MouseEvent.BUTTON3) {

			// spocita maximalni vzdalenost bodu od stredu kliknuti kdy jeste
			// spada do bodu
			double maxDist = MapPoint.getPointSize() / 2;

			// ziskani vsech bodu
			List<MapMarker> markers = map.getMapMarkerList();
			List<MapMarker> toDelete = new ArrayList<MapMarker>();

			// overeni zda nejaky bod neni vzdalen min jak maximalni mozna
			// vzdalenost kliku
			for (MapMarker mapMarker : markers) {
				Point markerPoint = map.getMapPosition(mapMarker.getLat(),
						mapMarker.getLon());

				double dist = Point.distance(e.getPoint().x, e.getPoint().y,
						markerPoint.x, markerPoint.y);

				// pridej bod mezi body do kterych se klik trefil
				// TODO smaze to bod a pak neni mozne jit na dalsi v seznamu
				if (dist <= maxDist) {

					// pridani bodu do bodu, ktere se maji smazat
					toDelete.add(mapMarker);
				}
			}

			// smazani prislusnych bodu
			for (MapMarker mapMarker : toDelete) {
				D.log("Mazu bod: " + mapMarker);

				markers.remove(mapMarker);
			}

			// repaint map
			map.repaint();
		}
	}
}
