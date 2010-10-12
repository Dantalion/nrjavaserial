package gnu.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class NativeResource {
	public void load(String libraryName) {		
		if(System.getProperty(libraryName + ".userlib") != null) {
			try {
				if(System.getProperty(libraryName + ".userlib").equalsIgnoreCase("sys")) {
					System.loadLibrary(libraryName);
				} else {
					System.load(System.getProperty(libraryName + ".userlib"));
				}
				return;
			} catch (Exception e){
				throw new NativeResourceException("Unable to load native resource from given path.\n" + e.getLocalizedMessage());
			}
		}
			
		loadLib(libraryName);	
	}

	private void loadLib(String name) {
		try {
			InputStream resourceSource = locateResource(name);
			File resourceLocation = prepResourceLocation(name);
			copyResource(resourceSource, resourceLocation);
			loadResource(resourceLocation);
			
		} catch (IOException ex) {
			throw new NativeResourceException("Unable to load deployed native resource");
		}
	}
	
	private InputStream locateResource(String name) {
		name += OSUtil.getExtension();
		
		if(OSUtil.isOSX()) {
			return getClass().getResourceAsStream("/native/osx/" + name);
		}
		
		if(OSUtil.isWindows() && !OSUtil.is64Bit()) {
			return getClass().getResourceAsStream("/native/windows/x86_32/" + name);
		}
		
		if(OSUtil.isWindows() && OSUtil.is64Bit()) {
			return getClass().getResourceAsStream("/native/windows/x86_64/" + name);
		}
		
		if(OSUtil.isLinux() && !OSUtil.is64Bit()) {
			return getClass().getResourceAsStream("/native/windows/x86_64/" + name);
		}

		if(OSUtil.isLinux() && OSUtil.is64Bit()) {
			return getClass().getResourceAsStream("/native/windows/x86_64/" + name);
 		}
		

		throw new NativeResourceException("Unable to locate the native library for " + OSUtil.getIdentifier());
	}
	
	private void loadResource(File resource) {
		System.load(resource.getAbsolutePath());
	}

	private void copyResource(InputStream io, File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		
		
		byte[] buf = new byte[256];
		int read = 0;
		while ((read = io.read(buf)) > 0) {
			fos.write(buf, 0, read);
		}
		fos.close();
		io.close();
	}

	private File prepResourceLocation(String fileName) {		
		String tmpDir = System.getProperty("java.io.tmpdir");
		if ((tmpDir == null) || (tmpDir.length() == 0)) {
			tmpDir = "tmp";
		}
		
		String displayName = new File(fileName).getName().split("\\.")[0];
		
		String user = System.getProperty("user.name");
		
		File fd = null;
		File dir = null;
		
		for(int i = 0; i < 10; i++) {
			dir = new File(tmpDir, displayName + "_" + user + "_" + (i));
			if (dir.exists()) {
				if (!dir.isDirectory()) {
					continue;
				}
				
				try {
					File[] files = dir.listFiles();
					for (int j = 0; j < files.length; j++) {
						if (!files[j].delete()) {
							continue;
						}
					}
				} catch (Throwable e) {
					
				}
			}
			
			if ((!dir.exists()) && (!dir.mkdirs())) {
				continue;
			}
			
			try {
				dir.deleteOnExit();
			} catch (Throwable e) {
				// Java 1.1 or J9
			}
			
			fd = new File(dir, fileName + OSUtil.getExtension());
			if ((fd.exists()) && (!fd.delete())) {
				continue;
			}
			
			try {
				if (!fd.createNewFile()) {
					continue;
				}
			} catch (IOException e) {
				continue;
			} catch (Throwable e) {
				// Java 1.1 or J9
			}
			
			break;
		}
		
		if(!fd.canRead()) {
			throw new NativeResourceException("Unable to deploy native resource");
		}
		
		return fd;
	}
	
	private static class OSUtil {
		public static boolean is64Bit() {
			System.out.println(getOsArch());
			return getOsArch().startsWith("x86_64");
		}
		
		public static boolean isWindows() {
			return getOsName().startsWith("Windows");
		}
		
		public static boolean isLinux() {
			return getOsName().startsWith("Linux");
		}
		
		public static boolean isOSX() {
			return getOsName().startsWith("Mac OS X");
		}
		
		public static String getExtension() {
			if(isWindows()) {
				return ".dll";
			}
			
			if(isLinux()) {
				return ".so";
			}
			
			if(isOSX()) {
				return ".jnilib";
			}
			
			return "";
		}
		
		public static String getOsName() {	
			return System.getProperty("os.name");
		}
		
		public static String getOsArch() {
			System.out.println(System.getProperty("os.arch"));
			return System.getProperty("os.arch");
		}
		
		public static String getIdentifier() {
			return getOsName() + " : " + getOsArch();
		}
	}
}
