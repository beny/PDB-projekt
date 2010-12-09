package cz.vutbr.fit.pdb03.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import cz.vutbr.fit.pdb03.Animal;
import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.DataBase;
import cz.vutbr.fit.pdb03.Log;

/**
 * Dialog, pro pridavani a editaci zvirete
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
public class AnimalDialog extends DefaultDialog implements ActionListener{

	private static final long serialVersionUID = -3290129275558417391L;

	public static final int INSERT = 0;
	public static final int UPDATE = 1;

	private static final String EDIT = "Uprav";
	private static final String SAVE = "Ulož";
	// hlavni panel
	JPanel pContent;

	// GUI elementy
	JLabel lId, lGenus, lSpecies, lGenusLat, lSpeciesLat, lDescription;
	JTextField tGenus, tSpecies, tGenusLat, tSpeciesLat;
	JTextArea taDescription;
	JButton bCancel, bSave, bDelete;

	private int mode;

	DataBase db;

	AnimalsDatabase frame;

	public AnimalDialog(AnimalsDatabase frame) {
		super();

		this.frame = frame;
		db = frame.getDb();

		// hlavni panel
		pContent = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		// init constraints
		gbc.gridx = gbc.gridy = 0;
		gbc.insets = new Insets(5, 5, 5, 5);

		// pridani labelu
		// TODO vyresit predavani ID lepe
		lId = new JLabel();
		lId.setVisible(false);
		lId.setText("0");
		pContent.add(lId, gbc);

		lGenus = new JLabel("Genus:");
		pContent.add(lGenus, gbc);

		gbc.gridy++;
		lGenusLat = new JLabel("Genus (latin):");
		pContent.add(lGenusLat, gbc);

		gbc.gridy++;
		lSpecies = new JLabel("Species:");
		pContent.add(lSpecies, gbc);

		gbc.gridy++;
		lSpeciesLat = new JLabel("Species (latin):");
		pContent.add(lSpeciesLat, gbc);

		gbc.gridy++;
		lDescription = new JLabel("Description:");
		pContent.add(lDescription, gbc);

		// pridani text fieldu
		gbc.gridx++;
		gbc.gridy = 0;

		tGenus = new JTextField(DataBase.MAX_STRING);
		pContent.add(tGenus, gbc);

		gbc.gridy++;
		tGenusLat = new JTextField(DataBase.MAX_STRING);
		pContent.add(tGenusLat, gbc);

		gbc.gridy++;
		tSpecies = new JTextField(DataBase.MAX_STRING);
		pContent.add(tSpecies, gbc);

		gbc.gridy++;
		tSpeciesLat = new JTextField(DataBase.MAX_STRING);
		pContent.add(tSpeciesLat, gbc);

		gbc.gridy++;
		taDescription = new JTextArea(5, 20);
		taDescription.setLineWrap(true);
		taDescription.setWrapStyleWord(true);
		JScrollPane js = new JScrollPane(taDescription);

		pContent.add(js, gbc);

		// pridani tlacitek
		gbc.gridy++;
		gbc.gridx = 1;

		bSave = new JButton(SAVE);
		bSave.addActionListener(this);
		pContent.add(bSave, gbc);

		gbc.gridx = 0;
		bDelete = new JButton("Smazat");
		bDelete.addActionListener(this);
		bDelete.setEnabled(false);
		pContent.add(bDelete, gbc);

		setContentPane(pContent);

		pack();
	}

	/**
	 * Naplenni formulare datama z objektu
	 * @param animal
	 */
	public void fill(Animal animal){
		lId.setText(animal.getId() + "");
		tGenus.setText(animal.getGenus());
		tGenusLat.setText(animal.getGenusLat());
		tSpecies.setText(animal.getSpecies());
		tSpeciesLat.setText(animal.getSpeciesLat());
		try {
			taDescription.setText(animal.getDescription(db));
		} catch (SQLException e){
			System.err.println("Chyba pri ziskavani popisu: " + e.getMessage());
		}
	}

	/**
	 * Metoda ktera zobrazuje/skryva do dialogu tlacitko na mazani
	 * @param enable
	 */
	public void enableDeleteButton(boolean enable){
		bDelete.setEnabled(enable);
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getMode() {
		return mode;
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		// vytvoreni zvirete
		Animal animal = new Animal();
		try {
			animal.setId(Integer.parseInt(lId.getText()));
		} catch(NumberFormatException e){
			Log.error("Chyba pri parsovani ID");
		}
		animal.setGenus(tGenus.getText());
		animal.setGenusLat(tGenusLat.getText());
		animal.setSpecies(tSpecies.getText());
		animal.setSpeciesLat(tSpeciesLat.getText());
		animal.setDescription(taDescription.getText());

		// kliknuto na save
		if(event.getSource() == bSave){

			switch (mode) {
			case INSERT:
				frame.addAnimal(animal);
				dispose();
				break;
			case UPDATE:
				bSave.setText(EDIT);
				frame.editAnimal(animal);
				dispose();
				break;
			}
		}

		// smazani zaznamu
		if(event.getSource() == bDelete){
			frame.deleteAnimal(animal);
			dispose();
		}
	}
}
