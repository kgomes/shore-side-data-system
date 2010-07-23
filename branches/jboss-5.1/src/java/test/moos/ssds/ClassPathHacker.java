package test.moos.ssds;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class ClassPathHacker {

	@SuppressWarnings("unchecked")
	private static final Class[] parameters = new Class[] { URL.class };

	public static void addFile(String s) throws IOException {
		File f = new File(s);
		addFile(f);
	}// end method

	public static void addFile(File f) throws IOException {
		addURL(f.toURL());
	}// end method

	@SuppressWarnings("unchecked")
	public static void addURL(URL u) throws IOException {

		URLClassLoader sysloader = (URLClassLoader) ClassLoader
				.getSystemClassLoader();
		Class sysclass = URLClassLoader.class;
		System.out.println("Classpath before hack: ");
		URL[] classpathUrls = sysloader.getURLs();
		for (int i = 0; i < classpathUrls.length; i++) {
			System.out.println(i + ". " + classpathUrls[i]);
		}

		try {
			Method method = sysclass.getDeclaredMethod("addURL", parameters);
			method.setAccessible(true);
			method.invoke(sysloader, new Object[] { u });
		} catch (Throwable t) {
			t.printStackTrace();
			throw new IOException(
					"Error, could not add URL to system classloader");
		}// end try catch

		System.out.println("Classpath after hack: ");
		classpathUrls = sysloader.getURLs();
		for (int i = 0; i < classpathUrls.length; i++) {
			System.out.println(i + ". " + classpathUrls[i]);
		}
	}// end method
}// end class