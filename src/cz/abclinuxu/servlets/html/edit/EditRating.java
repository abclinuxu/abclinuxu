/*
 * User: literakl
 * Date: 6.7.2004
 * Time: 15:31:48
 */
package cz.abclinuxu.servlets.html.edit;

import org.dom4j.Element;

import java.util.Map;

import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.Constants;

import javax.servlet.http.HttpSession;

/**
 * Utility class for manipulation with ratings.
 */
public class EditRating {
    public static final String PARAM_TYPE = "rtype";
    public static final String PARAM_VALUE = "rvalue";

    public static final int VALUE_MIN = 1;
    public static final int VALUE_MAX = 5;

    /**
     * Adds user vote to rating of this object. This method
     * doesn't care of either synchronization or persistance.
     * This shall be handled by caller.
     * @param object root element for the rated object.
     * @param params parameters
     * @return true if rating was successfull
     */
    public static boolean rate(Element object, Map params, Map env, HttpSession session) {
        String type = (String) params.get(PARAM_TYPE);
        int value = Misc.parseInt((String) params.get(PARAM_VALUE), 0);
        if (type==null || type.length()==0 || value<VALUE_MIN || value>VALUE_MAX) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Chyba v parametrech pro hlasování", env, session);
            return false;
        }

        int sum = 0, count = 0;
        Element rating = (Element) object.selectSingleNode("rating[type/text()=\""+type+"\"]");
        if (rating==null) {
            rating = object.addElement("rating");
            rating.addElement("type").setText(type);
            rating.addElement("sum");
            rating.addElement("count");
        } else {
            sum = Misc.parseInt(rating.elementText("sum"), 0);
            count = Misc.parseInt(rating.elementText("count"), 0);
        }

        rating.element("sum").setText(Integer.toString(sum+value));
        rating.element("count").setText(Integer.toString(count+1));

        return true;
    }
}
