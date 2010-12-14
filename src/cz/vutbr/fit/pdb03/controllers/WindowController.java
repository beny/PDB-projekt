package cz.vutbr.fit.pdb03.controllers;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import cz.vutbr.fit.pdb03.AnimalsDatabase;

/**
 * Kontroler zajistujici ovladani okenich akci
 * @author Pavel Srnec <xsrnec01@stud.fit.vutbr.cz>
 *
 */
public class WindowController extends WindowAdapter {

	AnimalsDatabase frame;

	public WindowController(AnimalsDatabase frame) {
		this.frame = frame;

		// nastaveni listneru
		frame.addWindowListener(this);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		// vlastnosti okna
		frame.setMinimumSize(new Dimension(800, 600));
	}

	@Override
	public void windowClosing(WindowEvent event) {
		frame.exitApp();
	}

}
