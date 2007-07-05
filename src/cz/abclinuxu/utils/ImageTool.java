/*
*  Copyright (C) 2006 Yin, Leos Literak
*
*  This program is free software; you can redistribute it and/or
*  modify it under the terms of the GNU General Public
*  License as published by the Free Software Foundation; either
*  version 2 of the License, or (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
*  General Public License for more details.
*
*  You should have received a copy of the GNU General Public License
*  along with this program; see the file COPYING.  If not, write to
*  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
*  Boston, MA 02111-1307, USA.
*/
package cz.abclinuxu.utils;

import cz.abclinuxu.AbcException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Tools for manipulating images
 */
public class ImageTool {

    /**
     * Loads image from file, checks it's dimension and
     * if needed, creates a thumbnail in file <code>thumbnailPath</code>
     * returning true. The thumbnail is created if the image is in any
     * dimension bigger 200 points.
     * @param imagePath path to original image
     * @param thumbnailPath path where the thumbnail should be saved
     * @return true if thumbnail was created
     */
    public static boolean createThumbnail(File imagePath, File thumbnailPath) throws IOException, AbcException {
        BufferedImage img = null;
        BufferedImage img2 = null;
        boolean save = false;

        img = ImageIO.read(imagePath);
        if (img == null)
            throw new AbcException("Nepodařilo se načíst obrázek ze souboru "+imagePath);

        if ((img2 = cutBottomIfNeeded(img, 200, 200)) != null) {
            img = img2;
            save = true;
        }

        if ((img2 = scaleIfNeeded(img, 200, 200)) != null) {
            img = img2;
            save = true;
        }

        if (save)
            ImageIO.write(img, "png", thumbnailPath);
        return save;
    }

    /**
     * If <code>img</code> is taller as compared to the <code>width</code> and
     * <code>height</code> of the target image, it cuts of the odd piece from
     * bottom of image and returns whats left from it.
     * @param img    original image to be cut
     * @param width  width of the target image
     * @param height height of the target image
     * @return new image, if it was created, or null
     */
    public static BufferedImage cutBottomIfNeeded(BufferedImage img, int width, int height) {
        int h;
        if (img.getWidth() > width) {
            float ratio = (float) height / (float) width;
            h = (int) (ratio * img.getWidth());
        } else
            h = height;

        if (h > img.getHeight())
            return null;

        return img.getSubimage(0, 0, img.getWidth(), h);
    }

    /**
     * If <code>img</code> exceeds the given dimension <code>width</code> and
     * <code>height</code>, it will be scaled to fit in and the result will be returned.
     *
     * @param img    original image to be scaled
     * @param width  width of the target image
     * @param height height of the target image
     * @return new image, if it was created, or null
     */
    public static BufferedImage scaleIfNeeded(BufferedImage img, int width, int height) {
        float wratio = (float) width / (float) img.getWidth();
        float hratio = (float) height / (float) img.getHeight();
        float ratio;

        if (wratio < hratio)
            ratio = wratio;
        else
            ratio = hratio;

        if (ratio >= 1)
            return null;

        AffineTransform tx = new AffineTransform();
        tx.scale(ratio, ratio);

        RenderingHints rh = new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        AffineTransformOp op = new AffineTransformOp(tx, rh);
        return op.filter(img, null);
    }
}
