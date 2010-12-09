package cz.vutbr.fit.pdb03.controllers;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.Map;

import javax.swing.ListModel;

import oracle.spatial.geometry.JGeometry;
import cz.vutbr.fit.pdb03.Animal;
import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.Log;
import cz.vutbr.fit.pdb03.dialogs.AnimalDialog;
import cz.vutbr.fit.pdb03.gui.GUIManager;

/**
 * Trida zajistujici odchyceni klikani do mapy
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
public class ListController extends MouseAdapter implements KeyListener {

	/**
	 * Reference na frame
	 */
	private AnimalsDatabase frame;

	Animal selectedAnimal;

	public ListController(AnimalsDatabase frame) {
		this.frame = frame;
	}


	public Animal getSelectedAnimal() {
		return selectedAnimal;
	}


	public void setSelectedAnimal(Animal selectedAnimal) {
		this.selectedAnimal = selectedAnimal;
		frame.getMenuController().setAnimalChosen(true);	// nastav ze je zvire vybrano
	}


	@Override
	public void mouseClicked(MouseEvent e) {

		if (frame.getDb().isConnected()) {
			// normalni klik
			if (e.getButton() == MouseEvent.BUTTON1) {

				int index = frame.getList().locationToIndex(e.getPoint());
				ListModel dlm = frame.getList().getModel();

				// implicitni zakazni menu
				frame.getMenuController().setAnimalChosen(false);

				Animal selectedAnimal = (Animal) dlm.getElementAt(index);
				setSelectedAnimal(selectedAnimal);

				try {
					Map<Integer, JGeometry> spatialInfo = frame.getDb()
							.selectAppareance(selectedAnimal.getId());
					frame.getMap().setMapData(spatialInfo);
				} catch (SQLException ex) {
					Log.error("Chyba pri hledani spatial info o zvireti "
							+ selectedAnimal.getId());
				}

				Log.debug("Aktivni zvire: " + getSelectedAnimal());

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


	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e){
		// odchyceni klavesy nahoru ci dolu
		if(e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN){
			Log.debug("Byla zmacknuta klavesa: nahoru ");

			ListModel dlm = frame.getList().getModel();

			// implicitni zakazni menu
			frame.getMenuController().setAnimalChosen(false);

			Animal selectedAnimal = (Animal) dlm.getElementAt(frame.getList().getSelectedIndex());
			setSelectedAnimal(selectedAnimal);

			try {
				Map<Integer, JGeometry> spatialInfo = frame.getDb()
						.selectAppareance(selectedAnimal.getId());
				frame.getMap().setMapData(spatialInfo);
			} catch (SQLException ex) {
				Log.error("Chyba pri hledani spatial info o zvireti "
						+ selectedAnimal.getId());
			}

			Log.debug("Aktivni zvire: " + getSelectedAnimal());
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {}
}
