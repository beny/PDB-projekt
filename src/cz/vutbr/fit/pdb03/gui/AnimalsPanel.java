package cz.vutbr.fit.pdb03.gui;

import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JScrollPane;

import cz.vutbr.fit.pdb03.Animal;
import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.controllers.ListController;

public class AnimalsPanel extends JScrollPane {

	private static final long serialVersionUID = -1966207604674886330L;

	AnimalsDatabase frame;
	private JList lAnimals;

	ListController listController;

	public AnimalsPanel(AnimalsDatabase frame) {
		this.frame = frame;

		listController = new ListController(frame);

		// data
		lAnimals = new JList();
		frame.setList(lAnimals);
		lAnimals.addMouseListener(listController);
		lAnimals.addKeyListener(listController);

		// vlastnosti
		setViewportView(lAnimals);
		setPreferredSize(new Dimension(300, 500));
	}

	/**
	 * Metoda ziskavajici JList se zviraty
	 * @return JList se zviraty
	 */
	public JList getList(){
		return lAnimals;
	}

	public ListController getListController() {
		return listController;
	}

	/**
	 * Ziskani aktualne vybraneho zvirete v seznamu
	 * @return objekt Animal s vybranym zviretem
	 */
	public Animal getSelectedAnimal(){
		return listController.getSelectedAnimal();
	}

	/**
	 * Nastaveni dat pro list zvirat
	 * @param animals
	 */
	public void setData(Vector<Animal> animals){
		lAnimals.setListData(animals);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		lAnimals.setEnabled(enabled);
	}
}
