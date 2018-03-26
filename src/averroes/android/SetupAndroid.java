package averroes.android;

import java.io.File;
import java.io.IOException;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jf.dexlib2.dexbacked.raw.ClassDefItem;
import org.jf.dexlib2.dexbacked.raw.MethodIdItem;
import org.jf.dexlib2.dexbacked.raw.RawDexFile;

import averroes.exceptions.AverroesException;

import averroes.options.AverroesOptions;
import averroes.soot.Names;
import averroes.util.DexUtils;
import soot.G;
import soot.Scene;
import soot.SootMethod;
import soot.Type;
import soot.coffi.Util;
import soot.jimple.infoflow.android.SetupApplication;
import soot.options.Options;

//Added by eshna 
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.infoflow.entryPointCreators.AndroidEntryPointCreator;

/**
 * Sets up Averroes such that it works with Android applications. Specifically,
 * this class analyzes the apk for xml callbacks and handles the dummy main
 * creation. Many parts of the code originate from FlowDroid.
 * 
 * @author Michael Appel
 *
 */

public class SetupAndroid {

	private static SetupAndroid instance;
	private final int apiVersion;
	private String apkFileLocation;
	private String androidJars;
	private SootMethod dummyMain = null;
	private RawDexFile rawDex;
	

	
	public static SetupAndroid v() {
		if (instance == null) {
			try {
				instance = new SetupAndroid();
			}
			catch (AverroesException ex) {
				ex.printStackTrace();
			}
		}
		return instance;
	}
	
	
	/**
	 * Constructor 
	 * @throws AverroesException if API version is not found
	 */
	
	private SetupAndroid() throws AverroesException {
		apkFileLocation = AverroesOptions.getApk();
		androidJars = AverroesOptions.getAndroidJar();

		apiVersion = Scene.v().getAndroidAPIVersion();
		if (apiVersion == -1){
			throw new AverroesException("Couldn't find the Android API version",
					new Throwable());
		}
		
	}
	
	public SootMethod getDummyMainMethod() {			
		if (dummyMain != null) {
			return dummyMain;
		}
		SetupApplication app = new SetupApplication(androidJars, apkFileLocation);	
		//Options.v().set_soot_classpath(apkFileLocation + File.pathSeparator + AverroesOptions.getAndroidJar());
		//System.out.println(app.getSootConfig());
		app.constructCallgraph(); //this creates the dummy main method 
		
		
		dummyMain = app.getDummyMainMethod();
		//System.out.println(dummyMain.getDeclaringClass());
		dummyMain.getDeclaringClass().setSuperclass(G.v().soot_Scene().getSootClass(Names.JAVA_LANG_OBJECT));
		System.out.println(dummyMain.getActiveBody());
		
		
				
		return dummyMain;	
	}
	
	public RawDexFile getRawDex() {
		// needs to be done after the constructor, hence the field is initialized here
		if (rawDex == null) {
			try {
				rawDex = DexUtils.getRawDex(new File(apkFileLocation), null);
			}
			catch (IOException ioEx) {
				ioEx.printStackTrace();
			}
		}
		return rawDex;
	}
	
	public int getReferencedApplicationClassCount() {
		// TODO: Refactor
		String[] classes = ClassDefItem.getClasses(getRawDex());
		List<String> result = new LinkedList<>();
		
		String patternString = AverroesOptions.getEscapedApplicationRegex();
		Pattern p = Pattern.compile(patternString);	

		for (String s: classes) {		
			Type jimpleType = Util.v().jimpleTypeOfFieldDescriptor(s);
			Matcher m = p.matcher(jimpleType.toString());
			while (m.find()) {
				String match = m.group();
				result.add(match);
			}	
		}
		
		/*for (String s: result) {
			System.out.println(s);
		}*/
		return result.size();
	}
	
	public int getReferencedApplicationMethodCount() {
		String[] methods = MethodIdItem.getMethods(getRawDex());
		
		List<String> result = new LinkedList<>();
		
		String patternString = AverroesOptions.getEscapedApplicationRegex();
		Pattern p = Pattern.compile(patternString);	

		for (String s: methods) {		
			String[] clazzAndMethod = s.split("-");
			Type jimpleType = Util.v().jimpleTypeOfFieldDescriptor(clazzAndMethod[0]);
			Matcher m = p.matcher(jimpleType.toString());
			while (m.find()) {
				String match = m.group();
				result.add(s);
			}	
		}
		/*for (String s: result) {
			System.out.println(s);
		}*/
		return result.size();	
	}
		
	public int getApiVersion() {	
		return apiVersion;
	}
	
	public String getApkFileLocation() {
		return apkFileLocation;
	}

}
