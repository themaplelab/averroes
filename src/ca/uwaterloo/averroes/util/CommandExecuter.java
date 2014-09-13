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
	public static boolean runDoopAverroes(String doopHome, String benchmark) throws IOException, InterruptedException {
		String[] cmdArray = { FileUtils.doopAverroesRunExe(doopHome), "1.4", AverroesProperties.getMainClass(),
				FileUtils.organizedApplicationJarFile(benchmark), FileUtils.placeholderLibraryJarFile(benchmark) };
		return run(cmdArray);
	}
	
	/**
	 * Run the executable for Doop running with Averroes.
	 * 
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static boolean runDoop(String doopHome, String benchmark) throws IOException, InterruptedException {
		String[] cmdArray = { FileUtils.doopRunExe(doopHome), "1.4", AverroesProperties.getMainClass(),
				FileUtils.organizedApplicationJarFile(benchmark), FileUtils.organizedLibraryJarFile(benchmark) };
				//AverroesProperties.getInputJarFilesForSpark().trim(), AverroesProperties.getLibraryClassPath().trim() };
		return run(cmdArray);
	}

}