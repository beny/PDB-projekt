package cz.vutbr.fit.pdb03.map;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

/**
 * Trida zajistujici odchyceni klikani do mapy
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
public class MapMouseListener extends MouseAdapter implements MouseListener {

	/**
	 * Reference na mapu (pro funkce pocitani pozice a pod)
	 */
	JMapViewer map;

	public MapMouseListener(JMapViewer map) {
		this.map = map;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Coordinate pointClicked = map.getPosition(e.getPoint());

		// pro leve tlacitko mysi
		if (e.getButton() == MouseEvent.BUTTON1) {

			// nakresli pouze bod pokud jsou body viditelne
			if (map.getMapMarkersVisible()) {
				MapMarkerPoint.counter = MapMarkerPoint.counter + 1;
				map.addMapMarker(new MapMarkerPoint(pointClicked.getLat(),
						pointClicked.getLon(), MapMarkerPoint.counter));
			}
		}
		// pro prave tlacitko mysi
		// TODO hazi nejakou vyjimku pri mazani bodu ze seznamu
		else if (e.getButton() == MouseEvent.BUTTON3) {

			// spocita maximalni vzdalenost bodu od stredu kliknuti kdy jeste
			// spada do bodu
			double maxDist = MapMarkerPoint.getPointSize() / 2;

			// ziskani vsech bodu
			List<MapMarker> markers = map.getMapMarkerList();

			// overeni zda nejaky bod neni vzdalen min jak maximalni mozna
			// vzdalenost kliku
			for (MapMarker mapMarker : markers) {
				Point markerPoint = map.getMapPosition(mapMarker.getLat(),
						mapMarker.getLon());

				double dist = Point.distance(e.getPoint().x, e.getPoint().y,
						markerPoint.x, markerPoint.y);

				// pridej bod mezi body do kterych se klik trefil
				if (dist <= maxDist) {
					markers.remove(mapMarker);
				}
			}

			// repaint map
			map.repaint();
		}
	}
}
