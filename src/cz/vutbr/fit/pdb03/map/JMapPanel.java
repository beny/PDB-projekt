package cz.vutbr.fit.pdb03.map;

import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;

import javax.swing.JButton;
import javax.swing.JComboBox;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.geometry.JGeometry.Point;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.OsmTileSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.DataBase;
import cz.vutbr.fit.pdb03.Log;
import cz.vutbr.fit.pdb03.controllers.MapController;
import cz.vutbr.fit.pdb03.dialogs.InfoDialog;
import cz.vutbr.fit.pdb03.gui.AnimalsPanel;
import cz.vutbr.fit.pdb03.gui.GUIManager;

/**
 * Trida rozsirujici moznosti zakladni mapy
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
public class JMapPanel extends JMapViewer implements ActionListener {

	private static final long serialVersionUID = -7269660504108541606L;

	// hlavni frame
	AnimalsDatabase frame;

	// databaze
	private DataBase db;

	// indikace editacniho modu
	private boolean editMode = false;

	// komponenta pro mapu
	JButton editButton, saveButton;
	private JComboBox comboElements;

	public JMapPanel(AnimalsDatabase frame) {
		super(new MemoryTileCache(), 4);

		this.frame = frame;

		// kontrolery
		new MapController(this);

		// databaze
		db = frame.getDb();

		// vlastnosti mapy
		setPreferredSize(null);
		setTileSource(new OsmTileSource.CycleMap());
		setTileLoader(new OsmTileLoader(this));

		initializeEditButtons();
	}

	/**
	 * Inicializace editacnich tlacitek
	 */
	protected void initializeEditButtons(){

		int buttonSizeX = 50;
		int buttonSizeY = 20;
		int smallSpace = 10;

		// edit tlacitko
		editButton = new JButton("edit");
		editButton.setBounds(50, smallSpace, buttonSizeX, buttonSizeY);
		editButton.addActionListener(this);
		add(editButton);

		// komponenty pro editaci

		// tlacitko pro ukladani
		saveButton = new JButton("save");
		saveButton.setBounds(50, smallSpace, buttonSizeX, buttonSizeY);
		saveButton.addActionListener(this);
		add(saveButton);

		// kombo pro vyber elementu
		String[] elements = {"Výskyt", "Trasa", "Území"};
		comboElements = new JComboBox(elements);
		comboElements.setBounds(50 + buttonSizeX + smallSpace, smallSpace, 120, buttonSizeY);
		// TODO add action listener nebo jaky se tady dava
		add(comboElements);

		setEditMode(false);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// TODO pridat moznost kreslit linestring
		// TODO pridat moznost kreslit polygon
	}


	public boolean isEditMode() {
		return editMode;
	}

	/**
	 * Metoda zobrazujici a schovavajici komponenty pro editaci elementu
	 * @param visible zda zobrazit ci nezobrazit
	 */
	public void setEditMode(boolean visible){

		// mod
		editMode = visible;

		// komponenty
		editButton.setVisible(!visible);
		saveButton.setVisible(visible);
		comboElements.setVisible(visible);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		// zmacknuto edit
		if (e.getSource() == editButton) {

			AnimalsPanel animalsPanel = frame.getAnimalsPanel();

			// pokud je nejake zvire vybrano
			if (!animalsPanel.getList().isSelectionEmpty()) {
				frame.setEnable(false);
				setEditMode(true);
			} else {
				InfoDialog dInfo = new InfoDialog("Musíte vybrat nějaké zvíře");
				GUIManager.moveToCenter(dInfo, frame);
				dInfo.setVisible(true);
			}
		}

		// zmacknuto save
		if(e.getSource() == saveButton){


			// TODO save elements
			Log.debug("pro zvire " + frame.getAnimalsPanel().getSelectedAnimal());

			for (MapMarker mapMarker : getMapMarkerList()) {
				JGeometry.Point point = new Point();
				point.set(mapMarker.getLat(), mapMarker.getLon());


			}

			// TODO odstranit oznaceni bodu/elementu

			// enable list
			frame.getAnimalsPanel().setEnabled(true);

			setEditMode(false);
		}

	}

}
