package cz.vutbr.fit.pdb03;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.HashMap;
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

import cz.vutbr.fit.pdb03.map.MapMouseListener;

/**
 * Hlavni trida zajistujici vykreselni hlavniho okna, rozdeleneho do tri
 * panelu.
 *
 * @author Ondřej Beneš <xbenes00@stud.fit.vutbr.cz>
 *
 */
public class AnimalsDatabase extends JFrame implements ActionListener{

	private static final long serialVersionUID = 1L;

	// komponenty jednotlivych casti hlavniho okna
	JComponent rightPanel, leftTopPanel, leftBottomPanel;

	// map items
	JMapViewer map;

	// application preferences
	Preferences prefs = Preferences.userNodeForPackage(this.getClass());

	// menu items
	private JMenuBar menuBar;
	private JMenu menuMap;
	private JMenuItem menuAbout;
	private JCheckBoxMenuItem menuMapShowMarkers;

	private JMenu menuDatabase;

	private JMenuItem menuDatabaseDisconnect;

	private DataBase db;

	private ConnectDialog conDialog;

	/**
	 * Zakladni konstruktor, ktery naplni hlavni okno
	 * @param title titulek hlavniho okna
	 */
	public AnimalsDatabase(String title) {
		super(title);

		// nastaveni pri zavirani okna
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {

				try {
					db.disconnect();

					// DEBUG
					System.out.println("Disconnected !!");
				} catch (SQLException e) {
					System.err.println("Error while disconnection from DB: " + e.getMessage());
				}

				setVisible(false);
				dispose();
			}
		});

		// nastaveni velikosti okna
		setExtendedState(MAXIMIZED_BOTH);
		setMinimumSize(new Dimension(500, 500));

		// pridani rozdeleni do jednotlivych podoken
		// TODO dodelat nejak poradne vahy pri resize oknu a pri prvnim spusteni
		JSplitPane splitHorizontal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getTopLeftPanel() , getTopRightPanel());
		splitHorizontal.setResizeWeight(0.5);
		splitHorizontal.setBorder(null);
		JSplitPane splitVertical= new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitHorizontal, getBottomPanel());
		add(splitVertical);

		//pridani hlavniho menu

		menuBar = new JMenuBar();

		menuDatabase = new JMenu("Databáze");
		menuBar.add(menuDatabase);

		menuDatabaseDisconnect = new JMenuItem("Odpojit od DB");
		menuDatabaseDisconnect.addActionListener(this);
		menuDatabase.add(menuDatabaseDisconnect);

		menuMap = new JMenu("Mapa");
		menuMap.addActionListener(this);
		menuBar.add(menuMap);

		menuMapShowMarkers = new JCheckBoxMenuItem("Zobraz body", true);
		menuMapShowMarkers.addActionListener(this);
		menuMap.add(menuMapShowMarkers);

		menuAbout = new JMenuItem("O aplikaci");
		menuAbout.addActionListener(this);
		menuBar.add(menuAbout);

		setJMenuBar(menuBar);

		// databaze a dialog pro pripojeni

		db = new DataBase();
		conDialog = new ConnectDialog(this, db);
		conDialog.setVisible(true);

	}

	/**
	 * Metoda pro ziskani panelu pro spodni cast okna
	 * @return komponenta vyplnena mapou
	 */
	public JComponent getBottomPanel() {

		// map panel
		map = new JMapViewer();
		map.setPreferredSize(null);
		map.setTileSource(new OsmTileSource.CycleMap());
		map.setTileLoader(new OsmTileLoader(map));

//		map.addMapRectangle(new MapRectangle(49.81, 8.6, 49.82, 8.2));
//		map.addMapMarker(new MapMarkerPoint(49.123, 8.456, 1));
//		map.addMapMarker(new MapMarkerPoint(49.321, 7.456, 2));
//		map.addMapMarker(new MapMarkerPoint(49.456, 7.123, 3));


		map.addMouseListener(new MapMouseListener(map));

		return map;
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

	/**
	 * Hlavni main pro spusteni aplikace
	 * @param args argumenty z prikazove radky
	 */
	public static void main(String[] args) {
		AnimalsDatabase aDb = new AnimalsDatabase("Animals database");
		aDb.setVisible(true);
	}


	@Override
	public void actionPerformed(ActionEvent event) {

		// zobrazeni/skryti markeru na mape
		if(event.getSource() == menuMapShowMarkers){
			map.setMapMarkerVisible(!map.getMapMarkersVisible());
			menuMapShowMarkers.setState(map.getMapMarkersVisible());
		}

		// informacni dialog
		if(event.getSource() == menuAbout){
			JOptionPane.showMessageDialog(this, "Projekt do předmětu Pokročilé databáze");
		}

		// odpojeni od databaze
		if(event.getSource() == menuDatabaseDisconnect){
			try {
				db.disconnect();

				// DEBUG
				System.out.println("Disconnected !!!");

				conDialog.setVisible(true);
			} catch (SQLException e){
				// DEBUG
				System.err.println("Error while disconnection from DB: " + e.getMessage());
			}
		}

	}


}
