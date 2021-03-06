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
package cz.abclinuxu.security;

/**
 * Defines available roles, that users may play.
 */
public class Roles {

    /** this role grants the user all rights */
    public static final String ROOT = "root";
    /** user can edit, move and remove discussions */
    public static final String DISCUSSION_ADMIN = "discussion admin";
    /** user can edit, approve and delete news */
    public static final String NEWS_ADMIN = "news admin";
    /** user can create, edit and delete surveys */
    public static final String SURVEY_ADMIN = "survey admin";
    /** user can create, edit and delete polls */
    public static final String POLL_ADMIN = "poll admin";
    /** user can edit other users */
    public static final String USER_ADMIN = "user admin";
    /** user can invalidate emails of other users */
    public static final String CAN_INVALIDATE_EMAILS = "email invalidator";
    /** user can edit tips */
    public static final String TIPS_ADMIN = "tip admin";
    /** user can choose stories to RSS digest */
    public static final String BLOG_DIGEST_ADMIN = "blog digest admin";
    /** advertisement manager */
    public static final String ADVERTISEMENT_ADMIN = "advertisement admin";
    /** tag administrator */
    public static final String TAG_ADMIN = "tag admin";
}
