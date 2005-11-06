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
package cz.abclinuxu.data;

/**
 * one choice of the poll. Its order in the poll is identified
 * by order in <code>choices</code> array of Poll.
 */
public class PollChoice {

    /** text of the poll choice */
    protected String text;

    /** number of votes for this choice */
    protected int count;

    /** associated poll's id */
    protected int poll;

    /** order of choice in the poll, used by incrementCounter */
    protected int id;


    public PollChoice(String text) {
        this.text = text;
    }

    /**
     * @return text of the poll choice
     */
    public String getText() {
        return text;
    }

    /**
     * sets text of the poll choice
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return number of votes for this choice
     */
    public int getCount() {
        return count;
    }

    /**
     * sets number of votes for this choice
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * This method may be called only by incrementCounter() in Persistance!
     * @return associated poll's id
     */
    public int getPoll() {
        return poll;
    }

    /**
     * Sets poll's id.<br>
     * This method may be called only from Persistance!
     */
    public void setPoll(int poll) {
        this.poll = poll;
    }

    /**
     * @return order of choice in the poll, used by incrementCounter
     */
    public int getId() {
        return id;
    }

    /**
     * Sets order of choice in the poll
     */
    public void setId(int id) {
        this.id = id;
    }
}
