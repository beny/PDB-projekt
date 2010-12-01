package cz.vutbr.fit.pdb03;

import java.awt.Dimension;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import cz.vutbr.fit.pdb03.controllers.ListController;
import cz.vutbr.fit.pdb03.controllers.MenuController;
import cz.vutbr.fit.pdb03.controllers.MouseController;
import cz.vutbr.fit.pdb03.controllers.WindowController;
import cz.vutbr.fit.pdb03.map.JMapPanel;

/**
 * Hlavni trida zajistujici vykreselni hlavniho okna, rozdeleneho do tri
 * panelu.
 *
 * @author Ondřej Beneš <xbenes00@stud.fit.vutbr.cz>
 *
 */
public class AnimalsDatabase extends JFrame{

	private static final long serialVersionUID = 1L;

	// komponenty jednotlivych casti hlavniho okna
	JComponent infoPanel, animalsPanel, mapPanel;

	// map items
	JMapPanel map;

	// database items
	private DataBase db;

	// controllers
	MenuController menuController;

	// seznam zvirat
	private JList list;
	private Vector<Animal> animals;
	private ArrayList<Animal> dbList;

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
		infoPanel = initInfoPanel();
		animalsPanel = initAnimalsPanel();
		mapPanel = map;

		// TODO dodelat nejak poradne vahy pri resize oknu a pri prvnim spusteni
		JSplitPane splitPaneH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, infoPanel, animalsPanel);
		splitPaneH.setResizeWeight(0.5);
		splitPaneH.setDividerLocation(600);
		splitPaneH.setBorder(null);

		JSplitPane splitPaneV= new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPaneH, mapPanel);
		splitPaneV.setResizeWeight(0.3);
		splitPaneV.setDividerLocation(200);
		add(splitPaneV);

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
		try{
			db.searchAnimals();
			dbList = (ArrayList<Animal>) db.searchResult;
		} catch(SQLException e){
			D.log("Chyba pri hledani zvirat: " + e.getMessage() , 1); // TODO odstranit magickou konstatntu
		}

		D.log("Nalezeno " + dbList.size() + " zvirat");

		animals.clear();
		for (Animal animal: dbList) {
			animals.add(animal);
		}

		list.setListData(animals);
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

	public JList getList() {
		return list;
	}

	public MenuController getMenuController() {
		return menuController;
	}

	public JComponent getInfoPanel() {
		return infoPanel;
	}

	public JComponent getAnimalsPanel() {
		return animalsPanel;
	}

	/**
	 * Metoda pro ziskani panelu pro levou horni cast okna
	 * @return komponenta vyplnena JTabbedPane pro obrazky
	 */
	private JComponent initInfoPanel() {

		// picture panel
		JTabbedPane picturesPanel = new JTabbedPane();
		picturesPanel.addTab("Fotky", new JLabel("Red panel", JLabel.CENTER));
		picturesPanel.addTab("Stopy", new JLabel("Blue panel", JLabel.CENTER));
		picturesPanel.addTab("Trus", new JLabel("Green panel", JLabel.CENTER));

		return new JScrollPane(picturesPanel);

	}

	/**
	 * Metoda pro ziskani panelu pro pravou horni cast okna
	 * @return komponenta se seznamem zvirat v databazi
	 */
	private JComponent initAnimalsPanel() {

		animals = new Vector<Animal>();
		list = new JList();
		list.addMouseListener(new ListController(this));
		JScrollPane scroll = new JScrollPane(list);
		scroll.setPreferredSize(new Dimension(300, 500));

		return scroll;
	}

}
