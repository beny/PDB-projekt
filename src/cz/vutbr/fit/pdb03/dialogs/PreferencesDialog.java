package cz.vutbr.fit.pdb03.dialogs;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

/**
 * Dialog s nastavenim
 *
 *
 */
public class PreferencesDialog extends JDialog {

	private final static long serialVersionUID = 2726995694418479544L;

	private final static int MAX_COORDS = 12;
	private final static int MAX_DATE = 10;

	private JPanel pGPS, pTime;
	private JButton bCancel, bSave;
	private JLabel lLat, lLon, lFrom, lTo;
	private JTextField tLat, tLon, tDate, tFrom, tTo;
	private JRadioButton rbNow, rbData, rbInterval, rbAll;


	public PreferencesDialog() {

		// vlastnosti okna
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(true);
		setMinimumSize(new Dimension(500, 270));
		setPreferredSize(new Dimension(500, 270));
		setResizable(false);

		// tab GPS
		initGPSTab();

		// tab Cas
		initTimeTab();

		// taby
		JTabbedPane tpSections = new JTabbedPane();
		tpSections.addTab("GPS", pGPS);
		tpSections.addTab("Čas", pTime);

		// tlacitka
		bCancel = new JButton("Cancel");
		bSave = new JButton("Save");

		JPanel buttons = new JPanel();
		buttons.add(bCancel);
		buttons.add(bSave);

		// layout
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = gbc.gridy = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;

		add(tpSections, gbc);

		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.gridy++;
		add(buttons, gbc);
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
}
