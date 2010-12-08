package cz.vutbr.fit.pdb03.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.map.MapPoint;

/**
 * Dialog s nastavenim
 *
 *
 */
public class PreferencesDialog extends DefaultDialog implements ActionListener {

	private final static long serialVersionUID = 2726995694418479544L;

	private final static int MAX_COORDS = 12;
	private final static int MAX_DATE = 10;

	private AnimalsDatabase frame;

	private JPanel pGPS, pTime;
	private JButton bCancel, bSave;
	private JLabel lLat, lLon, lFrom, lTo;
	private JTextField tLat, tLon, tDate, tFrom, tTo;
	private JRadioButton rbNow, rbData, rbInterval, rbAll;


	public PreferencesDialog(AnimalsDatabase frame) {
		super();
		this.frame = frame;

		// tab GPS
		initGPSTab();

		// tab Cas
		initTimeTab();

		JPanel pContent = new JPanel();

		pContent.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		// taby
		JTabbedPane tpSections = new JTabbedPane();
		tpSections.addTab("GPS", pGPS);
		tpSections.addTab("Čas", pTime);

		// tlacitka
		bCancel = new JButton("Cancel");
		bCancel.addActionListener(this);
		bSave = new JButton("Save");
		bSave.addActionListener(this);

		JPanel buttons = new JPanel();
		buttons.add(bCancel);
		buttons.add(bSave);

		// rozmisteni prvku
		gbc.gridx = gbc.gridy = 0;
		pContent.add(tpSections, gbc);

		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.gridy++;
		pContent.add(buttons, gbc);

		// nastaveni udaju o moji pozici
		setMyPosition(frame.getMap().getMyPosition());

		setContentPane(pContent);
		pack();
	}

	private void initTimeTab() {
		pTime = new JPanel();

		pTime.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = gbc.gridy = 0;

		rbNow = new JRadioButton("Nyní");
		rbData = new JRadioButton("Konkrétní datum");
		rbInterval = new JRadioButton("V intervalu");
		rbAll = new JRadioButton("Bez omezení");
		rbAll.setSelected(true);

		lFrom = new JLabel("Od:");
		lTo = new JLabel("Do:");

		tDate = new JTextField(MAX_DATE);
		tFrom = new JTextField(MAX_DATE);
		tTo = new JTextField(MAX_DATE);

		// seskupeni
		ButtonGroup bg = new ButtonGroup();
		bg.add(rbNow);
		bg.add(rbData);
		bg.add(rbInterval);
		bg.add(rbAll);

		// pridani na panel
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		pTime.add(rbNow, gbc);

		gbc.gridy++;
		pTime.add(rbData, gbc);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.gridx = 2;
		pTime.add(tDate, gbc);

		gbc.gridwidth = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		pTime.add(rbInterval, gbc);
		gbc.gridx = 1;
		pTime.add(lFrom, gbc);
		gbc.gridx = 2;
		pTime.add(tFrom, gbc);
		gbc.gridx = 3;
		pTime.add(lTo, gbc);
		gbc.gridx = 4;
		pTime.add(tTo, gbc);

		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.gridy++;
		gbc.gridx = 0;
		pTime.add(rbAll, gbc);



	}

	private void initGPSTab(){
		pGPS = new JPanel();

		pGPS.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 0;
		gbc.insets = new Insets(5, 5, 5, 5);
		lLat = new JLabel("Latitude: ");
		pGPS.add(lLat, gbc);

		gbc.gridy++;

		lLon = new JLabel("Longtitude: ");
		pGPS.add(lLon, gbc);

		gbc.gridy = 0;
		gbc.gridx = 1;

		tLat = new JTextField(MAX_COORDS);
		pGPS.add(tLat, gbc);

		gbc.gridy = 1;

		tLon = new JTextField(MAX_COORDS);
		pGPS.add(tLon, gbc);
	}

	public void setMyPosition(MapMarker myPosition){
		tLat.setText(myPosition.getLat() + "");
		tLon.setText(myPosition.getLon() + "");
	}

	public MapMarker getMyPosition() throws NumberFormatException{
		// TODO nejaka kontrola
		return new MapPoint(Double.parseDouble(tLat.getText()), Double.parseDouble(tLon.getText()), MapPoint.counter);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		// zmacknuto cancel
		if(e.getSource() == bCancel){
			dispose();
		}

		// zmacknuto save
		if(e.getSource() == bSave){

			try {
				frame.getMap().setMyPosition(getMyPosition());
				dispose();
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this,
						"Souřadnice nejsou ve správném formátu", "Chyba údajů",
						JOptionPane.ERROR_MESSAGE);
			}
			// TODO ulozeni nastaveni casu

		}
	}
}
