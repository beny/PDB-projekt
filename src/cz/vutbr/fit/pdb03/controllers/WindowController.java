package cz.vutbr.fit.pdb03.controllers;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

import javax.swing.JFrame;

import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.DataBase;

/**
 * Kontroler zajistujici ovladani okenich akci
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
public class WindowController extends WindowAdapter {

	private DataBase db;
	AnimalsDatabase frame;

	public WindowController(AnimalsDatabase frame) {
		this.frame = frame;
		db = frame.getDb();

		// nastaveni listneru
		frame.addWindowListener(this);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		// vlastnosti okna
		frame.setMinimumSize(new Dimension(800, 600));
	}

	@Override
	public void windowClosing(WindowEvent event) {

		if (db.isConnected()) {
			try {
				db.disconnect();

				// DEBUG
				System.out.println("Disconnected");
			} catch (SQLException e) {
				System.err
						.println("Error while disconnection from DB: " + e.getMessage());
			}
		}

		frame.setVisible(false);
		frame.dispose();
	}

}
