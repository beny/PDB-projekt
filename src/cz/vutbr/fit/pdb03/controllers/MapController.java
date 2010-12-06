package cz.vutbr.fit.pdb03.controllers;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import oracle.spatial.geometry.JGeometry;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.DataBase;
import cz.vutbr.fit.pdb03.Log;
import cz.vutbr.fit.pdb03.gui.AnimalsPanel;
import cz.vutbr.fit.pdb03.map.JMapPanel;
import cz.vutbr.fit.pdb03.map.MapPoint;

/**
 * Trida zajistujici udalosti okolo mapy a to klikani mysi a tlacitek v mape
 *
 */
public class MapController extends DefaultMapController implements
		ActionListener {

	ArrayList<MapMarker> linestring;
	AnimalsDatabase frame;

	DataBase db;

	JMapPanel map;
	private GeneralPath tempPath;

	public MapController(JMapViewer map) {
		super(map);
		super.setDoubleClickZoomEnabled(false);

		// frame a mapa
		this.map = (JMapPanel) map;
		frame = this.map.getFrame();
		db = frame.getDb();

		// data
		linestring = new ArrayList<MapMarker>();
	}

	/**
	 * Metoda ktera maze body podle toho kam se kliklo
	 *
	 * @param clicked
	 *            bod kam se kliklo
	 */
	private void deletePoints(Point clicked) {

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

			double dist = Point.distance(clicked.x, clicked.y, markerPoint.x,
					markerPoint.y);

			// pridej bod mezi body do kterych se klik trefil
			// TODO smaze to bod a pak neni mozne jit na dalsi v seznamu
			if (dist <= maxDist) {

				// pridani bodu do bodu, ktere se maji smazat
				toDelete.add(mapMarker);
			}
			((MapPoint) mapMarker).setSelected(false);
		}

		// smazani prislusnych bodu
		for (MapMarker mapMarker : toDelete) {
			Log.debug("Info o bodu: " + mapMarker);

			((MapPoint) mapMarker).setSelected(true);
			// markers.remove(mapMarker);
		}

		// repaint map
		map.repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {

		// bod v nekolika formatech
		Point clickedPoint = e.getPoint();
		Coordinate clickedCoordinate = map.getPosition(clickedPoint);
		MapPoint clickedMapPoint = new MapPoint(clickedCoordinate.getLat(), clickedCoordinate.getLon(), MapPoint.counter);

		// pro leve tlacitko mysi
		if (e.getButton() == MouseEvent.BUTTON1) {

			if (map.isEditMode()) {
				switch (map.getMode()) {
				case JMapPanel.MODE_POINT:
					// nakresli pouze bod pokud jsou body viditelne
					if (map.getMapMarkersVisible() && map.isEditMode()) {
						MapPoint.counter = MapPoint.counter + 1;
						map.addMapMarker(clickedMapPoint);

						Log.debug("Pridavam bod do mapy na souradnice: "
								+ clickedCoordinate);
					}
					break;
				case JMapPanel.MODE_LINESTRING:
				case JMapPanel.MODE_POLYGON:

					// odstran puvodni cast polygonu
					map.removeMapLinestring(linestring);

					// pridej novy bod a vykresli caru
					linestring.add(clickedMapPoint);
					map.addMapLinestring(linestring);

					Log.debug("Pridavam bod k linestring/polygon: "
							+ clickedCoordinate);
					break;

				default:
					break;
				}
			}
		}
		// pro prave tlacitko mysi
		else if (e.getButton() == MouseEvent.BUTTON3) {

			if (map.isEditMode()) {
				// deletePoints(e.getPoint());

			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		// doslo ke zmene v comboboxu
		if(e.getActionCommand() == JMapPanel.ACTION_CHANGE_TYPE){

			map.clear();
			linestring.clear(); // TODO doresit jak mazat docasna data
			Log.debug("Mapa vymazana");

			// TODO nastavit vse potrebne pro danou operaci
			JComboBox combo = ((JComboBox)e.getSource());

			int mode = combo.getSelectedIndex();
			map.setMode(mode);

			Log.debug("Mod = " + mode);
		}

		// zmacknuto edit
		if (e.getActionCommand() == JMapPanel.ACTION_EDIT) {

			AnimalsPanel animalsPanel = frame.getAnimalsPanel();

			// pokud je nejake zvire vybrano
			if (!animalsPanel.getList().isSelectionEmpty()) {
				frame.setEnable(false);
				map.setEditMode(true);
			} else {
				JOptionPane.showMessageDialog(frame, "Musíte vybrat nějaké zvíře", "Vyber zvíře", JOptionPane.ERROR_MESSAGE);
			}
		}

		// zmacknuto save
		if (e.getActionCommand() == JMapPanel.ACTION_SAVE) {

			switch(map.getMode()){
			case JMapPanel.MODE_POINT:

				List<MapMarker> points = map.getMapMarkerList();

				// TODO save to DB
				Log.debug("Ukladam " + points.size() + " bodu");

				break;
			case JMapPanel.MODE_LINESTRING:
				if (linestring.size() > 0) {

					// linestring uz je ulozen

					// TODO save to DB
					Log.debug("Ukladam linestring s " + linestring.size() + " ridicimi body");
				}
				break;
			case JMapPanel.MODE_POLYGON:
				if (linestring.size() > 0) {

					// odstran vse z mapy
					map.clear();

					// vytvor pole bodu

					double[] coords = new double[linestring.size() * 2];

					int i = 0;
					for (MapMarker mapMarker : linestring) {
						coords[i++] = mapMarker.getLat();
						coords[i++] = mapMarker.getLon();
					}

					// vytvoreni polygonu
					JGeometry geometry = JGeometry.createLinearPolygon(coords,
							2, 8307);

					// ulozeni do DB
					try {
						db.insertAppareance(frame.getAnimalsPanel().getSelectedAnimal().getId(), geometry);
					} catch (SQLException ex) {
						Log.error("Chyba pri ukladani polygonu do DB: " + ex.getMessage());
					}

					Log.debug("Ukladam polygon s " + linestring.size()
							+ " ridicimi body");


					Map<Integer, JGeometry> data = null;
					// TODO nacti vsechny ulozene entity a zobraz
					try {
						data  = db.selectAppareance(frame.getAnimalsPanel().getSelectedAnimal().getId());
					} catch (SQLException ex) {
						Log.error("Chyba pri ziskavani geometrii u zvirete: " + ex.getMessage());
					}

					map.setMapData(data);
				}
				break;
			}


			// TODO smaz pomocne promenne
			linestring.clear();

			// enable list
			frame.getAnimalsPanel().setEnabled(true);

			frame.setEnable(true);
			map.setEditMode(false);
		}

	}
}
