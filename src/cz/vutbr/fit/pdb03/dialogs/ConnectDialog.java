package cz.vutbr.fit.pdb03.dialogs;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.DataBase;
import cz.vutbr.fit.pdb03.Log;

/**
 * Dialog pro pripojeni
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 * @param <AnimalDatabase>
 */
public class ConnectDialog extends JDialog implements ActionListener{

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

	public ConnectDialog(AnimalsDatabase parent, DataBase db) {
		super((JFrame)parent, "Přístupový dialog");

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
		setModal(true);
		setResizable(false);
		pack();

		// TODO odstranit predplneni formulare
		fillDialog(ONDRA);

		// uzavirani dialogu
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				ConnectDialog dialog = (ConnectDialog) e.getSource();
				dialog.dispose();
			}
		});

	}

	@Override
	public void actionPerformed(ActionEvent event) {

		// zkouska loginu
		if (event.getSource() == bLogin) {
			try {
				db.connect(tUsername.getText(),	String.copyValueOf(pfPassword.getPassword()));
				Log.info("Connected to database");
			} catch (Exception e) {
				// TODO doresit nejak chybne prihlaseni
				lStatus.setText("Problem with login, try again");
			}

			frame.refreshAnimalsList();
			frame.setEnable(true);
			dispose();
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

