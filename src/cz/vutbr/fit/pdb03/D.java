package cz.vutbr.fit.pdb03;

/**
 * Trida pro vypis a logovani ruznych veci v aplikaci
 *
 * @author Ondřej Beneš <ondra.benes@gmail.com>
 *
 */
public class D {

	private final static boolean ENABLE = true;

	private final static int OUTPUT = 0;
	private final static int ERROR = 1;


	public static void log(String message){

		if(ENABLE){
			System.out.println(message);
		}
	}

	public static void log(String message, int type){
		if(ENABLE){
			switch (type) {
			case ERROR:	System.err.println(message);break;
			default: System.out.println(message);
			}
		}
	}
}
