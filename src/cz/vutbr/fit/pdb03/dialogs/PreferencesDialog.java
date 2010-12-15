package cz.vutbr.fit.pdb03.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.Log;
import cz.vutbr.fit.pdb03.T2SQL;
import cz.vutbr.fit.pdb03.gui.JCal;
import cz.vutbr.fit.pdb03.gui.JEntity;

/**
 * Dialog s nastavenim
 * @author Ondřej Beneš <xbenes00@stud.fit.vutbr.cz>
 *
 */
public class PreferencesDialog extends DefaultDialog implements ActionListener {

	private final static long serialVersionUID = 2726995694418479544L;

	private final static int MAX_COORDS = 12;

	private AnimalsDatabase frame;

	private JPanel pGPS, pTime;
	private JButton bCancel, bSave;
	private JLabel lLat, lLon, lFrom, lTo;
	private JTextField tLat, tLon;
	private JRadioButton rbNow, rbDate, rbInterval, rbAll;
	private JCal calDate, calFrom, calTo;

	DateFormat format = new SimpleDateFormat("dd-MM-yyyy");


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
		bCancel = new JButton("Storno");
		bCancel.addActionListener(this);
		bSave = new JButton("OK");
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

		// nastaveni udaju o moji pozici a datech
		setMyPosition(frame.getMap().getMyPosition());
		initData();

		setContentPane(pContent);
		pack();
	}

	/**
	 * Naplneni daty
	 */
	private void initData(){

		if(T2SQL.getMode().equals(T2SQL.NOW)){
			rbNow.setSelected(true);
		}
		else if(T2SQL.getMode().equals(T2SQL.NO_RESTRICTIONS)){
			rbAll.setSelected(true);
		}
		else if(T2SQL.getMode().equals(T2SQL.DATETIME)){
			rbDate.setSelected(true);

			if(T2SQL.getValidationDateFrom() != null){
				calDate.setDate(T2SQL.getValidationDateFrom());
			}
		}
		else if(T2SQL.getMode().equals(T2SQL.INTERVAL)){
			rbInterval.setSelected(true);

			if(T2SQL.getValidationDateFrom() != null){
				calFrom.setDate(T2SQL.getValidationDateFrom());
			}

			if(T2SQL.getValidationDateTo() != null){
				calTo.setDate(T2SQL.getValidationDateTo());
			}
		}
	}

	private void initTimeTab() {
		pTime = new JPanel();
                pTime.setToolTipText("Temporální nastavení systému - omezuje výskyt zvěře na určité časové období.");
		pTime.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = gbc.gridy = 0;

		rbNow = new JRadioButton("Nyní");
                rbNow.setToolTipText("<html>Zobrazí právě platný výskyt zvířecího druhu.<br>Upravený výskyt platí od nynějška dále.<br>Smazaný výskyt je smazán od nynějška dále.</html>");
		rbDate = new JRadioButton("Konkrétní datum");
                rbDate.setToolTipText("<html>Zobrazí platný výskyt druhu zvěře k zadanému datu.<br>Upravený výskyt platí od zadaného data dále.<br>Smazaný výskyt platí od zadaného data dále.</html>");
		rbInterval = new JRadioButton("V intervalu");
                rbInterval.setToolTipText("<html>Zobrazí, kde se druh zvěře vyskytoval někdy v zadaném časovém intervalu.<br>Upravený výskyt má platnost pouze pro tento interval.<br>Smazaný výsket je smazán pouze pro tento interval.</html>");
		rbAll = new JRadioButton("Bez omezení");
                rbAll.setToolTipText("<html>Zobrazí všechny výskyty druhu zvěře zadané v systému.<br>Upravený výskyt má platnost po celou dobu.<br>Smazaný výskyt je úplně vymazán.</html>");

		lFrom = new JLabel("Od:");
		lTo = new JLabel("Do:");

		calDate = new JCal();
		calFrom = new JCal();
		calTo = new JCal();

		// nastaveni dat
		// seskupeni
		ButtonGroup bg = new ButtonGroup();
		bg.add(rbNow);
		bg.add(rbDate);
		bg.add(rbInterval);
		bg.add(rbAll);

		// pridani na panel
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		pTime.add(rbNow, gbc);

		gbc.gridy++;
		pTime.add(rbDate, gbc);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.gridx = 2;
		pTime.add(calDate, gbc);

		gbc.gridwidth = 1;
		gbc.gridy++;
		gbc.gridx = 0;
		pTime.add(rbInterval, gbc);
		gbc.gridx = 1;
		pTime.add(lFrom, gbc);
		gbc.gridx = 2;
		pTime.add(calFrom, gbc);
		gbc.gridx = 3;
		pTime.add(lTo, gbc);
		gbc.gridx = 4;
		pTime.add(calTo, gbc);

		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.gridy++;
		gbc.gridx = 0;
		pTime.add(rbAll, gbc);



	}

	private void initGPSTab(){
		pGPS = new JPanel();
                pGPS.setToolTipText("<html>GPS souřadnice uživatele - lze zadat zde, nebo kliknutím do mapy v needitovatelném stavu.<br>V mapě je pozice uživatele zobrazena jako červený bod.</html>");
		pGPS.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 0;
		gbc.insets = new Insets(5, 5, 5, 5);
		lLat = new JLabel("Zeměpisná šířka: ");
		pGPS.add(lLat, gbc);

		gbc.gridy++;

		lLon = new JLabel("Zeměpisná délka: ");
		pGPS.add(lLon, gbc);

		gbc.gridy = 0;
		gbc.gridx = 1;

		tLat = new JTextField(MAX_COORDS);
		pGPS.add(tLat, gbc);

		gbc.gridy = 1;

		tLon = new JTextField(MAX_COORDS);
		pGPS.add(tLon, gbc);
	}

	/**
	 * Metoda po zmacknuti ulozeni
	 */
	private void save(){

		boolean error = false;

		// gps
		try {
			frame.getMap().setMyPosition(getMyPosition());
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(this,
					"Souřadnice nejsou ve správném formátu (XX.XXXX)",
					"Chyba údajů", JOptionPane.ERROR_MESSAGE);
			error = true;
		}

		// cas
		if (rbNow.isSelected()) {
			T2SQL.setCurrentTime();
		} else if (rbInterval.isSelected()) {
			try {
				Date from = calFrom.getDate();
				Date to = calTo.getDate();

				if (from.compareTo(to) > 0) {
					throw new Exception();
				}
				T2SQL.setValidationDates(from, to);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this,
						"Datum od není před datem do", "Chybné datum",
						JOptionPane.ERROR_MESSAGE);
				error = true;
			}
		} else if (rbAll.isSelected()) {
			T2SQL.setNoTemporalRestrictions();
		} else if (rbDate.isSelected()) {
			Date date = calDate.getDate();
			T2SQL.setValidationDate(date);
			dispose();
		}

		if(!error){
			Log.debug("Spatial data changed");
			frame.getDb().releaseCacheOnSpatialChange();
			frame.getAnimalsPanel().updateAnimalSpatialData();
			frame.reloadAnimalsList(AnimalsDatabase.SEARCH_ALL);
			dispose();
		}

	}

	public void setMyPosition(JEntity myPosition){
		tLat.setText(myPosition.getLat() + "");
		tLon.setText(myPosition.getLon() + "");
	}

	public JEntity getMyPosition() throws NumberFormatException {
		return new JEntity(Double.parseDouble(tLat.getText()),
				Double.parseDouble(tLon.getText()));
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		// zmacknuto cancel
		if(e.getSource() == bCancel){
			dispose();
		}

		// zmacknuto save
		if(e.getSource() == bSave){
			save();
		}
	}
}
