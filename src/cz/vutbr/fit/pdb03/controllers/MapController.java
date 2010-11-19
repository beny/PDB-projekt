package cz.vutbr.fit.pdb03.controllers;

import java.awt.event.MouseEvent;

import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;

public class MapController extends DefaultMapController{

	public MapController(JMapViewer map) {
		super(map);
	}


	@Override
	public void mouseClicked(MouseEvent e) {
		super.mouseClicked(e);

		System.out.println("Kliknuto do mapy"); // DEBUG
	}
}
