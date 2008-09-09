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
import java.util.List;

/**
 * A virtual base for all permitted classes.
 * @author lubos
 */
public abstract class TagExpression {
    /**
     * Parses a string inputted by user
     * @param str Input string, e.g. "ubuntu heron AND (nvidia OR !ati)"
     * @return
     */
    public static TagExpression parseString(String str, List<String> tags) throws InvalidInputException {
        if (countCharacters(str, '"') % 2 != 0)
            throw new InvalidInputException("Počet uvozovek není sudý!");
        
        if (countCharacters(str, '(') != countCharacters(str, ')'))
            throw new InvalidInputException("Otevírání a zavírání závorek není korektní!");
        
        int index = 0;
        
        TagAndChain chain = new TagAndChain();
        boolean not = false, and = false, or = false;
        
        while (true) {
            StringBuffer piecesb = new StringBuffer();
            index = getNextPiece(str, index, piecesb);
            if (index == -1)
                break;
            
            String piece = piecesb.toString();
            
            if (piece.equalsIgnoreCase("AND")) {
                if (or)
                    throw new InvalidInputException("AND a OR nelze kombinovat!");
                and = true;
            } else if (piece.equalsIgnoreCase("OR")) {
                if (and)
                    throw new InvalidInputException("AND a OR nelze kombinovat!");
                or = true;
            } else if (piece.equalsIgnoreCase("NOT") || piece.equals("!"))
                not = !not;
            else {
                if (piece.length() == 0)
                    continue;
                if (piece.startsWith("!")) {
                    not = !not;
                    piece = piece.substring(1);
                }
                
                TagExpression next;
                if (piece.startsWith("(")) {
                    piece = piece.substring(1, piece.length()-1);
                    
                    TagAndChain andC = (TagAndChain) parseString(piece, tags);
                    andC.setNegate(not);
                    next = andC;
                } else {
                    TagValue value = new TagValue(piece, not);
                    tags.add(piece);
                    next = value;
                }
                
                if (or) {
                    TagExpression last = chain.popLast();
                    if (last == null)
                        throw new InvalidInputException("Nepovolené použití operátoru OR!");
                    chain.addExpression(new TagOrChain(last, next));
                } else if (and && chain.size() == 0)
                    throw new InvalidInputException("Nepovolené použití operátoru AND!");
                else {
                    chain.addExpression(next);
                }
                
                not = and = or = false;
            }
        }
        
        return chain;
    }
    
    private static int countCharacters(String str, int sym) {
        int index = 0, count = 0;
        while ((index = str.indexOf(sym, index)) != -1) {
            count++;
            index++;
        }
        return count;
    }
    
    private static int getNextPiece(String str, int index, StringBuffer piece) {
        while (index < str.length() && str.charAt(index) == ' ')
            index++;
        if (index >= str.length())
            return -1;
        
        if (str.charAt(index) == '(' || str.substring(index).startsWith("!(")) {
            // subexpression
            int end = str.indexOf(')', index+1);
            int start = index;
            
            piece.append(str.substring(start, end+1));
            return end + 1;
        }
        
        if (str.charAt(index) == '"' || str.substring(index).startsWith("!\"")) {
            // a long string
            
            if (str.charAt(index) == '!') {
                piece.append('!');
                index++;
            }
            
            int end = str.indexOf('"', index+1);
            int start = index+1;
            
            piece.append(str.substring(start, end));
            return end + 1;
        }
        
        int end = str.indexOf(' ', index+1);
        if (end == -1)
            end = str.length();
        int start = index;
        
        piece.append(str.substring(start, end));
        return end;
    }
}
