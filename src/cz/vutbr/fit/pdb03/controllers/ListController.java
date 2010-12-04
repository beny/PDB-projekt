package cz.vutbr.fit.pdb03.controllers;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.ListModel;

import cz.vutbr.fit.pdb03.Animal;
import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.DataBase;
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
		map.addMouseListener(this);

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
			setSelectedAnimal((Animal) dlm.getElementAt(index));

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
