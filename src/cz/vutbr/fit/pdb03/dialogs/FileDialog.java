package cz.vutbr.fit.pdb03.dialogs;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * Dialog pro cteni obrazku
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
public class FileDialog extends JFileChooser {

	private final static long serialVersionUID = -2207787387581615333L;

	public FileDialog() {
		super();
		setDialogType(JFileChooser.OPEN_DIALOG);
		setFileHidingEnabled(true);
		setFileSelectionMode(JFileChooser.FILES_ONLY);
		setFileFilter(new ImageFilter());
		setMultiSelectionEnabled(true);
	}
}


/**
 * Filtr pro ziskani jen obrazku
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
class ImageFilter extends FileFilter {

	public final static String jpeg = "jpeg";
    public final static String jpg = "jpg";
    public final static String gif = "gif";
    public final static String tiff = "tiff";
    public final static String tif = "tif";
    public final static String png = "png";
    public final static String bmp = "bmp";

    @Override
	public boolean accept(File f) {

        String extension = getExtension(f);
        if (extension != null) {
            if (extension.equals(tiff) ||
                extension.equals(tif) ||
                extension.equals(gif) ||
                extension.equals(jpeg) ||
                extension.equals(jpg) ||
                extension.equals(png) ||
                extension.equals(bmp)) {
                    return true;
            } else {
                return false;
            }
        }

        return true;
    }

    @Override
	public String getDescription() {
        return "Obrázky";
    }

    /**
     * Ziskani extension souboru
     * @param f
     * @return
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}