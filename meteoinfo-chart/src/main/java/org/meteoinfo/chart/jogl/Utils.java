package org.meteoinfo.chart.jogl;

import java.io.File;

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
}
