package cz.vutbr.fit.pdb03.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;

import cz.vutbr.fit.pdb03.Animal;
import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.DataBase;
import cz.vutbr.fit.pdb03.Log;
import cz.vutbr.fit.pdb03.dialogs.AnimalDialog;
import cz.vutbr.fit.pdb03.dialogs.LoadingDialog;
import cz.vutbr.fit.pdb03.gui.GUIManager;
import cz.vutbr.fit.pdb03.gui.JEntity;
import cz.vutbr.fit.pdb03.gui.JPicture;

/**
 * Trida ktera se stara o udalosti seznamu se zviraty
 *
 * @author Ondřej Beneš <xbenes00@stud.fit.vutbr.cz>
 *
 */
public class ListController extends MouseAdapter implements KeyListener {

	/**
	 * Reference na frame
	 */
	private AnimalsDatabase frame;
	private LoadingDialog dLoading = null;
	Animal selectedAnimal;

	public ListController(AnimalsDatabase frame) {
		this.frame = frame;
	}

	public Animal getSelectedAnimal() {
		return selectedAnimal;
	}


	/**
	 * Nastaveni aktivniho zvirete
	 * @param selectedAnimal
	 */
	public void setSelectedAnimal(Animal selectedAnimal) {
		this.selectedAnimal = selectedAnimal;
		frame.getMenuController().setAnimalChosen(true);	// nastav ze je zvire vybrano

		new Thread(new Runnable() {
			@Override
			public void run() {
				// nastaveni objektu v mape
				try {
					List<JEntity> spatialInfo = frame.getDb()
							.selectAppareance(getSelectedAnimal().getId());
					frame.getMap().setMapData(spatialInfo);
				} catch (SQLException ex) {
					Log.error("Chyba pri hledani spatial info o zvireti "
							+ getSelectedAnimal().getId());
				}

				// info o zvireti
				frame.getPhotosPanel().setInfo(getSelectedAnimal());

				// ziskat fotky
				getPhotos(DataBase.ANIMAL_PHOTO);
				getPhotos(DataBase.FEET_PHOTO);
				getPhotos(DataBase.EXCREMENT_PHOTO);

				if(dLoading != null && dLoading.isVisible()){
					dLoading.dispose();
				}

				frame.getPhotosPanel().repaint();
			}
		}).start();

		dLoading = new LoadingDialog("Probíhá nahrávání dat z databáze, prosím vyčkejte");
		GUIManager.moveToCenter(dLoading, frame);
		dLoading.setVisible(true);

		Log.debug("Aktivni zvire: " + getSelectedAnimal());
	}

	/**
	 * Ziskani fotek z DB
	 * @param table nazev tabulky
	 * @param animal pro jake zvire se ma hledat
	 */
	private void getPhotos(String table) {

		try {

			// ziskat fotky
			List<JPicture> data = frame.getDb().selectPicture(
					getSelectedAnimal().getId(), true, table);

			// ziskat popisek pro fotky
			for (JPicture pic : data) {
				pic.setDescription(frame.getDb().getPhotoDescription(
						pic.getId(), table));
			}

			// nastavit fotky do panelu
			frame.getPhotosPanel().setPhotos(table, data);
		} catch (SQLException e) {
			Log.error("Chyba pri ziskavani obrazku z DB");
		}
	}

	/**
	 * Zobrazeni dialogu s editaci zvirete
	 */
	private void showAnimalDialog(){

		AnimalDialog dAnimal = new AnimalDialog(frame);
		GUIManager.moveToCenter(dAnimal, frame);

		dAnimal.fill(getSelectedAnimal());
		dAnimal.setMode(AnimalDialog.UPDATE);
		dAnimal.setVisible(true);
	}

	/**
	 * Dialog pro mazani
	 */
	private void showDeleteAnimalDialog(){
		int result = JOptionPane.showConfirmDialog(frame,
				"Chcete opravdu odstranit zvíře "+frame.getAnimalsPanel().getSelectedAnimal().toString()+"?",
				"Odstranění zvířete", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);

		if (result == JOptionPane.YES_OPTION) {
			frame.deleteAnimal(frame.getAnimalsPanel().getSelectedAnimal());
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {

		if (frame.getDb().isConnected()) {

			// implicitni zakazni menu
			frame.getMenuController().setAnimalChosen(false);

			// vyber zvirete podle kliku
			int index = frame.getList().locationToIndex(e.getPoint());
			ListModel dlm = frame.getList().getModel();
			Animal selectedAnimal = (Animal) dlm.getElementAt(index);
			frame.getList().setSelectedIndex(index);
			setSelectedAnimal(selectedAnimal);

			// prave tlacitko
			if(e.getButton() == MouseEvent.BUTTON3){
				JPopupMenu mContext = new JPopupMenu();

				JMenuItem miEdit = new JMenuItem("Uprav zvíře");
				miEdit.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						showAnimalDialog();
					}
				});

				mContext.add(miEdit);

				JMenuItem miDelete = new JMenuItem("Smaž zvíře");
				miDelete.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						showDeleteAnimalDialog();
					}
				});

				mContext.add(miDelete);

				mContext.show(frame.getList(), e.getPoint().x, e.getPoint().y);
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
				List<JEntity> spatialInfo = frame.getDb()
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
