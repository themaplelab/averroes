package ca.uwaterloo.averroes.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import ca.uwaterloo.averroes.properties.AverroesProperties;
import ca.uwaterloo.averroes.util.io.FileUtils;

public class CommandExecuter {

	public static boolean run(String[] cmdarray) throws IOException, InterruptedException {
		System.out.println("Spawning process " + Arrays.toString(cmdarray));
		Process p = Runtime.getRuntime().exec(cmdarray);

		BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));

		String line;
		while ((line = stdout.readLine()) != null) {
			System.out.println(line);
		}

		stdout.close();
		return p.waitFor() == 0;
	}

	/**
	 * Run the executable for Doop running with Averroes.
	 * 
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static boolean runDoop(String doopHome, String base, String benchmark, boolean isAverroes)
			throws IOException, InterruptedException {
		String exec = isAverroes ? FileUtils.doopAverroesRunExe(doopHome) : FileUtils.doopRunExe(doopHome);
		String lib = isAverroes ? FileUtils.placeholderLibraryJarFile(base, benchmark) : FileUtils
				.organizedLibraryJarFile(base, benchmark);
		String[] cmdArray = { exec, "1.4", AverroesProperties.getMainClass(),
				FileUtils.organizedApplicationJarFile(base, benchmark), lib };
		// AverroesProperties.getInputJarFilesForSpark().trim(),
		// AverroesProperties.getLibraryClassPath().trim() };
		return run(cmdArray);
	}
}