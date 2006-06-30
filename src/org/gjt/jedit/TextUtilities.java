/*
 * TextUtilities.java - Various text functions
 * Copyright (C) 1998, 2003 Slava Pestov
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.gjt.jedit;

//{{{ Imports
import java.util.*;
//}}}

/**
 * Contains several text manipulation methods.
 *
 * <ul>
 * <li>Bracket matching
 * <li>Word start and end offset calculation
 * <li>String comparison
 * <li>Converting tabs to spaces and vice versa
 * <li>Wrapping text
 * <li>String case conversion
 * </ul>
 *
 * @author Slava Pestov
 * @version $Id$
 */
public class TextUtilities
{

	//{{{ tabsToSpaces() method
	/**
	 * Converts tabs to consecutive spaces in the specified string.
	 * @param in The string
	 * @param tabSize The tab size
	 */
	public static String tabsToSpaces(String in, int tabSize)
	{
		StringBuffer buf = new StringBuffer();
		int width = 0;
		for(int i = 0; i < in.length(); i++)
		{
			switch(in.charAt(i))
			{
			case '\t':
				int count = tabSize - (width % tabSize);
				width += count;
				while(--count >= 0)
					buf.append(' ');
				break;
			case '\n':
				width = 0;
				buf.append(in.charAt(i));
				break;
			default:
				width++;
				buf.append(in.charAt(i));
				break;
                        }
                }
                return buf.toString();
	} //}}}

	//{{{ format() method
	/**
	 * Formats the specified text by merging and breaking lines to the
	 * specified width.
	 * @param text The text
	 * @param maxLineLength The maximum line length
	 * @param tabSize The tab size
	 */
	public static String format(String text, int maxLineLength, int tabSize)
	{
		StringBuffer buf = new StringBuffer();

		int index = 0;

		for(;;)
		{
			int newIndex = text.indexOf("\n\n",index);
			if(newIndex == -1)
				break;

			formatParagraph(text.substring(index,newIndex),
				maxLineLength,tabSize,buf);
			buf.append("\n\n");
			index = newIndex + 2;
		}

		if(index != text.length())
		{
			formatParagraph(text.substring(index),
				maxLineLength,tabSize,buf);
		}

		return buf.toString();
	} //}}}

	//{{{ toTitleCase() method
	/**
	 * Converts the specified string to title case, by capitalizing the
	 * first letter.
	 * @param str The string
	 * @since jEdit 4.0pre1
	 */
	public static String toTitleCase(String str)
	{
		if(str.length() == 0)
			return str;
		else
		{
			return Character.toUpperCase(str.charAt(0))
				+ str.substring(1).toLowerCase();
		}
	} //}}}

	//{{{ formatParagraph() method
	private static void formatParagraph(String text, int maxLineLength,
		int tabSize, StringBuffer buf)
	{
		// align everything to paragraph's leading indent
		int leadingWhitespaceCount = MiscUtilities.getLeadingWhiteSpace(text);
		String leadingWhitespace = text.substring(0,leadingWhitespaceCount);
		int leadingWhitespaceWidth = MiscUtilities.getLeadingWhiteSpaceWidth(text,tabSize);

		buf.append(leadingWhitespace);

		int lineLength = leadingWhitespaceWidth;
		StringTokenizer st = new StringTokenizer(text);
		while(st.hasMoreTokens())
		{
			String word = st.nextToken();
			if(lineLength == leadingWhitespaceWidth)
			{
				// do nothing
			}
			else if(lineLength + word.length() + 1 > maxLineLength)
			{
				buf.append('\n');
				buf.append(leadingWhitespace);
				lineLength = leadingWhitespaceWidth;
			}
			else
			{
				buf.append(' ');
				lineLength++;
			}
			buf.append(word);
			lineLength += word.length();
		}
	} //}}}

	//}}}
}
