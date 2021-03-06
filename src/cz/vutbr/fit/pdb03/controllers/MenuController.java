package cz.vutbr.fit.pdb03.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

import cz.vutbr.fit.pdb03.Animal;
import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.Log;
import cz.vutbr.fit.pdb03.dialogs.AnimalDialog;
import cz.vutbr.fit.pdb03.dialogs.ConnectDialog;
import cz.vutbr.fit.pdb03.dialogs.ImageUploadDialog;
import cz.vutbr.fit.pdb03.dialogs.LoadingDialog;
import cz.vutbr.fit.pdb03.dialogs.PreferencesDialog;
import cz.vutbr.fit.pdb03.dialogs.SearchByDescriptionDialog;
import cz.vutbr.fit.pdb03.dialogs.SearchByImageDialog;
import cz.vutbr.fit.pdb03.dialogs.SearchByNameDialog;
import cz.vutbr.fit.pdb03.gui.GUIManager;
import cz.vutbr.fit.pdb03.gui.JEntity;

/**
 * Trida zpracovavajici udalosti okolo menu
 *
 * @author Pavel Srnec <xsrnec01@stud.fit.vutbr.cz>
 *
 */
public class MenuController implements ActionListener{

	// pomocne konstanty
	public final static String CONNECT_TO_DB = "Připojit k databázi";
	public final static String DISCONNECT_FROM_DB = "Odpojit od databáze";

	// menu items
	private JMenuBar mBar;
	private JMenu mAnimal, mApplication, mSearch;
	private JMenuItem miApplicationInfo, miApplicationDatabaseConnection,
			miApplicationExit, miApplicationDatabaseCreate,
			miApplicationDatabaseSample, miAnimalAdd, miAnimalEdit,
			miAnimalDelete, miAnimalInsertPicture, miAnimalSearch,
			miAnimalArea, miApplicationPreferences, miSearchAll,
			miSearchByName, miSearchClose, miSearchByDescription,
			miSearchByPicture, miSearchByPictureDescription,
			miSearchExtinction, miSearchArea;

	// dialog
	private ConnectDialog dConnect;
	private LoadingDialog dLoading = null;

	// hlavni okno
	AnimalsDatabase frame;

	public MenuController(AnimalsDatabase frame) {

		this.frame = frame;

		// hlavniho menu
		mBar = new JMenuBar();

		// menu aplikace
		mApplication = new JMenu("Aplikace");
		mBar.add(mApplication);

		miApplicationPreferences = new JMenuItem("Nastavení");
		mApplication.add(miApplicationPreferences);
		miApplicationPreferences.addActionListener(this);

		mApplication.add(new JSeparator());

		miApplicationDatabaseConnection = new JMenuItem(CONNECT_TO_DB);
		miApplicationDatabaseConnection.addActionListener(this);
		mApplication.add(miApplicationDatabaseConnection);

		mApplication.add(new JSeparator());

		miApplicationDatabaseCreate = new JMenuItem("Vytvořit prázdnou databázi");
		miApplicationDatabaseCreate.addActionListener(this);
		mApplication.add(miApplicationDatabaseCreate);

		miApplicationDatabaseSample = new JMenuItem("Vytvořit vzorovou databázi");
		miApplicationDatabaseSample.addActionListener(this);
		mApplication.add(miApplicationDatabaseSample);

		mApplication.add(new JSeparator());

		miApplicationInfo = new JMenuItem("O aplikaci");
		miApplicationInfo.addActionListener(this);
		mApplication.add(miApplicationInfo);

		mApplication.add(new JSeparator());

		miApplicationExit = new JMenuItem("Ukončit program");
		miApplicationExit.addActionListener(this);
		mApplication.add(miApplicationExit);

		// menu hledat
		mSearch = new JMenu("Hledat");
		mBar.add(mSearch);

		miSearchAll = new JMenuItem("Všechna zvířata v databázi");
		miSearchAll.addActionListener(this);
		mSearch.add(miSearchAll);

		mSearch.add(new JSeparator());

		miSearchByName = new JMenuItem("Podle jména");
		miSearchByName.addActionListener(this);
		mSearch.add(miSearchByName);

		miSearchByDescription = new JMenuItem("Podle popisu");
		miSearchByDescription.addActionListener(this);
		mSearch.add(miSearchByDescription);

		miSearchByPicture = new JMenuItem("Podle obrázku");
		miSearchByPicture.addActionListener(this);
		mSearch.add(miSearchByPicture);

		miSearchByPictureDescription = new JMenuItem("Podle popisu obrázku");
		miSearchByPictureDescription.addActionListener(this);
		mSearch.add(miSearchByPictureDescription);

		mSearch.add(new JSeparator());

		miSearchClose = new JMenuItem("Nejbližší zvířata");
		miSearchClose.addActionListener(this);
		mSearch.add(miSearchClose);

		mSearch.add(new JSeparator());

		miSearchArea = new JMenuItem("Zvířata s největším územím výskytu");
		miSearchArea.addActionListener(this);
		mSearch.add(miSearchArea);

		miSearchExtinction = new JMenuItem("Vyhynulá zvířata");
		miSearchExtinction.addActionListener(this);
		mSearch.add(miSearchExtinction);

		// menu zvire
		mAnimal = new JMenu("Zvíře");
		mBar.add(mAnimal);

		miAnimalAdd = new JMenuItem("Přidat");
		miAnimalAdd.addActionListener(this);
		mAnimal.add(miAnimalAdd);

		miAnimalEdit = new JMenuItem("Upravit");
		miAnimalEdit.addActionListener(this);
		mAnimal.add(miAnimalEdit);

		miAnimalDelete = new JMenuItem("Odstranit");
		miAnimalDelete.addActionListener(this);
		mAnimal.add(miAnimalDelete);

		mAnimal.add(new JSeparator());

		miAnimalInsertPicture = new JMenuItem("Vložit obrázek ke zvířeti");
		miAnimalInsertPicture.addActionListener(this);
		mAnimal.add(miAnimalInsertPicture);

		mAnimal.add(new JSeparator());

		miAnimalSearch = new JMenuItem("Najít zvířata na stejném území");
		miAnimalSearch.addActionListener(this);
		mAnimal.add(miAnimalSearch);

		miAnimalArea = new JMenuItem("Zobrazit území obývané rodem");
		miAnimalArea.addActionListener(this);
		mAnimal.add(miAnimalArea);

		frame.setJMenuBar(mBar);

		setConnected(false);
		setAnimalChosen(false);
	}

	/**
	 * Nastaveni menu pri editaci
	 * @param enabled
	 */
	public void setEditMode(boolean enabled) {
		mAnimal.setEnabled(!enabled);
		mSearch.setEnabled(!enabled);

		miApplicationDatabaseConnection.setEnabled(!enabled);
		miApplicationDatabaseCreate.setEnabled(!enabled);
		miApplicationDatabaseSample.setEnabled(!enabled);
		miApplicationPreferences.setEnabled(!enabled);

	}

	/**
	 * Nastaveni menu podle pripojeni
	 * @param connected
	 */
	public void setConnected(boolean connected) {
		miApplicationDatabaseConnection.setText(connected?DISCONNECT_FROM_DB:CONNECT_TO_DB);
		miApplicationDatabaseSample.setEnabled(connected);
		miApplicationDatabaseCreate.setEnabled(connected);

		mSearch.setEnabled(connected);
		mAnimal.setEnabled(connected);

	}

	/**
	 * Nastaveni zda je vybrano zvire ci ne
	 * @param selected
	 */
	public void setAnimalChosen(boolean selected) {
		miAnimalArea.setEnabled(selected);
		miAnimalDelete.setEnabled(selected);
		miAnimalEdit.setEnabled(selected);
		miAnimalInsertPicture.setEnabled(selected);
		miAnimalSearch.setEnabled(selected);
	}

	/**
	 * Podle pripojeni zobrazi ci nezobrazi dialog
	 * @param connected
	 */
	private void setDatabaseConnection(boolean connected){
		if(connected){
			try {
				frame.getDb().disconnect();
				Log.info("Disconnected");
			} catch (SQLException e){
				Log.error("Chyba pri odpojeni od databaze");
			}

			frame.setEnable(frame.getDb().isConnected());
		}
		else {
			// dialog pro pripojeni
			dConnect = new ConnectDialog(frame, frame.getDb());

			GUIManager.moveToCenter(dConnect, frame);
			dConnect.setVisible(true);

			frame.setEnable(frame.getDb().isConnected());
		}
	}

	/**
	 * Inicializace databaze
	 */
	private void initDatabase(){
		if(frame.getDb().isConnected()){
			try{
				Log.debug("Vytvarim prazdnou databazi");
				frame.getDb().createDatabase();
			} catch (SQLException e){
				Log.error("Chyba pri vytvareni DB: " + e.getMessage());
			}
		}

		frame.reloadAnimalsList(AnimalsDatabase.SEARCH_ALL);
	}

	/**
	 * Inicializace a naplneni databaze
	 */
	private void initAndFillDatabase(){
		if(frame.getDb().isConnected()){
			try {
				Log.debug("Vytvarim databazi se vzorovymi daty");
				frame.getDb().fillDatabase();
			} catch(SQLException e){
				Log.error("Chyba SQL pri vytvareni databaze se vzorovymi daty: " + e.getMessage());
			} catch(IOException e){
				Log.error("Chyba cteni souboru pri vytvareni databaze se vzorovymi daty: " + e.getMessage());
			}
		}

		frame.reloadAnimalsList(AnimalsDatabase.SEARCH_ALL);
	}

	@Override
	public void actionPerformed(ActionEvent event) {


		// informacni dialog
		if(event.getSource() == miApplicationInfo){
			JOptionPane.showMessageDialog(frame, "Projekt do předmětu PDB 2010/2011\nAutoři aplikace: xizakt00, xsrnec01 a xbenes00\n\nProjekt umožňuje vkládat prostorová data (výskyt zvířete), která se temporálně\nvztahují k nastavenému času. Dále projekt umožňuje vkládat ke zvířecímu\ndruhu fotky zvířete, stop a trusu, mezi kterými lze vyhledávat mj. podle\npodobnosti jiného obrázku.", "O aplikaci", JOptionPane.INFORMATION_MESSAGE);
		}

		// pripojen/odpojeni k databazi
		if(event.getSource() == miApplicationDatabaseConnection){
			setDatabaseConnection(frame.getDb().isConnected());
		}

		// vytvoreni tabulek v DB
		if (event.getSource() == miApplicationDatabaseCreate) {
                        dLoading = new LoadingDialog(
					"Probíhá vytvoření databáze, prosím vyčkejte");
			GUIManager.moveToCenter(dLoading, frame);
			new Thread(new Runnable() {

				@Override
				public void run() {
					initDatabase();
					if(dLoading != null && dLoading.isVisible()){
						dLoading.dispose();
					}
				}
			}).start();
			dLoading.setVisible(true);
		}

		// naplneni DB vzorovymi daty
		if (event.getSource() == miApplicationDatabaseSample) {
                        dLoading = new LoadingDialog(
					"Probíhá vytvoření a naplnění databáze, prosím vyčkejte");
			GUIManager.moveToCenter(dLoading, frame);
			new Thread(new Runnable() {

				@Override
				public void run() {
					initAndFillDatabase();
					if(dLoading != null && dLoading.isVisible()){
						dLoading.dispose();
					}

				}
			}).start();
			dLoading.setVisible(true);
		}

		// obrazovka s nastavenim
		if(event.getSource() == miApplicationPreferences){
			PreferencesDialog dPreferences = new PreferencesDialog(frame);
			GUIManager.moveToCenter(dPreferences, frame);
			dPreferences.setVisible(true);
		}


		// ukonceni aplikace
		if (event.getSource() == miApplicationExit) {
			frame.exitApp();
		}

		// pridani zvirete
		if(event.getSource() == miAnimalAdd){
			AnimalDialog dAnimal = new AnimalDialog(frame);
			GUIManager.moveToCenter(dAnimal, frame);
			dAnimal.setVisible(true);
		}

		// hledani
		if(event.getSource() == miSearchAll){
			frame.reloadAnimalsList(AnimalsDatabase.SEARCH_ALL);
		}

		if(event.getSource() == miSearchClose){
			frame.reloadAnimalsList(AnimalsDatabase.SEARCH_CLOSE);
		}

		if(event.getSource() == miSearchExtinction){
			frame.reloadAnimalsList(AnimalsDatabase.SEARCH_EXTINCT);
		}

		if(event.getSource() == miSearchArea){
			frame.reloadAnimalsList(AnimalsDatabase.SEARCH_BIGGEST_AREA);
		}

		if(event.getSource() == miAnimalSearch){
			frame.reloadAnimalsList(AnimalsDatabase.SEARCH_SAME_AREA);
		}

		if(event.getSource() == miSearchByName){
			SearchByNameDialog dialog = new SearchByNameDialog(frame);
			GUIManager.moveToCenter(dialog, frame);
			dialog.setVisible(true);
		}

		if(event.getSource() == miSearchByDescription){
			SearchByDescriptionDialog dialog = new SearchByDescriptionDialog(frame);
			GUIManager.moveToCenter(dialog, frame);
			dialog.setType(SearchByDescriptionDialog.TYPE_DESCRIPTION);
			dialog.setVisible(true);
		}

		if(event.getSource() == miSearchByPictureDescription){
			SearchByDescriptionDialog dialog = new SearchByDescriptionDialog(frame);
			GUIManager.moveToCenter(dialog, frame);
			dialog.setType(SearchByDescriptionDialog.TYPE_PICTURE_DESCRIPTION);
			dialog.setVisible(true);
		}

		if(event.getSource() == miSearchByPicture){
			SearchByImageDialog dialog = new SearchByImageDialog(frame);
			GUIManager.moveToCenter(dialog, frame);
			dialog.setVisible(true);
		}

		// editace zvirete
		if(event.getSource() == miAnimalEdit){

			AnimalDialog dAnimal = new AnimalDialog(frame);
			GUIManager.moveToCenter(dAnimal, frame);

			dAnimal.fill(frame.getAnimalsPanel().getSelectedAnimal());
			dAnimal.setMode(AnimalDialog.UPDATE);
			dAnimal.setVisible(true);
		}

		// uzemi obyvane rodem
		if(event.getSource() == miAnimalArea){
			Animal animal = frame.getAnimalsPanel().getSelectedAnimal();
			try {
				List<JEntity> data = frame.getDb().selectAppareance(
						animal.getGenus(), animal.getGenusLat());
				frame.getMap().setData(data);
				frame.getMap().repaint();

				// zrus vyber a vymaz info panel
				frame.getAnimalsPanel().getList().clearSelection();
				frame.getPhotosPanel().clear();
			} catch(SQLException e){
				Log.error("Chyba pri hledani spolecne plochy");
			}
		}

		// smazani zvirete
		if(event.getSource() == miAnimalDelete) {
			int result = JOptionPane.showConfirmDialog(frame,
					"Chcete opravdu odstranit zvíře "+frame.getAnimalsPanel().getSelectedAnimal().toString()+"?",
					"Odstranění zvířete", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);

			if (result == JOptionPane.YES_OPTION) {
				frame.deleteAnimal(frame.getAnimalsPanel().getSelectedAnimal());
			}
		}

		// vkladani obrazku ke zvireti
		if(event.getSource() == miAnimalInsertPicture){
			ImageUploadDialog dialog = new ImageUploadDialog(frame);
			GUIManager.moveToCenter(dialog, frame);
			dialog.setVisible(true);
		}
	}
}