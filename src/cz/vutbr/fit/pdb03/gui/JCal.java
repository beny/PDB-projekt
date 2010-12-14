package cz.vutbr.fit.pdb03.gui;

import com.toedter.calendar.JDateChooser;

public class JCal extends JDateChooser {

	private final static long serialVersionUID = 8002072645475359234L;

	public JCal() {
		super();

		setDateFormatString("dd-MM-yyyy");

	}
}
