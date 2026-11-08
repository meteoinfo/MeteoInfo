package org.meteoinfo.chart.io;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.emf.EMFGraphics2D;
import org.freehep.graphicsio.ps.PSGraphics2D;
import org.meteoinfo.chart.Figure;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * Export a {@link Figure} to raster or vector formats.  EPS, PS, PDF and EMF
 * are written through Java2D so that 2-D graphics remain vector data.  A JOGL
 * 3-D axes is embedded as its last framebuffer snapshot.
 *
 * @author MeteoInfo
 */
public final class FigureExporter {

    private FigureExporter() {
    }

    public static void export(Figure figure, String fileName, int width, int height) throws IOException {
        File file = new File(fileName);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Cannot create directory: " + parent);
        }
        String ext = extension(file);
        switch (ext) {
            case "eps":
            case "ps":
                writePostScript(figure, file, width, height);
                break;
            case "pdf":
                writePdf(figure, file, width, height);
                break;
            case "emf":
                writeEmf(figure, file, width, height);
                break;
            default:
                writeRaster(figure, file, width, height, ext);
                break;
        }
    }

    private static void writePostScript(Figure figure, File file, int width, int height)
            throws IOException {
        VectorGraphics g = new PSGraphics2D(file, new Dimension(width, height));
        boolean started = false;
        try {
            g.startExport();
            started = true;
            figure.draw(g, new Rectangle2D.Double(0, 0, width, height));
        } finally {
            if (started) {
                g.endExport();
            }
            g.dispose();
        }
    }

    private static void writePdf(Figure figure, File file, int width, int height) throws IOException {
        Document document = new Document(new com.itextpdf.text.Rectangle(width, height));
        FileOutputStream out = new FileOutputStream(file);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            PdfTemplate template = cb.createTemplate(width, height);
            Graphics2D g2 = new PdfGraphics2D(template, width, height, true);
            try {
                figure.draw(g2, new Rectangle2D.Double(0, 0, width, height));
            } finally {
                g2.dispose();
            }
            cb.addTemplate(template, 0, 0);
        } catch (com.itextpdf.text.DocumentException ex) {
            throw new IOException("Cannot write PDF: " + file, ex);
        } finally {
            document.close();
            out.close();
        }
    }

    private static void writeEmf(Figure figure, File file, int width, int height) throws IOException {
        VectorGraphics g = new EMFGraphics2D(file, new Dimension(width, height));
        boolean started = false;
        try {
            g.startExport();
            started = true;
            figure.draw(g, new Rectangle2D.Double(0, 0, width, height));
        } finally {
            if (started) {
                g.endExport();
            }
            g.dispose();
        }
    }

    private static void writeRaster(Figure figure, File file, int width, int height,
                                    String ext) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            figure.draw(g, new Rectangle2D.Double(0, 0, width, height));
        } finally {
            g.dispose();
        }
        if (!ImageIO.write(image, ext.isEmpty() ? "png" : ext, file)) {
            if (!ImageIO.write(image, "png", file)) {
                throw new IOException("No image writer for " + file);
            }
        }
    }

    private static String extension(File file) {
        String name = file.getName();
        int index = name.lastIndexOf('.');
        return index < 0 ? "" : name.substring(index + 1).toLowerCase(Locale.ROOT);
    }
}
