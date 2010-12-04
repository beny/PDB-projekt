package cz.vutbr.fit.pdb03.controllers;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.Log;
import cz.vutbr.fit.pdb03.map.JMapPanel;
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
	private JMapPanel map;
	private AnimalsDatabase frame;

	public MouseController(AnimalsDatabase frame) {
		this.frame = frame;

		// pridani listeneru
		map = frame.getMap();
		map.addMouseListener(this);

	}

	/**
	 * Metoda ktera maze body podle toho kam se kliklo
	 * @param clicked bod kam se kliklo
	 */
	private void deletePoints(Point clicked){

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

			double dist = Point.distance(clicked.x, clicked.y,
					markerPoint.x, markerPoint.y);

			// pridej bod mezi body do kterych se klik trefil
			// TODO smaze to bod a pak neni mozne jit na dalsi v seznamu
			if (dist <= maxDist) {

				// pridani bodu do bodu, ktere se maji smazat
				toDelete.add(mapMarker);
			}
			((MapPoint)mapMarker).setSelected(false);
		}

		// smazani prislusnych bodu
		for (MapMarker mapMarker : toDelete) {
			Log.debug("Info o bodu: " + mapMarker);

			((MapPoint)mapMarker).setSelected(true);
//			markers.remove(mapMarker);
		}

		// repaint map
		map.repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Coordinate pointClicked = map.getPosition(e.getPoint());

		// pro leve tlacitko mysi
		if (e.getButton() == MouseEvent.BUTTON1) {

			// nakresli pouze bod pokud jsou body viditelne
			if (map.getMapMarkersVisible() && map.isEditMode()) {
				MapPoint.counter = MapPoint.counter + 1;
				map.addMapMarker(new MapPoint(pointClicked.getLat(),
						pointClicked.getLon(), MapPoint.counter));

				Log.debug("Pridavam bod do mapy na souradnice: " + pointClicked);
			}
		}
		// pro prave tlacitko mysi
		else if (e.getButton() == MouseEvent.BUTTON3) {

			if(map.isEditMode()){
				deletePoints(e.getPoint());
			}
		}
	}
}
