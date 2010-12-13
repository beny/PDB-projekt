package cz.vutbr.fit.pdb03.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cz.vutbr.fit.pdb03.Animal;
import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.DataBase;
import cz.vutbr.fit.pdb03.Log;

/**
 * Trida reprezentujici dialog pro nahravani obrazku do DB;
 *
 */
public class ImageUploadDialog extends DefaultDialog implements ActionListener {

	private static final long serialVersionUID = -1885133417729619677L;

	AnimalsDatabase frame;
	JButton bChoose, bCancel, bSave;
	JComboBox cType;
	JLabel lDesc;
	JTextField tDesc;

	private File[] files;
	private String table = DataBase.ANIMAL_PHOTO;

	public ImageUploadDialog(AnimalsDatabase frame) {
		this.frame = frame;
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = gbc.gridy = 0;

		bChoose = new JButton("Vyber soubory");
		bChoose.addActionListener(this);

		String[] elements = {"Zvíře", "Stopa", "Trus"};
		cType = new JComboBox(elements);
		cType.addActionListener(this);

		lDesc = new JLabel("Popis:");

		tDesc = new JTextField(DataBase.MAX_STRING);

		JPanel buttons = new JPanel();
		bCancel = new JButton("Storno");
		bCancel.addActionListener(this);
		buttons.add(bCancel);

		bSave = new JButton("Ulož");
		bSave.addActionListener(this);
		buttons.add(bSave);

		// rozmisteni prvku
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		add(bChoose, gbc);

		gbc.gridy++;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.anchor = GridBagConstraints.CENTER;
		add(cType, gbc);

		gbc.gridwidth = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		add(lDesc, gbc);

		gbc.gridx++;
		add(tDesc, gbc);

		gbc.gridy++;
		add(buttons, gbc);

		pack();
	}

	/**
	 * Ulozeni obrazku do databaze
	 */
	private void saveImages() {
		Animal animal = frame.getAnimalsPanel().getSelectedAnimal();

		for (File file : files) {
			try {
				frame.getDb().uploadImage(animal.getId(),
						getTableName(), file.getAbsolutePath(), 1,
						tDesc.getText());
			} catch (SQLException ex) {
				Log.error("Chyba pri nahravani obrazku do DB: "
						+ ex.getMessage());
			} catch (IOException ex) {
				Log.error("Chyba pri cteni obrazku: " + ex.getMessage());
			}
			Log.debug("Obrazek " + file.getName() + " nahran");

			// obnoveni
			frame.getAnimalsPanel()
					.getListController()
					.setSelectedAnimal(
							frame.getAnimalsPanel().getSelectedAnimal());
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		// tlacitko vyber
		if(e.getSource() == bChoose){
			JFileChooser fc = new FileDialog();
			int returnVal = fc.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {

            	files = fc.getSelectedFiles();
            	bChoose.setText("Vybráno " + files.length + " souborů");
            }
		}

		// tlacitko storno
		if(e.getSource() == bCancel){
			dispose();
		}

		// tlacitko save
		if(e.getSource() == bSave){
			saveImages();
			dispose();
		}
	}

	/**
	 * Ziskani jmena tabulky podle vybraneho combo
	 * @return
	 */
	public String getTableName(){
		if(cType.getSelectedIndex() == 0) return DataBase.ANIMAL_PHOTO;
		else if(cType.getSelectedIndex() == 1) return DataBase.FEET_PHOTO;
		else return DataBase.EXCREMENT_PHOTO;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}
}
