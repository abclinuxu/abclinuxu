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
package cz.abclinuxu.servlets.ws;

import cz.abclinuxu.exceptions.DuplicateKeyException;
import cz.abclinuxu.exceptions.InvalidInputException;
import cz.abclinuxu.exceptions.LdapException;
import cz.abclinuxu.exceptions.AccessDeniedException;

import javax.jws.WebService;
import javax.jws.WebParam;

/**
 * Web service interface for user account manipulation.
 * @author literakl
 * @since 20.4.2008
 */
@WebService
public interface UserAccountService {

    public void registerUser(@WebParam(name = "login") String login, @WebParam(name = "password") String password,
                             @WebParam(name = "openId") String openId, @WebParam(name = "name") String name,
                             @WebParam(name = "portal") String portal, @WebParam(name = "portalPassword") String portalPassword)
            throws DuplicateKeyException, InvalidInputException, AccessDeniedException, LdapException;

    public boolean login(@WebParam(name = "login") String login, @WebParam(name = "password") String password,
                         @WebParam(name = "portal") String portal, @WebParam(name = "portalPassword") String portalPassword)
            throws AccessDeniedException, LdapException;

    public boolean loginWithPasswordHash(@WebParam(name = "login") String login, @WebParam(name = "passwordHash") int passwordHash,
                                         @WebParam(name = "portal") String portal, @WebParam(name = "portalPassword") String portalPassword)
            throws AccessDeniedException, LdapException;

    public void changePassword(@WebParam(name = "login") String login, @WebParam(name = "password") String password,
                               @WebParam(name = "portal") String portal, @WebParam(name = "portalPassword") String portalPassword)
            throws AccessDeniedException, LdapException, InvalidInputException;

    public void updateUser(@WebParam(name = "acount") UserAccount account, @WebParam(name = "portal") String portal,
                           @WebParam(name = "portalPassword") String portalPassword)
            throws AccessDeniedException, LdapException, InvalidInputException;

    public UserAccount getUserInformation(@WebParam(name = "login") String login, @WebParam(name = "attributes") String[] attributes,
                                          @WebParam(name = "portal") String portal, @WebParam(name = "portalPassword") String portalPassword)
            throws AccessDeniedException, LdapException;
}
