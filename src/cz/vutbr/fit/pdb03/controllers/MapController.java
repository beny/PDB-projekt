package cz.vutbr.fit.pdb03.controllers;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import oracle.spatial.geometry.JGeometry;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.DataBase;
import cz.vutbr.fit.pdb03.Log;
import cz.vutbr.fit.pdb03.gui.AnimalsPanel;
import cz.vutbr.fit.pdb03.map.ConvertGeo;
import cz.vutbr.fit.pdb03.map.JMapPanel;
import cz.vutbr.fit.pdb03.map.MapPoint;

/**
 * Trida zajistujici udalosti okolo mapy a to klikani mysi a tlacitek v mape
 *
 */
public class MapController extends DefaultMapController implements
		ActionListener {

	// hlavni frame
	AnimalsDatabase frame;

	// databaze
	DataBase db;

	// mapa
	JMapPanel map;

	// pomocne pole
	ArrayList<MapMarker> linestring;

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
	 * Metoda cistici mapu a docasne nastaveni
	 */
	public void clearMap(){
		linestring.clear();
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

	/**
	 * Zaokrouhlovaci metoda
	 * @param d cislo ktere chceme zaokrouhlit
	 * @param decimalPlace na kolik mist
	 * @return zaokrouhlene cislo
	 */
	public static double round(double d, int decimalPlace) {
		BigDecimal bd = new BigDecimal(Double.toString(d));
		bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
		return bd.doubleValue();
	}

	@Override
	public void mouseClicked(MouseEvent e) {

		// bod v nekolika formatech
		Point clickedPoint = e.getPoint();
		Coordinate clickedCoordinate = map.getPosition(clickedPoint);
		MapPoint clickedMapPoint = new MapPoint(clickedCoordinate.getLat(), clickedCoordinate.getLon(), MapPoint.counter);

		// pro leve tlacitko mysi
		if (e.getButton() == MouseEvent.BUTTON1) {

			// editacni mod
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
			// needitacni mod
			else {
				int changePosition = JOptionPane.showConfirmDialog(
						frame,
						"Chcete změnit vaší pozici na souřadnice\n"
								+ round(clickedCoordinate.getLat(), 4) + " x "
								+ round(clickedCoordinate.getLon(), 4) + " ?",
						"Nová pozice", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);

				if(changePosition == JOptionPane.YES_OPTION){
					map.setMyPosition(clickedMapPoint);
				}
			}
		}

		// pro prave tlacitko mysi
		if (e.getButton() == MouseEvent.BUTTON3) {

			if (map.isEditMode()) {

				JPopupMenu mContext = new JPopupMenu();
				JMenuItem miDelete = new JMenuItem("Delete");
				mContext.add(miDelete);
				mContext.show(map, clickedPoint.x, clickedPoint.y);


			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		// doslo ke zmene v comboboxu
		if(e.getActionCommand() == JMapPanel.ACTION_CHANGE_TYPE){

			// vymaz mapu
			map.clear();

			// nastaveni modu
			JComboBox combo = ((JComboBox)e.getSource());
			int mode = combo.getSelectedIndex();
			map.setMode(mode);
			Log.debug("Mod: " + mode);
		}

		// zmacknuto edit
		if (e.getActionCommand() == JMapPanel.ACTION_EDIT) {

			// vymaz mapu
			map.clear();

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

			JGeometry geometry = null;

			// preved na spravnou geometrii
			switch(map.getMode()){
			case JMapPanel.MODE_POINT:
				geometry = ConvertGeo.createPoint(map.getMapMarkerList());
				break;
			case JMapPanel.MODE_LINESTRING:
				geometry = ConvertGeo.createLinestring(linestring);
				break;
			case JMapPanel.MODE_POLYGON:
				geometry = ConvertGeo.createPolygon(linestring);
				break;
			}

			// ulozeni do DB
			try {
				db.insertAppareance(frame.getAnimalsPanel().getSelectedAnimal()
						.getId(), geometry);
			} catch (SQLException ex) {
				Log.error("Chyba pri ukladani polygonu do DB: "
						+ ex.getMessage());
			}

			// odstran vse z mapy
			map.clear();

			// nacti vsechny data ulozena k prave vybranemu zvireti
			Map<Integer, JGeometry> data = null;
			try {
				data = db.selectAppareance(frame.getAnimalsPanel()
						.getSelectedAnimal().getId());
			} catch (SQLException ex) {
				Log.error("Chyba pri ziskavani geometrii u zvirete: "
						+ ex.getMessage());
			}

			// vykresli vsechny data ke zvireti
			map.setMapData(data);

			// enable list
			frame.setEnable(true);

			// zrusit edit mod u mapy
			map.setEditMode(false);
		}

	}
}
