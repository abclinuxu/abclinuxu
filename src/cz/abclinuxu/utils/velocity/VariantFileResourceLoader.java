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
import org.apache.velocity.util.StringUtils;
import org.apache.commons.collections.ExtendedProperties;

import java.io.*;
import java.util.Hashtable;

/**
 * This file based resource loader is used in situation, when
 * you have one root directory with several subdirectories.
 * All these subdirectories shall have same structure and naming
 * convention. These subdirectories are meant to contain different
 * variants of same website, one is supposed to be default and others
 * shall contain its modifications.<p>
 *
 * Example:<p>
 * <code>path=WEB-INF/templates<br>
 * default=web</code>
 * <p>
 * The directory <code>WEB-INF/templates</code> contains directories <code>web</code>
 * and <code>lynx</code>. Both are expected to have same structure. Actually
 * <code>web</code> contains file <code>header.vm</code> and subdirectory
 * <code>add</code> with file <code>category.vm</code>.
 * The directory <code>lynx</code> contains just <code>header.vm</code>.<p>
 * <dl>
 * <dt>#include('web/header.vm')</dt>
 * <dd>file <code>WEB-INF/templates/web/header.vm</code> exists, so it is returned</dd>
 * <dt>#include('lynx/header.vm')</dt>
 * <dd>file <code>WEB-INF/templates/lynx/header.vm</code> exists, so it is returned</dd>
 * <dt>#include('lynx/add/category.vm')</dt>
 * <dd>file <code>WEB-INF/templates/lynx/add/category.vm</code> doesn't exist, so
 * so we search default variant. The file <code>WEB-INF/templates/web/add/category.vm</code>
 * exists, so it is returned.</dd>
 * <dt>#include('lynx/add/article.vm')</dt>
 * <dd>file <code>WEB-INF/templates/lynx/add/article.vm</code> doesn't exist, so
 * so we search default variant. The file <code>WEB-INF/templates/web/add/article.vm</code>
 * doesn't exist, so ResourceNotFoundException is raised..</dd>
 * </dl>
 *
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
    /** fully qualified path to directory, where variants sit */
    String path;
    /** relative path to default variant. It must be path's subdirecory. */
    String defaultPath;

    /**
     * Initialize the template loader with a
     * a resources class.
     */
    public void init(ExtendedProperties configuration) {
        rsvc.info("VariantFileResourceLoader: initialization starting.");
        path = configuration.getString("path");
        defaultPath = configuration.getString("default");
        rsvc.info("VariantFileResourceLoader: using "+path+" with "+defaultPath+" as default variant subdir.");
    }

    /**
     * Get the InputStream that the Runtime will parse
     * to create a template.
     */
    public InputStream getResourceStream(String templateName) throws ResourceNotFoundException {
        if ( templateName==null || templateName.length()==0 )
            throw new ResourceNotFoundException("Need to specify a file name!");

        String template = StringUtils.normalizePath(templateName);
        if ( template==null || template.length()==0 ) {
            String msg = "File resource error : argument " + template +
                " contains .. and may be trying to access " +
                "content outside of template root.  Rejected.";
            rsvc.error( "FileResourceLoader : " + msg );
            throw new ResourceNotFoundException ( msg );
        }

        if ( template.startsWith("/") )
            template = template.substring(1);

        // check, that variant directory is present: 'variant_subdir/file';
        if ( template.indexOf('/')==-1 ) {
            String msg = "File resource error : argument " + template + " has illegal form!"+
                "It doesn't starts with variant's subdirectory!";
            rsvc.error( "FileResourceLoader : " + msg );
            throw new ResourceNotFoundException ( msg );
        }

        File file = findTemplateFile(template);
        if ( file!=null ) {
            try {
                return new BufferedInputStream( new FileInputStream(file.getAbsolutePath()) );
            } catch (FileNotFoundException e) {
                rsvc.error("Resource "+templateName+" dissappeared!");
            }
        }

        String msg = "VariantFileResourceLoader Error: cannot find resource "+templateName;
        throw new ResourceNotFoundException(msg);
    }

    /**
     * Try to find a template given a normalized path.
     *
     * @param String a normalized path
     * @return File that will be parsed
     *
     */
    protected File findTemplateFile(String template) {
        File file = null;

        file = new File(path,template);
        if ( file.canRead() )
            return file;

        // OK, file doesn't exists or we cannot read it in selected variant.
        // let's proceed with same search in default variant.

        int c = template.indexOf('/'); // this is checked in getResourceStream()
        String template2 = defaultPath+template.substring(c);
        if ( template2.equals(template) ) {
            // the searched template was already under default variant
            return null;
        }

        file = new File(path,template2);
        if ( file.canRead() )
            return file;

        return null; // not found in either selected or default variant.
    }

    /**
     * Given a template, check to see if the source of InputStream
     * has been modified.
     */
    public boolean isSourceModified(Resource resource) {
        File file = findTemplateFile(resource.getName());

        if ( file.canRead() ) {
            if ( file.lastModified()!=resource.getLastModified() ) {
                return true;
            } else {
                return false;
            }
        }

        return true; // something has changed, so it is modified
    }

    /**
     * Get the last modified time of the InputStream source
     * that was used to create the template. We need the template
     * here because we have to extract the name of the template
     * in order to locate the InputStream source.
     */
    public long getLastModified(Resource resource) {
        File file = findTemplateFile(resource.getName());

        if ( file.canRead() )
            return file.lastModified();
        else
            return 0;
    }
}
