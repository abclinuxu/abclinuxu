/*
 * User: literakl
 * Date: Jul 7, 2002
 * Time: 9:22:12 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.utils.velocity;

import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.commons.collections.ExtendedProperties;

import java.io.InputStream;

/**
 * This file based resource loader is used in situation, when
 * you have one root directory with several subdirectories.
 * All these subdirectories shall have same structure and naming
 * convention. These subdirectories are meant to contain different
 * variants of same website, one is supposed to be default and others
 * shall contain its modifications.<p>
 * Imagine, that the default tree represents website and there may be
 * variant for text based browsers. Most files can be shared between
 * both trees, except index file, header and footer. So you store them
 * in default tree. When you parse version for lynx, if resource is not
 * found in alternative tree, it is searched in default tree.<p>
 * With this approach you can easily create another variant of your
 * website, optimized for example to unnamed browser. Or you may build
 * complete wap version for mobile phones.
 */
public class VariantFileResourceLoader extends ResourceLoader {

    /**
     * Initialize the template loader with a
     * a resources class.
     */
    public void init(ExtendedProperties configuration) {
    }

    /**
     * Get the InputStream that the Runtime will parse
     * to create a template.
     */
    public InputStream getResourceStream(String source) throws ResourceNotFoundException {
        return null;
    }

    /**
     * Given a template, check to see if the source of InputStream
     * has been modified.
     */
    public boolean isSourceModified(Resource resource) {
        return false;
    }

    /**
     * Get the last modified time of the InputStream source
     * that was used to create the template. We need the template
     * here because we have to extract the name of the template
     * in order to locate the InputStream source.
     */
    public long getLastModified(Resource resource) {
        return 0;
    }
}
