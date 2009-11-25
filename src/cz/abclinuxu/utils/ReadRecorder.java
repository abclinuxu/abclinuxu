/*
 *  Copyright (C) 2006 Leos Literak
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
package cz.abclinuxu.utils;

import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;

import java.util.Map;

/**
 * This class records read of the object. It can filter out robots
 * and not to record the owner of the object (e.g. for blog).
 * @author literakl
 * @since 19.8.2006
 */
public class ReadRecorder {

    public static void log(GenericObject obj, String type, Map env) {
        Boolean bot = (Boolean) env.get(Constants.VAR_BOT_DETECTED);
        if (bot != null && bot) // not interested in spiders and various bots
            return;

        User user = (User) env.get(Constants.VAR_USER);
        if (user != null && (obj instanceof Item) && ((Item)obj).getOwner() == user.getId())
            return;

        Persistence persistence = PersistenceFactory.getPersistence();
        persistence.incrementCounter(obj, type);
    }
}
