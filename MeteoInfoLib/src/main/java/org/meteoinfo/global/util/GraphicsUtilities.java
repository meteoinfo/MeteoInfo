/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.global.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JComponent;

/**
 *
 * @author wyq
 */
public class GraphicsUtilities {

    private static GraphicsConfiguration getGraphicsConfiguration() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    }

    private static boolean isHeadless() {
        return GraphicsEnvironment.isHeadless();
    }

    public static BufferedImage convertToBufferedImage(Image img) {
        BufferedImage buff = createCompatibleTranslucentImage(img.getWidth(null), img.getHeight(null));

        Graphics2D g2 = buff.createGraphics();
        try {
            g2.drawImage(img, 0, 0, null);
        } finally {
            g2.dispose();
        }

        return buff;
    }

    public static BufferedImage createColorModelCompatibleImage(BufferedImage image) {
        ColorModel cm = image.getColorModel();
        return new BufferedImage(cm, cm.createCompatibleWritableRaster(image.getWidth(), image.getHeight()), cm.isAlphaPremultiplied(), null);
    }

    public static BufferedImage createCompatibleImage(BufferedImage image) {
        return createCompatibleImage(image, image.getWidth(), image.getHeight());
    }

    public static BufferedImage createCompatibleImage(BufferedImage image, int width, int height) {
        return isHeadless() ? new BufferedImage(width, height, image.getType()) : getGraphicsConfiguration().createCompatibleImage(width, height, image.getTransparency());
    }

    public static BufferedImage createCompatibleImage(int width, int height) {
        return isHeadless() ? new BufferedImage(width, height, 1) : getGraphicsConfiguration().createCompatibleImage(width, height);
    }

    public static BufferedImage createCompatibleTranslucentImage(int width, int height) {
        return isHeadless() ? new BufferedImage(width, height, 2) : getGraphicsConfiguration().createCompatibleImage(width, height, 3);
    }

    public static BufferedImage loadCompatibleImage(InputStream in)
            throws IOException {
        BufferedImage image = ImageIO.read(in);
        if (image == null) {
            return null;
        }
        return toCompatibleImage(image);
    }

    public static BufferedImage loadCompatibleImage(URL resource)
            throws IOException {
        BufferedImage image = ImageIO.read(resource);
        return toCompatibleImage(image);
    }

    public static BufferedImage toCompatibleImage(BufferedImage image) {
        if (isHeadless()) {
            return image;
        }

        if (image.getColorModel().equals(getGraphicsConfiguration().getColorModel())) {
            return image;
        }

        BufferedImage compatibleImage = getGraphicsConfiguration().createCompatibleImage(image.getWidth(), image.getHeight(), image.getTransparency());

        Graphics g = compatibleImage.getGraphics();
        try {
            g.drawImage(image, 0, 0, null);
        } finally {
            g.dispose();
        }

        return compatibleImage;
    }

    public static BufferedImage createThumbnailFast(BufferedImage image, int newSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        if (width > height) {
            if (newSize >= width) {
                throw new IllegalArgumentException("newSize must be lower than the image width");
            }
            if (newSize <= 0) {
                throw new IllegalArgumentException("newSize must be greater than 0");
            }

            float ratio = width / height;
            width = newSize;
            height = (int) (newSize / ratio);
        } else {
            if (newSize >= height) {
                throw new IllegalArgumentException("newSize must be lower than the image height");
            }
            if (newSize <= 0) {
                throw new IllegalArgumentException("newSize must be greater than 0");
            }

            float ratio = height / width;
            height = newSize;
            width = (int) (newSize / ratio);
        }

        BufferedImage temp = createCompatibleImage(image, width, height);
        Graphics2D g2 = temp.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            g2.drawImage(image, 0, 0, temp.getWidth(), temp.getHeight(), null);
        } finally {
            g2.dispose();
        }

        return temp;
    }

    public static BufferedImage createThumbnailFast(BufferedImage image, int newWidth, int newHeight) {
        if ((newWidth >= image.getWidth()) || (newHeight >= image.getHeight())) {
            throw new IllegalArgumentException("newWidth and newHeight cannot be greater than the image dimensions");
        }

        if ((newWidth <= 0) || (newHeight <= 0)) {
            throw new IllegalArgumentException("newWidth and newHeight must be greater than 0");
        }

        BufferedImage temp = createCompatibleImage(image, newWidth, newHeight);
        Graphics2D g2 = temp.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            g2.drawImage(image, 0, 0, temp.getWidth(), temp.getHeight(), null);
        } finally {
            g2.dispose();
        }

        return temp;
    }

    public static BufferedImage createThumbnail(BufferedImage image, int newSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        boolean isTranslucent = image.getTransparency() != 1;
        boolean isWidthGreater = width > height;

        if (isWidthGreater) {
            if (newSize >= width) {
                throw new IllegalArgumentException("newSize must be lower than the image width");
            }
        } else if (newSize >= height) {
            throw new IllegalArgumentException("newSize must be lower than the image height");
        }

        if (newSize <= 0) {
            throw new IllegalArgumentException("newSize must be greater than 0");
        }

        float ratioWH = width / height;
        float ratioHW = height / width;

        BufferedImage thumb = image;
        BufferedImage temp = null;

        Graphics2D g2 = null;
        try {
            int previousWidth = width;
            int previousHeight = height;
            do {
                if (isWidthGreater) {
                    width /= 2;
                    if (width < newSize) {
                        width = newSize;
                    }
                    height = (int) (width / ratioWH);
                } else {
                    height /= 2;
                    if (height < newSize) {
                        height = newSize;
                    }
                    width = (int) (height / ratioHW);
                }

                if ((temp == null) || (isTranslucent)) {
                    if (g2 != null) {
                        g2.dispose();
                    }
                    temp = createCompatibleImage(image, width, height);
                    g2 = temp.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                }

                g2.drawImage(thumb, 0, 0, width, height, 0, 0, previousWidth, previousHeight, null);

                previousWidth = width;
                previousHeight = height;

                thumb = temp;
            } while (newSize != (isWidthGreater ? width : height));
        } finally {
            if (g2 != null) {
                g2.dispose();
            }
        }

        if ((width != thumb.getWidth()) || (height != thumb.getHeight())) {
            temp = createCompatibleImage(image, width, height);
            g2 = temp.createGraphics();
            try {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                g2.drawImage(thumb, 0, 0, width, height, 0, 0, width, height, null);
            } finally {
                g2.dispose();
            }

            thumb = temp;
        }

        return thumb;
    }

    public static BufferedImage createThumbnail(BufferedImage image, int newWidth, int newHeight) {
        int width = image.getWidth();
        int height = image.getHeight();

        boolean isTranslucent = image.getTransparency() != 1;

        if ((newWidth >= width) || (newHeight >= height)) {
            throw new IllegalArgumentException("newWidth and newHeight cannot be greater than the image dimensions");
        }

        if ((newWidth <= 0) || (newHeight <= 0)) {
            throw new IllegalArgumentException("newWidth and newHeight must be greater than 0");
        }

        BufferedImage thumb = image;
        BufferedImage temp = null;

        Graphics2D g2 = null;
        try {
            int previousWidth = width;
            int previousHeight = height;
            do {
                if (width > newWidth) {
                    width /= 2;
                    if (width < newWidth) {
                        width = newWidth;
                    }
                }

                if (height > newHeight) {
                    height /= 2;
                    if (height < newHeight) {
                        height = newHeight;
                    }
                }

                if ((temp == null) || (isTranslucent)) {
                    if (g2 != null) {
                        g2.dispose();
                    }
                    temp = createCompatibleImage(image, width, height);
                    g2 = temp.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                }

                g2.drawImage(thumb, 0, 0, width, height, 0, 0, previousWidth, previousHeight, null);

                previousWidth = width;
                previousHeight = height;

                thumb = temp;
            } while ((width != newWidth) || (height != newHeight));
        } finally {
            if (g2 != null) {
                g2.dispose();
            }
        }

        if ((width != thumb.getWidth()) || (height != thumb.getHeight())) {
            temp = createCompatibleImage(image, width, height);
            g2 = temp.createGraphics();
            try {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                g2.drawImage(thumb, 0, 0, width, height, 0, 0, width, height, null);
            } finally {
                g2.dispose();
            }

            thumb = temp;
        }

        return thumb;
    }

    public static int[] getPixels(BufferedImage img, int x, int y, int w, int h, int[] pixels) {
        if ((w == 0) || (h == 0)) {
            return new int[0];
        }

        if (pixels == null) {
            pixels = new int[w * h];
        } else if (pixels.length < w * h) {
            throw new IllegalArgumentException("pixels array must have a length >= w*h");
        }

        int imageType = img.getType();
        if ((imageType == 2) || (imageType == 1)) {
            Raster raster = img.getRaster();
            return (int[]) raster.getDataElements(x, y, w, h, pixels);
        }

        return img.getRGB(x, y, w, h, pixels, 0, w);
    }

    public static void setPixels(BufferedImage img, int x, int y, int w, int h, int[] pixels) {
        if ((pixels == null) || (w == 0) || (h == 0)) {
            return;
        }
        if (pixels.length < w * h) {
            throw new IllegalArgumentException("pixels array must have a length >= w*h");
        }

        int imageType = img.getType();
        if ((imageType == 2) || (imageType == 1)) {
            WritableRaster raster = img.getRaster();
            raster.setDataElements(x, y, w, h, pixels);
        } else {
            img.setRGB(x, y, w, h, pixels, 0, w);
        }
    }

    public static void clear(Image img) {
        Graphics g = img.getGraphics();
        try {
            if ((g instanceof Graphics2D)) {
                ((Graphics2D) g).setComposite(AlphaComposite.Clear);
            } else {
                g.setColor(new Color(0, 0, 0, 0));
            }

            g.fillRect(0, 0, img.getWidth(null), img.getHeight(null));
        } finally {
            g.dispose();
        }
    }

    public static void tileStretchPaint(Graphics g, JComponent comp, BufferedImage img, Insets ins) {
        int left = ins.left;
        int right = ins.right;
        int top = ins.top;
        int bottom = ins.bottom;

        g.drawImage(img, 0, 0, left, top, 0, 0, left, top, null);
        g.drawImage(img, left, 0, comp.getWidth() - right, top, left, 0, img.getWidth() - right, top, null);
        g.drawImage(img, comp.getWidth() - right, 0, comp.getWidth(), top, img.getWidth() - right, 0, img.getWidth(), top, null);
        g.drawImage(img, 0, top, left, comp.getHeight() - bottom, 0, top, left, img.getHeight() - bottom, null);
        g.drawImage(img, left, top, comp.getWidth() - right, comp.getHeight() - bottom, left, top, img.getWidth() - right, img.getHeight() - bottom, null);
        g.drawImage(img, comp.getWidth() - right, top, comp.getWidth(), comp.getHeight() - bottom, img.getWidth() - right, top, img.getWidth(), img.getHeight() - bottom, null);
        g.drawImage(img, 0, comp.getHeight() - bottom, left, comp.getHeight(), 0, img.getHeight() - bottom, left, img.getHeight(), null);
        g.drawImage(img, left, comp.getHeight() - bottom, comp.getWidth() - right, comp.getHeight(), left, img.getHeight() - bottom, img.getWidth() - right, img.getHeight(), null);
        g.drawImage(img, comp.getWidth() - right, comp.getHeight() - bottom, comp.getWidth(), comp.getHeight(), img.getWidth() - right, img.getHeight() - bottom, img.getWidth(), img.getHeight(), null);
    }
}
