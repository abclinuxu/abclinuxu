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
import java.text.DateFormat;

/**
 * This class provides several methods, that
 * make velocity developer's life easier.
 */
public class VelocityHelper {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(VelocityHelper.class);
    static DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.SHORT);

    /**
     * Get name of this child in this relation context. Default name
     * from getChild may be overriden by relation.
     * If child doesn't have any name, it returns class name.
     */
    public String getChildName(Relation relation) throws PersistanceException {
        if ( relation==null || relation.getChild()==null ) return null;
        String name = getXPath(relation,"data/name");
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
            name = name.concat(" "+child.getId());
        }
        return name;
    }

    /**
     * Retrieves icon for child of this relation. If relation declares icon,
     * it will be used. Otherwise relation's child's icon will be returned.
     * If there is no icon at all, null will be returned.
     */
    public String getChildIcon(Relation relation) throws PersistanceException {
        if ( relation==null || relation.getChild()==null ) return null;
        String name = getXPath(relation,"data/icon");
        if ( name!=null && name.length()>0) return name;

        GenericObject child = relation.getChild();
        name = getXPath(child,"data/icon");
        return name;
    }

    /**
     * Gets text value of node selected by xpath expression for GenericObject.
     * If <code>obj</code> doesn't contain <code>Document data</code> field
     * or xpath element doesn't exist, null is returned.
     */
    public static String getXPath(GenericObject obj, String xpath) throws PersistanceException {
        if ( obj==null ) return null;
        if ( !obj.isInitialized() ) PersistanceFactory.getPersistance().synchronize(obj);
        Document doc = null;
        String value = null;

        if ( obj instanceof GenericDataObject ) {
            doc = ((GenericDataObject)obj).getData();
        } else if ( obj instanceof Relation ) {
            doc = ((Relation)obj).getData();
        } else if ( obj instanceof User ) {
            doc = ((User)obj).getData();
        }
        if ( doc==null ) return null;

        Node node = doc.selectSingleNode(xpath);
        if ( node!=null ) value = node.getText();

        return value;
    }

    /**
     * Converts string to encoding, which is best for servlets
     */
    private String normalizeEncoding(String in) {
        return in;
    }

    /**
     * Adds all parameters from request to specified map and returns it back.
     * If map is null, new HashMap is created. <p>
     * If there is only one value for a parameter, it will be stored directly
     * associated with parameter's name. But if there are at least two values,
     * they will be stored in list associated with parameter's name.
     */
    public static Map putParamsToMap(HttpServletRequest request, Map map) {
        if ( map==null ) map = new HashMap();
        Enumeration names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            String[] values = request.getParameterValues(name);

            if ( values.length==1 ) {
                String value = request.getParameter(name);
                try { value = new String(value.getBytes("ISO-8859-1")); } catch (UnsupportedEncodingException e) {}
                map.put(name,value.trim());

            } else {
                List list = new ArrayList(values.length);
                for (int i = 0; i < values.length; i++) {
                    String value = values[i];
                    try { value = new String(value.getBytes("ISO-8859-1")); } catch (UnsupportedEncodingException e) {}
                    list.add(value.trim());
                }
                map.put(name,list);
            }
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
            sb.append(encodeSpecial((String) params.get(key)));
            sb.append("\">\n");
        }
        return sb.toString();
    }

    /**
     * Takes list of relations in argument and sorts them by
     * relations.get(0).getChild()'s name in ascendant order.
     * If generic object doesn't contain property name,
     * it will be appended to the end of list.
     * @return new list, where relations are sorted by date
     */
    public List sortByName(List relations) throws PersistanceException {
        List sorted = new ArrayList(relations.size());
        int size = 0, i = 0;

        for (Iterator iter = relations.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();

            for (i=size;i>0;) {
                String current = getChildName((Relation) sorted.get(i-1));
                String used = getChildName(relation);
                if ( current.compareTo(used)<0 ) break;
                i--;
            }
            sorted.add(i,relation);
            size++;
        }

        return sorted;
    }

    /**
     * Takes list of relations in argument and sorts them by
     * relations.get(0).getChild().getUpdated() in descendant order
     * (freshest objects first). If generic object doesn't contain
     * such field, it will be appended to the end of list.
     * @return new list, where relations are sorted by date
     */
    public List sortByDate(List relations) {
        List sorted = new ArrayList(relations.size());
        int size = 0, i = 0;

        for (Iterator iter = relations.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();

            for (i=size;i>0;) {
                Relation current = (Relation) sorted.get(i-1);
                if ( ! isOlder(current.getChild(),relation.getChild()) ) break;
                i--;
            }
            sorted.add(i,relation);
            size++;
        }

        return sorted;
    }

    /**
     * Calculates percentage with correct rounding. 0<=count<=base.
     * @return Number between 0 and 100
     */
    public int percent(int count, int base) {
        if ( base<=0 ) return 0;
        double percent = 100*count/(double)base;
        return (int)(percent+0.5);
    }

    /**
     * @return String containing one asterisk for each ten percents.
     */
    public String percentBar(int percent) {
        if ( percent<5 )       return "";
        else if ( percent<15 ) return "*";
        else if ( percent<25 ) return "**";
        else if ( percent<35 ) return "***";
        else if ( percent<45 ) return "****";
        else if ( percent<55 ) return "*****";
        else if ( percent<65 ) return "******";
        else if ( percent<75 ) return "*******";
        else if ( percent<85 ) return "********";
        else if ( percent<95 ) return "*********";
        else                   return "**********";
    }

    /**
     * Does standard HTML conversions like & to &amp;amp; or &lt; to &amp;lt;.
     * @return Modified String, which may be inserted into html page without affecting its structure.
     */
    public String encodeSpecial(String in) {
        if ( in==null ) return null;
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

    /**
     * @return int value of <code>str</code> or 0
     */
    public int parseInt(String str) {
        if ( str!=null && str.length()>0 ) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {}
        }
        return 0;
    }

    /**
     * Returns formatted String according to current locale.
     */
    public String showDate(Date date) {
        if ( date==null ) return null;
        return dateFormat.format(date);
    }

    /**
     * @return true, if <code>a</code> is older than <code>b</code>.
     * If one argument doesn't contain updated property, it is considered
     * as older than the other one.
     */
    private boolean isOlder(GenericObject a, GenericObject b) {

        try {
            if ( !a.isInitialized() ) PersistanceFactory.getPersistance().synchronize(a);
            if ( !b.isInitialized() ) PersistanceFactory.getPersistance().synchronize(b);
            Date aDate = null, bDate = null;

            if ( a instanceof GenericDataObject ) aDate = ((GenericDataObject)a).getUpdated();
            else if ( a instanceof Link ) aDate = ((Link)a).getUpdated();
            else if ( a instanceof Poll ) aDate = ((Poll)a).getCreated();
            else return true; // a is older

            if ( b instanceof GenericDataObject ) bDate = ((GenericDataObject)b).getUpdated();
            else if ( b instanceof Link ) bDate = ((Link)b).getUpdated();
            else if ( b instanceof Poll ) bDate = ((Poll)b).getCreated();
            else return false; // a is fresher

            if ( a==null ) return false; // just created
            if ( b==null ) return true;
            return aDate.before(bDate);
        } catch (Exception e) {
            log.error("isOlder bug: "+a.toString()+" | "+b.toString(),e);
            return true;
        }
    }
}
