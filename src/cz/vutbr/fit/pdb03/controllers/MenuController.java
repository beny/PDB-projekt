package cz.vutbr.fit.pdb03.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import cz.vutbr.fit.pdb03.Animal;
import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.D;
import cz.vutbr.fit.pdb03.DataBase;
import cz.vutbr.fit.pdb03.map.JMapPane;

public class MenuController implements ActionListener{

	// menu items
	private JMenuBar menuBar;
	private JMenu menuMap, menuDatabase, menuAbout, menuAnimal;
	private JMenuItem menuAboutInfo, menuDatabaseDisconnect, menuDatabaseCreate, menuAnimalSample;
	private JCheckBoxMenuItem menuMapShowMarkers;

	// databaze
	DataBase db;

	// mapa
	JMapPane map;

	// hlavni okno
	AnimalsDatabase frame;

	public MenuController(AnimalsDatabase frame) {

		// init promenych
		db = frame.getDb();
		map = frame.getMap();
		this.frame = frame;

		// hlavniho menu
		menuBar = new JMenuBar();

		// menu databaze
		menuDatabase = new JMenu("Databáze");
		menuBar.add(menuDatabase);

		menuDatabaseDisconnect = new JMenuItem("Odpojit od databáze");
		menuDatabaseDisconnect.addActionListener(this);
		menuDatabase.add(menuDatabaseDisconnect);

		menuDatabaseCreate = new JMenuItem("Vytvořit databázi");
		menuDatabaseCreate.addActionListener(this);
		menuDatabase.add(menuDatabaseCreate);

		// menu mapa
		menuMap = new JMenu("Mapa");
		menuMap.addActionListener(this);
		menuBar.add(menuMap);

		menuMapShowMarkers = new JCheckBoxMenuItem("Zobraz body", true);
		menuMapShowMarkers.addActionListener(this);
		menuMap.add(menuMapShowMarkers);

		// menu zvire
		menuAnimal = new JMenu("Zvíře");
		menuBar.add(menuAnimal);

		// FIXME menu polozka pro pridani testovaciho zvirete
		menuAnimalSample = new JMenuItem("Vlož testovací zvíře");
		menuAnimalSample.addActionListener(this);
		menuAnimal.add(menuAnimalSample);

		// menu about
		menuAbout = new JMenu("About");
		menuBar.add(menuAbout);

		menuAboutInfo = new JMenuItem("O aplikaci");
		menuAboutInfo.addActionListener(this);
		menuAbout.add(menuAboutInfo);

		frame.setJMenuBar(menuBar);
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		// zobrazeni/skryti markeru na mape
		if(event.getSource() == menuMapShowMarkers){
			map.setMapMarkerVisible(!map.getMapMarkersVisible());
			menuMapShowMarkers.setState(map.getMapMarkersVisible());
		}

		// informacni dialog
		if(event.getSource() == menuAboutInfo){
			// TODO
		}

		// odpojeni od databaze
		if(event.getSource() == menuDatabaseDisconnect){
			try {
				D.log("Disconnected");
				db.disconnect();
				frame.getConnectDialog().setVisible(true);
			} catch (SQLException e){
				System.err.println("Error while disconnection from DB: " + e.getMessage());
			}
		}

		// vytvoreni tabulek v DB
		if(event.getSource() == menuDatabaseCreate){
			if(db.isConnected()){
				try{
					D.log("Creating empty database");
					db.createDatabase();
				} catch (SQLException e){
					System.err.println("Chyba pri vytvareni DB: " + e.getMessage());
				}
			}
		}

		// vytvoreni testovaciho zvirete
		if(event.getSource() == menuAnimalSample){

			// testovaci zvire
			Animal testAnimal = new Animal();
			testAnimal.setGenus("test animal");
			testAnimal.setFamily("test family");

			try{
				D.log("Vkladani zvirete do DB");
				// TODO kontrola zda uz zvire v DB neni
				db.searchAnimals(testAnimal.getGenus(), testAnimal.getFamily());

				// TODO pokud neni tak jej uloz
			} catch(SQLException e){
				D.log("Chyba pri vytvareni testovaciho zvirete", 1); // FIXME odstranit magickou konstantu
			}
		}

	}
}
