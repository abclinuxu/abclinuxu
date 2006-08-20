/*
 *  Copyright (C) 2005 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */
package cz.abclinuxu.migrate;

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.edit.*;
import cz.abclinuxu.servlets.html.view.ShowObject;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import org.dom4j.Element;

import java.util.*;

/**
 * This class has single purpose - fill database for developers with some data,
 * so they can run small abclinuxu on their computers.
 * @author literakl
 * @since 7.11.2005
 */
public class GenerateDevelDatabase {
    static User user, admin;
    static int ridArticle, ridDriver, ridHardware, ridNews, ridQuestion;

    public static void main(String[] args) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistance(PersistenceFactory.defaultDevelUrl);
        admin = (User) persistence.findById(new User(1));
        user = (User) persistence.findById(new User(2));

        generateArticles(persistence);
        generateNews(persistence);
        generateHardwareItems(persistence);
        generateDrivers();
        generateDiscussions(persistence);
        generateBlogs(persistence);
        generateDictionaryItems();
        generatePoll(persistence);
        generateFAQs(persistence);
    }

    private static void generateHardwareItems(Persistence persistence) throws Exception {
        Map map = new HashMap();
        Map params = new HashMap();
        map.put(Constants.VAR_PARAMS, params);
        map.put(Constants.VAR_USER, user);

        params.put(EditHardware.PARAM_NAME, "Askey");
        params.put(EditHardware.PARAM_SETUP, "Plug n' pray");
        params.put(EditHardware.PARAM_NOTE, "Poznámka");
        params.put(EditHardware.PARAM_IDENTIFICATION, "Tí tá tá tá tí");
        params.put(EditHardware.PARAM_TECHPARAM, "Kus drátu");
        params.put(EditHardware.PARAM_DRIVER, "kernel");
        params.put(EditHardware.PARAM_PRICE, "low");
        map.put(EditHardware.VAR_RELATION, persistence.findById(new Relation(148)));

        EditHardware servlet = new EditHardware();
        servlet.actionAddStep2(null, null, map, false);
        Relation created = (Relation) map.get(ShowObject.VAR_RELATION);
        ridHardware = created.getId();
    }

    private static void generateDiscussions(Persistence persistence) throws Exception {
        Map map = new HashMap();
        Map params = new HashMap();
        map.put(Constants.VAR_PARAMS, params);
        map.put(Constants.VAR_URL_UTILS, new UrlUtils(UrlUtils.PREFIX_CLANKY, null));

        params.put(EditDiscussion.PARAM_TITLE, "Jak nainstalovat balíèek do Mandrivy");
        params.put(EditDiscussion.PARAM_TEXT, "Na¹el jsem balíèek RPM se super aplikací, jak jej mám nainstalovat?");
        params.put(EditDiscussion.PARAM_AUTHOR_ID, "2");
        map.put(EditDiscussion.VAR_RELATION, persistence.findById(new Relation(49645)));

        EditDiscussion servlet = new EditDiscussion();
        servlet.actionAddQuestion2(null, null, map, false);
        Relation relation = (Relation) map.get(EditDiscussion.VAR_RELATION);

        params.clear();
        params.put(EditDiscussion.PARAM_DISCUSSION, Integer.toString(relation.getChild().getId()));
        params.put(EditDiscussion.PARAM_TITLE, "RTFM");
        params.put(EditDiscussion.PARAM_TEXT, "man urpmi");
        params.put(EditDiscussion.PARAM_AUTHOR, "chytrak");
        map.put(EditDiscussion.VAR_RELATION, relation);

        servlet.actionAddComment2(null, null, map, false);
        Relation created = (Relation) map.get(ShowObject.VAR_RELATION);
        ridQuestion = created.getId();
    }

    private static void generateFAQs(Persistence persistence) throws Exception {
        Map map = new HashMap();
        Map params = new HashMap();
        map.put(Constants.VAR_PARAMS, params);
        map.put(Constants.VAR_USER, user);

        map.put(EditFaq.VAR_RELATION, persistence.findById(new Relation(94480)));
        params.put(EditFaq.PARAM_TITLE, "Zapnutí DMA");
        params.put(EditFaq.PARAM_TEXT, "man hdparm");

        EditFaq servlet = new EditFaq();
        servlet.actionAddStep2(null, null, map, false);

        EditRelated servlet2 = new EditRelated();
        params.put(EditRelated.PARAM_URL, UrlUtils.PREFIX_CLANKY+"/"+ridArticle);
        params.put(EditRelated.PARAM_TITLE, "první odkaz");
        servlet2.actionAddStep2(null, null, params, false);

        params.put(EditRelated.PARAM_URL, UrlUtils.PREFIX_DRIVERS+"/"+ridDriver);
        servlet2.actionAddStep2(null, null, params, false);

        params.put(EditRelated.PARAM_URL, UrlUtils.PREFIX_HARDWARE+"/"+ridHardware);
        servlet2.actionAddStep2(null, null, params, false);

        params.put(EditRelated.PARAM_URL, "http://www.linux.cz");
        params.put(EditRelated.PARAM_TITLE, "oficialni stranky");
        params.put(EditRelated.PARAM_DESCRIPTION, "ponekud chude, nemyslite?");
        servlet2.actionAddStep2(null, null, params, false);
    }

    private static void generateBlogs(Persistence persistence) throws Exception {
        Map map = new HashMap();
        Map params = new HashMap();
        map.put(Constants.VAR_PARAMS, params);
        map.put(Constants.VAR_USER, admin);

        Relation blogRelation = (Relation) persistence.findById(new Relation(72131));
        persistence.synchronize(blogRelation.getChild());
        params.put(EditBlog.PARAM_TITLE, "Blogosféra");
        params.put(EditBlog.PARAM_CONTENT, "Blogy jsou dobrý sluha, ale ¹patný pán.");

        EditBlog servlet = new EditBlog();
        servlet.actionAddStoryStep2(null, null, blogRelation, map, false);

        params.put(EditBlog.PARAM_TITLE, "Øe¹ení základní otázky");
        params.put(EditBlog.PARAM_CONTENT, "Odpovìï zní<break>Blue screen of death. Reboot your computer please.");
        servlet.actionAddStoryStep2(null, null, blogRelation, map, false);
    }

    private static void generateDictionaryItems() throws Exception {
        Map map = new HashMap();
        Map params = new HashMap();
        map.put(Constants.VAR_PARAMS, params);
        map.put(Constants.VAR_USER, user);

        params.put(EditDictionary.PARAM_NAME, "GPL");
        params.put(EditDictionary.PARAM_DESCRIPTION, "Svobodná licence která zaruèuje v¹em u¾ivatelùm stejná práva.");

        EditDictionary servlet = new EditDictionary();
        servlet.actionAddStep2(null, null, map, false);
    }

    private static void generateDrivers() throws Exception {
        Map map = new HashMap();
        Map params = new HashMap();
        map.put(Constants.VAR_PARAMS, params);
        map.put(Constants.VAR_USER, user);

        params.put(EditDriver.PARAM_NAME, "Urychlovaè");
        params.put(EditDriver.PARAM_URL, "http://www.abclinuxu.cz");
        params.put(EditDriver.PARAM_VERSION, "0.99");
        params.put(EditDriver.PARAM_NOTE, "Zmìní vá¹ poèítaè na namydlený blesk.");

        EditDriver servlet = new EditDriver();
        servlet.actionAddStep2(null, null, map, false);
        Relation created = (Relation) map.get(ShowObject.VAR_RELATION);
        ridDriver = created.getId();
    }

    private static void generateNews(Persistence persistence) throws Exception {
        Map map = new HashMap();
        Map params = new HashMap();
        map.put(Constants.VAR_PARAMS, params);
        map.put(Constants.VAR_USER, user);

        params.put(EditNews.PARAM_TITLE, "Abíèko je Open Source!");
        params.put(EditNews.PARAM_CONTENT, "Uvolnil jsem zdrojáky Abíèka komunitì. Èas uká¾e, zda to byl dobrý krok.");
        params.put(EditNews.PARAM_CATEGORY, "INFO");

        EditNews servlet = new EditNews();
        servlet.actionAddStep2(null, null, map, false);

        Relation created = (Relation) map.get(ShowObject.VAR_RELATION);
        ridNews = created.getId();

        Item item = (Item) created.getChild();
        Element element = (Element) item.getData().selectSingleNode("/data/title");
        String url = UrlUtils.PREFIX_NEWS + "/" + URLManager.enforceLastURLPart(element.getTextTrim());
        url = URLManager.protectFromDuplicates(url);

        created.setUrl(url);
        created.setParent(new Category(Constants.CAT_NEWS));
        created.setUpper(Constants.REL_NEWS);
        persistence.update(created);

    }

    private static void generatePoll(Persistence persistence) throws Exception {
        Map map = new HashMap();
        Map params = new HashMap();
        map.put(Constants.VAR_PARAMS, params);
        map.put(Constants.VAR_USER, admin);

        map.put(EditPoll.VAR_RELATION, persistence.findById(new Relation(Constants.REL_POLLS)));
        params.put(EditPoll.PARAM_QUESTION, "Uvolnìní zdrojákù abíèka je");
        List choices = new ArrayList();
        choices.add("kravina");
        choices.add("zajímavé");
        choices.add("bomba");
        params.put(EditPoll.PARAM_CHOICES, choices);
        params.put(EditPoll.PARAM_URL, "uvolneni-zdrojaku-abicka");

        EditPoll servlet = new EditPoll();
        servlet.actionAddStep2(null, null, map, false);
    }

    private static void generateArticles(Persistence persistence) throws Exception {
        Map map = new HashMap();
        Map params = new HashMap();
        map.put(Constants.VAR_PARAMS, params);
        map.put(Constants.VAR_USER, admin);

        Relation articles = (Relation) persistence.findById(new Relation(4));
        map.put(EditArticle.VAR_RELATION, articles);
        map.put(Constants.VAR_USER, admin);
        params.put(EditArticle.PARAM_TITLE, "Jaderné noviny 000");
        params.put(EditArticle.PARAM_PEREX, "Jádro portováno do toastovaèe!");
        params.put(EditArticle.PARAM_CONTENT, "Slavný kernel hacker a fanou¹ek Linuxu JXD naportoval kernel " +
                "na toastovaè. A¾ si pøí¹tì budete dìlat toasty, o optimální teplotu se bude starat Linuks.");
        params.put(EditArticle.PARAM_AUTHOR, Integer.toString(user.getId()));
        params.put(EditArticle.PARAM_PUBLISHED, Constants.isoFormat.format(new Date()));

        EditArticle editArticle = new EditArticle();
        editArticle.actionAddStep2(null, null, map, false);

        Relation created = (Relation) map.get(ShowObject.VAR_RELATION);
        ridArticle = created.getId();

        articles = (Relation) persistence.findById(new Relation(14358));
        map.put(EditArticle.VAR_RELATION, articles);
        params.put(EditArticle.PARAM_TITLE, "Developerská databáze abclinuxu");
        params.put(EditArticle.PARAM_PEREX, "Krátké info ohlednì databáze pro vývoj abíèka.");
        params.put(EditArticle.PARAM_CONTENT, "Databáze obsahuje dva u¾ivatele - admin a user, oba mají heslo changeit. " +
                "Dále je zde pár reprezentantù v¹ech objektù, které se na abíèku vyskytují, abyste si mohli neru¹enì " +
                "hrát a zkou¹et, jak co funguje.");
        params.put(EditArticle.PARAM_AUTHOR, Integer.toString(admin.getId()));
        params.put(EditArticle.PARAM_PUBLISHED, Constants.isoFormat.format(new Date()));
        editArticle.actionAddStep2(null, null, map, false);

        Relation article = (Relation) map.get(EditArticle.VAR_RELATION);
        Relation dizRelation = EditDiscussion.createEmptyDiscussion(article, user, persistence);

        params.clear();
        params.put(EditDiscussion.PARAM_DISCUSSION, Integer.toString(dizRelation.getChild().getId()));
        params.put(EditDiscussion.PARAM_TITLE, "WTF?");
        params.put(EditDiscussion.PARAM_TEXT, "I never expected this to happen ..");
        params.put(EditDiscussion.PARAM_RELATION_SHORT, Integer.toString(dizRelation.getId()));

        EditDiscussion editDiz = new EditDiscussion();
        editDiz.actionAddComment2(null, null, map, false);
    }
}
