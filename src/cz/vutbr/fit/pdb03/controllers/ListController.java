package cz.vutbr.fit.pdb03.controllers;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.Map;

import javax.swing.ListModel;

import oracle.spatial.geometry.JGeometry;
import cz.vutbr.fit.pdb03.Animal;
import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.DataBase;
import cz.vutbr.fit.pdb03.Log;
import cz.vutbr.fit.pdb03.dialogs.AnimalDialog;
import cz.vutbr.fit.pdb03.gui.GUIManager;
import cz.vutbr.fit.pdb03.map.JMapPanel;

/**
 * Trida zajistujici odchyceni klikani do mapy
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
public class ListController extends MouseAdapter {

	/**
	 * Reference na mapu (pro funkce pocitani pozice a pod)
	 */
	private JMapPanel map;
	private AnimalsDatabase frame;
	DataBase db;

	Animal selectedAnimal;

	public ListController(AnimalsDatabase frame) {
		this.frame = frame;
		db = frame.getDb();

		// pridani listeneru
		map = frame.getMap();

	}


	public Animal getSelectedAnimal() {
		return selectedAnimal;
	}


	public void setSelectedAnimal(Animal selectedAnimal) {
		this.selectedAnimal = selectedAnimal;
	}


	@Override
	public void mouseClicked(MouseEvent e) {

		// normalni klik
		if(e.getButton() == MouseEvent.BUTTON1){

			int index = frame.getList().locationToIndex(e.getPoint());
			ListModel dlm = frame.getList().getModel();

			Animal selectedAnimal = (Animal) dlm.getElementAt(index);
			setSelectedAnimal(selectedAnimal);

			Log.info("Ziskavam data o zvireti " + selectedAnimal);

			try {
				Map<Integer, JGeometry> spatialInfo = db.selectAppareance(selectedAnimal.getId());
//				Log.debug("data o pozici zvirete v DB: " + spatialInfo);
				map.setMapData(spatialInfo);
			} catch (SQLException ex){
				Log.error("Chyba pri hledani spatial info o zvireti " + selectedAnimal.getId());
			}

		}

		// pro leve tlacitko mysi dvojklik
		if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {

			int index = frame.getList().locationToIndex(e.getPoint());
			frame.getList().ensureIndexIsVisible(index);
			AnimalDialog dAnimal = new AnimalDialog(frame);
			GUIManager.moveToCenter(dAnimal, frame);
			dAnimal.fill(getSelectedAnimal());
			dAnimal.enableDeleteButton(true);
			dAnimal.setMode(AnimalDialog.UPDATE);
			dAnimal.setVisible(true);
		}

	}
}
