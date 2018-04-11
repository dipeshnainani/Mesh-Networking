/**
 * This is the command file which takes in the command line arguments when they are entered in.
 * @author dipeshnainani
 *
 */
public class BuoyCommand {
	public static String command = "";
	public static String ipaddress = "";
	public static String multiaddress = "";
	public static void main(String[] args)
	{
		command = args[0];
		ipaddress = args[1];
		multiaddress = args[2];
	}

}
