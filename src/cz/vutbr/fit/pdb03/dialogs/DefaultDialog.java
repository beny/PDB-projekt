package cz.vutbr.fit.pdb03.dialogs;

import javax.swing.JDialog;

/**
 * Trida se spolecnymi vlastnostmi dialogu
 * @author Pavel Srnec <xsrnec01@stud.fit.vutbr.cz>
 *
 */
public class DefaultDialog extends JDialog {

	private static final long serialVersionUID = -1777653407058631531L;

	public DefaultDialog() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(true);
		setResizable(false);
	}
}
