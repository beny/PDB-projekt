package cz.vutbr.fit.pdb03;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.OsmTileSource;

public class AnimalsDatabase extends JFrame {

	public AnimalsDatabase(String title) {
		super(title);

		// set window properties
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(MAXIMIZED_BOTH);
		setLayout(new GridBagLayout());

		// create constrains for layour
		GridBagConstraints c = new GridBagConstraints();

		// few help panels
		JPanel redPanel = new JPanel();
		redPanel.setBackground(Color.RED);
		redPanel.add(new JLabel("Picture panel"));

		JPanel bluePanel = new JPanel();
		bluePanel.setBackground(Color.BLUE);

		JPanel greenPanel = new JPanel();
		greenPanel.setBackground(Color.GREEN);

		JPanel yellowPanel = new JPanel();
		yellowPanel.setBackground(Color.YELLOW);

		JPanel cyanPanel = new JPanel();
		cyanPanel.setBackground(Color.CYAN);
		cyanPanel.add(new JLabel("Info panel"));

		// main three panels

		// picture panel
		JTabbedPane picturesPanel = new JTabbedPane();
		picturesPanel.addTab("Red", redPanel);
		picturesPanel.addTab("Blue", bluePanel);
		picturesPanel.addTab("Green", greenPanel);

		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.6;
		c.weighty = 0.5;
		c.fill = GridBagConstraints.BOTH;
		add(picturesPanel, c);

		// map panel
		JMapViewer map = new JMapViewer();
		map.setPreferredSize(null);
		map.setTileSource(new OsmTileSource.CycleMap());
		map.setTileLoader(new OsmTileLoader(map));
		c.gridy = 1;
		add(map, c);

		// info panel
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 0.3;
		c.weighty = 1;
		c.gridheight = GridBagConstraints.REMAINDER;
		add(cyanPanel, c);


	}

	public static void main(String[] args) {
		AnimalsDatabase aDb = new AnimalsDatabase("Animals database");
		aDb.setVisible(true);
	}

}
