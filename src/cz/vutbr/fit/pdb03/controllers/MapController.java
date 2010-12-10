package cz.vutbr.fit.pdb03.controllers;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.RepaintManager;

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
	private boolean gotPoint = false;

	// hlavni frame
	private AnimalsDatabase frame;
	private JMapPanel map;

	// pomocne promenne
	private JEntity close = null;

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
		map.clearMapData();

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
		map.initTempData();

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
		List<JEntity> tempData = map.getTempData();
		List<JEntity> data = map.getData();

		try {
			// ulozeni novych entit
			for (JEntity geometry : tempData) {
				frame.getDb().insertAppareance(
						frame.getAnimalsPanel().getSelectedAnimal().getId(),
						geometry);
			}

			// uprava starsich entit
			for (JEntity geometry : data) {
				frame.getDb().updateAppareance(geometry.getId(), geometry);
			}
		} catch (SQLException ex) {
			Log.error("Chyba pri ukladani geometrie do DB: " + ex.getMessage());
		}

		reloadMapData();
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

		// bod v nekolika formatech
		final Point clickedPoint = e.getPoint();
		final Coordinate clickedCoordinate = map.getPosition(clickedPoint);

		// prekresleni bodu pod mys
		if(gotPoint){
			Log.debug("presouvam bod");
			close.movePoint(clickedCoordinate.getLat(),
					clickedCoordinate.getLon());
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
			if(gotPoint){
				gotPoint = false;
				close = null;
				map.repaint();
				map.setEditButtonsEnabled(true);
			}
			// editacni mod
			else if (map.isEditMode()) {
				switch (map.getDrawMode()) {
				case JMapPanel.MODE_POINT:
					Log.debug("Kreslim novy bod " + clickedCoordinate);
					map.tempAddPoint(clickedJEntity); break;
//				case JMapPanel.MODE_CURVE:
//				case JMapPanel.MODE_POLYGON:
				default:
					break;
				}
			}
		}

		// pro prave tlacitko mysi
		if (e.getButton() == MouseEvent.BUTTON3) {

			// kontextove menu
			JPopupMenu mContext = new JPopupMenu();
			JMenuItem miSetPosition = new JMenuItem("Nastavit jako moje pozice");
			miSetPosition.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					map.setMyPosition(clickedJEntity);
				}
			});
			mContext.add(miSetPosition);

			// posun bod
			if(map.isEditMode()){
				close = map.detectHit(e.getPoint());

				// nasel se nejaky bod
				if(close != null){
					map.repaint();
					Log.debug("bod " + close);
					mContext.add(new JSeparator());
					JMenuItem move = new JMenuItem("Chyť bod");
					move.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent arg0) {
							gotPoint = true;
							map.setEditButtonsEnabled(false);
							close.movePoint(clickedCoordinate.getLat(),
									clickedCoordinate.getLon());
							map.repaint();
						}
					});
					mContext.add(move);

				}
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
