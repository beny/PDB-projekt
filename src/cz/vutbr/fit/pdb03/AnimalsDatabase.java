package cz.vutbr.fit.pdb03;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JSplitPane;

import cz.vutbr.fit.pdb03.controllers.MenuController;
import cz.vutbr.fit.pdb03.controllers.MouseController;
import cz.vutbr.fit.pdb03.controllers.WindowController;
import cz.vutbr.fit.pdb03.dialogs.ConnectDialog;
import cz.vutbr.fit.pdb03.gui.AnimalsPanel;
import cz.vutbr.fit.pdb03.gui.GUIManager;
import cz.vutbr.fit.pdb03.gui.PhotosPanel;
import cz.vutbr.fit.pdb03.map.JMapPanel;

/**
 * Hlavni trida zajistujici vykreselni hlavniho okna, rozdeleneho do tri
 * panelu.
 *
 * @author Ondřej Beneš <xbenes00@stud.fit.vutbr.cz>
 *
 */
public class AnimalsDatabase extends JFrame  {

	private static final long serialVersionUID = 1L;

	// komponenty jednotlivych casti hlavniho okna
	PhotosPanel photosPanel;
	JMapPanel mapPanel;
	AnimalsPanel animalsPanel;

	// map items
	JMapPanel map;

	// database items
	private DataBase db;

	// controllers
	MenuController menuController;

	// seznam zvirat
	private JList list;
	private Vector<Animal> vAnimals = new Vector<Animal>();

	/**
	 * Zakladni konstruktor, ktery naplni hlavni okno
	 * @param title titulek hlavniho okna
	 */
	public AnimalsDatabase(String title) {
		super(title);

		// databaze
		db = new DataBase();

		// mapa
		map = new JMapPanel(this);

		// nastaveni kontroleru
		new MouseController(this);
		new WindowController(this);
		menuController = new MenuController(this);

		// pridani rozdeleni do jednotlivych podoken
		photosPanel = new PhotosPanel(this);
		animalsPanel = new AnimalsPanel(this);
		mapPanel = map;

		// soupaci panely
		JSplitPane splitPaneH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, photosPanel, animalsPanel);
		splitPaneH.setResizeWeight(0.5);
		splitPaneH.setDividerLocation(600);
		splitPaneH.setBorder(null);

		JSplitPane splitPaneV = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPaneH, mapPanel);
		splitPaneV.setResizeWeight(0.3);
		splitPaneV.setDividerLocation(200);
		add(splitPaneV);

		// v zakladu zakaz vse co pouziva DB
		setEnable(false);

		// pokud neni pripojeni k DB, zobraz dialog
		if(!db.isConnected()){
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            @Override
				public void run() {
	            	ConnectDialog dConnect = new ConnectDialog(AnimalsDatabase.this, db);
					GUIManager.moveToCenter(dConnect, AnimalsDatabase.this);
					dConnect.setVisible(true);
	            }
			});
		}

	}

	/**
	 * Hlavni main pro spusteni aplikace
	 * @param args argumenty z prikazove radky
	 */
	public static void main(String[] args) {
		AnimalsDatabase aDb = new AnimalsDatabase("Animals database");
		aDb.setVisible(true);
	}

	/**
	 * Obnoveni seznamu zvirat
	 */
	public void refreshAnimalsList(){

		ArrayList<Animal> dbAnimals = new ArrayList<Animal>();

		// nalezeni zvirat
		try{
			db.searchAnimals();
			dbAnimals = (ArrayList<Animal>) db.searchResult;
		} catch(SQLException e){
			Log.error("Chyba pri hledani zvirat: " + e.getMessage());
		}

		Log.info("Nalezeno "+ dbAnimals + " zvirat");

		// nastaveni novych zvirat
		vAnimals.clear();
		for (Animal animal: dbAnimals) {
			vAnimals.add(animal);
		}

		animalsPanel.setData(vAnimals);
	}

	/**
	 * Nastavovani zda jsou prvky k dispozici dle pripojeni
	 * @param enable
	 */
	public void setEnable(boolean enable){

		// disable menu items
		menuController.setConnectionState(db.isConnected());
		animalsPanel.setEnabled(enable);
		photosPanel.setEnabled(enable);
	}

	/**
	 * Ziskani instance mapy
	 * @return reference na objekt mapy
	 */
	public JMapPanel getMap() {
		return map;
	}

	/**
	 * Ziskani instance databaze
	 * @return reference na objekt databaze
	 */
	public DataBase getDb() {
		return db;
	}

	public void setList(JList list) {
		this.list = list;
	}

	public JList getList() {
		return list;
	}

	public MenuController getMenuController() {
		return menuController;
	}

	public PhotosPanel getPhotosPanel() {
		return photosPanel;
	}

	public AnimalsPanel getAnimalsPanel() {
		return animalsPanel;
	}
}
