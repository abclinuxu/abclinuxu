/*
 * User: literakl
 * Date: 31.8.2003
 * Time: 20:25:13
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.security.Guard;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Serves for manipulating of surveys.
 */
public class EditSurvey extends AbcFMServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditSurvey.class);

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT2 = "edit2";


    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

//        if ( ACTION_ADD.equals(action) ) {
//            int rights = Guard.check(user, category, Guard.OPERATION_ADD, Category.class);
//            switch ( rights ) {
//                case Guard.ACCESS_LOGIN:
//                    return FMTemplateSelector.select("ViewUser", "login", env, request);
//                case Guard.ACCESS_DENIED:
//                    return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
//                default:
//                    return FMTemplateSelector.select("EditCategory", "add", env, request);
//            }
//
//        } else if ( ACTION_ADD_STEP2.equals(action) ) {
//            int rights = Guard.check(user, category, Guard.OPERATION_ADD, Category.class);
//            switch ( rights ) {
//                case Guard.ACCESS_LOGIN:
//                    return FMTemplateSelector.select("ViewUser", "login", env, request);
//                case Guard.ACCESS_DENIED:
//                    return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
//                default:
//                    return actionAddStep2(request, response, env);
//            }
//
//        } else if ( ACTION_EDIT.equals(action) ) {
//            int rights = Guard.check(user, category, Guard.OPERATION_EDIT, null);
//            switch ( rights ) {
//                case Guard.ACCESS_LOGIN:
//                    return FMTemplateSelector.select("ViewUser", "login", env, request);
//                case Guard.ACCESS_DENIED:
//                    return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
//                default:
//                    return actionEditStep1(request, env);
//            }
//
//        } else if ( ACTION_EDIT2.equals(action) ) {
//            int rights = Guard.check(user, category, Guard.OPERATION_EDIT, null);
//            switch ( rights ) {
//                case Guard.ACCESS_LOGIN:
//                    FMTemplateSelector.select("ViewUser", "login", env, request);
//                case Guard.ACCESS_DENIED:
//                    return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
//                default:
//                    return actionEditStep2(request, response, env);
//            }
//
//        }
        return FMTemplateSelector.select("EditCategory", "add", env, request);
    }
}
