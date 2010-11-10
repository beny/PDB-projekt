package cz.vutbr.fit.pdb03;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class ConnectDialog extends JDialog implements ActionListener {

	private JLabel titleLabel;
	private JPanel textPanel;
	private JLabel usernameLabel;
	private JLabel passwordLabel;
	private JPanel panelForTextFields;
	private JTextField usernameField;
	private JTextField loginField;
	private JPanel completionPanel;
	private JLabel userLabel;
	private JLabel passLabel;
	private JButton loginButton;
	private JLabel databaseLabel;
	private JTextField databaseField;
	private JLabel checkLabel;
	private JPasswordField passwordField;
	private DataBase db;
	private JFrame parent;

	public ConnectDialog(Frame parent, DataBase db) {
		super(parent, "Přístupový dialog");

		this.db = db;
		this.parent = (JFrame) parent;

		// hlavni panel
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout(10, 10));

		// panel s labely
		textPanel = new JPanel();
		textPanel.setPreferredSize(new Dimension(70, 80));
		contentPane.add(textPanel, BorderLayout.LINE_START);

		usernameLabel = new JLabel("Username");
		usernameLabel.setPreferredSize(new Dimension(70, 30));
		usernameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		textPanel.add(usernameLabel);

		passwordLabel = new JLabel("Password");
		passwordLabel.setPreferredSize(new Dimension(70, 30));
		passwordLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		textPanel.add(passwordLabel);

		// panel s input fieldy
		panelForTextFields = new JPanel();
		panelForTextFields.setPreferredSize(new Dimension(100, 70));
		contentPane.add(panelForTextFields, BorderLayout.CENTER);

		usernameField = new JTextField(8);
		usernameField.setPreferredSize(new Dimension(100, 30));
		panelForTextFields.add(usernameField);

		passwordField = new JPasswordField(8);
		passwordField.setPreferredSize(new Dimension(100, 30));
		panelForTextFields.add(passwordField);

		// login tlacitko
		loginButton = new JButton("Login");
		loginButton.addActionListener(this);
		contentPane.add(loginButton, BorderLayout.PAGE_END);

		// nastaveni dialogu
		setContentPane(contentPane);
		setModal(true);
		setLocationRelativeTo(parent);
		setResizable(false);
		pack();

		// uzavirani dialogu
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				ConnectDialog dialog = (ConnectDialog)e.getSource();
				dialog.setVisible(false);
				dialog.dispose();

				JFrame parent = dialog.parent;
				parent.setVisible(false);
				parent.dispose();

			}
		});

	}

	@Override
	public void actionPerformed(ActionEvent event) {

		// zkouska loginu
		if (event.getSource() == loginButton) {
			try {
				// DEBUG
				System.out.println("Connection to DB");
				db.connect(usernameField.getText(),
						String.copyValueOf(passwordField.getPassword()));
			} catch (ClassNotFoundException e) {
				System.err.println("ClassNotFoundException: " + e.getMessage());
			} catch (SQLException e) {
				loginButton.setText("Problem with DB, try again later");
			} catch (Exception e) {
				loginButton.setText("Problem with login, try again");
			}

			if (db.isConnected()) {
				// DEBUG
				System.out.println("Connected !!!	");
				setVisible(false);
			}
		}
	}
}
