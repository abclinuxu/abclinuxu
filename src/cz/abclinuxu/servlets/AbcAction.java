package cz.abclinuxu.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/*
 * User: literakl
 * Date: 8.2.2004
 * Time: 21:10:56
 */

/**
 * Contains logic.
 */
public interface AbcAction {
    /** holds action to be invoked */
    String PARAM_ACTION = "action";

    /**
     * Put your processing here. Return null, if you have redirected browser to another URL.
     * @param env holds all variables, that shall be available in template, when it is being processed.
     * It may also contain VAR_USER and VAR_PARAMS objects.
     * @return name of template to be executed or null
     */
    String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception;
}
