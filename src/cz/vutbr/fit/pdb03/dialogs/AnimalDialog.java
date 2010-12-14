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
import cz.vutbr.fit.pdb03.gui.GUIManager;

/**
 * Dialog, pro pridavani a editaci zvirete
 * @author Ondřej Beneš <xbenes00@stud.fit.vutbr.cz>
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
	JButton bCancel, bSave;

	private int mode;

	DataBase db;

	AnimalsDatabase frame;

	private LoadingDialog dLoading = null;

	private Animal animal;

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

		lGenus = new JLabel("Rodové jméno:");
		pContent.add(lGenus, gbc);

		gbc.gridy++;
		lGenusLat = new JLabel("Rodové jméno (latinsky):");
		pContent.add(lGenusLat, gbc);

		gbc.gridy++;
		lSpecies = new JLabel("Druhové jméno:");
		pContent.add(lSpecies, gbc);

		gbc.gridy++;
		lSpeciesLat = new JLabel("Druhové jméno (latinsky):");
		pContent.add(lSpeciesLat, gbc);

		gbc.gridy++;
		lDescription = new JLabel("Popis:");
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

		// tlacitka
		JPanel buttons = new JPanel();

		bCancel = new JButton("Storno");
		bCancel.addActionListener(this);
		buttons.add(bCancel);

		bSave = new JButton(SAVE);
		bSave.addActionListener(this);
		buttons.add(bSave);

		// pridani tlacitek
		gbc.gridy++;
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.LINE_END;
		pContent.add(buttons, gbc);

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
			Log.error("Chyba pri ziskavani popisu: " + e.getMessage());
		}
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
		animal = new Animal();
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

			new Thread(new Runnable() {

				@Override
				public void run() {
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

					if(dLoading != null && dLoading.isVisible()){
						dLoading.dispose();
					}
				}
			}).start();

			dLoading = new LoadingDialog("Probíhá připojování k DB, prosím vyčkejte");
			GUIManager.moveToCenter(dLoading, this);
			dLoading.setVisible(true);
		}

		// cancel tlacitko
		if(event.getSource() == bCancel){
			dispose();
		}
	}
}
