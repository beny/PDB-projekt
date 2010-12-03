package cz.vutbr.fit.pdb03.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import cz.vutbr.fit.pdb03.Animal;
import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.D;
import cz.vutbr.fit.pdb03.DataBase;

/**
 * Dialog, pro pridavani a editaci zvirete
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
public class AnimalDialog extends JDialog implements ActionListener{

	private static final long serialVersionUID = -3290129275558417391L;

	public static final int INSERT = 0;
	public static final int UPDATE = 1;

	private static final String EDIT = "Uprav";
	private static final String SAVE = "Ulož";
	// hlavni panel
	JPanel pContent;

	// GUI elementy
	JLabel lId, lGenus, lFamily, lGenusLat, lFamilyLat, lDescription;
	JTextField tGenus, tFamily, tGenusLat, tFamilyLat;
	JTextArea taDescription;
	JButton bCancel, bSave, bDelete;

	private int mode;

	DataBase db;

	AnimalsDatabase parent;

	public AnimalDialog(AnimalsDatabase parent) {

		this.parent = parent;
		db = parent.getDb();

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
		pContent.add(lId, gbc);

		lGenus = new JLabel("Genus:");
		pContent.add(lGenus, gbc);

		gbc.gridy++;
		lGenusLat = new JLabel("Genus (latin):");
		pContent.add(lGenusLat, gbc);

		gbc.gridy++;
		lFamily = new JLabel("Family:");
		pContent.add(lFamily, gbc);

		gbc.gridy++;
		lFamilyLat = new JLabel("Family (latin):");
		pContent.add(lFamilyLat, gbc);

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
		tFamily = new JTextField(DataBase.MAX_STRING);
		pContent.add(tFamily, gbc);

		gbc.gridy++;
		tFamilyLat = new JTextField(DataBase.MAX_STRING);
		pContent.add(tFamilyLat, gbc);

		gbc.gridy++;
		taDescription = new JTextArea(5, 20);
		pContent.add(taDescription, gbc);

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

		add(pContent);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setMode(AnimalDialog.INSERT);
		pack();
		setResizable(false);
		setModal(true);
	}

	/**
	 * Naplenni formulare datama z objektu
	 * @param animal
	 */
	public void fill(Animal animal){
		lId.setText(animal.getId() + "");
		tGenus.setText(animal.getGenus());
		tGenusLat.setText(animal.getGenusLat());
		tFamily.setText(animal.getSpecies());
		tFamilyLat.setText(animal.getSpeciesLat());
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

		// kliknuto na save
		if(event.getSource() == bSave){

			Animal animal;

			switch (mode) {
			case INSERT:

				animal = new Animal(tGenus.getText(),
						tGenusLat.getText(), tFamily.getText(),
						tFamilyLat.getText(), taDescription.getText());

				try {
					db.insertAnimal(animal);
				} catch (SQLException e) {
					System.err.println("Chyba pri vkladani zvirete do DB: " + e.getMessage());
				}

				parent.refreshAnimalsList();
				dispose();

				break;
			case UPDATE:

				bSave.setText(EDIT);
				int id = 0;

				try{
					id = Integer.parseInt(lId.getText());
					db.searchAnimals(id);
				} catch (NumberFormatException e) {
					System.err.println("Chyba pri editaci zvirete, neexistujici ID: " + e.getMessage());
				} catch (SQLException e) {
					System.err.println("Chyba hledani zvirete podle ID v DB: " + e.getMessage());
				}

				if (id != 0) {

					animal = new Animal(id, tGenus.getText(),
							tGenusLat.getText(), tFamily.getText(),
							tFamilyLat.getText(), taDescription.getText());

					D.log("Id zvirete ktere chci ulozit:" + id);

					try {
						db.updateAnimal(animal);
					} catch (SQLException e) {
						System.err.println("Chyba hledani zvirete podle ID v DB: " + e.getMessage());
					}
				}

				parent.refreshAnimalsList();
				dispose();
				break;
			}
		}

		// smazani zaznamu
		if(event.getSource() == bDelete){

			int id = 0;
			try{
				id = Integer.parseInt(lId.getText());
				db.deleteAnimal(id);
				D.log("Mazu zvire s ID " + id);
			} catch (NumberFormatException e) {
				System.err.println("Chyba pri editaci zvirete, neexistujici ID: " + e.getMessage());
			} catch (SQLException e) {
				System.err.println("Chyba pri mazani zvirete z DB" + e.getMessage());
			}

			parent.refreshAnimalsList();
			dispose();
		}
	}
}
