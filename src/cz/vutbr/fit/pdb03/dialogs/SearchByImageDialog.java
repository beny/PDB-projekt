package cz.vutbr.fit.pdb03.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.DataBase;

/**
 * Trida reprezentujici dialog pro hledani podle obrazku
 *
 */
public class SearchByImageDialog extends DefaultDialog implements ActionListener {

	private static final long serialVersionUID = -1885133417729619677L;

	AnimalsDatabase frame;
	JButton bChoose, bCancel, bSearch;
	JComboBox cType;

	private File file;
	private String table = DataBase.ANIMAL_PHOTO;

	public SearchByImageDialog(AnimalsDatabase frame) {
		this.frame = frame;
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = gbc.gridy = 0;

		bChoose = new JButton("Vyber soubor");
		bChoose.addActionListener(this);

		String[] elements = {"Fotka zvířete", "Fotka stopy", "Fotka trusu"};
		cType = new JComboBox(elements);
		cType.addActionListener(this);

		JPanel buttons = new JPanel();
		bCancel = new JButton("Storno");
		bCancel.addActionListener(this);
		buttons.add(bCancel);

		bSearch = new JButton("Hledej");
		bSearch.addActionListener(this);
		buttons.add(bSearch);

		// rozmisteni prvku
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		add(bChoose, gbc);

		gbc.gridy++;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.anchor = GridBagConstraints.CENTER;
		add(cType, gbc);

		gbc.gridwidth = 1;
		gbc.gridy++;
		add(buttons, gbc);

		pack();
	}

	/**
	 * Nastaveni a vyvolani hledani
	 */
	private void search(){

		String table = new String();
		switch(cType.getSelectedIndex()){
		case 0:	table = DataBase.ANIMAL_PHOTO; break;
		case 1: table = DataBase.FEET_PHOTO; break;
		case 2: table = DataBase.EXCREMENT_PHOTO; break;
		}
		frame.setSearchFilename(file.getAbsolutePath());
		frame.setSearchTable(table);
		frame.reloadAnimalsList(AnimalsDatabase.SEARCH_IMAGE);
		dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		// tlacitko vyber
		if(e.getSource() == bChoose){
			JFileChooser fc = new FileDialog();
			fc.setMultiSelectionEnabled(false);
			int returnVal = fc.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {

            	file = fc.getSelectedFile();
            	bChoose.setText("Obrázek vybrán");
            }
		}

		// tlacitko storno
		if(e.getSource() == bCancel){
			dispose();
		}

		// tlacitko save
		if(e.getSource() == bSearch){
			search();
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
