package de.pfannekuchen.forgenogradleapi;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.apache.commons.lang3.ArrayUtils;

import de.pfannekuchen.accountapi.MicrosoftAccount;

/**
 * Pre Main for logging into accounts
 * @author Pancake
 */
public class PreMain {

	private static final File TOKEN = new File(".token");

	/**
	 * Pre main launching GradleStart after logging into the account
	 * @param args Launch arguments
	 * @throws ClassNotFoundException Gradle Start not found
	 * @throws SecurityException Gradle Start not found
	 * @throws NoSuchMethodException Gradle Start not found
	 * @throws InvocationTargetException Gradle Start not found
	 * @throws IllegalArgumentException Gradle Start not found
	 * @throws IllegalAccessException Gradle Start not found
	 */
	public static void main(String[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		System.setProperty("java.awt.headless", "false");
		
		MicrosoftAccount account = null;
		// Try to log in using existing token
		if (TOKEN.exists()) {
			try {
				String refreshToken = new String(Files.readAllBytes(TOKEN.toPath()), StandardCharsets.UTF_8);
				account = new MicrosoftAccount(refreshToken);
				System.out.println("Logged into Microsoft Account using cached token!");
			} catch (Exception e) {
				System.err.println("Unable to use cached token to log into Microsoft Account!");
			}
		}
		
		// Try to log in using webbrowser
		if (account == null) {
			try {
				account = new MicrosoftAccount();
				Files.write(TOKEN.toPath(), account.getAccountToken().getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
				System.out.println("Logged into Microsoft Account using webbrowser!");
			} catch (Exception e) {
				System.err.println("Unable to log into Microsoft Account!");
			}
		}
		
		// Add launch arguments
		if (account != null) {
			if (args == null)
				args = new String[0];
			args = ArrayUtils.addAll(args, "--uuid", account.getUuid().toString(), "--username", account.getUsername(), "--accessToken", account.getAccessToken());
			System.out.println("Launching with Microsoft Account...\n");
		} else {
			System.out.println("Launching without Microsoft Account...\n");
		}
		
		Class.forName("GradleStart").getMethod("main", String[].class).invoke(null, new Object[] {args});
	}
	
}
