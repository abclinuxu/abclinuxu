package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.ReadRecorder;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Server;
import cz.abclinuxu.exceptions.MissingArgumentException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import java.io.IOException;
import java.net.URLEncoder;

import freemarker.template.TemplateException;

/**
 * Share page identified by relation id.
 * User: literakl
 * Date: 18.4.2009
 */
public class SharePage implements AbcAction, Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SharePage.class);

    public static final String PARAM_RELATION = "rid";
    public static final String PARAM_SERVICE = "s";

    public static final String VAR_LINK_URL = "LINK_URL";
    public static final String VAR_LINK_TITLE = "LINK_TITLE";

    public static final String PREF_SERVICES = "services";
    public static final String PREF_URL = ".url";
    public static final String PREF_ID = ".id";

    private static Map<String, Service> services = Collections.emptyMap();

    static {
        SharePage instance = new SharePage();
        ConfigurationManager.getConfigurator().configureAndRememberMe(instance);
    }

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Chybí parametr rid!");
        Tools.sync(relation);

        String type = (String) params.get(PARAM_SERVICE);
        if ("email".equals(type))
            return null; // TODO

        Service service = services.get(type);
        if (service == null)
            throw new MissingArgumentException("Služba '" + type + "' nebyla nalezena!");

        return handleSocialBookmark(relation, service, response, env);
    }

    private String handleSocialBookmark(Relation relation, Service service, HttpServletResponse response, Map env) throws IOException {
        Map map = new HashMap();
        map.put(VAR_LINK_TITLE, URLEncoder.encode(Tools.childName(relation), "UTF-8"));
        String tmp = AbcConfig.getAbsoluteUrl() + UrlUtils.getRelationUrl(relation, UrlUtils.PREFIX_NONE);
        map.put(VAR_LINK_URL, URLEncoder.encode(tmp, "UTF-8"));
        try {
            String url = FMUtils.executeCode(service.url, map);
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, url);

            ReadRecorder.log(relation.getChild(), Constants.COUNTER_LINK, env);
            ReadRecorder.log(new Server(service.id), Constants.COUNTER_LINK, env);

            return null;
        } catch (TemplateException e) {
            log.error("Failed to redirect to " + service + ", " + relation, e);
            return null;
        }
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        Map<String, Service> newServices = new HashMap<String, Service>();
        String list = prefs.get(PREF_SERVICES, "");
        StringTokenizer stk = new StringTokenizer(list, ",");
        while (stk.hasMoreTokens()) {
            String key = stk.nextToken();
            String url = prefs.get(key + PREF_URL, null);
            int id = prefs.getInt(key + PREF_ID, 0);
            if (url == null || id == 0) {
                log.warn("Failed to initialize bookmark service '" + key + "'");
                continue;
            }
            newServices.put(key, new Service(url, id));
        }
        services = newServices;
    }

    private class Service {
        int id;
        String url;

        private Service(String url, int id) {
            this.id = id;
            this.url = url;
        }

        @Override
        public String toString() {
            return url;
        }
    }
}
