package cz.vutbr.fit.pdb03.map;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.OsmTileSource;

import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.DataBase;
import cz.vutbr.fit.pdb03.controllers.MapController;

/**
 * Trida rozsirujici moznosti zakladni mapy
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
public class JMapPanel extends JMapViewer implements ActionListener {

	// databaze
	private DataBase db;

	// indikace editacniho modu
	private boolean editMode = false;

	// komponenta pro mapu
	JButton editButton, saveButton;
	private JComboBox comboElements;

	public JMapPanel(AnimalsDatabase frame) {
		super(new MemoryTileCache(), 4);

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
		if(e.getSource() == editButton){
			setEditMode(true);
		}

		// zmacknuto save
		if(e.getSource() == saveButton){

			// TODO save elements

			// TODO odstranit oznaceni bodu/elementu

			setEditMode(false);
		}

	}

	protected void initializeEditButtons(){

		int buttonSize = 40;
		int smallSpace = 10;

		// edit tlacitko
		editButton = new JButton("edit");
		editButton.setBounds(50, smallSpace, buttonSize, 20);
		editButton.addActionListener(this);
		add(editButton);

		// komponenty pro editaci

		// tlacitko pro ukladani
		saveButton = new JButton("save");
		saveButton.setBounds(50, smallSpace, buttonSize, 20);
		saveButton.addActionListener(this);
		add(saveButton);

		// kombo pro vyber elementu
		String[] elements = {"Bod", "Čára", "Multiline", "Polygon", "Rectangle"};
		comboElements = new JComboBox(elements);
		comboElements.setBounds(50 + buttonSize + smallSpace, smallSpace, 120, 20);
		// TODO add action listener nebo jaky se tady dava
		add(comboElements);

		setEditMode(false);
	}

}
