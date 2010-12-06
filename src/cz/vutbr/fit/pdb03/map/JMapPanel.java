package cz.vutbr.fit.pdb03.map;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;

import oracle.spatial.geometry.JGeometry;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.OsmTileSource;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.MapRectangle;

import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.DataBase;
import cz.vutbr.fit.pdb03.Log;
import cz.vutbr.fit.pdb03.controllers.MapController;

/**
 * Trida rozsirujici moznosti zakladni mapy
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
public class JMapPanel extends JMapViewer {

	private static final long serialVersionUID = -7269660504108541606L;

	// konstanty akci
	public final static String ACTION_EDIT = "EDIT";
	public final static String ACTION_SAVE = "SAVE";
	public final static String ACTION_CHANGE_TYPE = "CHANGE";

	public final static int MODE_POINT = 0;
	public final static int MODE_LINESTRING = 1;
	public final static int MODE_POLYGON = 2;

	// hlavni frame
	AnimalsDatabase frame;

	// kontroler
	MapController mapController;

	// databaze
	private DataBase db;

	// indikace editacniho modu
	private boolean editMode = false;

	// druh akce ktera se tedka bude delat
	private int mode = -1;

	// komponenta pro mapu
	JButton editButton, saveButton;
	private JComboBox comboElements;

	// data
	List<List<MapMarker>> mapLinestringList;	// mnozina linestring
	List<List<MapMarker>> mapPolygonList;	// mnozina polygonu

	public JMapPanel(AnimalsDatabase frame) {
		super(new MemoryTileCache(), 4);

		// hlavni frame
		this.frame = frame;

		// kontrolery
		mapController = new MapController(this);

		// databaze
		db = frame.getDb();

		// vlastnosti mapy
		setPreferredSize(null);
		setTileSource(new OsmTileSource.CycleMap());
		setTileLoader(new OsmTileLoader(this));

		// inicializace tlacitek
		initializeEditButtons();

		// inicializace datovych slozek
		mapLinestringList = new LinkedList<List<MapMarker>>();
		mapPolygonList = new LinkedList<List<MapMarker>>();
	}

	/**
	 * Inicializace editacnich tlacitek
	 */
	protected void initializeEditButtons(){

		int buttonSizeX = 50;
		int buttonSizeY = 20;
		int smallSpace = 10;

		// edit tlacitko
		editButton = new JButton("edit");
		editButton.setBounds(50, smallSpace, buttonSizeX, buttonSizeY);
		editButton.setActionCommand(ACTION_EDIT);
		editButton.addActionListener(mapController);
		add(editButton);

		// komponenty pro editaci

		// tlacitko pro ukladani
		saveButton = new JButton("save");
		saveButton.setBounds(50, smallSpace, buttonSizeX, buttonSizeY);
		saveButton.setActionCommand(ACTION_SAVE);
		saveButton.addActionListener(mapController);
		add(saveButton);

		// kombo pro vyber elementu
		String[] elements = {"Výskyt", "Trasa", "Území"};
		comboElements = new JComboBox(elements);
		comboElements.setBounds(50 + buttonSizeX + smallSpace, smallSpace, 120, buttonSizeY);
		comboElements.setActionCommand(ACTION_CHANGE_TYPE);
		setMode(MODE_POINT);
		comboElements.addActionListener(mapController);
		add(comboElements);

		setEditMode(false);
	}

	/**
	 * Smaze z mapy vsechny data
	 */
	public void clear(){
		setMapLinestringList(new LinkedList<List<MapMarker>>());
		setMapMarkerList(new LinkedList<MapMarker>());
		setMapPolygonList(new LinkedList<List<MapMarker>>());
		setMapRectangleList(new LinkedList<MapRectangle>());
		mapController.clearMap();
	}

	/**
	 * Metoda ktera naplni mapu z data z JGeometry
	 * @param data
	 */
	public void setMapData(Map<Integer, JGeometry> data){

		clear();
		for (Map.Entry<Integer, JGeometry> entry : data.entrySet()){
		    Log.debug("Geometrie s ID " + entry.getKey() + " je : " + entry.getValue());

		    JGeometry geometry = entry.getValue();

		    switch (geometry.getType()) {
			case JGeometry.GTYPE_POINT:
				addMapMarker(ConvertGeo.createPoint(geometry));
				Log.debug("Geometrie je bod");
				break;
			case JGeometry.GTYPE_CURVE:
				addMapLinestring(ConvertGeo.createLinestring(geometry));
				Log.debug("Geometrie je krivka");
				break;
			case JGeometry.GTYPE_POLYGON:
				addMapPolygon(ConvertGeo.createPolygon(geometry));
				break;
			case JGeometry.GTYPE_MULTIPOINT:
				// TODO
				Log.debug("Geometrie je mnozina bodu");
				break;
			case JGeometry.GTYPE_MULTICURVE:
				// TODO
				Log.debug("Geometrie je mnozina krivek");
				break;
			case JGeometry.GTYPE_MULTIPOLYGON:
				// TODO
				Log.debug("Geometrie je mnozina polygonu");
				break;

			default:
				break;
			}


		}
	}


	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		for (List<MapMarker> linestring : mapLinestringList) {
			paintLinestring(g, linestring);
		}

		for (List<MapMarker> polygon : mapPolygonList) {
			paintPolygon(g, polygon);
		}
	}

	/**
	 * Vykresleni jedne linestring
	 * @param g graficky kontext
	 * @param linestring samotna linestring
	 */
	protected void paintLinestring(Graphics g, List<MapMarker> linestring){
		if(linestring != null){

			Graphics2D g2 = (Graphics2D) g;

			// inicializace linestring
			GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, linestring.size());

			// zakresli prvni bod
			MapMarker firstMarker = linestring.get(0);
			Point firstPoint = getMapPosition(firstMarker.getLat(), firstMarker.getLon(), false);
			path.moveTo(firstPoint.x, firstPoint.y);

			// zbytek cary
			for (MapMarker mapMarker : linestring) {
				Point p = getMapPosition(mapMarker.getLat(), mapMarker.getLon(), false);
				path.lineTo(p.x, p.y);
			}

			g2.draw(path);
		}
	}

	/**
	 * Vykresleni jednoho polygonu
	 * @param g graficky kontext
	 * @param polygon samotny polygon
	 */
	protected void paintPolygon(Graphics g, List<MapMarker> polygon){
		if(polygon != null){

			Graphics2D g2 = (Graphics2D) g;

			// inicializace linestring
			GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, polygon.size());

			// zakresli prvni bod
			MapMarker firstMarker = polygon.get(0);
			Point firstPoint = getMapPosition(firstMarker.getLat(), firstMarker.getLon(), false);
			path.moveTo(firstPoint.x, firstPoint.y);

			// zbytek cary
			for (MapMarker mapMarker : polygon) {
				Point p = getMapPosition(mapMarker.getLat(), mapMarker.getLon(), false);
				path.lineTo(p.x, p.y);
			}

			// uzavreni polygonu
			path.closePath();

			Composite originComposite = g2.getComposite();
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 0.5));
			g2.setPaint(Color.BLUE);
			g2.fill(path);
			g2.setComposite(originComposite);
			g2.setPaint(Color.BLACK);
			g2.draw(path);
		}
	}

	public AnimalsDatabase getFrame() {
		return frame;
	}

	public void addMapLinestring(List<MapMarker> linestring){
		mapLinestringList.add(linestring);
		repaint();
	}

	public void removeMapLinestring(List<MapMarker> linestring){
		mapLinestringList.remove(linestring);
		repaint();
	}

	public List<List<MapMarker>> getMapLinestringList() {
		return mapLinestringList;
	}

	public void setMapLinestringList(List<List<MapMarker>> mapLinestringList) {
		this.mapLinestringList = mapLinestringList;
		repaint();
	}

	public void addMapPolygon(List<MapMarker> polygon){
		mapPolygonList.add(polygon);
		repaint();
	}

	public void removeMapPolygon(List<MapMarker> polygon){
		mapPolygonList.remove(polygon);
		repaint();
	}

	public List<List<MapMarker>> getMapPolygonList() {
		return mapPolygonList;
	}

	public void setMapPolygonList(List<List<MapMarker>> mapPolygonList) {
		this.mapPolygonList = mapPolygonList;
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
		editButton.setVisible(!visible);
		saveButton.setVisible(visible);
		comboElements.setVisible(visible);
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
		comboElements.setSelectedIndex(mode);
	}
}
