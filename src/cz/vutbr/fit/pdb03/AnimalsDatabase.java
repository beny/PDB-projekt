package cz.vutbr.fit.pdb03;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.OsmTileSource;

import cz.vutbr.fit.pdb03.controllers.MenuController;
import cz.vutbr.fit.pdb03.controllers.MouseController;
import cz.vutbr.fit.pdb03.controllers.WindowController;
import cz.vutbr.fit.pdb03.dialogs.ConnectDialog;
import cz.vutbr.fit.pdb03.map.JMapPane;

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
	JComponent rightPanel, leftTopPanel, leftBottomPanel;

	// map items
	JMapPane map;

	// database items
	private DataBase db;

	// dialogs
	private ConnectDialog connectDialog;

	/**
	 * Zakladni konstruktor, ktery naplni hlavni okno
	 * @param title titulek hlavniho okna
	 */
	public AnimalsDatabase(String title) {
		super(title);

		// databaze
		db = new DataBase();

		// mapa
		map = new JMapPane(this);

		// nastaveni kontroleru
		new MouseController(this);
		new WindowController(this);
		new MenuController(this);

		// pridani rozdeleni do jednotlivych podoken
		// TODO dodelat nejak poradne vahy pri resize oknu a pri prvnim spusteni
		JSplitPane splitPaneH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getTopLeftPanel() , getTopRightPanel());
		splitPaneH.setResizeWeight(0.5);
		splitPaneH.setDividerLocation(600);
		splitPaneH.setBorder(null);

		JSplitPane splitPaneV= new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPaneH, map);
		splitPaneV.setResizeWeight(0.3);
		splitPaneV.setDividerLocation(200);
		add(splitPaneV);

		// dialog pro pripojeni
		connectDialog = new ConnectDialog(this, db);
		connectDialog.fillDialog(ConnectDialog.ONDRA);
		connectDialog.setVisible(true);
	}


	/**
	 * Metoda pro ziskani panelu pro levou horni cast okna
	 * @return komponenta vyplnena JTabbedPane pro obrazky
	 */
	public JComponent getTopLeftPanel() {

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
	public JComponent getTopRightPanel() {

		Vector<String> properties = new Vector<String>();
		properties.add("Prase domácí");
		properties.add("Tygr usurijský");
		properties.add("Kachna blátotlačka");
		properties.add("Slon indický");
		properties.add("Lev mandžuský");
		properties.add("Kráva domácí");
		JList list = new JList(properties);

		JScrollPane scroll = new JScrollPane(list);
		scroll.setPreferredSize(new Dimension(300, 500));

		return scroll;
	}

	public JMapPane getMap() {
		return map;
	}

	public void setMap(JMapPane map) {
		this.map = map;
	}

	public DataBase getDb() {
		return db;
	}

	public void setDb(DataBase db) {
		this.db = db;
	}

	public ConnectDialog getConnectDialog() {
		return connectDialog;
	}

	public void setConnectDialog(ConnectDialog connectDialog) {
		this.connectDialog = connectDialog;
	}

	/**
	 * Hlavni main pro spusteni aplikace
	 * @param args argumenty z prikazove radky
	 */
	public static void main(String[] args) {
		AnimalsDatabase aDb = new AnimalsDatabase("Animals database");
		aDb.setVisible(true);
	}

}
