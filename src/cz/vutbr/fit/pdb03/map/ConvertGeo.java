package cz.vutbr.fit.pdb03.map;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import oracle.spatial.geometry.JGeometry;

import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

import cz.vutbr.fit.pdb03.DataBase;
import cz.vutbr.fit.pdb03.Log;

/**
 * Trida konvertujici moji implementaci souradnic do JGeometry
 *
 */
public class ConvertGeo {

	/**
	 * Vytvoreni polygonu ze zadaneho listu markeru
	 * @param list
	 * @return polygon v JGeometry
	 */
	public static JGeometry createPolygon(List<MapMarker> list) {
		double[] coords = new double[list.size() * 2];

		int i = 0;
		for (MapMarker mapMarker : list) {
			coords[i++] = mapMarker.getLat();
			coords[i++] = mapMarker.getLon();
		}

		// vytvoreni polygonu
		JGeometry geometry = JGeometry.createLinearPolygon(coords,
				DataBase.DIMENSIONS, DataBase.SRID);

		return geometry;
	}

	/**
	 * Prevod JGeometry na polygon pro mapu
	 * @param geometry
	 * @return
	 */
	public static List<MapMarker> createPolygon(JGeometry geometry){
		double[] points = geometry.getOrdinatesArray();

		// prevod geometrie na polygon
		ArrayList<MapMarker> polygon = new ArrayList<MapMarker>();
		for (int i = 0; i < points.length; i++) {
			double x = points[i++];
			double y = points[i];
			MapPoint tmpPoint = new MapPoint(x, y, MapPoint.counter);
			polygon.add(tmpPoint);
		}

		return polygon;
	}

	/**
	 * Vytvoreni JGeometry pro linestring
	 * @param list
	 * @return JGeometry pro zadanou linestring
	 */
	public static JGeometry createLinestring(List<MapMarker> list){
		double[] coords = new double[list.size() * 2];

		int i = 0;
		for (MapMarker mapMarker : list) {
			coords[i++] = mapMarker.getLat();
			coords[i++] = mapMarker.getLon();
		}

		// vytvoreni polygonu
		JGeometry geometry = JGeometry.createLinearLineString(coords,
				DataBase.DIMENSIONS, DataBase.SRID);

		return geometry;
	}

	/**
	 * Vytvoreni linestring z JGeometry
	 * @param geometry
	 * @return
	 */
	public static List<MapMarker> createLinestring(JGeometry geometry) {
		return ConvertGeo.createPolygon(geometry);
	}

	/**
	 * Vytvor bod ulozitelny do DB
	 * @param list seznam bodu z mapy
	 * @return JGeometry vyjadrujici seznam
	 */
	public static JGeometry createPoint(List<MapMarker> list) {
		JGeometry geometry = null;

		double[] coords = new double[list.size() * 2];

		if(list.size() == 1){
			MapMarker point = list.get(0);
			coords[0] = point.getLat();
			coords[1] = point.getLon();
			geometry = JGeometry.createPoint(coords, DataBase.DIMENSIONS, DataBase.SRID);
		} else if(list.size() > 1){

			// TODO
//			int i = 0;
//			for (MapMarker mapMarker : list) {
//				coords[i++] = mapMarker.getLat();
//				coords[i++] = mapMarker.getLon();
//			}
//
//			geometry = JGeometry.createMultiPoint(coords, DataBase.DIMENSIONS, DataBase.SRID);
		}
		return geometry;
	}

	/**
	 * Vytvor bod, zobrazitelny na mape
	 * @param geometry JGeometry vyjadrujici bod
	 * @return bod zobrazitelny na mape
	 */
	public static MapMarker createPoint(JGeometry geometry) {

		double[] points = geometry.getPoint();
		MapMarker mapMarker = new MapPoint(points[0], points[1], MapPoint.counter);
		return mapMarker;
	}


}
