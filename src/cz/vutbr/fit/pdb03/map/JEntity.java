package cz.vutbr.fit.pdb03.map;

import org.openstreetmap.gui.jmapviewer.Coordinate;

import oracle.spatial.geometry.JGeometry;

/**
 * Rozsireni JGeometry o zjednodusujici funkce a obaleni pro DB
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
public class JEntity extends JGeometry {

	private final static long serialVersionUID = 3345323925830946171L;

	private final static int SRID = 8307;

	/**
	 * ID entity v databazi
	 */
	private int id;


	public JEntity(JGeometry geometry, int id){
		this(geometry.getType(),
				(geometry.getPoint() == null) ? Double.NaN : geometry.
						getPoint()[0],
				(geometry.getPoint() == null) ? Double.NaN : geometry
						.getPoint()[1],
				Double.NaN, geometry.getElemInfo(),
				geometry.getOrdinatesArray());
		setId(id);
	}

	public JEntity(double x, double y) {
		super(x, y, SRID);
	}

	public JEntity(double x, double y, int id) {
		super(x, y, SRID);
	}

	public JEntity(int gtype, double x, double y, double z, int[] elemInfo, double[] ordinates) {
		super(gtype, SRID, x, y, z, elemInfo, ordinates);
	}

	public JEntity(int gtype, int[] elemInfo, double[] ordinates){
		super(gtype, SRID, elemInfo, ordinates);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getLat(){
		return getJavaPoint().getX();
	}

	public double getLon(){
		return getJavaPoint().getY();
	}

	/**
	 * Presun bod
	 * @param lat
	 * @param lon
	 */
	public void movePoint(double lat, double lon){
		x = lat;
		y = lon;
	}

	@Override
	public String toString() {
		return "id: " + getId() + ", type: " + getType();
	}

}
