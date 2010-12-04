package cz.vutbr.fit.pdb03.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.DataBase;
import cz.vutbr.fit.pdb03.Log;
import cz.vutbr.fit.pdb03.dialogs.AboutDialog;
import cz.vutbr.fit.pdb03.dialogs.AnimalDialog;
import cz.vutbr.fit.pdb03.dialogs.ConnectDialog;
import cz.vutbr.fit.pdb03.gui.GUIManager;
import cz.vutbr.fit.pdb03.map.JMapPanel;

public class MenuController implements ActionListener{

	public static final String CONNECT_TO_DB = "Připojit k databázi";
	public static final String DISCONNECT_FROM_DB = "Odpojit od databáze";

	// menu items
	private JMenuBar mBar;
	private JMenu mDatabase, mAbout, mAnimal;
	private JMenuItem miAboutInfo, miDatabaseConnection, miDatabaseCreate,
			miAnimalRefresh, miAnimalAdd;

	// dialog
	private ConnectDialog dConnect;

	// databaze
	DataBase db;

	// mapa
	JMapPanel map;

	// hlavni okno
	AnimalsDatabase frame;

	public MenuController(AnimalsDatabase frame) {

		// init promenych
		db = frame.getDb();
		map = frame.getMap();
		this.frame = frame;

		// hlavniho menu
		mBar = new JMenuBar();

		// menu databaze
		mDatabase = new JMenu("Databáze");
		mBar.add(mDatabase);

		miDatabaseConnection = new JMenuItem(CONNECT_TO_DB);
		miDatabaseConnection.addActionListener(this);
		mDatabase.add(miDatabaseConnection);

		miDatabaseCreate = new JMenuItem("Vytvořit prázdnou databázi");
		miDatabaseCreate.addActionListener(this);
		mDatabase.add(miDatabaseCreate);

		// menu zvire
		mAnimal = new JMenu("Zvíře");
		mBar.add(mAnimal);

		miAnimalAdd = new JMenuItem("Přidat zvíře");
		miAnimalAdd.addActionListener(this);
		mAnimal.add(miAnimalAdd);

		miAnimalRefresh = new JMenuItem("Obnov seznam zvířat");
		miAnimalRefresh.addActionListener(this);
		mAnimal.add(miAnimalRefresh);

		// menu about
		mAbout = new JMenu("About");
		mBar.add(mAbout);

		miAboutInfo = new JMenuItem("O aplikaci");
		miAboutInfo.addActionListener(this);
		mAbout.add(miAboutInfo);

		frame.setJMenuBar(mBar);
	}

	/**
	 * zmena polozky v menu dle pripojeni v DB
	 * @param connected
	 */
	public void setConnectionState(boolean connected){

		// nastaveni polozky pro pripojeni k databazi
		miDatabaseConnection.setText(connected?DISCONNECT_FROM_DB:CONNECT_TO_DB);
		miAnimalAdd.setEnabled(connected);
		miAnimalRefresh.setEnabled(connected);
		miDatabaseCreate.setEnabled(connected);
	}


	@Override
	public void actionPerformed(ActionEvent event) {


		// informacni dialog
		if(event.getSource() == miAboutInfo){
			AboutDialog dAbout = new AboutDialog();
			GUIManager.moveToCenter(dAbout, frame);
			dAbout.setVisible(true);
		}

		// pripojen/odpojeni k databazi
		if(event.getSource() == miDatabaseConnection){
			if(db.isConnected()){
				try {
					db.disconnect();
					Log.info("Disconnected");
				} catch (SQLException e){
					System.err.println("Error while disconnection from DB: " + e.getMessage());
				}

				frame.setEnable(db.isConnected());
				// TODO disable GUI
			}
			else {
				// dialog pro pripojeni
				dConnect = new ConnectDialog(frame, db);
				dConnect.fillDialog(ConnectDialog.ONDRA); // TODO remove

				GUIManager.moveToCenter(dConnect, frame);
				dConnect.setVisible(true);

				frame.setEnable(db.isConnected());
				// TODO enable GUI
			}
		}

		// vytvoreni tabulek v DB
		if(event.getSource() == miDatabaseCreate){
			if(db.isConnected()){
				try{
					Log.debug("Creating empty database");
					db.createDatabase();
				} catch (SQLException e){
					System.err.println("Chyba pri vytvareni DB: " + e.getMessage());
				}
			}

			frame.refreshAnimalsList();
		}

		// pridani zvirete
		if(event.getSource() == miAnimalAdd){
			AnimalDialog dAnimal = new AnimalDialog(frame);
			GUIManager.moveToCenter(dAnimal, frame);
			dAnimal.setVisible(true);
		}

		// obnoveni seznamu zivrat
		if(event.getSource() == miAnimalRefresh){
			frame.refreshAnimalsList();
		}
	}
}