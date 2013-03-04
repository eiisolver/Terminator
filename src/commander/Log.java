package commander;

import java.util.*;

public class Log {
	public static boolean logging = false;
	public static boolean buffering = false;
	private static List<String> buf = new ArrayList<String>();
	
	public static void log(String msg) {
		if (logging) {
			System.err.println(msg);
		}
		if (buffering) {
		   buf.add(msg);
		}
	}
	
	public static List<String> getBuffer() {
	   List<String> result = new ArrayList<String>(buf);
	   buf.clear();
	   return result;
	}

}
