/*
 * User: literakl
 * Date: Jun 11, 2002
 * Time: 8:14:22 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.utils;

import cz.abclinuxu.servlets.AbcVelocityServlet;
import cz.abclinuxu.servlets.edit.EditUser;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.data.User;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Attribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * This class works as interface to ILikeQ payments.
 */
public class Reward extends AbcVelocityServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Reward.class);
    static org.apache.log4j.Logger logReward = org.apache.log4j.Logger.getLogger("rewards");


    /** called by ILikeQ engine to log payment */
    public static final String ACTION_LOG = "log";
    /** called, when payment is complete */
    public static final String ACTION_THANKS = "thanks";

    /** id of receiver of the payment */
    public static final String PARAM_REWARDED = "who";
    /** how many Q's he has received */
    public static final String PARAM_AMOUNT = "amount";
    /** status of the payment, 0 means OK */
    public static final String PARAM_STATUS = "status";
    /** how user has described this payment */
    public static final String PARAM_MESSAGE = "detail2";


    protected String process(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        String action = (String) request.getParameter(AbcVelocityServlet.PARAM_ACTION);

        if ( action==null || action.length()==0 ) {
            throw new Exception("Chybí paramater action!");
        } else if ( action.equals(ACTION_LOG) ) {
            return actionLog(request,response,ctx);
        } else if ( action.equals(ACTION_THANKS) ) {
            return actionThanks(request,response,ctx);
        }
        return null;
    }

    /**
     * Called by ILikeQ engine to log event
     */
    protected String actionLog(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = null;

        int status = 0, amount = 0, id = 0;
        String tmp = (String) request.getParameter(PARAM_REWARDED);
        try {
            id = Integer.parseInt(tmp);
	    user = new User(id);
            persistance.synchronize(user);
	    tmp = (String) request.getParameter(PARAM_AMOUNT);
            amount = Integer.parseInt(tmp);
            tmp = (String) request.getParameter(PARAM_STATUS);
            status = Integer.parseInt(tmp);
        } catch (NumberFormatException e) {
            log.warn("Cannot parse integer "+tmp,e);
        }

        if ( status!=0 ) {
            log.warn("Payment was not successful! Status is "+status);
            return null;
        }

        Document document = user.getData();
        Element node = (Element) document.selectSingleNode("data/ilikeq");
        if (node==null) {
            log.warn("Payment cannot be assigned to user, because he has no ilikeq tag! Impossible."+user);
            return null;
        }

        Attribute attribute = node.attribute("amount");
        if ( attribute==null ) {
            node.addAttribute("amount",""+amount);
        } else {
            try {
                int original = Integer.parseInt(attribute.getValue());
                attribute.setValue(""+(original+amount));
            } catch (NumberFormatException e) {
                log.warn("Cannot parse attribute amount "+attribute.getValue(),e);
            }
        }
        logReward.info(user.getId()+" | "+user.getName()+"|"+amount);
        return null;
    }

    /**
     * Called by ILikeQ engine to log event
     */
    protected String actionThanks(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);
        ServletUtils.addMessage("Va¹e platba probìhla v poøádku. Dìkujeme.",ctx,request.getSession());
        UrlUtils.redirect(response, "/Index", ctx);
        return null;
    }
}
