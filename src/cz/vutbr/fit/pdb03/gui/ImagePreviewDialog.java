package cz.vutbr.fit.pdb03.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;

import javax.swing.JPanel;

import cz.vutbr.fit.pdb03.Log;
import cz.vutbr.fit.pdb03.dialogs.DefaultDialog;

public class ImagePreviewDialog extends DefaultDialog {

	private final static long serialVersionUID = 4730679266665138535L;

	public ImagePreviewDialog(Image img) {
		add(new FullImage(img));


		setPreferredSize(new Dimension(250, 250));
		setMinimumSize(new Dimension(250, 250));
	}
}


class FullImage extends JPanel{

	private final static long serialVersionUID = -9087648115206402954L;
	Image pic;

	public FullImage(Image pic) {
		this.pic = pic;

		correctSize();
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.drawImage(pic, 0, 0, null);
	}

	private void correctSize() {
		int width = pic.getWidth(this);
		int height = pic.getHeight(this);
		if (width == -1 || height == -1)
			return;
		addNotify();
	}

	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y,
			int width, int height) {
		if ((infoflags & ImageObserver.ERROR) != 0) {
			Log.error("Error loading image");
		}
		if ((infoflags & ImageObserver.WIDTH) != 0
				&& (infoflags & ImageObserver.HEIGHT) != 0)
			correctSize();
		if ((infoflags & ImageObserver.SOMEBITS) != 0)
			repaint();
		if ((infoflags & ImageObserver.ALLBITS) != 0) {
			correctSize();
			repaint();
			return false;
		}
		return true;
	}
}