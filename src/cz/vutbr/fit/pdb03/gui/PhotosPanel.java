package cz.vutbr.fit.pdb03.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.sql.SQLException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

import cz.vutbr.fit.pdb03.Animal;
import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.Log;

/**
 * Trida zajistujici panel s fotkama a info
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
public class PhotosPanel extends JTabbedPane {

	private static final long serialVersionUID = 8533602586891866222L;

	AnimalsDatabase frame;
	private JPanel pInfoTab;
	private JScrollPane sInfoTab;

	// popisek jednotlivych info labelu
	private final static String genus = "Jméno: ";
	private final static String genusLat = "Jméno latinsky: ";
	private final static String species = "Druhové jméno: ";
	private final static String speciesLat = "Druhové jméno latinsky: ";
	private final static String description = "Popis: ";
	private final static String distance = "Vzdálenost k nejbližšímu zvířeti: ";
	private final static String area = "Plocha obývající zvířetem: ";

	private JLabel lGenus, lGenusLat, lSpecies, lSpeciesLat, lDescription,
			lDistance, lArea;
	private JLabel lGenus2, lGenusLat2, lSpecies2, lSpeciesLat2, lDistance2,
			lArea2;
	private JTextArea lDescription2;

	public PhotosPanel(AnimalsDatabase frame) {
		this.frame = frame;

		// inicializace
		initInfoTab();

		addTab("Fotky", new JLabel("Tady budou fotky", JLabel.LEFT));
		addTab("Stopy", new JLabel("Tady budou stopy", JLabel.CENTER));
		addTab("Trus", new JLabel("Tady bude velky hovno", JLabel.CENTER));


	}

	/**
	 * Inicializace tabu s info
	 */
	private void initInfoTab(){

		pInfoTab = new JPanel();

		// layout
		pInfoTab.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.FIRST_LINE_END;

		// labely
		gbc.gridx = 0;
		gbc.gridy++;
		lGenus = new JLabel(genus);
		pInfoTab.add(lGenus, gbc);

		gbc.gridy++;
		lGenusLat = new JLabel(genusLat);
		pInfoTab.add(lGenusLat, gbc);

		gbc.gridy++;
		lSpecies = new JLabel(species);
		pInfoTab.add(lSpecies, gbc);

		gbc.gridy++;
		lSpeciesLat = new JLabel(speciesLat);
		pInfoTab.add(lSpeciesLat, gbc);

		gbc.gridy++;
		lDescription = new JLabel(description);
		pInfoTab.add(lDescription, gbc);

		gbc.gridy++;
		lDistance = new JLabel(distance);
		pInfoTab.add(lDistance, gbc);

		gbc.gridy++;
		lArea = new JLabel(area);
		pInfoTab.add(lArea, gbc);

		// datove slozky
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.gridx = 1;
		gbc.gridy = 0;
		lGenus2 = new JLabel();
		pInfoTab.add(lGenus2, gbc);

		gbc.gridy++;
		lGenusLat2 = new JLabel();
		pInfoTab.add(lGenusLat2, gbc);

		gbc.gridy++;
		lSpecies2 = new JLabel();
		pInfoTab.add(lSpecies2, gbc);

		gbc.gridy++;
		lSpeciesLat2 = new JLabel();
		pInfoTab.add(lSpeciesLat2, gbc);

		gbc.gridy++;
		lDescription2 = new JTextArea();
		lDescription2.setWrapStyleWord(true);
		lDescription2.setLineWrap(true);
		lDescription2.setEditable(false);
		lDescription2.setColumns(20);
		lDescription2.setOpaque(false);
		pInfoTab.add(lDescription2, gbc);

		gbc.gridy++;
		lDistance2 = new JLabel();
		pInfoTab.add(lDistance2, gbc);

		gbc.gridy++;
		lArea2 = new JLabel();
		pInfoTab.add(lArea2, gbc);

		// scroll
		sInfoTab = new JScrollPane(pInfoTab);

		// pridani tabu
		addTab("Info", sInfoTab);
	}

	/**
	 * Nastavi data o zvireti do vsech tabu
	 * @param animal
	 */
	public void setAnimalData(Animal animal) {

		MapMarker m = frame.getMap().getMyPosition();
		Point2D p = new Point();
		p.setLocation(m.getLat(), m.getLon());
		lGenus2.setText(animal.getGenus());
		lGenusLat2.setText(animal.getGenusLat());
		lSpecies2.setText(animal.getSpecies());
		lSpeciesLat2.setText(animal.getSpeciesLat());

		try {
			lDescription2.setText(animal.getDescription(frame.getDb()));
			lDistance2.setText(animal.getNearestAppareance(frame.getDb(), p)
					+ " km");
			lArea2.setText(animal.getAppareanceArea(frame.getDb()) + " km2");
		} catch (SQLException ex) {
			Log.error("Chyba pri ziskavani udaju o zvireti z DB");
		}
	}

	/**
	 * Vycisteni formulare
	 */
	public void clear() {
		lGenus2.setText("");
		lGenusLat2.setText("");
		lSpecies2.setText("");
		lSpeciesLat2.setText("");
		lDescription2.setText("");
		lDistance2.setText("");
		lArea2.setText("");

	}
}
