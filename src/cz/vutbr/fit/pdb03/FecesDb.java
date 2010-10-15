package cz.vutbr.fit.pdb03;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

public class FecesDb extends JFrame {
	
	private static int frameWidth = 200;
	private static int frameHeight = 200;
	private static JLabel label = new JLabel("Nazdar prdi, hovna se blizi");


	public static void main(String[] args) {
		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// nastav menu 
		JMenu menu = new JMenu("About");
		menu.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				new AboutDialog(new JFrame(), "title", "message");
				
			}
		});
		
		JMenuBar menubar = new JMenuBar();
		menubar.add(menu);
		
		frame.setJMenuBar(menubar);
		frame.add(label);
		
		// umisti okno doprostred
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((dim.width-frameWidth)/2, (dim.height-frameHeight)/2);
	
		// nastav velikost
		frame.setSize(frameWidth, frameHeight);
		frame.setVisible(true);
	}
} 
class AboutDialog extends JDialog implements ActionListener {
	public AboutDialog(JFrame parent, String title, String message) {
		super(parent, title, true);
		if (parent != null) {
			Dimension parentSize = parent.getSize(); 
			Point p = parent.getLocation(); 
			setLocation(p.x + parentSize.width / 4, p.y + parentSize.height / 4);
		}
		JPanel messagePane = new JPanel();
		messagePane.add(new JLabel(message));
		getContentPane().add(messagePane);
		JPanel buttonPane = new JPanel();
		JButton button = new JButton("OK"); 
		buttonPane.add(button); 
		button.addActionListener(this);
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack(); 
		setVisible(true);
	}
	public void actionPerformed(ActionEvent e) {
		setVisible(false); 
		dispose(); 
	}
}