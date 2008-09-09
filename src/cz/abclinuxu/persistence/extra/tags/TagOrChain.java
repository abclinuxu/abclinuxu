/*
 *  Copyright (C) 2008 Leos Literak
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

package cz.abclinuxu.persistence.extra.tags;

import cz.abclinuxu.exceptions.InvalidInputException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author lubos
 */
public class TagOrChain extends TagExpression {
    List objects;
    
    public TagOrChain() {
        objects = new ArrayList(3);
    }
    
    public TagOrChain(TagExpression e1, TagExpression e2) {
        objects = new ArrayList(3);
        
        addExpression(e1);
        addExpression(e2);
    }
    
    public void addExpression(TagExpression expr) {
        objects.add(expr);
    }
    
    @Override
    public String toString() {
        if (objects.size() == 0)
            throw new InvalidInputException("Prázdná řada TagOrChain!");
        if (objects.size() == 1)
            return objects.get(0).toString();
        
        StringBuilder sb = new StringBuilder("(");
        Iterator it = objects.iterator();
        
        while (it.hasNext()) {
            Object obj = it.next();
            
            if (sb.length() > 1)
                sb.append(" OR ");
            sb.append(obj.toString());
        }
        
        sb.append(")");
        
        return sb.toString();
    }
}
