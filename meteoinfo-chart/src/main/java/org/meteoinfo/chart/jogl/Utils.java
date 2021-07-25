package org.meteoinfo.chart.jogl;

import java.io.File;
import java.io.InputStream;
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
        try (InputStream in = Utils.class.getResourceAsStream(fileName)) {
            return new Scanner(in, "UTF-8").useDelimiter("\\A").next();
        }
    }
}
