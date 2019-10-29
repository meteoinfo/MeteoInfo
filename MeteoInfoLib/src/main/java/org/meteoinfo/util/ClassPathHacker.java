/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.util;

import java.io.IOException;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.net.URL;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Yaqiang Wang
 */
public class ClassPathHacker {

    private static final Class[] parameters = new Class[]{URL.class};

    /**
     * Add a jar file
     *
     * @param s Jar file path
     * @throws IOException
     */
    public static void addFile(String s) throws IOException {
        File f = new File(s);
        addFile(f);
    }

    /**
     * Add a jar file
     *
     * @param f Jar file object
     * @throws IOException
     */
    public static void addFile(File f) throws IOException {
        addURL(f.toURI().toURL());
    }

    /**
     * Add URL
     *
     * @param u The URL
     * @throws IOException
     */
    public static void addURL(URL u) throws IOException {
        URL urls [] = {};
        URLClassLoader sysloader = new URLClassLoader(urls);
        Class sysclass = URLClassLoader.class;

        try {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[]{u});
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }

    }

}
