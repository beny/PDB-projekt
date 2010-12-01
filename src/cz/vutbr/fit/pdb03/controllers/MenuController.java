package cz.vutbr.fit.pdb03.controllers;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import cz.vutbr.fit.pdb03.Animal;
import cz.vutbr.fit.pdb03.AnimalsDatabase;
import cz.vutbr.fit.pdb03.D;
import cz.vutbr.fit.pdb03.DataBase;
import cz.vutbr.fit.pdb03.GUIManager;
import cz.vutbr.fit.pdb03.dialogs.AboutDialog;
import cz.vutbr.fit.pdb03.dialogs.AnimalDialog;
import cz.vutbr.fit.pdb03.map.JMapPanel;

public class MenuController implements ActionListener{

	public static final String CONNECT_TO_DB = "Připojení k databázi";
	public static final String DISCONNECT_FROM_DB = "Odpojeni od databáze";

	// menu items
	private JMenuBar mBar;
	private JMenu mMap, mDatabase, mAbout, mAnimal;
	private JMenuItem miAboutInfo, miDatabaseConnection, miDatabaseCreate,
			miAnimalRefresh, miAnimalAdd;

	// dialog
	private ConnectDialog connectDialog;

	// databaze
	DataBase db;

	// mapa
	JMapPanel map;

	// hlavni okno
	AnimalsDatabase frame;

	public MenuController(AnimalsDatabase frame) {

		// init promenych
		db = frame.getDb();
		map = frame.getMap();
		this.frame = frame;

		// hlavniho menu
		mBar = new JMenuBar();

		// menu databaze
		mDatabase = new JMenu("Databáze");
		mBar.add(mDatabase);

		miDatabaseConnection = new JMenuItem(CONNECT_TO_DB);
		miDatabaseConnection.addActionListener(this);
		mDatabase.add(miDatabaseConnection);

		miDatabaseCreate = new JMenuItem("Vytvořit prázdnou databázi");
		miDatabaseCreate.addActionListener(this);
		mDatabase.add(miDatabaseCreate);

		// menu mapa
		mMap = new JMenu("Mapa");
		mMap.addActionListener(this);
		mBar.add(mMap);

		// menu zvire
		mAnimal = new JMenu("Zvíře");
		mBar.add(mAnimal);

		miAnimalAdd = new JMenuItem("Přidat zvíře");
		miAnimalAdd.addActionListener(this);
		mAnimal.add(miAnimalAdd);

		miAnimalRefresh = new JMenuItem("Obnov seznam zvířat");
		miAnimalRefresh.addActionListener(this);
		mAnimal.add(miAnimalRefresh);

		// menu about
		mAbout = new JMenu("About");
		mBar.add(mAbout);

		miAboutInfo = new JMenuItem("O aplikaci");
		miAboutInfo.addActionListener(this);
		mAbout.add(miAboutInfo);

		frame.setJMenuBar(mBar);
	}

	@Override
	public void actionPerformed(ActionEvent event) {


		// informacni dialog
		if(event.getSource() == miAboutInfo){
			AboutDialog dAbout = new AboutDialog();
			GUIManager.moveToCenter(dAbout, frame);
			dAbout.setVisible(true);
		}

		// pripojen/odpojeni k databazi
		if(event.getSource() == miDatabaseConnection){
			if(db.isConnected()){
				try {
					db.disconnect();
					D.log("Disconnected");
				} catch (SQLException e){
					System.err.println("Error while disconnection from DB: " + e.getMessage());
				}

				miDatabaseConnection.setText(CONNECT_TO_DB);
				// TODO disable GUI
			}
			else {
				// dialog pro pripojeni
				connectDialog = new ConnectDialog(frame, db);
				connectDialog.fillDialog(ConnectDialog.ONDRA); // TODO remove

				GUIManager.moveToCenter(connectDialog, frame);
				connectDialog.setVisible(true);

				miDatabaseConnection.setText(DISCONNECT_FROM_DB);
				// TODO enable GUI
			}
		}

		// vytvoreni tabulek v DB
		if(event.getSource() == miDatabaseCreate){
			if(db.isConnected()){
				try{
					D.log("Creating empty database");
					db.createDatabase();
				} catch (SQLException e){
					System.err.println("Chyba pri vytvareni DB: " + e.getMessage());
				}
			}

			frame.refreshAnimalsList();
		}

		// pridani zvirete
		if(event.getSource() == miAnimalAdd){
			AnimalDialog dAnimal = new AnimalDialog(frame);
			GUIManager.moveToCenter(dAnimal, frame);
			dAnimal.setVisible(true);
		}

		// obnoveni seznamu zivrat
		if(event.getSource() == miAnimalRefresh){
			frame.refreshAnimalsList();
		}
	}


}

/**
 * Dialog pro pripojeni
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 * @param <AnimalDatabase>
 */
class ConnectDialog extends JDialog implements ActionListener{

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

		// uzavirani dialogu
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				ConnectDialog dialog = (ConnectDialog) e.getSource();
				dialog.setVisible(false);
				dialog.dispose();

				// TODO opravit
//				dialog.frame.setVisible(false);
//				dialog.frame.dispose();
			}
		});

	}

	@Override
	public void actionPerformed(ActionEvent event) {

		// zkouska loginu
		if (event.getSource() == bLogin) {
			try {
				db.connect(tUsername.getText(),	String.copyValueOf(pfPassword.getPassword()));
				D.log("Connected to database");
			} catch (Exception e) {
				lStatus.setText("Problem with login, try again");
			}

			setVisible(false);
			frame.refreshAnimalsList();

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

