package cz.vutbr.fit.pdb03.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.DataBase;

/**
 * Vyhledavani podle jmena
 * @author Pavel Srnec <xsrnec01@stud.fit.vutbr.cz>
 *
 */
public class SearchByNameDialog extends DefaultDialog implements ActionListener, KeyListener {

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
		lGenus = new JLabel("Rodové jméno:");
		lSpecies = new JLabel("Druhové jméno:");

		tGenus = new JTextField(DataBase.MAX_STRING);
		tGenus.addKeyListener(this);
		tSpecies = new JTextField(DataBase.MAX_STRING);
		tSpecies.addKeyListener(this);

		bCancel = new JButton("Storno");
		bCancel.addActionListener(this);

		bSearch = new JButton("Hledat");
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

	/**
	 * Vyvolej hledani
	 */
	private void search(){

		if (tGenus.getText().length() > 0 || tSpecies.getText().length() > 0) {
			frame.setSearchGenus(tGenus.getText());
			frame.setSearchSpecies(tSpecies.getText());
			frame.reloadAnimalsList(AnimalsDatabase.SEARCH_BY_NAME);
		}
		dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		// uzavreni dialogu
		if(e.getSource() == bCancel){
			dispose();
		}

		// hledani
		if(e.getSource() == bSearch){
			search();
		}

	}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER){
			search();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {}
}
