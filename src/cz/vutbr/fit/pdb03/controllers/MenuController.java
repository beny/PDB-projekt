package cz.vutbr.fit.pdb03.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.DataBase;
import cz.vutbr.fit.pdb03.Log;
import cz.vutbr.fit.pdb03.dialogs.AnimalDialog;
import cz.vutbr.fit.pdb03.dialogs.ConnectDialog;
import cz.vutbr.fit.pdb03.dialogs.PreferencesDialog;
import cz.vutbr.fit.pdb03.gui.GUIManager;
import cz.vutbr.fit.pdb03.map.JMapPanel;

public class MenuController implements ActionListener{

	public static final String CONNECT_TO_DB = "Připojit k databázi";
	public static final String DISCONNECT_FROM_DB = "Odpojit od databáze";

	// menu items
	private JMenuBar mBar;
	private JMenu mDatabase, mAnimal, mApplication;
	private JMenuItem miApplicationInfo, miDatabaseConnection, miDatabaseCreate, miDatabaseSample,
			miAnimalRefresh, miAnimalAdd, miApplicationPreferences;

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

		// menu aplikace
		mApplication = new JMenu("Aplikace");
		mBar.add(mApplication);

		miApplicationPreferences = new JMenuItem("Nastavení aplikace");
		mApplication.add(miApplicationPreferences);
		miApplicationPreferences.addActionListener(this);

		miApplicationInfo = new JMenuItem("O aplikaci");
		miApplicationInfo.addActionListener(this);
		mApplication.add(miApplicationInfo);

		// menu databaze
		mDatabase = new JMenu("Databáze");
		mBar.add(mDatabase);

		miDatabaseConnection = new JMenuItem(CONNECT_TO_DB);
		miDatabaseConnection.addActionListener(this);
		mDatabase.add(miDatabaseConnection);

		miDatabaseCreate = new JMenuItem("Vytvořit prázdnou databázi");
		miDatabaseCreate.addActionListener(this);
		mDatabase.add(miDatabaseCreate);

		miDatabaseSample = new JMenuItem("Vytvořit a naplnit databázi vzorky");
		miDatabaseSample.addActionListener(this);
		mDatabase.add(miDatabaseSample);

		// menu zvire
		mAnimal = new JMenu("Zvíře");
		mBar.add(mAnimal);

		miAnimalAdd = new JMenuItem("Přidat zvíře");
		miAnimalAdd.addActionListener(this);
		mAnimal.add(miAnimalAdd);

		miAnimalRefresh = new JMenuItem("Obnov seznam zvířat");
		miAnimalRefresh.addActionListener(this);
		mAnimal.add(miAnimalRefresh);

		frame.setJMenuBar(mBar);
	}

	/**
	 * zmena polozky v menu dle pripojeni v DB
	 * @param connected
	 */
	public void setConnectionState(boolean connected){

		// nastaveni polozky pro pripojeni k databazi
		miDatabaseConnection.setText(connected?DISCONNECT_FROM_DB:CONNECT_TO_DB);
		miDatabaseSample.setEnabled(connected);
		miAnimalAdd.setEnabled(connected);
		miAnimalRefresh.setEnabled(connected);
		miDatabaseCreate.setEnabled(connected);
	}


	@Override
	public void actionPerformed(ActionEvent event) {


		// informacni dialog
		if(event.getSource() == miApplicationInfo){
			JOptionPane.showMessageDialog(frame, "Autoři aplikace: dlouhý, široký a bystrozraký", "O aplikaci", JOptionPane.INFORMATION_MESSAGE);
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
					Log.error("Chyba pri vytvareni DB: " + e.getMessage());
				}
			}

			frame.refreshAnimalsList();
		}

		// naplneni DB vzorovymi daty
		if(event.getSource() == miDatabaseSample){
			if(db.isConnected()){
				try {
					Log.debug("Vytvarim databazi se vzorovymi daty");
					db.fillDatabase();
				} catch(SQLException e){
					Log.error("Chyba SQL pri vytvareni databaze se vzorovymi daty: " + e.getMessage());
				} catch(IOException e){
					Log.error("Chyba cteni souboru pri vytvareni databaze se vzorovymi daty: " + e.getMessage());
				}
			}

			frame.refreshAnimalsList();
		}

		// obrazovka s nastavenim
		if(event.getSource() == miApplicationPreferences){
			PreferencesDialog dPreferences = new PreferencesDialog();
			GUIManager.moveToCenter(dPreferences, frame);
			dPreferences.setVisible(true);
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