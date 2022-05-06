package org.meteoinfo.chart.jogl;

import java.io.*;
import java.util.Scanner;

public class Utils {
    public static File getFilePath(File file) throws NullPointerException {
        if (file.getPath().startsWith("resources" + File.separator) || file.getPath().startsWith(File.separator + "resources" + File.separator)) {

            final File modifiedFilePath = new File(Utils.class.getClassLoader().getResource(file.getPath().substring(file.getPath().indexOf("resources" + File.separator) + 10).replace("\\", "/")).getFile());

            if (!modifiedFilePath.exists()) {
                System.out.printf("%s does not exist", modifiedFilePath);
                System.exit(1);
            }
            return getFilePath(modifiedFilePath);

        }
        return file;
    }

    /**
     * Loads the resource.
     *
     * @param fileName of the resource to load.
     * @return content of the resource converted to UTF-8 text.
     * @throws Exception when an error occurs loading resource.
     */
    public static String loadResource(String fileName) throws Exception {
        try (InputStream inputStream = Utils.class.getResourceAsStream(fileName)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(" ", -1);
                if (tokens[0].equals("#include")) {
                    String includeFilename = tokens[1];
                    int pos = fileName.lastIndexOf("/");
                    includeFilename = fileName.substring(0, pos + 1) + includeFilename;
                    if (includeFilename.equals(fileName)) {
                        throw new IOException("Do not include the calling file.");
                    }
                    sb.append(loadResource(includeFilename));
                } else {
                    sb.append(line).append("\n");
                }
            }
            return sb.toString();
        }
    }

    /**
     * Loads the resource.
     *
     * @param fileName of the resource to load.
     * @return content of the resource converted to UTF-8 text.
     * @throws Exception when an error occurs loading resource.
     */
    public static String loadSimpleResource(String fileName) throws Exception {
        try (InputStream in = Utils.class.getResourceAsStream(fileName)) {
            return new Scanner(in, "UTF-8").useDelimiter("\\A").next();
        }
    }
}
