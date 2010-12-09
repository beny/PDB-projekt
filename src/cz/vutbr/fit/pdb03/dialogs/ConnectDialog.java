package cz.vutbr.fit.pdb03.dialogs;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.DataBase;
import cz.vutbr.fit.pdb03.Log;
import cz.vutbr.fit.pdb03.gui.GUIManager;

/**
 * Dialog pro pripojeni
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 * @param <AnimalDatabase>
 */
public class ConnectDialog extends DefaultDialog implements ActionListener{

	private static final long serialVersionUID = 4256996833054036876L;

	public final static String PAVEL = "xsrnec01";
	public final static String TOMAS = "xizakt00";
	public final static String ONDRA = "xbenes00";

	private JLabel lUsername, lPassword, lStatus;
	private JTextField tUsername;
	private JButton bLogin;
	private JPasswordField pfPassword;
	private DataBase db;
	private AnimalsDatabase frame;

	private LoadingDialog dLoading;

	public ConnectDialog(AnimalsDatabase parent, DataBase db) {
		super();

		this.db = db;
		frame = parent;

		// hlavni panel
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(5, 5, 5, 5);

		lUsername = new JLabel("Username");
		contentPanel.add(lUsername, gbc);

		gbc.gridy = 1;
		lPassword = new JLabel("Password");
		contentPanel.add(lPassword, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		tUsername = new JTextField(8);
		contentPanel.add(tUsername, gbc);

		gbc.gridy = 1;
		pfPassword = new JPasswordField(8);
		contentPanel.add(pfPassword, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = GridBagConstraints.REMAINDER;

		bLogin = new JButton("Login");
		getRootPane().setDefaultButton(bLogin);
		bLogin.addActionListener(this);
		contentPanel.add(bLogin, gbc);

		gbc.gridy = 3;
		lStatus = new JLabel();
		lStatus.setForeground(Color.RED);
		contentPanel.add(lStatus, gbc);

		// nastaveni dialogu
		setContentPane(contentPanel);
		pack();

		// TODO odstranit predplneni formulare
		fillDialog(ONDRA);
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		// zkouska loginu
		if (event.getSource() == bLogin) {

			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						db.connect(tUsername.getText(),	String.copyValueOf(pfPassword.getPassword()));
						Log.info("Connected to database");
					} catch (Exception e) {
						// TODO doresit nejak chybne prihlaseni
						lStatus.setText("Problem with login, try again");
					}

					frame.reloadAnimalsList(AnimalsDatabase.SEARCH_ALL);
					frame.setEnable(true);
					dLoading.dispose();
					dispose();
				}
			}).start();

			dLoading = new LoadingDialog("Probíhá připojování k DB, prosím vyčkejte");
			GUIManager.moveToCenter(dLoading, this);
			dLoading.setVisible(true);
		}
	}

	/**
	 * Metoda ktera predvyplni formular pro rychlejsi testovani
	 * @param user
	 */
	public void fillDialog(String user){
		tUsername.setText(user);
		pfPassword.setText(user);
	}

}

