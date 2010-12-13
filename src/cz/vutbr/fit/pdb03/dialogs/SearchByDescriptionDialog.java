package cz.vutbr.fit.pdb03.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.Log;

/**
 * Dialog pro hledani podle popisu jak pro zvirata tak pro obrazky.
 *
 * Pro typ je nutno nastavit typ.
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
public class SearchByDescriptionDialog extends DefaultDialog implements ActionListener{

	private final static long serialVersionUID = -1693457717317002638L;

	public final static int TYPE_DESCRIPTION = 1;
	public final static int TYPE_PICTURE_DESCRIPTION = 2;

	AnimalsDatabase frame;

	JLabel lDescription;
	JTextArea taDescription;
	JButton bCancel, bSearch;

	private int type;

	public SearchByDescriptionDialog(AnimalsDatabase frame) {
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
		lDescription = new JLabel("Popis:");

		taDescription = new JTextArea(5, 20);
		taDescription.setLineWrap(true);
		taDescription.setWrapStyleWord(true);

		bCancel = new JButton("Storno");
		bCancel.addActionListener(this);

		bSearch = new JButton("Hledat");
		bSearch.addActionListener(this);

		JPanel buttons = new JPanel();

		// rozlozeni prvku
		buttons.add(bCancel);
		buttons.add(bSearch);

		content.add(lDescription, gbc);

		gbc.gridy = 0;
		gbc.gridx++;
		content.add(new JScrollPane(taDescription), gbc);

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
			frame.setSearchDescription(taDescription.getText());

			switch(type){
			case TYPE_DESCRIPTION:
				frame.reloadAnimalsList(AnimalsDatabase.SEARCH_BY_DESCRIPTION);
				dispose();
				break;
			case TYPE_PICTURE_DESCRIPTION:
				frame.reloadAnimalsList(AnimalsDatabase.SEARCH_BY_PICTURE_DESCRIPTION);
				dispose();
				break;
			default:
				Log.error("Neni nastaven typ dialogu");
			}
		}
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;

		if(type == AnimalsDatabase.SEARCH_BY_PICTURE_DESCRIPTION){
			lDescription.setText("Popis:");
		}

		// TODO overit pro dalsi typy dialogu
	}
}
