package cz.vutbr.fit.pdb03.controllers;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import oracle.spatial.geometry.JGeometry;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.DefaultMapController;

import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.Log;
import cz.vutbr.fit.pdb03.map.JEntity;
import cz.vutbr.fit.pdb03.map.JMapPanel;

/**
 * Trida zajistujici udalosti okolo mapy a to klikani mysi a tlacitek v mape
 *
 */
public class MapController extends DefaultMapController implements
		ActionListener {

	/**
	 * Detekce pohybu
	 */
	private boolean gotEntity = false;

	// hlavni frame
	private AnimalsDatabase frame;
	private JMapPanel map;

	// pomocne promenne
	private JEntity hitEntity = null;

	public MapController(JMapPanel map) {
		super(map);
		super.setDoubleClickZoomEnabled(false);

		// frame a mapa
		this.map = map;
		frame = map.getFrame();
	}

	/**
	 * Znovu nacte data a zobrazi na mape
	 */
	private void reloadMapData(){

		// odstran vse z mapy
		map.clearMap();

		// nacti vsechny data ulozena k prave vybranemu zvireti
		List<JEntity> data = null;
		try {
			data = frame.getDb().selectAppareance(frame.getAnimalsPanel()
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

		// zrusit mod u menu
		frame.getMenuController().setEditMode(false);

	}

	/**
	 * Akce vyvolana zmacknutim tlacitka upravit
	 */
	private void editAction() {

		// inicializace docasnych dat
		map.clearTempData();

		// musi byt vybrano zvire
		if (!frame.getAnimalsPanel().getList().isSelectionEmpty()) {
			frame.setEnable(false); // zruseni dostupnosti prvku v hlavnim okne
			map.setEditMode(true);	// nastaveni edit modu
			frame.getMenuController().setEditMode(true);	// disable menu
		} else {
			JOptionPane.showMessageDialog(frame, "Musíte vybrat nějaké zvíře",
					"Vyber zvíře", JOptionPane.ERROR_MESSAGE);
		}

	}

	/**
	 * Akce po zmacknuti tlacitka ulozeni
	 */
	private void saveAction() {

		// ziskani nakreslenych entit
		List<JEntity> insertData = map.getInsertData();
		List<JEntity> updateData = map.getUpdateData();
		List<JEntity> deleteData = map.getDeleteData();

		// ulozeni novych entit
		List<JGeometry> saveInsertData = checkMulti(insertData);
		try {
			// vlozeni geometrii
			for (JGeometry geometry : saveInsertData) {
				frame.getDb().insertAppareance(
						frame.getAnimalsPanel().getSelectedAnimal().getId(),
						geometry);
			}
			Log.debug("Vlozeno " + saveInsertData.size() + " geometrii");

			//  uprava entit
			for (JEntity geometry : updateData){
				frame.getDb().updateAppareance(geometry.getId(), geometry);
			}
			Log.debug("Upraveno " + updateData.size() + " geometrii");

			//  smazani entit
			for (JEntity geometry : deleteData){
				frame.getDb().deleteSpatialData(geometry.getId());
			}
			Log.debug("Smazano " + deleteData.size() + " geometrii");

		} catch (SQLException ex) {
			Log.error("Chyba pri ukladani geometrie do DB: " + ex.getMessage());
		}

		reloadMapData();
	}

	/**
	 * Prevod vlozenych entit na entity a multi-entity
	 * @param data
	 * @return
	 */
	private List<JGeometry> checkMulti(List<JEntity> data){

		// detekce multi entit
		int numPoints = 0;
		int numCurves = 0;
		int numPolygons = 0;
		for (JEntity geometry : data) {
			switch (geometry.getType()) {
			case JEntity.GTYPE_POINT:
				numPoints++;
				break;
			case JEntity.GTYPE_CURVE:
				numCurves++;
				break;
			case JEntity.GTYPE_POLYGON:
				numPolygons++;
				break;
			default:
				break;
			}
		}

//		Log.debug("Nalezeno " + numPoints + " bodu");
//		Log.debug("Nalezeno " + numCurves + " krivek");
//		Log.debug("Nalezeno " + numPolygons + " polygonu");

		// docasne pole
		JEntity point = null;
		JEntity curve = null;
		JEntity polygon = null;
		List<JEntity> points = new LinkedList<JEntity>();
		List<JEntity> curves = new LinkedList<JEntity>();
		List<JEntity> polygons = new LinkedList<JEntity>();

		// priprava novych geometrii
		for (JEntity geometry : data) {

			if (geometry.getType() == JEntity.GTYPE_POINT){
				if(numPoints == 1){
					point = geometry;
				}
				else if(numPoints > 1){
					points.add(geometry);
				}
			}

			if (geometry.getType() == JEntity.GTYPE_CURVE) {
				if(numCurves == 1){
					curves.add(geometry);
				}
				else if(numCurves > 1){
					curves.add(geometry);
				}
			}
			if (geometry.getType() == JEntity.GTYPE_POLYGON) {
				if(numPolygons == 1){
					polygon = geometry;
				}
				else {
					polygons.add(geometry);
				}
			}
		}

		// ulozne promenne
		List<JGeometry> saveInsertData = new LinkedList<JGeometry>();
		if(numPoints > 1){
			saveInsertData.add(JEntity.createMultiPoint(points));
		} else if(numPoints == 1) {
			saveInsertData.add(point);
		}

		if(numCurves > 1){
			saveInsertData.add(JEntity.createMultiCurve(curves));
		} else if(numCurves == 1) {
			saveInsertData.add(curve);
		}

		if(numPolygons > 1){
			saveInsertData.add(JEntity.createMultiPolygon(polygons));
		} else if(numPolygons == 1){
			saveInsertData.add(polygon);
		}

		return saveInsertData;
	}

	/**
	 * Metoda menici mod kresleni
	 */
	private void changeDrawTypeAction(JComboBox combo){

		int mode = combo.getSelectedIndex();
		map.setDrawMode(mode);
		Log.debug("Mod: " + mode);
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
	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);

		// prekresleni bodu pod mys
		if(gotEntity){

			// bod v nekolika formatech
			Point movePoint = e.getPoint();
			Coordinate moveCoords = map.getPosition(movePoint);

			hitEntity.movePoint(moveCoords.getLat(), moveCoords.getLon());
			// TODO ostatni entity

			map.repaint();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {

		// bod v nekolika formatech
		final Point clickedPoint = e.getPoint();
		final Coordinate clickedCoordinate = map.getPosition(clickedPoint);
		final JEntity clickedJEntity = new JEntity(clickedCoordinate.getLat(), clickedCoordinate.getLon());

		// pro leve tlacitko mysi
		if (e.getButton() == MouseEvent.BUTTON1) {

			// polozeni bodu
			if(gotEntity){

				Log.debug("Konec presunu bodu");

				hitEntity.setSelected(false);
				gotEntity = false;
				hitEntity = null;
				map.repaint();
				map.setEditButtonsEnabled(true);
			}
			// editacni mod
			else if (map.isEditMode()) {
				switch (map.getDrawMode()) {
				case JMapPanel.MODE_POINT:
					Log.debug("Kreslim novy bod " + clickedCoordinate);
					map.addPoint(clickedJEntity); break;

				default:
					break;
				}
			}
		}

		// pro prave tlacitko mysi
		if (e.getButton() == MouseEvent.BUTTON3) {

			// kontextove menu
			JPopupMenu mContext = new JPopupMenu();

			// posun bod
			if(map.isEditMode()){
				map.detectHit(e.getPoint());
				hitEntity = map.getHitEntity();

				// nasel se nejaky bod
				if(hitEntity != null){

					// presun geometrie
					JMenuItem move = new JMenuItem("Chyť");
					move.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent arg0) {
							gotEntity = true;

							map.setEditButtonsEnabled(false);

							// nove nepridavej do update
							if (hitEntity.getId() != 0) {
								map.updateEntity(hitEntity);
							}
							hitEntity.movePoint(clickedCoordinate.getLat(),
									clickedCoordinate.getLon());

							// TODO move ostanich entit

							Log.debug("Geometrie " + hitEntity + " chycena");
							map.repaint();
						}
					});
					mContext.add(move);

					// mazani geometrie
					JMenuItem delete = new JMenuItem("Smaž");
					delete.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent arg0) {
							map.deleteEntity(hitEntity);

							Log.debug("Geometrie " + hitEntity + " smazana");
						}
					});
					mContext.add(delete);

				}
			}
			// pokud neni editacni mod tak jen nastavovani pozice
			else {
				JMenuItem miSetPosition = new JMenuItem(
						"Nastavit jako moje pozice");
				miSetPosition.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						map.setMyPosition(clickedJEntity);
					}
				});
				mContext.add(miSetPosition);
			}

			mContext.show(map, clickedPoint.x, clickedPoint.y);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand() == JMapPanel.ACTION_CHANGE_TYPE){
			changeDrawTypeAction(((JComboBox)e.getSource()));
		}

		if (e.getActionCommand() == JMapPanel.ACTION_EDIT) {
			editAction();
		}

		if(e.getActionCommand() == JMapPanel.ACTION_CANCEL){
			reloadMapData();
		}

		if (e.getActionCommand() == JMapPanel.ACTION_SAVE) {
			saveAction();
		}

		if(e.getActionCommand() == JMapPanel.ACTION_NEXT_OBJECT){
			Log.debug("Tady ulozim a vykreslim jeden hotovy objekt a kreslime dalsi");
		}
	}
}
