
package commander;

import com.aisandbox.sys.SandboxClient;


/**
 * Wrapper class for the AI sandbox client.
 * 
 * @author Matthias F. Brandstetter
 */
public class SandboxClientWrapper
{
	private static final boolean VERBOSE = true; // print exception stack traces and verbose output
	private static final int CONNECT_TIMEOUT = 10; // in seconds, 0 to wait forever
	
	private static final String DEF_HOST = "localhost";
	private static final int DEF_PORT = 41041;
	
	private static String host;
	private static int port;
	
	
	/**
	 * Program entry point.
	 * Program can either be called w/o arguments or with <server> <port> as arguments.
	 */
	public static void main(String [] args)
	{
		parseCmdLine(args);
		
		// init. client and connect to server
		SandboxClient client = new SandboxClient(host, port, VERBOSE);
		if(!client.connect(CONNECT_TIMEOUT))
		{
			System.err.println("\nERROR: Could not connect to server.");
			System.exit(1);
		}
		
		// run commander, automatically disconnect from server afterwards
		client.run(true);
	}

	// Parse command line arguments
	private static void parseCmdLine(String [] args)
	{
	   System.out.println("parseCmdLine");
		if(args.length <= 1)
		{
			// no arguments given, use default values
			host = DEF_HOST;
			port = DEF_PORT;
		}
		else if(args.length == 2)
		{
			// try to parse command line arguments <host> <port>
			try
			{
				host = args[0];
				port = Integer.valueOf(args[1]);
			}
			catch(Exception ex)
			{
				showUsage(true);
			}
		} else {
         showUsage(true);
		}
		if (args.length == 1) {
		   Utils.DEBUG = true;
		   System.err.println("Debug is true");
		} 
	}

	// Show usage help text and optionally exit
	private static void showUsage(boolean exit)
	{
		System.out.println("AI Sandbox Java Client.");
		System.out.println("Without arguments we try to connect to " + DEF_HOST + ":" + DEF_PORT);
		System.out.println("Or alternatively call: " + SandboxClient.class.getSimpleName() + " <host> <port>");
		
		if(exit)
			System.exit(1);
	}
}
















































