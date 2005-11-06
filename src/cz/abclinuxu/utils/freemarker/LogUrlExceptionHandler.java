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
package cz.abclinuxu.utils.freemarker;

import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateException;
import freemarker.core.Environment;

import java.io.Writer;

import org.apache.log4j.Logger;
import cz.abclinuxu.servlets.utils.ServletUtils;

/**
 * User: literakl
 * Date: 21.4.2005
 */
public class LogUrlExceptionHandler implements TemplateExceptionHandler {
    static Logger log = Logger.getLogger(LogUrlExceptionHandler.class);


    /**
     * Logs template exception and current URL.
     * @param e
     * @param environment
     * @param writer
     * @throws TemplateException
     */
    public void handleTemplateException(TemplateException e, Environment environment, Writer writer) throws TemplateException {
        String url = ServletUtils.getCurrentURL();
        log.error("Chyba v sablone na adrese "+url+"\n"+e.getMessage()+"\n"+e.getFTLInstructionStack());
    }
}
