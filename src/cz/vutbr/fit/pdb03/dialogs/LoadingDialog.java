package cz.vutbr.fit.pdb03.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class LoadingDialog extends DefaultDialog {

	private static final long serialVersionUID = -7777460264482052284L;

	public LoadingDialog() {

//		setUndecorated(true);

		JPanel pContent = new JPanel();
		pContent.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);

		gbc.gridy = 0;
		JProgressBar progress = new JProgressBar();
		progress.setIndeterminate(true);
		pContent.add(progress, gbc);

		gbc.gridy++;
		JLabel info = new JLabel("Probíhá nahrávání z databáze, prosím vyčkejte", JLabel.CENTER);
		pContent.add(info, gbc);

		setContentPane(pContent);
		pack();
	}

}
