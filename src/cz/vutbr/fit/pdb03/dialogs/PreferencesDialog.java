package cz.vutbr.fit.pdb03.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.ParseException;
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
import cz.vutbr.fit.pdb03.T2SQL;
import cz.vutbr.fit.pdb03.map.JEntity;

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
	private JRadioButton rbNow, rbDate, rbInterval, rbAll;

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

		if(T2SQL.getMode() == T2SQL.NOW){
			rbNow.setSelected(true);
		}
		else if(T2SQL.getMode() == T2SQL.NO_RESTRICTIONS){
			rbAll.setSelected(true);
		}
		else if(T2SQL.getMode() == T2SQL.DATETIME){
			rbDate.setSelected(true);

			if(T2SQL.getValidationDateFrom() != null){
				tDate.setText(format.format(T2SQL.getValidationDateFrom()));
			}
		}
		else if(T2SQL.getMode() == T2SQL.INTERVAL){
			rbInterval.setSelected(true);

			if(T2SQL.getValidationDateFrom() != null){
				tFrom.setText(format.format(T2SQL.getValidationDateFrom()));
			}

			if(T2SQL.getValidationDateTo() != null){
				tTo.setText(format.format(T2SQL.getValidationDateTo()));
			}
		}
	}

	private void initTimeTab() {
		pTime = new JPanel();

		pTime.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = gbc.gridy = 0;

		rbNow = new JRadioButton("Nyní");
		rbDate = new JRadioButton("Konkrétní datum");
		rbInterval = new JRadioButton("V intervalu");
		rbAll = new JRadioButton("Bez omezení");

		lFrom = new JLabel("Od:");
		lTo = new JLabel("Do:");

		tDate = new JTextField(MAX_DATE);
		tFrom = new JTextField(MAX_DATE);
		tTo = new JTextField(MAX_DATE);

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

			boolean error = false;

			// gps
			try {
				frame.getMap().setMyPosition(getMyPosition());
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this,
						"Souřadnice nejsou ve správném formátu (XX.XXXX)", "Chyba údajů",
						JOptionPane.ERROR_MESSAGE);
				error = true;
			}

			// cas
			if (rbNow.isSelected()) {
				T2SQL.setCurrentTime();
			} else if (rbInterval.isSelected()) {
				try {
					Date from = format.parse(tFrom.getText());
					Date to = format.parse(tTo.getText());

					if(from.compareTo(to) > 0){
						throw new Exception();
					}
					T2SQL.setValidationDates(from, to);
				} catch (ParseException ex) {
					JOptionPane.showMessageDialog(this,
							"Datum je ve špatném formátu (DD-MM-YYYY)", "Chybné datum",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this,
							"Datum od není před datem do", "Chybné datum",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
			} else if (rbAll.isSelected()) {
				T2SQL.setNoTemporalRestrictions();
			} else if (rbDate.isSelected()) {
				try {
					Date date = format.parse(tDate.getText());
					T2SQL.setValidationDate(date);
					dispose();
				} catch (ParseException ex) {
					JOptionPane.showMessageDialog(this,
							"Datum je ve špatném formátu (DD-MM-YYYY)", "Chybné datum",
							JOptionPane.ERROR_MESSAGE);
					error = true;
				}
			}

			if(!error){
				frame.reloadAnimalsList(AnimalsDatabase.SEARCH_ALL);
				dispose();
			}
		}
	}
}
