package cz.vutbr.fit.pdb03.dialogs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import cz.vutbr.fit.pdb03.DataBase;

public class ConnectDialog extends JDialog implements ActionListener{

	private JLabel usernameLabel, passwordLabel, statusLabel;
	private JTextField usernameField;
	private JButton loginButton;
	private JPasswordField passwordField;
	private DataBase db;
	private JFrame parent;

	public ConnectDialog(Frame parent, DataBase db) {
		super(parent, "Přístupový dialog");

		this.db = db;
		this.parent = (JFrame) parent;

		// hlavni panel
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(5, 5, 5, 5);

		usernameLabel = new JLabel("Username");
		contentPane.add(usernameLabel, c);

		c.gridy = 1;
		passwordLabel = new JLabel("Password");
		contentPane.add(passwordLabel, c);

		c.gridx = 1;
		c.gridy = 0;
		usernameField = new JTextField(8);
		contentPane.add(usernameField, c);

		c.gridy = 1;
		passwordField = new JPasswordField(8);
		contentPane.add(passwordField, c);

		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = GridBagConstraints.REMAINDER;

		loginButton = new JButton("Login");
		getRootPane().setDefaultButton(loginButton);
		loginButton.addActionListener(this);
		contentPane.add(loginButton, c);


		c.gridy = 3;
		statusLabel = new JLabel();
		statusLabel.setForeground(Color.RED);
		contentPane.add(statusLabel, c);

		// nastaveni dialogu
		setContentPane(contentPane);
		setModal(true);
		setResizable(false);

		// nastaveni pozice a velikosti
		Toolkit tk = Toolkit.getDefaultToolkit();
	    Dimension screenSize = tk.getScreenSize();
	    int screenHeight = screenSize.height;
	    int screenWidth = screenSize.width;
	    setLocation(screenWidth / 3, screenHeight / 3);
	    setSize(300,200);

	    // odchytavani klavesy Enter
	    addKeyListener(new KeyAdapter() {
	    	@Override
	    	public void keyPressed(KeyEvent e) {

	    		if(e.getKeyCode() == KeyEvent.VK_ENTER){
	    			System.out.println("Zmacknuty Enter");
	    		}

	    	}
		});

		// uzavirani dialogu
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				ConnectDialog dialog = (ConnectDialog) e.getSource();
				dialog.setVisible(false);
				dialog.dispose();

				dialog.parent.setVisible(false);
				dialog.parent.dispose();
			}
		});

	}

	@Override
	public void actionPerformed(ActionEvent event) {

		// zkouska loginu
		if (event.getSource() == loginButton) {
			try {
				System.out.println("Connecting to database"); // DEBUG
				db.connect(usernameField.getText(),	String.copyValueOf(passwordField.getPassword()));
			} catch (Exception e) {
				statusLabel.setText("Problem with login, try again");
			}

			// kontrola pripojeni
			if (db.isConnected()) {
				System.out.println("Connected");// DEBUG
				setVisible(false);
			}
		}
	}

}
