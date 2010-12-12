package cz.vutbr.fit.pdb03.map;

import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import oracle.spatial.geometry.JGeometry;

/**
 * Rozsireni JGeometry o zjednodusujici funkce a obaleni pro DB
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
public class JEntity extends JGeometry {

	private final static long serialVersionUID = 3345323925830946171L;

	private final static int SRID = 8307;
	private final static int DIMENSION = 2;

	private boolean selected = false;

	/**
	 * ID entity v databazi
	 */
	private int id = 0;

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

	public JEntity(JGeometry geometry){
		this(geometry, 0);
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

	/**
	 * Presun bodu na nove misto
	 * @param lat
	 * @param lon
	 */
	public void movePoint(double lat, double lon){
		x = lat;
		y = lon;
	}

	/**
	 * Presun objekt obsahujici vic bodu
	 * @param lat
	 * @param lon
	 */
	public void moveMultiPoint(double lat, double lon) {
		// TODO overit pro ostatni entity

		List<JEntity> points = convert(getOrdinatesArray());

		// najdi prvni bod a presun jej
		JEntity firstPoint = points.get(0);
		double diffX = firstPoint.getLat() - lat;
		double diffY = firstPoint.getLon() - lon;
		firstPoint.movePoint(lat, lon);

		// presun ostatni body
		for (JEntity point : points) {
			if(point == firstPoint) continue;	// preskoc prvni bod
			point.movePoint(point.getLat() - diffX, point.getLon() - diffY);
		}

		// zpet ulozit do entity
		JGeometry geom = JGeometry.createMultiPoint(convert(points), DIMENSION,
				SRID);
		ordinates = geom.getOrdinatesArray();

	}

	/**
	 * Najde nejblizsi bod k bodu
	 * @param hitPoint
	 * @param entity
	 * @return
	 */
	public double diffPoint(java.awt.Point hitPoint, JMapPanel map) {
		java.awt.Point entityPoint = map.getMapPosition(getLat(), getLon(),
				false);
		return Point2D.distance(hitPoint.getX(), hitPoint.getY(),
				entityPoint.getX(), entityPoint.getY());
	}

	@Override
	public String toString() {
		return "id: " + getId() + ", type: " + getType() + ", coords: " + x
				+ " x " + y;
	}

	/**
	 * Prevod pole JEntity na pole Object[] kde polozky jsou body double[]
	 * @param points
	 * @return
	 */
	public static Object[] convert(List<JEntity> points){

		Object[] coords = new Object[points.size()];

		int i = 0;
		for (JEntity p : points) {
			coords[i] = new double[] {p.getLat(), p.getLon()};
			i++;
		}

		return coords;
	}

	/**
	 * Prevod seznamu pro curves a polygony na Object[]
	 * @param entities
	 * @return
	 */
	public static Object[] convertMulti(List<JEntity> entities){

		Object[] array = new Object[entities.size()];

		int i = 0;
		for (JEntity e : entities) {
			List<JEntity> points = convert(e.getOrdinatesArray());
			array[i] = convertDouble(points);
			i++;
		}

		return array;
	}

	/**
	 * Prevod z pole JEntit na jednorozmerne pole double[]
	 * @param points
	 * @return
	 */
	public static double[] convertDouble(List<JEntity> points){
		double[] array = new double[points.size()*2];
		int i = 0;
		for (JEntity p : points) {
			array[i] = p.getLat();
			i++;
			array[i] = p.getLon();
			i++;
		}

		return array;
	}

	/**
	 * Prevod bodu z Object[] do pole JEntity
	 * @param points
	 * @return
	 */
	public static List<JEntity> convert(double[] points){

		List<JEntity> data = new LinkedList<JEntity>();

		for (int i = 0; i < points.length; i++) {

			double[] coords = { points[i], points[++i]};
			data.add(new JEntity(coords[0], coords[1]));
		}

		return data;
	}

	/**
	 * Vytvoreni JGeometry pro MultiPoint
	 * @param points
	 * @return
	 */
	public static JGeometry createMultiPoint(List<JEntity> points){
		return JGeometry.createMultiPoint(convert(points), DIMENSION, SRID);
	}

	/**
	 * Vytvoreni JGeometry pro Curve
	 * @param points
	 * @return
	 */
	public static JGeometry createCurve(List<JEntity> points){
		return JGeometry.createLinearLineString(convertDouble(points), DIMENSION, SRID);
	}

	/**
	 * Vytvoreni JGeometry pro Polygon
	 * @param points
	 * @return
	 */
	public static JGeometry createPolygon(List<JEntity> points){
		return JGeometry.createLinearPolygon(convertDouble(points), DIMENSION, SRID);
	}

	/**
	 * Vytvoreni JGeometry pro MultiCurve
	 * @param points
	 * @return
	 */
	public static JGeometry createMultiCurve(List<JEntity> points){
		return JGeometry.createLinearMultiLineString(convertMulti(points), DIMENSION, SRID);
	}

	/**
	 * Vytvoreni JGeometry pro MultiPolygon
	 * @param points
	 * @return
	 */
	public static JGeometry createMultiPolygon(List<JEntity> points){
		return JGeometry.createLinearPolygon(convertMulti(points), DIMENSION, SRID);
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

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

}
