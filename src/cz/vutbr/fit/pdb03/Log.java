package cz.vutbr.fit.pdb03;


/**
 * Trida na logovani udalosti
 * @author Ondřej Beneš <xbenes00@stud.fit.vutbr.cz>
 *
 */
public final class Log {

	// debugovaci levely
	private final static int INFO = 1;
	private final static int DEBUG = 2;
	private final static int WARNING = 3;
	private final static int ERROR = 4;
	private final static int ALL = 10;

	// zvoleny debug level
	public final static int LEVEL = ALL; // chci vypsat debug, info

	public static void info(String message){
		if (LEVEL >= INFO)
			System.out.println("INFO: " + message);
	}

	public static void debug(String message) {
		if (LEVEL >= DEBUG)
			System.out.println("DEBUG: " + message);
	}

	public static void warning(String message) {
		if (LEVEL >= WARNING)
			System.err.println("WARNING: " + message);
	}

	public static void error(String message) {
		if (LEVEL >= ERROR)
			System.err.println("ERROR: " + message);
	}

}
