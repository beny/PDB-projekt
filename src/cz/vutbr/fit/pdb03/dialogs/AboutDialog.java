package cz.vutbr.fit.pdb03.dialogs;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Dialog ktery obsahuje info o projektu
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
public class AboutDialog extends JDialog {

	private static final long serialVersionUID = -1849226305505965710L;

	JLabel lInfo;
	JPanel pContent;

	public AboutDialog() {

		pContent = new JPanel();

		// info label
		lInfo = new JLabel("Nejake info o projektu");
		pContent.add(lInfo);

		// nastaveni dialogu
		setResizable(false);
		setModal(true);
		add(pContent);
		pack();
	}
}
