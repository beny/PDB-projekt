package cz.vutbr.fit.pdb03.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.ImageObserver;
import java.sql.SQLException;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import oracle.ord.im.OrdImage;
import oracle.sql.BLOB;
import cz.vutbr.fit.pdb03.Animal;
import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.Log;
import cz.vutbr.fit.pdb03.dialogs.LoadingDialog;

/**
 * Jeden radek ktery bude obsahovat obrazek a jeho popis
 * @author Ondřej Beneš <xbenes00@stud.fit.vutbr.cz>
 *
 */
public class ImageRecord extends JPanel implements MouseListener{

	private final static long serialVersionUID = 6570452902350045589L;
	private static final int ROTATE_LEFT = 0;
	private static final int ROTATE_RIGHT = 1;

	private PictureThumbnail thumbnail;
	private JPicture originImage;
	private Image image;
	private AnimalsDatabase frame;
	private String desc;

	private LoadingDialog dLoading = null;

	public ImageRecord(JPicture originImage, AnimalsDatabase frame) {
		this.frame = frame;
		this.originImage = originImage;

		JTextArea text = new JTextArea();
		text = new JTextArea();
		text.setWrapStyleWord(true);
		text.setLineWrap(true);
		text.setEditable(false);
		text.setColumns(30);
		text.setOpaque(false);

		// vytvor obrazek
		createThumb(originImage.getPic());
		thumbnail = new PictureThumbnail(image);
		originImage.getPic();

		// nastaveni popisuku
		text.setText(originImage.getDescription());

		add(thumbnail);
		add(text);
		addMouseListener(this);
	}

	/**
	 * Vytvoreni nahledu z OrdImage
	 * @param origin
	 */
	private void createThumb(OrdImage origin){

		try {
		BLOB blob = origin.getContent();
		int length = (int) blob.length();

		byte[] mybytes= blob.getBytes(1,length);
			image = Toolkit.getDefaultToolkit().createImage(mybytes);
		} catch (SQLException e){
			Log.error("Chyba pri vytvareni nahledu");
		}
	}

	/**
	 * Smazani fotky z DB
	 */
	private void deletePhoto(){
                dLoading = new LoadingDialog("Mažu obrázek z databáze");
		GUIManager.moveToCenter(dLoading, frame);
		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					frame.getDb().deletePicture(originImage.getId(),
							originImage.getTable());
				} catch (SQLException e) {
					Log.error("Chyba pri mazani obrazku z DB");
				}

				Animal animal = frame.getAnimalsPanel().getSelectedAnimal();
				frame.getAnimalsPanel().getListController()
						.setSelectedAnimal(animal);

				if (dLoading != null && dLoading.isVisible()) {
					dLoading.dispose();
				}
			}
		}).start();
		dLoading.setVisible(true);
	}

	/**
	 * Vyvolani dialogu pro editaci popisku fotky a jeho ulozeni
	 */
	private void editPhoto() {
		dLoading = new LoadingDialog("Upravuju popisek fotky v databázi");
		GUIManager.moveToCenter(dLoading, frame);
		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					frame.getDb().setPhotoDescription(originImage.getId(),
							originImage.getTable(), getDesc());
				} catch (SQLException e) {
					Log.error("Chyba pri zmene popisku fotky v DB");
				}

				// obnoveni listu
				Animal animal = frame.getAnimalsPanel().getSelectedAnimal();
				frame.getAnimalsPanel().getListController()
						.setSelectedAnimal(animal);

				if (dLoading != null && dLoading.isVisible()) {
					dLoading.dispose();
				}
			}
		}).start();
		dLoading.setVisible(true);
	}

	/**
	 * Rotace fotek
	 * @param direction
	 */
	private void rotatePhoto(final int direction){

		dLoading = new LoadingDialog("Upravuju popisek fotky v databázi");
		GUIManager.moveToCenter(dLoading, frame);
		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					frame.getDb().rotatePicture(originImage.getId(),
							originImage.getTable(),
							(direction == ROTATE_LEFT) ? -90 : 90);
				} catch (SQLException e) {
					Log.error("Chyba pri zmene rotace fotky v DB: "
							+ e.getMessage());
				}

				// obnoveni listu
				Animal animal = frame.getAnimalsPanel().getSelectedAnimal();
				frame.getAnimalsPanel().getListController()
						.setSelectedAnimal(animal);

				if (dLoading != null && dLoading.isVisible()) {
					dLoading.dispose();
				}
			}
		}).start();
		dLoading.setVisible(true);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// levy double klik
		if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
			ImagePreviewDialog preview = new ImagePreviewDialog(image,
					(int)thumbnail.getOriginalWidth(), (int)thumbnail.getOriginalHeight());
			GUIManager.moveToCenter(preview, frame);
			preview.setVisible(true);
		}

		// pravy klik
		if (e.getButton() == MouseEvent.BUTTON3) {
			JPopupMenu mContext = new JPopupMenu();

			JMenuItem miEdit = new JMenuItem("Upravit");
			miEdit.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {

					String retval = JOptionPane.showInputDialog(frame,
							"Úprava titulku fotky",
							originImage.getDescription());
                                        if (retval == null) return;
					if (!retval.equals(originImage.getDescription())) {
                                            setDesc(retval);
                                            editPhoto();
					}
				}
			});
			mContext.add(miEdit);

			JMenuItem miDelete = new JMenuItem("Smazat");
			miDelete.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					int retval = JOptionPane.showConfirmDialog(frame,
							"Opravdu chcete smazat fotku?", "Mazání fotky",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);

					if(retval == JOptionPane.YES_OPTION){
						deletePhoto();
					}
				}
			});
			mContext.add(miDelete);

			JMenuItem miRotateLeft = new JMenuItem("Otoč doleva");
			miRotateLeft.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					rotatePhoto(ROTATE_LEFT);

				}
			});
			mContext.add(new JSeparator());
			mContext.add(miRotateLeft);

			JMenuItem miRotateRight = new JMenuItem("Otoč doprava");
			miRotateRight.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					rotatePhoto(ROTATE_RIGHT);

				}
			});
			mContext.add(miRotateRight);

			mContext.show(this, e.getPoint().x, e.getPoint().y);
		}

	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent arg0) {}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
}

/**
 * Nahled obrazku ktery je obsazen v zazanmu
 * @author Ondřej Beneš <xbenes00@stud.fit.vutbr.cz>
 *
 */
class PictureThumbnail extends JPanel{

	private final static long serialVersionUID = 3478184348179077461L;
	private final static int MAX_SIZE = 100;
	Image pic;
	private double originalWidth, originalHeight;
	private int width, height;

	public PictureThumbnail(Image pic) {
		setPreferredSize(new Dimension(MAX_SIZE, MAX_SIZE));
		setMaximumSize(new Dimension(MAX_SIZE, MAX_SIZE));
		setBackground(Color.BLACK);

		this.pic = pic;
		correctSize();
	}

	@Override
	protected void paintComponent(Graphics g) {
		double width, height;

		originalWidth = pic.getWidth(this);
		originalHeight = pic.getHeight(this);
		if(originalWidth > originalHeight){
			width = MAX_SIZE;
			height = MAX_SIZE/(originalWidth/originalHeight);
		}
		else {
			height = MAX_SIZE;
			width = MAX_SIZE/(originalHeight/originalWidth);
		}

		g.drawImage(pic, 0, 0, (int)width, (int)height, null);
	}

	private void correctSize() {

		width = pic.getWidth(this);
		height = pic.getHeight(this);
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

	public double getOriginalWidth() {
		return width;
	}

	public double getOriginalHeight() {
		return height;
	}
}
