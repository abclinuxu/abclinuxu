/*
 * Copyright (C) 2006 Yin, Leos Literak
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; see the file
 * COPYING. If not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package cz.abclinuxu.utils;

import cz.abclinuxu.AbcException;
import cz.abclinuxu.data.ImageAssignable;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Tools for manipulating images
 */
public class ImageTool {
    private static final Logger log = Logger.getLogger(ImageTool.class);

    /**
     * Stores image in file system and informs object that it has image assigned
     *
     * @param image    Image to to stored
     * @param imageNo  Identification of image in object
     * @param object   Object to which image belongs
     * @param res      Image restrictions
     * @param env      Storage for error messages
     * @param errorKey Key under which error messaged are filed
     * @return {@code true} if stored successfully, {@code false} otherwise
     */
    public static boolean storeImage(int imageNo, FileItem image, ImageAssignable object, ImageRestriction res, Map env, String errorKey) {
        if (image == null) {
            ServletUtils.addError(errorKey, res.getEMsg(ImageRestriction.EMPTY), env, null);
            return false;
        }

        String suffix = res.validateExtension(image.getName().toLowerCase());
        if (suffix == null) {
            ServletUtils.addError(errorKey, res.getEMsg(ImageRestriction.FILETYPE), env, null);
            return false;
        }

        BufferedImage resized = null;
        try {
            Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix(suffix);
            ImageReader reader = readers.next();
            ImageInputStream iis = ImageIO.createImageInputStream(image.getInputStream());
            reader.setInput(iis, false);
            if (!res.permitAnimation && reader.getNumImages(true) > 1) {
                ServletUtils.addError(errorKey, res.getEMsg(ImageRestriction.ANIMATED), env, null);
                return false;
            }
            // check dimensions
            if (reader.getHeight(0) > res.height || reader.getWidth(0) > res.width) {
                // resize if permitted, it is able to resize only static images
                if (res.permitResize && reader.getNumImages(true) == 1) {
                    resized = reader.read(0);
                    resized = scaleIfNeeded(resized, res.width, res.height);
                    // create error
                } else {
                    ServletUtils.addError(errorKey, res.getEMsg(ImageRestriction.MAX_DIM), env, null);
                    return false;
                }
            }
        } catch (Exception e) {
            ServletUtils.addError(errorKey, res.getEMsg(ImageRestriction.IO_FAILED), env, null);
            log.warn("Exception during image parsing", e);
            return false;
        }

        String fileName = object.proposeImageUrl(imageNo, suffix);
        File file = new File(AbcConfig.calculateDeployedPath(fileName));
        try {
            // image was not resized
            if (resized == null)
                image.write(file);
                // image was resized, store resized variant
            else
                ImageIO.write(resized, suffix, file);
        } catch (Exception e) {
            ServletUtils.addError(errorKey, res.getEMsg(ImageRestriction.STORAGE_FAILED), env, null);
            log.error("Není možné uložit obrázek " + file.getAbsolutePath() + " na disk!", e);
            return false;
        }

        object.assignImage(imageNo, "/" + fileName);
        return true;
    }

    /**
     * Deletes image and informs object that its image was deleted
     *
     * @param imageNo Identification of image in object
     * @param object  Object which image is deleted
     * @return {@code true} if image was deleted, {@code false} if image was not present or not deleted
     */
    public static boolean deleteImage(int imageNo, ImageAssignable object) {
        String url = object.detractImage(imageNo);
        if (url != null) {
            // expects filename without absolute / in the beginning
            String localPath = AbcConfig.calculateDeployedPath(url.substring(1));
            return (new File(localPath).delete());
        }
        return false;
    }

    /**
     * Loads image from file, checks it's dimension and if needed, creates a thumbnail in file
     * <code>thumbnailPath</code> returning true. The thumbnail is created if the image is in any dimension
     * bigger 200 points.
     *
     * @param imagePath     path to original image
     * @param thumbnailPath path where the thumbnail should be saved
     * @return true if thumbnail was created
     */
    public static boolean createThumbnail(File imagePath, File thumbnailPath) throws IOException, AbcException {
        BufferedImage img, img2;
        boolean save = false;

        img = ImageIO.read(imagePath);
        if (img == null)
            throw new AbcException("Nepodařilo se načíst obrázek ze souboru " + imagePath);

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
     * Creates thumbnail with given absolute width or height.
     *
     * @param imagePath     path to original image
     * @param thumbnailPath path for image to be created
     * @param size          maximum width
     * @param height        true if the size is height, false if the size is width
     * @return true if thumbnail was created or false, if the image was smaller
     * @throws IOException  failed to read image
     * @throws AbcException failed to read image
     */
    public static boolean createThumbnailMaxSize(File imagePath, File thumbnailPath, int size, boolean height) throws IOException, AbcException {
        BufferedImage img, img2;
        img = ImageIO.read(imagePath);
        if (img == null)
            throw new AbcException("Nepodařilo se načíst obrázek ze souboru " + imagePath);

        if (height)
            img2 = scaleIfNeeded(img, img.getWidth(), size);
        else
            img2 = scaleIfNeeded(img, size, img.getHeight());
        if (img2 == null)
            return false;

        ImageIO.write(img2, "png", thumbnailPath);
        return true;
    }

    /**
     * If <code>img</code> is taller as compared to the <code>width</code> and <code>height</code> of the
     * target image, it cuts of the odd piece from bottom of image and returns whats left from it.
     *
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
     * If <code>img</code> exceeds the given dimension <code>width</code> and <code>height</code>, it will be
     * scaled to fit in and the result will be returned.
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

    public static void main(String[] args) throws Exception {
        System.out.println("Usage: image width size");
        File imageFile = new File(args[0]);
        int width = Integer.parseInt(args[1]);
        int height = Integer.parseInt(args[2]);

        int position = imageFile.getName().lastIndexOf('.');
        String fileName = imageFile.getName().substring(0, position);
        String extension = imageFile.getName().substring(position);

        createThumbnail(imageFile, new File(imageFile.getParent(), fileName + "_mini" + extension));
        createThumbnailMaxSize(imageFile, new File(imageFile.getParent(), fileName + "_mini_width" + extension), width, false);
        createThumbnailMaxSize(imageFile, new File(imageFile.getParent(), fileName + "_mini_height" + extension), height, true);
    }

    // Image restrictions

    public static final ImageRestriction USER_PHOTO_RES = new ImageRestriction() {
        {
            this.width = 500;
            this.height = 500;
            this.suffixes = Arrays.asList("jpg", "jpeg", "png", "gif");
            this.permitAnimation = false;
            this.permitResize = false;

            this.errorMsgs = Arrays.asList("Vyberte soubor s Vaší fotografií!", "Soubor musí být typu JPG, PNG, GIF nebo JPEG!", "Fotografie přesahuje povolené maximální rozměry!", "Nelze načíst obrázek!", "Chyba při zápisu na disk!", "Animované obrázky nejsou povoleny!");

        }};

    public static final ImageRestriction USER_AVATAR_RES = new ImageRestriction() {
        {
            this.width = 50;
            this.height = 50;
            this.suffixes = Arrays.asList("jpg", "jpeg", "png", "gif");
            this.permitAnimation = false;
            this.permitResize = false;

            this.errorMsgs = Arrays.asList("Vyberte soubor s avatarem!", "Soubor musí být typu JPG, PNG, GIF nebo JPEG!", "Avatar přesahuje povolené maximální rozměry!", "Nelze načíst obrázek!", "Chyba při zápisu na disk!", "Animované obrázky nejsou povoleny!");
        }};

    public static final ImageRestriction AUTHOR_PHOTO_RES = new ImageRestriction() {
        {
            this.width = 150;
            this.height = 100;
            this.suffixes = Arrays.asList("jpg", "jpeg", "png", "gif");
            this.permitAnimation = false;
            this.permitResize = true;

            this.errorMsgs = Arrays.asList("Vyberte soubor s fotkou autora!", "Soubor musí být typu JPG, PNG, GIF nebo JPEG!", "Obrázek autora přesahuje povolené maximální rozměry!", "Nelze načíst obrázek!", "Chyba při zápisu na disk!", "Animované obrázky nejsou povoleny!");
        }};

    static abstract class ImageRestriction {

        static final int EMPTY = 0;
        static final int FILETYPE = 1;
        static final int MAX_DIM = 2;
        static final int IO_FAILED = 3;
        static final int STORAGE_FAILED = 4;
        static final int ANIMATED = 5;

        int width;
        int height;
        List<String> suffixes;
        boolean permitAnimation;
        boolean permitResize;
        List<String> errorMsgs;

        protected ImageRestriction() {
            if (permitResize && permitAnimation)
                throw new IllegalArgumentException("Unable to create image restriction allowing both resizing and animated images");
        }

        /**
         * Returns error message for given index
         *
         * @param i Index
         * @return Error message to be shown to user
         */
        String getEMsg(int i) {
            return errorMsgs.get(i);
        }

        /**
         * Validates extension from filename
         *
         * @param filename Name of file
         * @return File type extension if valid, {@code elsewhere}
         */
        String validateExtension(String filename) {
            int i;
            String suffix = "";
            if (filename != null && (i = filename.lastIndexOf('.')) != -1)
                suffix = filename.substring(i + 1);

            if (suffixes.contains(suffix))
                return suffix;
	    return null;
	}
    }
}
