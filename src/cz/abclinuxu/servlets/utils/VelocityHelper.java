/*
 * User: literakl
 * Date: Jan 15, 2002
 * Time: 7:39:39 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.utils;

import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.PersistanceException;
import cz.abclinuxu.servlets.AbcServlet;
import org.dom4j.Document;
import org.dom4j.Node;
import org.apache.velocity.context.Context;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.io.UnsupportedEncodingException;
import java.io.StringWriter;

/**
 * This class provides several methods, that
 * make velocity developer's life easier.
 */
public class VelocityHelper {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(VelocityHelper.class);

    /**
     * Get name of this child in this relation context. Default name
     * from getChild may be overriden by Name attribute of relation.
     * If child doesn't have any name, it return class name.
     */
    public String getChildName(Relation relation) throws PersistanceException {
        if ( relation==null || relation.getChild()==null ) return null;
        String name = relation.getName();
        if ( name!=null && name.length()>0) return name;

        GenericObject child = relation.getChild();
        if ( !child.isInitialized() ) PersistanceFactory.getPersistance().synchronize(child);

        if ( child instanceof GenericDataObject ) {
            Document data = ((GenericDataObject)child).getData();
            if ( data!=null ) {
                Node node = data.selectSingleNode("data/name");
                if ( node!=null ) name = node.getText();
            }

        } else if ( child instanceof Link ) {
            name = ((Link)child).getText();

        } else if ( child instanceof Poll ) {
            name = ((Poll)child).getText();

        } else if ( child instanceof User ) {
            name = ((User)child).getName();

        } else if ( child instanceof Server ) {
            name = ((Server)child).getName();
        }

        if ( name==null || name.length()==0) {
            name = child.getClass().getName().substring(name.lastIndexOf('.')+1);
        }
        return normalizeEncoding(name);
    }

    /**
     * Gets text value of node selected by xpath expression for GenericObject.
     * If <code>obj</code> doesn't contain <code>Document data</code> field
     * or xpath element doesn't exist, null is returned.
     */
    public String getXPath(GenericObject obj, String xpath) throws PersistanceException {
        if ( obj==null ) return null;
        if ( !obj.isInitialized() ) PersistanceFactory.getPersistance().synchronize(obj);
        Document doc = null;
        String value = null;

        if ( obj instanceof GenericDataObject ) {
            doc = ((GenericDataObject)obj).getData();
        } else if ( obj instanceof User ) {
            doc = ((User)obj).getData();
        }
        if ( doc==null ) return null;

        Node node = doc.selectSingleNode(xpath);
        if ( node!=null ) value = node.getText();

        return normalizeEncoding(value);
    }

    /**
     * Converts string to encoding, which is best for servlets
     */
    private String normalizeEncoding(String in) {
        return in;
    }

    /**
     * Adds all parameters from request to specified map and returns it back.
     * If map is null, new HashMap is created.
     */
    public static Map putParamsToMap(HttpServletRequest request, Map map) {
        if ( map==null ) map = new HashMap();
        Enumeration names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            String value = request.getParameter(name);
            try {
                value = new String(value.getBytes("ISO-8859-1"));
            } catch (UnsupportedEncodingException e) {}
            map.put(name,value);
        }
        return map;
    }

    /**
     * Merges template specified in <code>templateName</code> with context
     * and returns it as String. If an exception is caught, empty string
     * is returned.
     * @param templateName name of template
     * @param context context holding parameters
     * @return String holding the result og merge.
     */
    public static String mergeTemplate(String templateName, Context context) {
        try {
            Template template = Velocity.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.merge(context,writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("Cannot merge template "+templateName,e);
            return "";
        }
    }

    /**
     * Creates string, which contains every item in <code>VAR_PARAMS</code> as hidden field
     * except that listed in <code>prohibited</code> list.
     */
    public String saveParams(Map params, List prohibited) {
        if ( params==null || params.size()==0 ) return "";
        if ( prohibited==null ) prohibited = new ArrayList(1);

        StringBuffer sb = new StringBuffer();
        Set entries = params.keySet();
        for (Iterator iter = entries.iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            if ( prohibited.contains(key) ) continue;
            sb.append("<input type=\"hidden\" name=\"");
            sb.append(key);
            sb.append("\" value=\"");
            sb.append(encode((String) params.get(key)));
            sb.append("\">\n");
        }
        return sb.toString();
    }

    /**
     * Does standard HTML conversions like & to &amp;amp; or &lt; to &amp;lt;.
     * @return Modified String, which may be inserted into html page without affecting its structure.
     */
    private String encode(String in) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < in.length(); i++) {
            int c = in.charAt(i);
            switch (c) {
                case '"': sb.append("&quot;");break;
                case '<': sb.append("&lt;");break;
                case '>': sb.append("&gt;");break;
                case '&': sb.append("&amp;");break;
                default: sb.append((char)c);
            }
        }
        return sb.toString();
    }
}
