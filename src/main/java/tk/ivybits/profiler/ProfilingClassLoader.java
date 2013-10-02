package tk.ivybits.profiler;

import sun.misc.Launcher;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class ProfilingClassLoader extends URLClassLoader {

    public ProfilingClassLoader() {
        this(new URL[]{}, new URLClassLoader(new URL[0]));
        System.out.println(getParent());
    }

    ProfilingClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, true);
    }

    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        System.out.println("L: " + name);
        return Launcher.getLauncher().getClassLoader().loadClass(name);
        /*
        synchronized (getClassLoadingLock(name)) {
            if (name.startsWith("tk.ivybits.profiler") || name.startsWith("sun.util.resources")) {
                return sys.loadClass(name);
            }
            System.out.println("Loading " + name);
            Class c = null;
            try {
                c = findClass(name);
            } catch (Exception e) {

            }

            if (c == null) {
                System.out.println("Delegating " + name + " to sys");
                return sys.loadClass(name);
            }
            System.out.println("Loaded " + c.getSimpleName());
            if (true) {
                resolveClass(c);
            }
            return c;
        }
          */
    }

    public Class findClass(String name) throws ClassNotFoundException {
        System.out.println("F: " + name);
        return super.findClass(name);
    }

    //@Override
    public Class<?> findClass0(String name) throws ClassNotFoundException {
        System.out.println("Classloading " + name);

        Class result = null;
        InputStream stream = null;
        try {
            String path = name.replace('.', '/').concat(".class");
            Method findResource = ClassLoader.class.getDeclaredMethod("findResource", String.class);
            findResource.setAccessible(true);
            URL res = (URL) findResource.invoke(getParent(), path);
            if (res != null) {
                stream = res.openStream();
                if (stream != null) {
                    byte[] buf = new byte[stream.available()];
                    stream.read(buf);
                    byte[] bytes = ProfilingTransformer.transform(name.replace(".", "/"), buf);
                    result = super.defineClass(name, bytes, 0, bytes.length);
                    System.out.println("Transformed " + result);
                    if (result != null) {
                        resolveClass(result);
                    } else {
                        System.err.println(".............");
                    }
                } else {
                    System.out.println("Stream for " + name + " is null");
                    return null;
                }
            } else {
                System.out.println("URL for " + name + " is null");
                return null;
            }
        } catch (SecurityException se) {
            return getParent().loadClass(name);
        } catch (ClassFormatError cfe) {
            return getParent().loadClass(name);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        } finally {
            if (stream != null)
                try {
                    stream.close();
                } catch (Throwable tt) {
                    tt.printStackTrace();
                }
        }
        return result;
    }
}
