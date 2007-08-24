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
package cz.abclinuxu.utils.format;

import org.apache.log4j.Logger;

import java.util.Map;
import java.io.StringReader;
import java.io.IOException;

/**
 * Renders texts in html format. Only emoticons are converted to images.
 */
public class HTMLFormatRenderer implements Renderer {
    static Logger log = Logger.getLogger(HTMLFormatRenderer.class);

    static Renderer instance;

    static {
        instance = new HTMLFormatRenderer();
    }

    public static Renderer getInstance() {
        return instance;
    }

    /**
     * Renders texts in simple format.
     */
    public String render(String input, Map params) {
        if (input==null || input.length()==0)
            return "";

        if (params.containsKey(RENDER_EMOTICONS))
            return renderWithEmoticons(input);
        else
            return input;
    }

    protected String renderWithEmoticons(String input) {
        StringReader reader = new StringReader(input);
        StringBuffer sb = new StringBuffer((int) (1.2*input.length()));

        int c = 'X', d = 'X', e = 'X';
        try {
            c = reader.read();

            START:
            while ( c!=-1 ) {
                if ( c==':' || c==';' ) {
                    d = reader.read();
                    if ( d=='-' ) {
                        e = reader.read();
                        switch ( e ) {
                            case ')':
                                if ( c==':' )
                                    sb.append(SharedConfig.getImageOfUsmev());
                                else
                                    sb.append(SharedConfig.getImageOfMrk());
                                break;
                            case '(':
                                sb.append(SharedConfig.getImageOfSmutek()); break;
                            case 'D':
                                sb.append(SharedConfig.getImageOfSmich()); break;
                            case -1:
                                sb.append((char) c);
                                sb.append((char) d);
                                break;
                            default:
                                sb.append((char) c);
                                sb.append((char) d);
                                sb.append((char) e);
                        }
                    } else {
                        sb.append((char) c);
                        if ( d!=-1 )
                                sb.append((char) d);
                    }
                } else {
                    sb.append((char) c);
                }
                c = reader.read();
            }
        } catch (IOException ioe) {
            log.error("Error while rendering with emoticons!", ioe);
            if (log.isDebugEnabled()) {
                log.debug("Cannot render! ["+(char)c+","+(char)d+","+(char)e+"]\n"+input);
            }
        }
        return sb.toString();
    }
}
