package cz.vutbr.fit.pdb03.gui;

import oracle.ord.im.OrdImage;

/**
 * Reprezentace obrazku pro nase ucely
 * @author Pavel Srnec <xsrnec01@stud.fit.vutbr.cz>
 *
 */
public class JPicture extends OrdImage {

	private OrdImage pic;
	private int id;
	private String description;
	private String table;

	public JPicture(OrdImage pic, int id, String table) {
		this.pic = pic;
		this.id = id;
		this.table = table;
	}

	public OrdImage getPic() {
		return pic;
	}

	public void setPic(OrdImage pic) {
		this.pic = pic;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}
}
