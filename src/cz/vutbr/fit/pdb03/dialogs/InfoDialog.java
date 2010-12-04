package cz.vutbr.fit.pdb03.dialogs;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * Dialog pro ruzne informace
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
public class InfoDialog extends JDialog implements ActionListener{

	private static final long serialVersionUID = 3478309242909649301L;

	private JPanel pContent;
	private JLabel lInfo;

	private JButton bOk;

	public InfoDialog() {

		pContent = new JPanel();
		pContent.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(5, 5, 5, 5);

		// info label
		lInfo = new JLabel();
		pContent.add(lInfo, gbc);

		gbc.gridy++;
		// ok button
		bOk = new JButton("OK");
		bOk.addActionListener(this);
		pContent.add(bOk, gbc);

		// nastaveni dialogu
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		setModal(true);
		add(pContent);
		pack();
	}

	public InfoDialog(String message){
		this();

		lInfo.setText(message);
		pack();
	}

	public void setText(String message){
		lInfo.setText(message);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		// uzavreni okna
		if(e.getSource() == bOk){
			dispose();
		}

	}
}
