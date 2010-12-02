package cz.vutbr.fit.pdb03;


/**
 * Trida na logovani udalosti
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
public final class Log {

	public static void info(String message){
		System.out.println("INFO: " + message);
	}

	public static void debug(String message){
		System.out.println("DEBUG: " + message);
	}

	public static void warning(String message){
		System.err.println("WARNING: " + message);
	}

	public static void error(String message){
		System.err.println("ERROR: " + message);
	}

}
