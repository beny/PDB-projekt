package cz.vutbr.fit.pdb03;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JSplitPane;

import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

import cz.vutbr.fit.pdb03.controllers.MenuController;
import cz.vutbr.fit.pdb03.controllers.WindowController;
import cz.vutbr.fit.pdb03.dialogs.ConnectDialog;
import cz.vutbr.fit.pdb03.dialogs.LoadingDialog;
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

	// search mody
	public final static int SEARCH_ALL = 0;
	public final static int SEARCH_BY_NAME = 1;
	public final static int SEARCH_BY_DESCRIPTION = 2;
	public final static int SEARCH_BY_PICTURE = 3;
	public final static int SEARCH_BY_PICTURE_DESCRIPTION = 4;
	public final static int SEARCH_CLOSE = 5;
	public final static int SEARCH_BIGGEST_AREA = 6;
	public final static int SEARCH_EXTINCT = 7;
	public final static int SEARCH_SAME_AREA = 8;

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

	// pomocne promenne pro hledani
	private String searchGenus, searchSpecies, searchDescription;

	private LoadingDialog dLoading;

	// druh hledani

	/**
	 * Zakladni konstruktor, ktery naplni hlavni okno
	 * @param title titulek hlavniho okna
	 */
	public AnimalsDatabase(String title) {
		super(title);

		// menu pro Mac OSX
		System.setProperty("apple.laf.useScreenMenuBar", "true");

		// ikona
		setIconImage(new ImageIcon(AnimalsDatabase.class.getResource("images/icon.png")).getImage());

		// databaze
		db = new DataBase();

		// mapa
		map = new JMapPanel(this);

		// nastaveni kontroleru
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
	public void reloadAnimalsList(final int searchType){

		new Thread(new Runnable() {

			@Override
			public void run() {

				ArrayList<Animal> dbAnimals = new ArrayList<Animal>();

				map.clear();
				photosPanel.clear();
				// nalezeni zvirat
				try{

					switch (searchType) {
					case SEARCH_ALL:
						db.searchAnimals();
						break;
					case SEARCH_BY_NAME:
						db.searchAnimals(getSearchGenus(), getSearchSpecies());
						break;
					case SEARCH_BY_DESCRIPTION:
						db.searchAnimals(getSearchDescription());
						break;
					case SEARCH_BY_PICTURE:
						break; // TODO
					case SEARCH_BY_PICTURE_DESCRIPTION:
						db.searchAnimalsByPicture(getSearchDescription());
						break;
					case SEARCH_BIGGEST_AREA:
						db.searchAnimalsByAreaSize();
						break;
					case SEARCH_SAME_AREA:
						int id = getAnimalsPanel().getSelectedAnimal().getId();
						Log.debug("Hledam zvirata ktere jsou na stejnem uzemi jako "
								+ getAnimalsPanel().getSelectedAnimal().getGenus());
						db.searchAnimalsOnArea(id);
						break;
					case SEARCH_CLOSE:
						MapMarker marker = map.getMyPosition();
						Point2D temp = new Point();
						temp.setLocation(marker.getLat(), marker.getLon());
						db.searchNearestAnimals(temp);
						break;
					case SEARCH_EXTINCT:
						db.searchExtinctAnimals();
						break;
					default:
						break;
					}

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

				dLoading.dispose();
			}
		}).start();

		dLoading = new LoadingDialog("Probíhá hledání v DB, prosím vyčkejte");
		GUIManager.moveToCenter(dLoading, this);
		dLoading.setVisible(true);

	}

	/**
	 * Ukonceni aplikace
	 */
	public void exitApp(){
		if (db.isConnected()) {
			try {
				db.disconnect();

				Log.info("Disconnected");
			} catch (SQLException e) {
				System.err
						.println("Error while disconnection from DB: " + e.getMessage());
			}
		}

		setVisible(false);
		dispose();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();

		exitApp();
	}

	/**
	 * Pridani zvirete do DB
	 * @param animal
	 */
	public void addAnimal(Animal animal){

		try {
			db.insertAnimal(animal);
		} catch (SQLException e) {
			Log.error("Chyba pri vkladani zvirete do DB: " + e.getMessage());
		}

		reloadAnimalsList(AnimalsDatabase.SEARCH_ALL);
	}


	/**
	 * Metoda upravujici zvire v DB
	 * @param old
	 */
	public void editAnimal(Animal animal){

		int id = animal.getId();

		// podiva se zda v DB existuje
		try{
			db.searchAnimals(id);
		} catch (SQLException e) {
			System.err.println("Chyba hledani zvirete podle ID v DB: " + e.getMessage());
		}

		if (id != 0) {
			Log.debug("Id zvirete ktere chci ulozit:" + id);

			try {
				db.updateAnimal(animal);
			} catch (SQLException e) {
				System.err.println("Chyba hledani zvirete podle ID v DB: " + e.getMessage());
			}
		}

		reloadAnimalsList(AnimalsDatabase.SEARCH_ALL);
	}

	/**
	 * Odstraneni zvirete z DB
	 * @param animal
	 */
	public void deleteAnimal(Animal animal){
		try{
			db.deleteAnimal(animal.getId());
			Log.debug("Mazu zvire s ID " + animal.getId());
		} catch (SQLException e) {
			System.err.println("Chyba pri mazani zvirete z DB" + e.getMessage());
		}

		reloadAnimalsList(AnimalsDatabase.SEARCH_ALL);
	}

	/**
	 * Nastavovani zda jsou prvky k dispozici dle pripojeni
	 * @param enable
	 */
	public void setEnable(boolean enable){

		// disable/enable menu items
		menuController.setConnected(db.isConnected());
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

	public String getSearchGenus() {
		return searchGenus;
	}

	public void setSearchGenus(String searchGenus) {
		this.searchGenus = searchGenus;
	}

	public String getSearchSpecies() {
		return searchSpecies;
	}

	public void setSearchSpecies(String searchSpecies) {
		this.searchSpecies = searchSpecies;
	}

	public String getSearchDescription() {
		return searchDescription;
	}

	public void setSearchDescription(String searchDescription) {
		this.searchDescription = searchDescription;
	}
}
