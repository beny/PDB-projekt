package cz.vutbr.fit.pdb03.gui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.geom.Point2D;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import cz.vutbr.fit.pdb03.Animal;
import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.DataBase;
import cz.vutbr.fit.pdb03.Log;

/**
 * Trida zajistujici panel s fotkama a info
 * @author Ondřej Beneš <xbenes00@stud.fit.vutbr.cz>
 *
 */
public class PhotosPanel extends JTabbedPane {

	private static final long serialVersionUID = 8533602586891866222L;

	AnimalsDatabase frame;
	private JPanel pInfoTab;
	private JScrollPane sInfoTab;

	// popisek jednotlivych info labelu
	private final static String description = "Popis: ";
	private final static String distance = "Vzdálenost k nejbližšímu zvířeti: ";
	private final static String area = "Tento druh obývá plochu: ";

	private JLabel lDescription, lDistance, lArea;
	private JLabel lName, lNameLat, lDistance2, lArea2;
	private JTextArea lDescription2;
	private JPanel pAnimal, pFootprints, pFeces;

	public PhotosPanel(AnimalsDatabase frame) {
		this.frame = frame;

		// inicializace
		initInfoTab();

		// inicializace tabu
		pAnimal = new JPanel();
		pFootprints = new JPanel();
		pFeces = new JPanel();

		pAnimal.setLayout(new BoxLayout(pAnimal, BoxLayout.PAGE_AXIS));
		pFootprints.setLayout(new BoxLayout(pFootprints, BoxLayout.PAGE_AXIS));
		pFeces.setLayout(new BoxLayout(pFeces, BoxLayout.PAGE_AXIS));

		addTab("Fotky zvířete", new JScrollPane(pAnimal));
		addTab("Fotky stop zvířete", new JScrollPane(pFootprints));
		addTab("Fotky trusu zvířete", new JScrollPane(pFeces));
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

		Font fTitle = new Font(Font.SANS_SERIF, Font.BOLD, 15);
		Font fSubTitle = new Font(Font.SANS_SERIF, Font.BOLD, 12);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		lName = new JLabel();
		lName.setFont(fTitle);
		pInfoTab.add(lName, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		lNameLat = new JLabel();
		lNameLat.setFont(fSubTitle);
		pInfoTab.add(lNameLat, gbc);

		// labely
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.gridx = 0;
		gbc.gridy = 2;

		gbc.gridwidth = 1;
		gbc.gridy++;
		lDistance = new JLabel("");
		lDistance.setFont(fSubTitle);
		pInfoTab.add(lDistance, gbc);

		gbc.gridx++;
		lDistance2 = new JLabel();
		pInfoTab.add(lDistance2, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		lArea = new JLabel("");
		lArea.setFont(fSubTitle);
		pInfoTab.add(lArea, gbc);

		gbc.gridx++;
		lArea2 = new JLabel();
		pInfoTab.add(lArea2, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		lDescription = new JLabel("");
		lDescription.setFont(fSubTitle);
		pInfoTab.add(lDescription, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		lDescription2 = new JTextArea();
		lDescription2.setWrapStyleWord(true);
		lDescription2.setLineWrap(true);
		lDescription2.setEditable(false);
		lDescription2.setColumns(40);
		lDescription2.setOpaque(false);
		pInfoTab.add(lDescription2, gbc);

		// scroll
		sInfoTab = new JScrollPane(pInfoTab);

		// pridani tabu
		addTab("Informace", sInfoTab);
	}

	/**
	 * Nastavi data o zvireti do vsech tabu
	 * @param animal
	 */
	public void setInfo(Animal animal) {

		JEntity m = frame.getMap().getMyPosition();
		Point2D p = new Point2D.Double(m.getLat(), m.getLon());
                lDescription.setText(description);
                lArea.setText(area);
                lDistance.setText(distance);
		lName.setText(((animal.getGenus() == null) ? "" : animal.getGenus())
				+ " "
				+ ((animal.getSpecies() == null) ? "" : animal.getSpecies()));
		lNameLat.setText(((animal.getGenusLat() == null) ? "" : animal
				.getGenusLat())
				+ " "
				+ ((animal.getSpeciesLat() == null) ? "" : animal
						.getSpeciesLat()));

		try {

			String desc = animal.getDescription(frame.getDb());

			if (desc != null && desc.length() > 0) {
				lDescription2.setText(desc);
			}

			int distance = (int) Math.round(animal.getNearestAppareance(frame.getDb(), p));
			int area = (int) Math.round(animal.getAppareanceArea(frame.getDb()));

			lDistance2.setText(((distance == -1)?" - ":distance + " km"));
			lArea2.setText(((area == -1)?" - ":"<html>"+ area + " km<small><sup>2</sup></small></html>"));
		} catch (SQLException ex) {
			Log.error("Chyba pri ziskavani udaju o zvireti z DB");
		}
	}

	/**
	 * Nastaveni fotek do tabu
	 * @param tab
	 * @param data
	 */
	public void setPhotos(String tab, List<JPicture> data){

		JPanel tmp = null;

		if(tab.equals(DataBase.ANIMAL_PHOTO)) tmp = pAnimal;
		if(tab.equals(DataBase.FEET_PHOTO)) tmp = pFootprints;
		if(tab.equals(DataBase.EXCREMENT_PHOTO)) tmp = pFeces;

		if(tmp != null){
			tmp.removeAll();
			for (JPicture pic : data) {
				tmp.add(new ImageRecord(pic, frame));
			}
		}
	}

	/**
	 * Vycisteni info panelu
	 */
	public void clear() {
		lName.setText("");
		lNameLat.setText("");
		lDescription2.setText("");
		lDistance2.setText("");
		lArea2.setText("");
                lDescription.setText("");
                lArea.setText("");
                lDistance.setText("");

		setPhotos(DataBase.ANIMAL_PHOTO, new LinkedList<JPicture>());
		setPhotos(DataBase.EXCREMENT_PHOTO, new LinkedList<JPicture>());
		setPhotos(DataBase.FEET_PHOTO, new LinkedList<JPicture>());
	}
}
