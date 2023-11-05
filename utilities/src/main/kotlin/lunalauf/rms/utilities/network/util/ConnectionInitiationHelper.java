package lunalauf.rms.utilities.network.util;

import java.util.Random;

public class ConnectionInitiationHelper {

	private static final Random random = new Random();

	public static String getSynMessage() {
		return String.valueOf(random.nextLong());
	}

	public static String getAckMessage(String synMessage) throws NumberFormatException {
		long number = Long.parseLong(synMessage);
		return String.valueOf(Math.abs(number) - 11);
	}

}
