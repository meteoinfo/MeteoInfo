package org.meteoinfo.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public class SystemUtils {

    /**
     * Add path to Classpath with reflection since the URLClassLoader.addURL(URL url) method is protected
     * @param path The path
     * @throws Exception
     */
    public static void addPath(String path) throws Exception {
        File f = new File(path);
        URL url = f.toURL();

        ClassLoader prevCl = Thread.currentThread().getContextClassLoader();

        // Create class loader using given codebase
        // Use prevCl as parent to maintain current visibility
        ClassLoader urlCl = URLClassLoader.newInstance(new URL[]{url}, prevCl);

        try {
            // Save class loader so that we can restore later
            Thread.currentThread().setContextClassLoader(urlCl);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Restore
            Thread.currentThread().setContextClassLoader(prevCl);
        }
    }

    /**
     * Sets the java library path to the specified path
     *
     * @param path the new library path
     * @throws Exception
     */
    public static void setLibraryPath(String path) throws Exception {
        String lib = System.getProperty("java.library.path");
        path = path + ";" + lib;
        System.setProperty("java.library.path", path);

        //set sys_paths to null
        try {
            final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
            sysPathsField.setAccessible(true);
            sysPathsField.set(null, null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds the specified path to the java library path
     *
     * @param pathToAdd the path to add
     * @throws Exception
     */
    public static void addLibraryPath(String pathToAdd) throws Exception{
        try {
            final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
            usrPathsField.setAccessible(true);

            //get array of paths
            final String[] paths = (String[]) usrPathsField.get(null);

            //check if the path to add is already present
            for (String path : paths) {
                if (path.equals(pathToAdd)) {
                    return;
                }
            }

            //add the new path
            final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
            newPaths[newPaths.length - 1] = pathToAdd;
            usrPathsField.set(null, newPaths);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
