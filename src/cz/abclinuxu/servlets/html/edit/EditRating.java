/*
 * User: literakl
 * Date: 6.7.2004
 * Time: 15:31:48
 */
package cz.abclinuxu.servlets.html.edit;

import org.dom4j.Element;
import org.dom4j.Document;

import java.util.Map;
import java.util.prefs.Preferences;

import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.GenericDataObject;
import cz.abclinuxu.exceptions.MissingArgumentException;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Utility class for manipulation with ratings.
 */
public class EditRating implements AbcAction, Configurable {
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_TYPE = "rtype";
    public static final String PARAM_VALUE = "rvalue";

    public static final int VALUE_MIN = 1;
    public static final int VALUE_MAX = 5;

//    public static final String PREF_TEMPLATE_START = "template.start";
//    public static final String PREF_TEMPLATE_END = "template.end";
    public static final String PREF_MESSAGE_OK = "msg.ok";
    public static final String PREF_MESSAGE_MISSING_DATA = "msg.data.missing";
    public static final String PREF_MESSAGE_ALREADY_RATED = "msg.already.rated";

    public static final String SESSION_PREFIX = "rating_";

    static String msgOK, msgMissingData, msgAlreadyRated;
    static {
        ConfigurationManager.getConfigurator().configureAndRememberMe(new EditRating());
    }

    /**
     * Rates object identified by relation id and redirects back.
     */
    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);

        if ( relation!=null ) {
            relation = (Relation) persistance.findById(relation);
            persistance.synchronize(relation.getChild());
        } else
            throw new MissingArgumentException("Chybí parametr relationId!");

        GenericDataObject object = ((GenericDataObject)relation.getChild());
        Document data = object.getData();
        synchronized (data.getRootElement()) {
            String key = generateKey(relation.getId());
            boolean result = EditRating.rate(data.getRootElement(), key, params, env, request.getSession());
            if ( result ) {
                ServletUtils.addError(Constants.ERROR_GENERIC, msgOK, env, null);
                persistance.update(object);
            }
        }

        return "/print/misc/rating_result.ftl";
    }

    /**
     * @param id identificator of the object
     * @return key to be used in rate method
     */
    public static String generateKey(int id) {
        return SESSION_PREFIX+id;
    }

    /**
     * @param id identificator of the object
     * @return key to be used in rate method
     */
    public static String generateKey(int id, int subId) {
        return SESSION_PREFIX+id+"_"+subId;
    }

    /**
     * Adds user vote to rating of this object. This method
     * doesn't care of either synchronization or persistance.
     * This shall be handled by caller.
     * @param object root element for the rated object.
     * @param key this object's key used to track multiple rating
     * @param params parameters
     * @return true if rating was successfull
     */
    public static boolean rate(Element object, String key, Map params, Map env, HttpSession session) {
        String type = (String) params.get(PARAM_TYPE);
        int value = Misc.parseInt((String) params.get(PARAM_VALUE), 0);
        if (type==null || type.length()==0 || value<VALUE_MIN || value>VALUE_MAX) {
            ServletUtils.addError(Constants.ERROR_GENERIC, msgMissingData, env, session);
            return false;
        }

        key = key + "_" + type;
        if (session.getAttribute(key)!=null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, msgAlreadyRated, env, session);
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

        session.setAttribute(key, Boolean.TRUE);
        return true;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        msgOK = prefs.get(PREF_MESSAGE_OK, "");
        msgMissingData = prefs.get(PREF_MESSAGE_MISSING_DATA, "");
        msgAlreadyRated = prefs.get(PREF_MESSAGE_ALREADY_RATED, "");
    }
}
