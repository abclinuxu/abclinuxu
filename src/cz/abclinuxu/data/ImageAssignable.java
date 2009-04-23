/*
 *  Copyright (C) 2009 Karel Piwko
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
package cz.abclinuxu.data;

/**
 * Allows object to have assigned image(s).
 * These image(s) can be either assigned or detracted. 
 * 
 * Object implementing this interface is supposed to store given information in
 * its content. No changes to persistence layer are made.
 * 
 * Each object allow to manage arbitrary number of images. 
 * It is supposed to have different behavior for distinct image numbers
 * @author kapy
 */
public interface ImageAssignable {
	
	/**
	 * Informs object where image assigned to it is stored
	 * @param imageNo Number of image 
	 * @param imageUrl
	 */
	public void assignImage(int imageNo, String imageUrl);
	
	/**
	 * Removes image information from object
	 * @param imageNo Number of image
	 * @return Returns URL where image is stored
	 */
	public String detractImage(int imageNo);
	
	/**
	 * Makes proposal about where image should be stored.
	 * This path should be unique for each object and should
	 * not be absolute. 
	 * @param imageNo Number of image
	 * @param suffix File extension
	 * @return URL where image can be stored 
	 */
	public String proposeImageUrl(int imageNo, String suffix);

}
