package cz.vutbr.fit.pdb03.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.Log;
import cz.vutbr.fit.pdb03.dialogs.AnimalDialog;
import cz.vutbr.fit.pdb03.dialogs.ConnectDialog;
import cz.vutbr.fit.pdb03.dialogs.PreferencesDialog;
import cz.vutbr.fit.pdb03.dialogs.SearchByDescriptionDialog;
import cz.vutbr.fit.pdb03.dialogs.SearchByNameDialog;
import cz.vutbr.fit.pdb03.gui.GUIManager;

public class MenuController implements ActionListener{

	// pomocne konstanty
	public final static String CONNECT_TO_DB = "Připojit k databázi";
	public final static String DISCONNECT_FROM_DB = "Odpojit od databáze";

	public final static int MODE_DISCONNECTED = 0;
	public final static int MODE_CONNECTED = 1;
	public final static int MODE_ANIMAL = 2;
	public final static int MODE_ANIMAL_OFF = 3;


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

	// hlavni okno
	AnimalsDatabase frame;

	// mod menu
	int mode = MODE_DISCONNECTED;

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

		miApplicationDatabaseSample = new JMenuItem("Vytvořit a naplnit databázi vzorky");
		miApplicationDatabaseSample.addActionListener(this);
		mApplication.add(miApplicationDatabaseSample);

		mApplication.add(new JSeparator());

		miApplicationInfo = new JMenuItem("O aplikaci");
		miApplicationInfo.addActionListener(this);
		mApplication.add(miApplicationInfo);

		mApplication.add(new JSeparator());

		miApplicationExit = new JMenuItem("Ukončit");
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

		miAnimalArea = new JMenuItem("Území obývané rodem");
		miAnimalArea.addActionListener(this);
		mAnimal.add(miAnimalArea);

		frame.setJMenuBar(mBar);

		setMode(MODE_ANIMAL_OFF);
	}

	private void setDatabaseConnection(boolean connected){
		if(connected){
			try {
				frame.getDb().disconnect();
				Log.info("Disconnected");
			} catch (SQLException e){
				System.err.println("Error while disconnection from DB: " + e.getMessage());
			}

			frame.setEnable(frame.getDb().isConnected());
		}
		else {
			// dialog pro pripojeni
			dConnect = new ConnectDialog(frame, frame.getDb());
			dConnect.fillDialog(ConnectDialog.ONDRA); // TODO remove

			GUIManager.moveToCenter(dConnect, frame);
			dConnect.setVisible(true);

			frame.setEnable(frame.getDb().isConnected());
		}
	}

	private void initDatabase(){
		if(frame.getDb().isConnected()){
			try{
				Log.debug("Creating empty database");
				frame.getDb().createDatabase();
			} catch (SQLException e){
				Log.error("Chyba pri vytvareni DB: " + e.getMessage());
			}
		}

		frame.reloadAnimalsList(AnimalsDatabase.SEARCH_ALL);
	}

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
			JOptionPane.showMessageDialog(frame, "Autoři aplikace: xizakt00, xsrnec01 a xbenes00", "O aplikaci do předmětu PDB", JOptionPane.INFORMATION_MESSAGE);
		}

		// pripojen/odpojeni k databazi
		if(event.getSource() == miApplicationDatabaseConnection){
			setDatabaseConnection(frame.getDb().isConnected());
		}

		// vytvoreni tabulek v DB
		if(event.getSource() == miApplicationDatabaseCreate){
			initDatabase();
		}

		// naplneni DB vzorovymi daty
		if(event.getSource() == miApplicationDatabaseSample){
			initAndFillDatabase();
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
			frame.reloadAnimalsList(AnimalsDatabase.SEARCH_AREA);
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

		// TODO hledani podle obrazku
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;

		// pripojeno ci ne
		if(mode == MODE_CONNECTED || mode == MODE_DISCONNECTED){
			boolean connected = (mode == MODE_CONNECTED)?true:false;
			miApplicationDatabaseConnection.setText(connected?DISCONNECT_FROM_DB:CONNECT_TO_DB);
			miApplicationDatabaseSample.setEnabled(connected);
			miApplicationDatabaseCreate.setEnabled(connected);

			mSearch.setEnabled(connected);
			mAnimal.setEnabled(connected);
		}

		// povoleni operaci pri vybrani zvirete
		if(mode == MODE_ANIMAL || mode == MODE_ANIMAL_OFF){

			boolean selected = (mode == MODE_ANIMAL)?true:false;
			miAnimalArea.setEnabled(selected);
			miAnimalDelete.setEnabled(selected);
			miAnimalEdit.setEnabled(selected);
			miAnimalInsertPicture.setEnabled(selected);
			miAnimalSearch.setEnabled(selected);
		}

	}
}