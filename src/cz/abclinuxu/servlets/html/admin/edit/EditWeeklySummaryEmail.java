package cz.abclinuxu.servlets.html.admin.edit;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.security.ActionCheck;
import cz.abclinuxu.security.AdminLogger;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.servlets.AbcAutoAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.ajax.WeeklyHtmlEmail;
import cz.abclinuxu.servlets.utils.ParameterChecker;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.PageNavigation;
import cz.abclinuxu.servlets.utils.url.PwdNavigator;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.scheduler.WeeklyEmail;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;
import org.dom4j.Document;

import java.util.Calendar;
import java.util.Map;

import freemarker.template.TemplateException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This action is used to preview and edit weekly summary email.
 * User: literakl
 * Date: 5.2.2010
 */
public class EditWeeklySummaryEmail extends AbcAutoAction {
    public static final String DEFAULT_ACTION = "show";

    public static final String PARAM_HTML_VARIANT = "html";
    public static final String PARAM_PLAIN_TEXT_VARIANT = "text";

    public static final String VAR_SUBSCRIBED_COUNT = "SUBSCRIPTIONS";
    public static final String VAR_HTML_VARIANT = "HTML_VARIANT";
    public static final String VAR_PLAIN_TEXT_VARIANT = "TEXT_VARIANT";

    @Override
    protected void init(HttpServletRequest request, HttpServletResponse response, Map<String, Object> env) {
        super.init(request, response, env);
        params.put(PARAM_RELATION_SHORT, String.valueOf(Constants.REL_WEEKLY_SUMMARY_EMAIL));
    }

    @ActionCheck(userRequired = true, itemOwnerOrRole = Roles.ADVERTISEMENT_ADMIN)
    public String actionShow() throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Item item = (Item) persistence.findById(new Item(Constants.ITEM_WEEKLY_SUMMARY_EMAIL));
        Element root = item.getData().getRootElement();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        WeeklyEmail.prepareData(env, calendar);
        env.put(Constants.VAR_USER, user);

        try {
            String textVariant = WeeklyEmail.processPlainTextVariant(root, env);
            env.put(VAR_PLAIN_TEXT_VARIANT, textVariant);
        } catch (TemplateException e) {
            ServletUtils.addError(PARAM_PLAIN_TEXT_VARIANT, "V šabloně je chyba! " + e.getMessage(), env, null);
        }

        try {
            String htmlVariant = WeeklyEmail.processHtmlVariant(root, env);
            htmlVariant = htmlVariant.replace(Constants.INLINE_PREFIX, "");
            WeeklyHtmlEmail.setContent(htmlVariant); // ugly but effective
        } catch (TemplateException e) {
            ServletUtils.addError(PARAM_HTML_VARIANT, "V šabloně je chyba! " + e.getMessage(), env, null);
        }

        env.put(VAR_SUBSCRIBED_COUNT, SQLTool.getInstance().countUsersWithWeeklyEmail());

        return FMTemplateSelector.select("EditWeeklySummaryEmail", "show", env, request);
    }

    @ActionCheck(userRequired = true, itemOwnerOrRole = Roles.ADVERTISEMENT_ADMIN)
    public String actionEdit() throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Item item = (Item) persistence.findById(new Item(Constants.ITEM_WEEKLY_SUMMARY_EMAIL));
        Element root = item.getData().getRootElement();
        params.put(PARAM_HTML_VARIANT, root.elementText("html"));
        params.put(PARAM_PLAIN_TEXT_VARIANT, root.elementText("text"));

        PwdNavigator navigator = new PwdNavigator(env, PageNavigation.WEEKLY_SUMMARY_EMAIL);
        env.put(Constants.VAR_PARENTS, navigator.navigate());

        return FMTemplateSelector.select("EditWeeklySummaryEmail", "edit", env, request);
    }

    @ActionCheck(itemOwnerOrRole = Roles.ADVERTISEMENT_ADMIN, checkReferer = true, checkPost = true)
    public String actionEdit2() throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Item item = (Item) persistence.findById(new Item(Constants.ITEM_WEEKLY_SUMMARY_EMAIL)).clone();
        ParameterChecker checker = new ParameterChecker(env);

        setHtmlContent(item, checker);
        setPlainTextContent(item, checker);
        if (checker.isFailed()) {
            return FMTemplateSelector.select("EditWeeklySummaryEmail", "edit", env, request);
        }

        persistence.update(item);
        AdminLogger.logEvent(user, "  edit | weekly summary email");

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.make(UrlUtils.PREFIX_ADMINISTRATION + "/mailing/vikend"));
        return null;
    }

    /**
     * Updates content from parameters. Changes are not synchronized with persistence.
     * @param item item to be updated
     * @param text freemarker code
     */
    private void setHtmlContent(Item item, ParameterChecker checker) {
        Document doc = item.getData();
        Element element = DocumentHelper.makeElement(doc, "/data/html");
        String text = checker.getString(PARAM_HTML_VARIANT, false);
        element.setText(text);
    }

    /**
     * Updates content from parameters. Changes are not synchronized with persistence.
     * @param item item to be updated
     * @param text freemarker code
     */
    private void setPlainTextContent(Item item, ParameterChecker checker) {
        Document doc = item.getData();
        Element element = DocumentHelper.makeElement(doc, "/data/text");
        String text = checker.getString(PARAM_PLAIN_TEXT_VARIANT, false);
        element.setText(text);
    }
}
