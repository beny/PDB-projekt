package cz.vutbr.fit.pdb03.gui;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import cz.vutbr.fit.pdb03.AnimalsDatabase;

public class PhotosPanel extends JScrollPane {

	private static final long serialVersionUID = 8533602586891866222L;

	AnimalsDatabase frame;
	private JTabbedPane pPictures;

	public PhotosPanel(AnimalsDatabase frame) {
		this.frame = frame;

		pPictures = new JTabbedPane();
		pPictures.addTab("Info", new JLabel("Tady bude info o zvireti", JLabel.CENTER));
		pPictures.addTab("Fotky", new JLabel("Tady budou fotky", JLabel.CENTER));
		pPictures.addTab("Stopy", new JLabel("Tady budou stopy", JLabel.CENTER));
		pPictures.addTab("Trus", new JLabel("Tady bude velky hovno", JLabel.CENTER));

		setViewportView(pPictures);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		pPictures.setEnabled(enabled);
	}
}
