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
 * A chain of values joined by "AND"
 * @author lubos
 */
public class TagAndChain extends TagExpression {
    List<TagExpression> objects;
    boolean negate;
    
    public TagAndChain() {
        objects = new ArrayList<TagExpression>(3);
        negate = false;
    }
    
    public void addExpression(TagExpression expr) {
        objects.add(expr);
    }
    
    public TagExpression popLast() {
        if (objects.size() == 0)
            return null;
        
        return objects.remove(objects.size()-1);
    }
    
    public void setNegate(boolean negate) {
        this.negate = negate;
    }
    
    public boolean getNegate() {
        return negate;
    }
    
    public int size() {
        return objects.size();
    }
    
    @Override
    public String toString() {
        if (objects.size() == 0)
            throw new InvalidInputException("Prázdná řada TagAndChain!");
        if (objects.size() == 1) {
            if (negate)
                return "NOT "+objects.get(0).toString();
            else
                return objects.get(0).toString();
        }
        
        StringBuilder sb = new StringBuilder();
        Iterator it = objects.iterator();
        boolean first = true;
        
        sb.append(negate ? "NOT (" : "(");
        
        while (it.hasNext()) {
            Object obj = it.next();
            
            if (!first)
                sb.append(" AND ");
            sb.append(obj.toString());
            
            first = false;
        }
        
        sb.append(")");
        
        return sb.toString();
    }
}
