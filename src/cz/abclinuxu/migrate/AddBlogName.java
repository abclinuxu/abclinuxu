package cz.abclinuxu.migrate;

import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;

import java.util.List;
import java.util.Iterator;

import org.dom4j.Element;

/**
 * Add attribute blog name to the blog element of each user,
 * who has blog.
 * User: literakl
 * Date: 12.2.2005
 */
public class AddBlogName {

    public static void main(String[] args) {
        SQLTool sqlTool = SQLTool.getInstance();
        Persistance persistance = PersistanceFactory.getPersistance();

        List blogs = sqlTool.findSectionRelationsWithType(Category.SECTION_BLOG, null);
        for (Iterator iter = blogs.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();
            Category blog = (Category) persistance.findById(relation.getChild());
            User user = (User) persistance.findById(new User(blog.getOwner()));
            Element element = (Element) user.getData().selectSingleNode("//settings/blog");
            element.addAttribute("name", blog.getSubType());
            persistance.update(user);
            System.out.println("Nastaveno jmeno blogu uzivateli "+user.getName()+" na "+blog.getSubType());
        }
    }
}
