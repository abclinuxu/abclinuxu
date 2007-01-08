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

import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
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
        Persistence persistence = PersistenceFactory.getPersistance();

        List blogs = sqlTool.findCategoryRelationsWithType(Category.BLOG, null);
        for (Iterator iter = blogs.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();
            Category blog = (Category) persistence.findById(relation.getChild());
            User user = (User) persistence.findById(new User(blog.getOwner()));
            Element element = (Element) user.getData().selectSingleNode("//settings/blog");
            element.addAttribute("name", blog.getSubType());
            persistence.update(user);
            System.out.println("Nastaveno jmeno blogu uzivateli "+user.getName()+" na "+blog.getSubType());
        }
    }
}
