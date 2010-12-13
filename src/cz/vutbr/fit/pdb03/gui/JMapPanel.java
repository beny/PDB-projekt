package cz.vutbr.fit.pdb03.gui;

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

	private final static int MY_POINT_SIZE = 8;
	private final static Color MY_POINT_COLOR = Color.RED;

	private final static int POINT_SIZE = 8;
	private final static Color POINT_COLOR = Color.GREEN;

	private final static Color POINT_SELECTED_COLOR = Color.CYAN;

	private final static Color CURVE_COLOR = Color.GREEN;
	private final static Color CURVE_SELECTED_COLOR = Color.CYAN;

	// konstanty akci
	public final static String ACTION_EDIT = "EDIT";
	public final static String ACTION_SAVE = "SAVE";
	public final static String ACTION_CHANGE_TYPE = "CHANGE";
	public final static String ACTION_CANCEL = "CANCEL";

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
	JButton bEdit, bSave, bCancel;
	private JComboBox comboElements;

	// detekce kolize
	private JEntity hitEntity;

	// data
	JEntity myPosition;	// moje poloha
	List<JEntity> data;	// puvodni data

	List<JEntity> insertData;	// nova data
	List<JEntity> updateData;	// zmenena data
	List<JEntity> deleteData;	// odstranene entity

	List<JEntity> tempDraw;	// docasne pole pro kresleni krivek a polygonu

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
		initEditButtons();

		// inicializace dat
		initData();

		// vycentrovani
		setDisplayPositionByLatLon(DEFAULT_LAT, DEFAULT_LON, DEFAULT_ZOOM);
	}


	/**
	 * Inicializace editacnich tlacitek
	 */
	private void initEditButtons(){

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

		setEditMode(false);
	}

	/**
	 * Prvnotni inicializace dat
	 */
	private void initData(){
		data = new LinkedList<JEntity>();
		insertData = new LinkedList<JEntity>();
		updateData = new LinkedList<JEntity>();
		deleteData = new LinkedList<JEntity>();
		tempDraw = new LinkedList<JEntity>();
		myPosition = new JEntity(DEFAULT_LAT, DEFAULT_LON);
	}

	/**
	 * Vycisteni pomocnych poli
	 */
	public void clearTempData(){
		insertData.clear();
		updateData.clear();
		deleteData.clear();
		tempDraw.clear();
	}


	/**
	 * Smaze z mapy vsechny data
	 */
	public void clearMap(){
		data.clear();
		clearTempData();
		repaint();
	}


	/**
	 * Detekce nejblizsiho bodu/entity
	 * @param clickedPoint
	 * @return
	 */
	public void detectHit(Point clickedPoint) {

		// pomocne promenne
		double minDistance = MAX_DISTANCE;
		double distance = 0;
		JEntity tempHit = null;
		hitEntity = null;

		// pro puvodni data
		for (JEntity entity : data) {
			distance = getMinDistance(entity, clickedPoint);

			// pokud je bliz, zvol tento
			if (distance < minDistance) {
				tempHit = entity;
				minDistance = distance;
			}
		}

		// pridana data
		for (JEntity entity : insertData) {
			distance = getMinDistance(entity, clickedPoint);

			// pokud je bliz, zvol tento
			if (distance < minDistance) {
				tempHit = entity;
				minDistance = distance;
			}
		}

		// zmenena data
		for (JEntity entity : updateData) {
			distance = getMinDistance(entity, clickedPoint);

			// pokud je bliz, zvol tento
			if (distance < minDistance) {
				tempHit = entity;
				minDistance = distance;
			}
		}

		// vyber nejblizsi
		if(tempHit != null){
			tempHit.setSelected(true);
			hitEntity = tempHit;
			repaint();
		}
	}

	/**
	 * Ziska vzdalenost nejblizsiho bodu dane entity k bodu. Na zaklade toho se
	 * pote vybere nejblizsi entita
	 *
	 * @param entity
	 * @param clickedPoint
	 * @return vzdalenost
	 */
	private double getMinDistance(JEntity entity, Point clickedPoint) {
		double distance = MAX_DISTANCE;
		double minDistance = MAX_DISTANCE;

		// predpokladejme ze neni hit
		entity.setSelected(false);

		// najdi nejblizsi bod jakekoliv entity
		switch (entity.getType()) {
		case JEntity.GTYPE_POINT:
			distance = entity.diffPoint(clickedPoint, this);
			Log.debug("testuju bod " + entity + " je vzdaleny " + distance);
			break;
		case JEntity.GTYPE_MULTIPOINT:
		case JEntity.GTYPE_CURVE:
		case JEntity.GTYPE_POLYGON:
			List<JEntity> points = JEntity.convert(entity.getOrdinatesArray());

			for (JEntity p : points) {
				distance = p.diffPoint(clickedPoint, this);
				if(distance < minDistance){
					minDistance = distance;
				}
			}
			distance = minDistance;
			break;
		case JEntity.GTYPE_MULTICURVE:
			List<JEntity> curves = JEntity.convertMulti(entity
					.getOrdinatesOfElements(), JEntity.GTYPE_MULTICURVE);
			for (JEntity curve : curves) {
				distance = getMinDistance(curve, clickedPoint);

				// pokud je bliz, zvol tento
				if (distance < minDistance) {
					minDistance = distance;
				}
			}
			distance = minDistance;
			break;
		case JEntity.GTYPE_MULTIPOLYGON:
			List<JEntity> polygons = JEntity.convertMulti(entity
					.getOrdinatesOfElements(), JEntity.GTYPE_MULTIPOLYGON);
			for (JEntity polygon : polygons) {
				distance = getMinDistance(polygon, clickedPoint);

				// pokud je bliz, zvol tento
				if (distance < minDistance) {
					minDistance = distance;
				}
			}
			distance = minDistance;
			break;
		}

		return distance;
	}

	/**
	 * Metoda disablujici tlacitka v mape
	 * @param enabled
	 */
	public void setEditButtonsEnabled(boolean enabled){
		bEdit.setEnabled(enabled);
		bSave.setEnabled(enabled);
		comboElements.setEnabled(enabled);
	}

	/**
	 * Metoda ktera naplni mapu z data z JGeometry
	 * @param data
	 */
	public void setMapData(List<JEntity> data){
		this.data = data;
		Log.debug("Nacteno " + data.size() + " geometrii");
		repaint();
	}

	/**
	 * Ukonci docasne kresleni a podle modu ulozi danou entitu do pole k ulozeni
	 */
	public void saveTempDraw() {

		if (tempDraw.size() > 0) {
			switch (drawMode) {
			case MODE_POINT:	// tohle by tu ciste teoreticky nemuselo byt
				break;
			case MODE_CURVE:
				JEntity curve = new JEntity(JEntity.createCurve(tempDraw));
				insertData.add(curve);
				tempDraw.clear();
				repaint();
				break;
			case MODE_POLYGON:
				JEntity polygon = new JEntity(JEntity.createPolygon(tempDraw));
				insertData.add(polygon);
				tempDraw.clear();
				repaint();
				break;
			}
		}
	}

	/**
	 * Pridavani bodu pri vkladani noveho elementu
	 * @param point
	 */
	public void addPoint(JEntity point){
		switch (drawMode) {
		case MODE_POINT: insertData.add(point);	break;
		case MODE_CURVE:
		case MODE_POLYGON:
			tempDraw.add(point);
			break;
		}

		repaint();
	}

	/**
	 * Presun polozky mezi upravene
	 * @param entity
	 */
	public void updateEntity(JEntity entity){
		data.remove(entity);
		insertData.remove(entity);
		updateData.add(entity);
		repaint();
	}

	/**
	 * Vlozeni entity do mazani
	 * @param entity
	 */
	public void deleteEntity(JEntity entity){
		data.remove(entity);
		insertData.remove(entity);
		updateData.remove(entity);
		deleteData.add(entity);

		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// puvodni data
		for (JEntity entity : data) {
			paintEntity(g, entity);
		}

		// pokud je v editacnim modu, kresli i nove vykreslene body
		if (isEditMode()) {
			// nove vlozene
			for (JEntity entity : insertData) {
				paintEntity(g, entity);
			}

			// updatovane
			for (JEntity entity : updateData) {
				paintEntity(g, entity);
			}
		}

		// vykresli moji polohu
		paintMyPoint(g, myPosition);

		// vykresli docasne kresleni
		paintTemp(g);
	}

	/**
	 * Vyber jaka entita se ma kreslit
	 * @param g
	 * @param entity
	 */
	protected void paintEntity(Graphics g, JEntity entity){
		switch (entity.getType()) {
		case JEntity.GTYPE_POINT: paintPoint(g, entity); break;
		case JEntity.GTYPE_MULTIPOINT: paintMultiPoint(g, entity); break;
		case JEntity.GTYPE_CURVE: paintCurve(g, entity); break;
		case JEntity.GTYPE_MULTICURVE: paintMultiCurve(g, entity); break;
		case JEntity.GTYPE_POLYGON: paintPolygon(g, entity); break;
		case JEntity.GTYPE_MULTIPOLYGON: paintMultiPolygon(g, entity); break;
		}
	}

	/**
	 * Vykresleni bodu s moji pozici
	 * @param g
	 * @param myPoint
	 */
	protected void paintMyPoint(Graphics g, JEntity myPoint) {
		Graphics2D g2 = (Graphics2D) g;
		Point2D p = myPoint.getJavaPoint();
		Point mp = getMapPosition(p.getX(), p.getY(), false);

		g2.setColor(MY_POINT_COLOR);
		g2.fillOval(mp.x - MY_POINT_SIZE / 2, mp.y - MY_POINT_SIZE / 2,
				MY_POINT_SIZE, MY_POINT_SIZE);
	}

	/**
	 * Vykresleni docasne krivky
	 * @param g
	 */
	protected void paintTemp(Graphics g) {

		Graphics2D g2 = (Graphics2D) g;

		int i = 0;

		GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD,
				tempDraw.size());
		for (JEntity point : tempDraw) {
			Point p = getMapPosition(point.getLat(), point.getLon(), false);

			paintPoint(g, point);
			if(i == 0){
				path.moveTo(p.getX(), p.getY());
			}
			path.lineTo(p.getX(), p.getY());
			i++;
		}

		g2.setColor(CURVE_COLOR);
		g2.setStroke(stroke);
		g2.draw(path);
	}


	/**
	 * Vykresleni bodu
	 * @param g
	 * @param point
	 */
	protected void paintPoint(Graphics g, JEntity point){
		Graphics2D g2 = (Graphics2D) g;
		Point2D p = point.getJavaPoint();
		Point mp = getMapPosition(p.getX(), p.getY(), false);

		// pokud bod vybran
		if(point.isSelected()){
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

		List<JEntity> data = JEntity.convert(points.getOrdinatesArray());
		for (JEntity point : data) {
			point.setSelected(points.isSelected());	// pripadne oznaceni bodu
			paintPoint(g, point);
		}
	}

	/**
	 * Vykresleni krivky
	 * @param g
	 * @param curve
	 */
	protected void paintCurve(Graphics g, JEntity curve) {

		Graphics2D g2 = (Graphics2D) g;

		List<JEntity> points = JEntity.convert(curve.getOrdinatesArray());

		int i = 0;

		GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD,
				points.size());
		for (JEntity point : points) {
			Point p = getMapPosition(point.getLat(), point.getLon(), false);

			if(isEditMode()){
				point.setSelected(curve.isSelected());
				paintPoint(g, point);
			}
			if(i == 0){
				path.moveTo(p.getX(), p.getY());
			}
			path.lineTo(p.getX(), p.getY());
			i++;
		}

		g2.setColor(curve.isSelected()?CURVE_SELECTED_COLOR:CURVE_COLOR);
		g2.setStroke(stroke);
		g2.draw(path);
	}

	/**
	 * Vykresleni multicurve
	 * @param g
	 * @param curves
	 */
	protected void paintMultiCurve(Graphics g, JEntity curves) {

		List<JEntity> cs = curves.getCurves();

		for (JEntity c : cs) {
			c.setSelected(curves.isSelected());
			paintCurve(g, c);
		}
	}

	/**
	 * Vykresleni polygonu
	 * @param g
	 * @param map
	 */
	protected void paintPolygon(Graphics g, JEntity polygon) {
		Graphics2D g2 = (Graphics2D) g;

		List<JEntity> points = JEntity.convert(polygon.getOrdinatesArray());

		int i = 0;

		GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD,
				points.size());
		for (JEntity point : points) {
			Point p = getMapPosition(point.getLat(), point.getLon(), false);

			if(isEditMode()){
				point.setSelected(polygon.isSelected());
				paintPoint(g, point);
			}
			if(i == 0){
				path.moveTo(p.getX(), p.getY());
			}
			path.lineTo(p.getX(), p.getY());
			i++;
		}

		path.closePath();

		g2.setColor(polygon.isSelected()?CURVE_SELECTED_COLOR:CURVE_COLOR);

		Composite originComposite = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				(float) 0.5));
		g2.fill(path);
		g2.setComposite(originComposite);
		g2.setStroke(stroke);
		g2.draw(path);
	}

	/**
	 * Vykresleni multipolygonu
	 * @param g
	 * @param polygons
	 */
	protected void paintMultiPolygon(Graphics g, JEntity polygons) {
		List<JEntity> ps = polygons.getPolygons();

		for (JEntity p : ps) {
			p.setSelected(polygons.isSelected());
			paintPolygon(g, p);

			// TODO linearizace polygonu za sebe
		}
	}

	public AnimalsDatabase getFrame() {
		return frame;
	}

	public JEntity getMyPosition() {
		return myPosition;
	}

	public void setMyPosition(JEntity myPosition) {
		this.myPosition = myPosition;

		// obnova zvirete
		frame.getAnimalsPanel().updateAnimalSpatialData();
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

		setEditButtonsEnabled(true);

		// mod
		editMode = visible;

		// komponenty
		bEdit.setVisible(!visible);
		bCancel.setVisible(visible);
		bSave.setVisible(visible);
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

	public List<JEntity> getInsertData() {
		return insertData;
	}

	public List<JEntity> getUpdateData() {
		return updateData;
	}

	public List<JEntity> getDeleteData() {
		return deleteData;
	}

	public void setTempData(List<JEntity> tempData) {
		insertData = tempData;
	}

	public JEntity getHitEntity() {
		return hitEntity;
	}
}
