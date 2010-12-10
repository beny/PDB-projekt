package cz.vutbr.fit.pdb03.map;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.OsmTileSource;

import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.Log;
import cz.vutbr.fit.pdb03.controllers.MapController;

/**
 * Trida rozsirujici moznosti zakladni mapy
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
public class JMapPanel extends JMapViewer {

	private static final long serialVersionUID = -7269660504108541606L;

	private final static BasicStroke stroke = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);

	private final static double DEFAULT_LAT = 49;
	private final static double DEFAULT_LON = 17;
	private final static int DEFAULT_ZOOM = 2;

	private final static int MY_POINT_SIZE = 10;
	private final static Color MY_POINT_COLOR = Color.RED;

	private final static int POINT_SIZE = 10;
	private final static Color POINT_COLOR = Color.GREEN;

	private final static Color POINT_SELECTED_COLOR = Color.CYAN;

	// konstanty akci
	public final static String ACTION_EDIT = "EDIT";
	public final static String ACTION_SAVE = "SAVE";
	public final static String ACTION_CHANGE_TYPE = "CHANGE";
	public final static String ACTION_CANCEL = "CANCEL";
	public final static String ACTION_NEXT_OBJECT = "NEXT";

	public final static int MODE_POINT = 0;
	public final static int MODE_CURVE = 1;
	public final static int MODE_POLYGON = 2;

	// maximalni distance, ktera se bere v uvahu
	private final static double MAX_DISTANCE = POINT_SIZE/2;



	// hlavni frame
	AnimalsDatabase frame;

	// kontroler
	MapController mapController;

	// indikace editacniho modu
	private boolean editMode = false;

	// mod vykreslovani
	private int drawMode = MODE_POINT;

	// komponenta pro mapu
	JButton bEdit, bSave, bCancel, bNext;
	private JComboBox comboElements;

	// data
	JEntity myPosition;	// moje poloha
	List<JEntity> data;
	List<JEntity> tempData;

	// detekce bodu
	JEntity close;
	double distance;
	double newDistance;

	public JMapPanel(AnimalsDatabase frame) {
		super(new MemoryTileCache(), 4);

		// hlavni frame
		this.frame = frame;

		// kontrolery
		mapController = new MapController(this);

		// vlastnosti mapy
		setPreferredSize(null);
		setTileSource(new OsmTileSource.CycleMap());
		setTileLoader(new OsmTileLoader(this));

		// inicializace tlacitek
		initializeEditButtons();

		// data
		data = new LinkedList<JEntity>();
		myPosition = new JEntity(DEFAULT_LAT, DEFAULT_LON);

		// vycentrovani
		setDisplayPositionByLatLon(DEFAULT_LAT, DEFAULT_LON, DEFAULT_ZOOM);
	}

	/**
	 * Inicializace editacnich tlacitek
	 */
	protected void initializeEditButtons(){

		int buttonSizeX = 70;
		int buttonSizeY = 20;
		int smallSpace = 10;

		// edit tlacitko
		bEdit = new JButton("uprav");
		bEdit.setBounds(50, smallSpace, buttonSizeX, buttonSizeY);
		bEdit.setActionCommand(ACTION_EDIT);
		bEdit.addActionListener(mapController);
		add(bEdit);

		bCancel = new JButton("zruš");
		bCancel.setBounds(50, smallSpace + 30, buttonSizeX, buttonSizeY);
		bCancel.setActionCommand(ACTION_CANCEL);
		bCancel.addActionListener(mapController);
		add(bCancel);

		// komponenty pro editaci
		// tlacitko pro ukladani
		bSave = new JButton("ulož");
		bSave.setBounds(50, smallSpace, buttonSizeX, buttonSizeY);
		bSave.setActionCommand(ACTION_SAVE);
		bSave.addActionListener(mapController);
		add(bSave);

		// kombo pro vyber elementu
		String[] elements = {"Výskyt", "Trasa", "Území"};
		comboElements = new JComboBox(elements);
		comboElements.setBounds(50 + buttonSizeX + smallSpace, smallSpace, 120, buttonSizeY);
		comboElements.setActionCommand(ACTION_CHANGE_TYPE);
		setDrawMode(MODE_POINT);
		comboElements.addActionListener(mapController);
		add(comboElements);

		// ulozeni konkretniho objektu pro zadavani noveho
		bNext = new JButton("nový objekt");
		bNext.setBounds(50 + buttonSizeX + smallSpace, smallSpace + 30, 120, buttonSizeY);
		bNext.setActionCommand(ACTION_NEXT_OBJECT);
		bNext.addActionListener(mapController);
		add(bNext);

		setEditMode(false);
	}

	/**
	 * Metoda disablujici tlacitka v mape
	 * @param enabled
	 */
	public void setEditButtonsEnabled(boolean enabled){
		bEdit.setEnabled(enabled);
		bNext.setEnabled(enabled);
		bSave.setEnabled(enabled);
		comboElements.setEnabled(enabled);
	}

	public JEntity detectHit(Point hitPoint){


		close = null;
		distance = MAX_DISTANCE;

		for (JEntity entity : data) {
			switch (entity.getType()) {
			case JEntity.GTYPE_POINT:
				// zmer vzdalenost
				newDistance = diffPoint(hitPoint, entity);
				break;
//			case JEntity.GTYPE_MULTIPOINT: paintMultiPoint(g, entity); break;
//			case JEntity.GTYPE_CURVE: paintCurve(g, entity); break;
//			case JEntity.GTYPE_MULTICURVE: paintMultiCurve(g, entity); break;
//			case JEntity.GTYPE_POLYGON: paintPolygon(g, entity); break;
//			case JEntity.GTYPE_MULTIPOLYGON: paintMultiPolygon(g, entity); break;
			default:
				break;
			}

			// pokud je bliz, zvol tento
			if(newDistance < distance){
				close = entity;
			}
		}

		for (JEntity entity : tempData) {
			switch (entity.getType()) {
			case JEntity.GTYPE_POINT:
				// zmer vzdalenost
				newDistance = diffPoint(hitPoint, entity);
				break;
//			case JEntity.GTYPE_MULTIPOINT: paintMultiPoint(g, entity); break;
//			case JEntity.GTYPE_CURVE: paintCurve(g, entity); break;
//			case JEntity.GTYPE_MULTICURVE: paintMultiCurve(g, entity); break;
//			case JEntity.GTYPE_POLYGON: paintPolygon(g, entity); break;
//			case JEntity.GTYPE_MULTIPOLYGON: paintMultiPolygon(g, entity); break;
			default:
				break;
			}

			// pokud je bliz, zvol tento
			if(newDistance < distance){
				close = entity;
			}
		}

		return close;
	}

	/**
	 * Najde nejblizsi bod k bodu
	 * @param hitPoint
	 * @param entity
	 * @return
	 */
	private double diffPoint(Point hitPoint, JEntity entity) {
		Point entityPoint = getMapPosition(entity.getLat(), entity.getLon(),
				true);
		return Point2D.distance(hitPoint.getX(), hitPoint.getY(),
				entityPoint.getX(), entityPoint.getY());
	}

	/**
	 * Smaze z mapy vsechny data
	 */
	public void clearMapData(){
		data.clear();
		repaint();
	}

	/**
	 * Metoda ktera naplni mapu z data z JGeometry
	 * @param data
	 */
	public void setMapData(List<JEntity> data){
		this.data = data;
		repaint();
	}

	/**
	 * Inicializace temp dat
	 */
	public void initTempData(){
		tempData = new LinkedList<JEntity>();
	}

	/**
	 * Pridavani bodu pri vkladani noveho elementu
	 * @param point
	 */
	public void tempAddPoint(JEntity point){
		tempData.add(point);
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		for (JEntity entity : data) {
			switch (entity.getType()) {
			case JEntity.GTYPE_POINT: paintPoint(g, entity); break;
//			case JEntity.GTYPE_MULTIPOINT: paintMultiPoint(g, entity); break;
//			case JEntity.GTYPE_CURVE: paintCurve(g, entity); break;
//			case JEntity.GTYPE_MULTICURVE: paintMultiCurve(g, entity); break;
//			case JEntity.GTYPE_POLYGON: paintPolygon(g, entity); break;
//			case JEntity.GTYPE_MULTIPOLYGON: paintMultiPolygon(g, entity); break;
			default:
				break;
			}

		}

		// pokud je v editacnim modu, kresli i nove vykreslene body
		if (isEditMode()) {
			for (JEntity entity : tempData) {
				switch (entity.getType()) {
				case JEntity.GTYPE_POINT:
					paintPoint(g, entity);
					break;
				// case JEntity.GTYPE_MULTIPOINT: paintMultiPoint(g, entity);
				// break;
				// case JEntity.GTYPE_CURVE: paintCurve(g, entity); break;
				// case JEntity.GTYPE_MULTICURVE: paintMultiCurve(g, entity);
				// break;
				// case JEntity.GTYPE_POLYGON: paintPolygon(g, entity); break;
				// case JEntity.GTYPE_MULTIPOLYGON: paintMultiPolygon(g,
				// entity); break;
				default:
					break;
				}

			}
		}

		// vykresli moji polohu
		paintMyPoint(g, myPosition);
	}

	/**
	 * Vykresleni bodu s moji pozici
	 * @param g
	 * @param myPoint
	 */
	protected void paintMyPoint(Graphics g, JEntity myPoint) {
		Graphics2D g2 = (Graphics2D) g;
		Point2D p = myPoint.getJavaPoint();
		Point mp = getMapPosition(p.getX(), p.getY(), true);

		g2.setColor(MY_POINT_COLOR);
		g2.fillOval(mp.x - MY_POINT_SIZE / 2, mp.y - MY_POINT_SIZE / 2,
				MY_POINT_SIZE, MY_POINT_SIZE);
	}

	/**
	 * Vykresleni bodu
	 * @param g
	 * @param point
	 */
	protected void paintPoint(Graphics g, JEntity point){
		Graphics2D g2 = (Graphics2D) g;
		Point2D p = point.getJavaPoint();
		Point mp = getMapPosition(p.getX(), p.getY(), true);

		// pokud je pobliz mys
		if(point == close && isEditMode()){
			g2.setColor(POINT_SELECTED_COLOR);
		}
		else {
			g2.setColor(POINT_COLOR);
		}
		g2.fillOval(mp.x - POINT_SIZE / 2, mp.y - POINT_SIZE / 2,
				POINT_SIZE, POINT_SIZE);
	}

	/**
	 * Vykresleni skupin bodu
	 * @param g
	 * @param points
	 */
	protected void paintMultiPoint(Graphics g, JEntity points) {
		Object[] groups = points.getOrdinatesOfElements();

		for (Object point : groups) {
			Point mp = getMapPosition(((double[])point)[0], ((double[])point)[1]);
			paintPoint(g, new JEntity(mp.getX(), mp.getY()));
		}

	}

	/**
	 * Vykresleni krivky
	 * @param g
	 * @param curve
	 */
	protected void paintCurve(Graphics g, JEntity curve) {
		Graphics2D g2 = (Graphics2D) g;
		// inicializace linestring
		GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD,
				curve.getNumPoints());

		// TODO ziskat bodu
		double[] points = curve.getOrdinatesArray();
		// zakresli prvni bod
		Point2D first = getMapPosition(curve.getFirstPoint()[0],
				curve.getFirstPoint()[1], false);
		path.moveTo(first.getX(), first.getY());

		for (int i = 0; i < points.length; i++) {
			Point2D p = getMapPosition(points[i+1], points[i++]);
			// TODO zvyrazeni bodu
			path.lineTo(p.getX(), p.getY());
		}

		g2.setPaint(Color.GREEN);
		g2.setStroke(stroke);
		g2.draw(path);
	}

	protected void paintMultiCurve(Graphics g, JEntity curves) {
		// TODO Auto-generated method stub

	}

	/**
	 * Vykresleni polygonu
	 * @param g
	 * @param map
	 */
	protected void paintPolygon(Graphics g, JEntity polygon) {

		Graphics2D g2 = (Graphics2D) g;
		// inicializace linestring
		GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD,
				polygon.getNumPoints());

		double[] points = polygon.getOrdinatesArray();
		// zakresli prvni bod
		Point2D first = getMapPosition(polygon.getFirstPoint()[0],
				polygon.getFirstPoint()[1], false);
		path.moveTo(first.getX(), first.getY());

		Log.debug("Body polygonu: " + points);

		for (int i = 0; i < points.length; i++) {
			Point2D p = getMapPosition(points[i+1], points[i++]);
			// TODO zvyrazeni bodu
			path.lineTo(p.getX(), p.getY());
		}

		// uzavreni polygonu
		path.closePath();

		g2.setPaint(Color.GREEN);
		g2.setStroke(stroke);
		Composite originComposite = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				(float) 0.5));
		g2.fill(path);
		g2.setComposite(originComposite);
		g2.draw(path);
	}

	protected void paintMultiPolygon(Graphics g, JEntity polygons) {
		// TODO Auto-generated method stub

	}

//	/**
//	 * Vykresleni jedne linestring
//	 * @param g graficky kontext
//	 * @param linestring samotna linestring
//	 */
//	protected void paintLinestring(Graphics g, List<MapMarker> linestring){
//		if(linestring != null){
//
//			Graphics2D g2 = (Graphics2D) g;
//
//			// inicializace linestring
//			GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, linestring.size());
//
//			// zakresli prvni bod
//			MapMarker firstMarker = linestring.get(0);
//			Point firstPoint = getMapPosition(firstMarker.getLat(), firstMarker.getLon(), false);
//			path.moveTo(firstPoint.x, firstPoint.y);
//			((MapPoint)firstMarker).paint(g2, firstPoint); // FIXME
//
//			// zbytek cary
//			for (MapMarker mapMarker : linestring) {
//				Point p = getMapPosition(mapMarker.getLat(), mapMarker.getLon(), false);
//				path.lineTo(p.x, p.y);
//				((MapPoint)mapMarker).paint(g2, p); // FIXME
//			}
//
//			g2.setColor(Color.GREEN);
//			g2.setStroke(stroke);
//			g2.draw(path);
//			repaint();
//		}
//	}

//	/**
//	 * Vykresleni jednoho polygonu
//	 * @param g graficky kontext
//	 * @param polygon samotny polygon
//	 */
//	protected void paintPolygon(Graphics g, List<MapMarker> polygon){
//		if(polygon != null){
//
//			Graphics2D g2 = (Graphics2D) g;
//
//			// inicializace linestring
//			GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, polygon.size());
//
//			// zakresli prvni bod
//			MapMarker firstMarker = polygon.get(0);
//			Point firstPoint = getMapPosition(firstMarker.getLat(), firstMarker.getLon(), false);
//			path.moveTo(firstPoint.x, firstPoint.y);
//
//			// zbytek cary
//			for (MapMarker mapMarker : polygon) {
//				Point p = getMapPosition(mapMarker.getLat(), mapMarker.getLon(), false);
//				path.lineTo(p.x, p.y);
//			}
//
//			// uzavreni polygonu
//			path.closePath();
//
//			g2.setPaint(Color.GREEN);
//			g2.setStroke(stroke);
//			Composite originComposite = g2.getComposite();
//			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 0.5));
//			g2.fill(path);
//			g2.setComposite(originComposite);
//			g2.draw(path);
//			repaint();
//		}
//	}

	public AnimalsDatabase getFrame() {
		return frame;
	}

	public JEntity getMyPosition() {
		return myPosition;
	}

	public void setMyPosition(JEntity myPosition) {
		this.myPosition = myPosition;
		repaint();
	}

	public boolean isEditMode() {
		return editMode;
	}

	/**
	 * Metoda zobrazujici a schovavajici komponenty pro editaci elementu
	 * @param visible zda zobrazit ci nezobrazit
	 */
	public void setEditMode(boolean visible){

		// mod
		editMode = visible;

		// komponenty
		bEdit.setVisible(!visible);
		bCancel.setVisible(visible);
		bSave.setVisible(visible);
		bNext.setVisible(visible);
		comboElements.setVisible(visible);
	}

	public int getDrawMode() {
		return drawMode;
	}

	public void setDrawMode(int drawMode) {
		this.drawMode = drawMode;
		comboElements.setSelectedIndex(drawMode);
	}

	public List<JEntity> getData() {
		return data;
	}

	public void setData(List<JEntity> data) {
		this.data = data;
	}

	public List<JEntity> getTempData() {
		return tempData;
	}

	public void setTempData(List<JEntity> tempData) {
		this.tempData = tempData;
	}
}
