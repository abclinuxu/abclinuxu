/*
 *  Copyright (C) 2008 Karel Piwko
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
package cz.abclinuxu.data.view;

import cz.abclinuxu.data.Tag;
import cz.abclinuxu.utils.TagTool;

/**
 * Allows Tag to have weight and font size assigned and to be sorted by
 * title of tag
 * @author kapy
 *
 */
public class CloudTag extends Tag implements Comparable<CloudTag>{
	private float weight;
	private String cssClass;

	/**
	 * Constructs CloudTag by cloning raw tag and computing its weight by
	 * additional arguments <code>minOccurs</code> and <code>maxOccurs</code>
	 * @param tag Raw tag
	 * @param minOccurs Bottom threshold for tag occurrence
	 * @param maxOccurs Top threshold for tag occurrence
	 */
	public CloudTag(Tag tag, int minOccurs, int maxOccurs) {
		super(tag);
		this.weight = TagTool.tagWeight(tag, minOccurs, maxOccurs);
//        this.fontSize = minFontSize + Math.round((maxFontSize - minFontSize) * weight);
        if (weight < 0.05)
            cssClass = "class0";
        else if (weight < 0.15)
            cssClass = "class1";
        else if (weight < 0.25)
            cssClass = "class2";
        else if (weight < 0.35)
            cssClass = "class3";
        else if (weight < 0.45)
            cssClass = "class4";
        else if (weight < 0.55)
            cssClass = "class5";
        else if (weight < 0.65)
            cssClass = "class6";
        else if (weight < 0.75)
            cssClass = "class7";
        else if (weight < 0.85)
            cssClass = "class8";
        else
            cssClass = "class9";
	}

	/**
	 * Gets css class id for tag with this weight
	 * @return css class
	 */
	public String getCssClass() {
		return this.cssClass;
	}

	public int compareTo(CloudTag other) {
		return (other!=null) ? title.compareTo(other.title) : 0;
	}

}
