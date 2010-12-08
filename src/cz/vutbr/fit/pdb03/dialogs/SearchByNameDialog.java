package cz.vutbr.fit.pdb03.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.DataBase;

/**
 * Vyhledavani podle jmena
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
public class SearchByNameDialog extends DefaultDialog implements ActionListener{

	private final static long serialVersionUID = 748219567580839044L;

	AnimalsDatabase frame;

	JLabel lGenus, lSpecies;
	JTextField tGenus, tSpecies;
	JButton bCancel, bSearch;

	public SearchByNameDialog(AnimalsDatabase frame) {
		super();
		this.frame = frame;

		// content panel a nastaveni layoutu
		JPanel content = new JPanel();

		content.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.insets = new Insets(5, 5, 5, 5);

		// inicializace prvku
		lGenus = new JLabel("Genus:");
		lSpecies = new JLabel("Species:");

		tGenus = new JTextField(DataBase.MAX_STRING);
		tSpecies = new JTextField(DataBase.MAX_STRING);

		bCancel = new JButton("Storno");
		bCancel.addActionListener(this);

		bSearch = new JButton("Search");
		bSearch.addActionListener(this);

		JPanel buttons = new JPanel();

		// rozlozeni prvku
		buttons.add(bCancel);
		buttons.add(bSearch);

		content.add(lGenus, gbc);

		gbc.gridy++;
		content.add(lSpecies, gbc);

		gbc.gridy = 0;
		gbc.gridx++;
		content.add(tGenus, gbc);
		gbc.gridy++;
		content.add(tSpecies, gbc);

		gbc.gridy++;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.anchor = GridBagConstraints.LINE_END;
		content.add(buttons, gbc);

		setContentPane(content);
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		// uzavreni dialogu
		if(e.getSource() == bCancel){
			dispose();
		}

		// hledani
		if(e.getSource() == bSearch){
			frame.setSearchGenus(tGenus.getText());
			frame.setSearchSpecies(tSpecies.getText());
			frame.reloadAnimalsList(AnimalsDatabase.SEARCH_BY_NAME);
			dispose();
		}

	}
}
