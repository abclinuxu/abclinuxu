/*
 * User: literakl
 * Date: Apr 21, 2002
 * Time: 8:51:06 AM
 */
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.news.NewsCategories;
import cz.abclinuxu.utils.news.NewsCategory;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.utils.search.CreateIndex;
import cz.abclinuxu.utils.search.AbcCzechAnalyzer;
import cz.abclinuxu.utils.search.AbcQueryParser;
import cz.abclinuxu.utils.search.MyDocument;
import cz.abclinuxu.data.User;
import org.apache.lucene.search.*;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.dom4j.Node;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.text.NumberFormat;

/**
 * Performs search across the data.
 */
public class Search implements AbcAction {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Search.class);
    static org.apache.log4j.Category seachLog = org.apache.log4j.Category.getInstance("search");

    /** contains relation, that match the expression */
    public static final String VAR_RESULT = "RESULT";
    /** query to be searched */
    public static final String VAR_QUERY = "QUERY";
    /** total number of found documents */
    public static final String VAR_TOTAL = "TOTAL";
    /** holds map of chosen types */
    public static final String VAR_TYPES = "TYPES";
    public static final String VAR_NEWS_CATEGORIES = "CATEGORIES";

    /** expression to be searched */
    public static final String PARAM_QUERY = "query";
    /** type of object to search */
    public static final String PARAM_TYPE = "type";
    /** n-th oldest object, from where to display */
    public static final String PARAM_FROM = "from";
    /** how many objects to display */
    public static final String PARAM_COUNT = "count";
    /** id of parental relation */
    public static final String PARAM_PARENT = "parent";
    /** news category */
    public static final String PARAM_CATEGORY = "category";


    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        return performSearch(request, env);
    }

    public static String performSearch(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Types types = new Types(params.get(PARAM_TYPE));
        env.put(VAR_TYPES, types);
        boolean onlyNews = types.size()==1 && types.isNews();
        NewsCategoriesSet newsCategoriesSet = getNewsCategories(params);

        String queryString = (String) params.get(PARAM_QUERY);
        if ( queryString == null || queryString.length()==0 )
            return choosePage(onlyNews, request, env, newsCategoriesSet);

        env.put(VAR_QUERY,queryString);

        Query query = null;
        try {
            query = AbcQueryParser.parse(queryString, new AbcCzechAnalyzer(), types, newsCategoriesSet);
            query = AbcQueryParser.addParentToQuery((String)params.get(PARAM_PARENT), query);
            seachLog.info(queryString);
        } catch (ParseException e) {
            ServletUtils.addError(PARAM_QUERY, "Hledaný øetìzec obsahuje chybu!", env, null);
            return choosePage(onlyNews, request, env, newsCategoriesSet);
        }

        try {
            int from = getFrom(params);
            int count = getPageSize(params, env);
            List list = new ArrayList(count);
            NumberFormat percentFormat = NumberFormat.getPercentInstance();

            Searcher searcher = new IndexSearcher(CreateIndex.getIndexPath());
            Hits hits = searcher.search(query);
            int total = hits.length();
            for ( int i=from,j=0; i<total && j<count; i++, j++ ) {
                Document doc = hits.doc(i);
                float score = (hits.score(i)>0.01) ? hits.score(i) : 0.01f;
                doc.add(Field.UnIndexed("score", percentFormat.format(score)));
                list.add(doc);
            }

            Paging paging = new Paging(list,from,count,total);
            env.put(VAR_RESULT,paging);
            env.put(VAR_TOTAL,new Integer(total));
        } catch (Exception e) {
            log.error("Cannot search "+query,e);
            ServletUtils.addError(PARAM_QUERY,"Nemohu provést dané hledání. Zadejte jiný øetìzec!",env,null);
            return choosePage(onlyNews, request, env, newsCategoriesSet);
        }

        return choosePage(onlyNews, request, env, newsCategoriesSet);
    }

    private static String choosePage(boolean displayNews, HttpServletRequest request, Map env, NewsCategoriesSet newsCategories) throws Exception {
        if (displayNews) {
            env.put(VAR_NEWS_CATEGORIES, newsCategories);
            return FMTemplateSelector.select("Search", "news", env, request);
        } else
            return FMTemplateSelector.select("Search", "show", env, request);
    }

    /**
     * Converts selected categories to list.
     */
    private static List getSelectedCategories(Map params) {
        Object o = params.get(PARAM_CATEGORY);
        if (o==null)
            return Collections.EMPTY_LIST;
        if (o instanceof List)
            return (List) o;
        List list = new ArrayList(1);
        list.add(o);
        return list;
    }

    public static NewsCategoriesSet getNewsCategories(Map params) {
        List selected = getSelectedCategories(params);
        return new NewsCategoriesSet(selected);
    }

    /**
     * Extracts value of FROM encoded in parameter name.
     */
    private static int getFrom(Map params) {
        for ( Iterator iter = params.keySet().iterator(); iter.hasNext(); ) {
            String param = (String) iter.next();
            if (!param.startsWith(PARAM_FROM)) continue;
            if (param.length()<6) continue;
            int from = Misc.parseInt(param.substring(5), 0);
            return from;
        }
        return 0;
    }

    /**
     * Gets page size for found documents. Paramaters take precendence over user settings.
     * @return page size for found documents.
     */
    private static int getPageSize(Map params, Map env) {
        int count = -1;
        String str = (String) params.get(PARAM_COUNT);
        if (str!=null && str.length()>0)
            count = Misc.parseInt(str, -1);

        User user = (User) env.get(Constants.VAR_USER);
        if (user!=null && count<0) {
            Node node = user.getData().selectSingleNode("/data/settings/found_size");
            if ( node!=null )
                count = Misc.parseInt(node.getText(),-1);
        }

        if (count==-1)
            return 50;
        else
            return Misc.limit(count, 10, 100);
    }

    public static class NewsCategoriesSet extends AbstractCollection {
        List list;
        List selected;

        public NewsCategoriesSet(List selected) {
            this.selected = selected;
            Collection categories = NewsCategories.getAllCategories();
            list = new ArrayList(categories.size());
            for ( Iterator iter = categories.iterator(); iter.hasNext(); ) {
                NewsCategory newsCategory = (NewsCategory) iter.next();
                add(new SelectedNewsCategory(newsCategory, selected));
            }
        }

        public boolean isNothingSelected() {
            return selected.size()==0;
        }

        public boolean isEverythingSelected() {
            return selected.size()==list.size();
        }

        public List getSelected() {
            return selected;
        }

        public Iterator iterator() {
            return list.iterator();
        }

        public int size() {
            return list.size();
        }

        public boolean add(Object o) {
            return list.add(o);
        }
    }

    public static class SelectedNewsCategory extends NewsCategory {
        boolean set;

        public SelectedNewsCategory(NewsCategory category, List selected) {
            super(category.getKey(), category.getName(), category.getDesc());
            setSet(selected);
        }

        public boolean isSet() {
            return set;
        }

        public void setSet(boolean set) {
            this.set = set;
        }

        public void setSet(List selected) {
            if (selected.size()==0)
                set = true;
            else
                set = selected.contains(getKey());
        }
    }

    public static class Types {
        Map map = new HashMap();

        public Types(Object param) {
            if (param==null)
                return;

            List params;
            if (param instanceof String) {
                params = new ArrayList(1);
                params.add(param);
            } else
                params = (List) param;

            for ( Iterator iter = params.iterator(); iter.hasNext(); ) {
                String s = (String) iter.next();
                map.put(s, Boolean.TRUE);
            }
        }

        public boolean isNothingSelected() {
            return map.size()==0;
        }

        public boolean isEverythingSelected() {
            return map.size()==MyDocument.ALL_TYPES_COUNT;
        }

        public boolean isSection() {
            if ( map.size()==0 )
                return true;
            return map.containsKey(MyDocument.TYPE_CATEGORY);
        }

        public boolean isHardware() {
            if ( map.size()==0 )
                return true;
            return map.containsKey(MyDocument.TYPE_HARDWARE);
        }

        public boolean isSoftware() {
            if ( map.size()==0 )
                return true;
            return map.containsKey(MyDocument.TYPE_SOFTWARE);
        }

        // todo verifikovat zda to skutecne vyhledava jen v ovladacich
        public boolean isDriver() {
            if ( map.size()==0 )
                return true;
            return map.containsKey(MyDocument.TYPE_DRIVER);
        }

        public boolean isDiscussion() {
            if ( map.size()==0 )
                return true;
            return map.containsKey(MyDocument.TYPE_DISCUSSION);
        }

        public boolean isArticle() {
            if ( map.size()==0 )
                return true;
            return map.containsKey(MyDocument.TYPE_ARTICLE);
        }

        public boolean isNews() {
            if ( map.size()==0 )
                return true;
            return map.containsKey(MyDocument.TYPE_NEWS);
        }

        public boolean isBlog() {
            if ( map.size()==0 )
                return true;
            return map.containsKey(MyDocument.TYPE_BLOG);
        }

        // todo verifikovat zda to skutecne vyhledava jen v pojmech
        public boolean isDictionary() {
            if ( map.size()==0 )
                return true;
            return map.containsKey(MyDocument.TYPE_DICTIONARY);
        }

        public Map getMap() {
            return Collections.unmodifiableMap(map);
        }

        public int size() {
            return map.size();
        }
    }
}
