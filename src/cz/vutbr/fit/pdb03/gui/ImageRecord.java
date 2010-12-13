package cz.vutbr.fit.pdb03.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.sql.SQLException;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import oracle.ord.im.OrdImage;
import oracle.sql.BLOB;
import cz.vutbr.fit.pdb03.Log;

/**
 * Jeden radek ktery bude obsahovat obrazek a jeho popis
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
public class ImageRecord extends JPanel {

	private final static long serialVersionUID = 6570452902350045589L;

	private PictureThumbnail pic;
	private Image originalPic;
	private Image thumbPic;

	public ImageRecord(JPicture picture) {


		JTextArea text = new JTextArea();
		text = new JTextArea();
		text.setWrapStyleWord(true);
		text.setLineWrap(true);
		text.setEditable(false);
		text.setColumns(30);
		text.setOpaque(false);

		createThumb(picture.getPic());
		pic = new PictureThumbnail(originalPic);
		// TODO nastaveni obrazku
		picture.getPic();

		// nastaveni popisuku
		text.setText(picture.getDescription());

		add(pic);
		add(text);
	}

	private void createThumb(OrdImage img){

		try {
		BLOB blob = img.getContent();
		int length = (int) blob.length();

		byte[] mybytes= blob.getBytes(1,length);
			originalPic = Toolkit.getDefaultToolkit().createImage(mybytes);
		} catch (SQLException e){
			Log.error("Chyba pri vytvareni nahledu");
		}
	}
}

/**
 * Nahled obrazku ktery je obsazen v zazanmu
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
class PictureThumbnail extends JPanel{

	private final static long serialVersionUID = 3478184348179077461L;
	private final static int MAX_SIZE = 100;
	Image pic;

	public PictureThumbnail(Image pic) {
		setPreferredSize(new Dimension(MAX_SIZE, MAX_SIZE));
		setMaximumSize(new Dimension(MAX_SIZE, MAX_SIZE));
		setBackground(Color.BLACK);

		this.pic = pic;

		rightSize();
	}

	@Override
	protected void paintComponent(Graphics g) {
		double w, h, r = 0;

		double iw = pic.getWidth(this);
		double ih = pic.getHeight(this);
		if(iw > ih){
			w = MAX_SIZE;
			h = MAX_SIZE/(iw/ih);
		}
		else {
			h = MAX_SIZE;
			w = MAX_SIZE/(ih/iw);
		}

		g.drawImage(pic, 0, 0, (int)w, (int)h, null);
	}

	private void rightSize() {
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
			rightSize();
		if ((infoflags & ImageObserver.SOMEBITS) != 0)
			repaint();
		if ((infoflags & ImageObserver.ALLBITS) != 0) {
			rightSize();
			repaint();
			return false;
		}
		return true;
	}
}
