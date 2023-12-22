package org.meteoinfo.ndarray.io.npy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.meteoinfo.ndarray.Array;

public class Npz {

    /**
     * Open npz data file
     * @param fileName The file name
     * @return ZipFile object
     */
    public static ZipFile open(String fileName) {
        File file = new File(fileName);
        try {
            ZipFile zipFile = new ZipFile(file);
            return zipFile;
        } catch (IOException e) {
            throw new RuntimeException("failed to read zip file: " + fileName, e);
        }
    }

    /**
     * Returns the names of the entries of the given NPZ file.
     *
     * @param fileName a NPZ file name
     * @return the names of the entries of the NPZ file
     */
    public static List<String> entries(String fileName) {
        File file = new File(fileName);
        return entries(file);
    }

    /**
     * Returns the names of the entries of the given NPZ file.
     *
     * @param npz a NPZ file
     * @return the names of the entries of the NPZ file
     */
    public static List<String> entries(File npz) {
        try (ZipFile zip = new ZipFile(npz)) {
            return entries(zip);
        } catch (IOException e) {
            throw new RuntimeException("failed to read zip file: " + npz, e);
        }
    }

    /**
     * Returns the names of the entries of the given NPZ file.
     *
     * @param npz a NPZ file
     * @return the names of the entries of the NPZ file
     */
    public static List<String> entries(ZipFile npz) {
        ArrayList<String> entries = new ArrayList<String>();
        Enumeration<ZipEntry> zipEntries = (Enumeration<ZipEntry>) npz.entries();
        while (zipEntries.hasMoreElements()) {
            ZipEntry e = zipEntries.nextElement();
            if (e.isDirectory())
                continue;
            entries.add(e.getName());
        }
        return entries;
    }

    /**
     * Read an array from an entry of a NPZ file.
     *
     * @param npz   the NPZ file
     * @param entry the name of the entry in which the array is stored
     * @return the array of the entry
     */
    public static Array load(ZipFile npz, String entry) {
        ZipEntry e = npz.getEntry(entry);
        try (java.io.InputStream stream = npz.getInputStream(e);
             java.nio.channels.ReadableByteChannel channel = Channels.newChannel(stream)) {
            return Npy.load(channel);
        } catch (IOException ex) {
            throw new RuntimeException("failed to read entry " + entry, ex);
        }
    }

    /**
     * Read an array from an entry of a NPZ file.
     *
     * @param npz   the NPZ file
     * @param entry the name of the entry in which the array is stored
     * @return the NPY array of the entry
     */
    public static NpyArray<?> read(File npz, String entry) {
        try (ZipFile zip = new ZipFile(npz)) {
            return read(zip, entry);
        } catch (IOException e) {
            throw new RuntimeException("failed to read zip file: " + npz, e);
        }
    }

    /**
     * Read an array from an entry of a NPZ file.
     *
     * @param npz   the NPZ file
     * @param entry the name of the entry in which the array is stored
     * @return the NPY array of the entry
     */
    public static NpyArray<?> read(ZipFile npz, String entry) {
        ZipEntry e = npz.getEntry(entry);
        try (java.io.InputStream stream = npz.getInputStream(e);
             java.nio.channels.ReadableByteChannel channel = Channels.newChannel(stream)) {
            return Npy.read(channel);
        } catch (IOException ex) {
            throw new RuntimeException("failed to read entry " + entry, ex);
        }
    }

    /**
     * Open the given file as an NPZ file. This function is useful when you want
     * to do multiple things with a NPZ file, e.g.
     *
     * <pre>{@code
     * Npz.use(file, npz -> {
     *   for (var entry : Npz.entries(npz)) {
     *     var array = Npz.read(npz, entry);
     *     // ...
     *   }
     * });
     * }</pre>
     *
     * @param npz the NPZ file
     * @param fn  a consumer function of the opened NPZ file
     */
    public static void use(File npz, Consumer<ZipFile> fn) {
        try (ZipFile zip = new ZipFile(npz)) {
            fn.accept(zip);
        } catch (IOException e) {
            throw new RuntimeException("failed to use NPZ file " + npz, e);
        }
    }

    public static void create(File file, Consumer<ZipOutputStream> fn) {
        try (FileOutputStream fileOut = new FileOutputStream(file);
             ZipOutputStream zipOut = new ZipOutputStream(fileOut)) {
            fn.accept(zipOut);
        } catch (IOException e) {
            throw new RuntimeException("failed to create NPZ file: " + file, e);
        }
    }

    public static ZipOutputStream create(File file) {
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(file);
            ZipOutputStream zipOut = new ZipOutputStream(fileOut);
            return zipOut;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static ZipOutputStream create(String fileName) {
        File file = new File(fileName);
        return create(file);
    }

    public static void write(ZipOutputStream npz, String entry, Array array) {
        ZipEntry e = new ZipEntry(entry);
        try {
            npz.putNextEntry(e);
            Npy.save(npz, array);
            npz.closeEntry();
        } catch (IOException ex) {
            throw new RuntimeException("failed to write NPZ entry: " + entry, ex);
        }
    }

    public static void write(ZipOutputStream npz, String entry, NpyArray<?> array) {
        ZipEntry e = new ZipEntry(entry);
        try {
            npz.putNextEntry(e);
            Npy.write(npz, array);
            npz.closeEntry();
        } catch (IOException ex) {
            throw new RuntimeException("failed to write NPZ entry: " + entry, ex);
        }
    }

}
