package averroes.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.raw.RawDexFile;

import com.google.common.base.MoreObjects;
import com.google.common.io.ByteStreams;

import averroes.android.SetupAndroid;

/**
 * 
 * @author Michael Appel 
 * 
 * Provides dex related utils.
 */

public class DexUtils {
	
	/**
	 * dexlib2 does not provide means to create a raw dex file. This method serves
	 * as utility to return an object of type "RawDexFile".
	 * @return
	 */
	public static RawDexFile getRawDex(File dexFile, String dexEntry) throws IOException {
        ZipFile zipFile = null;
        boolean isZipFile = false;
        try {
            zipFile = new ZipFile(dexFile);
            // if we get here, it's safe to assume we have a zip file
            isZipFile = true;

            String zipEntryName = MoreObjects.firstNonNull(dexEntry, "classes.dex");
            ZipEntry zipEntry = zipFile.getEntry(zipEntryName);
            if (zipEntry == null) {
                throw new FileNotFoundException("zip file" + dexFile.getName() + " does not contain a " + zipEntryName + " file");
            }
            long fileLength = zipEntry.getSize();

            byte[] dexBytes = new byte[(int)fileLength];
            ByteStreams.readFully(zipFile.getInputStream(zipEntry), dexBytes);

            int api = SetupAndroid.v().getApiVersion();
            Opcodes opcodes = Opcodes.forApi(api);
            
            return new RawDexFile(opcodes, dexBytes);
        } catch (IOException ex) {
            // don't continue on if we know it's a zip file
            if (isZipFile) {
                throw ex;
            }
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException ex) {
                    // just eat it
                }
            }
        }	
        return null;
	}

}