/*
 * User: literakl
 * Date: 29.1.2004
 * Time: 19:38:01
 */
package cz.abclinuxu.utils.format;

import org.apache.log4j.Logger;

import java.util.Map;
import java.io.StringReader;
import java.io.IOException;

/**
 * Renders texts in simple format. E.g. empty lines are replaced with paragraph.
 */
public class SimpleFormatRenderer implements Renderer {
    static Logger log = Logger.getLogger(SimpleFormatRenderer.class);

    static Renderer instance;

    static {
        instance = new SimpleFormatRenderer();
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
            return renderWithoutEmoticons(input);
    }

    protected String renderWithEmoticons(String input) {
        StringReader reader = new StringReader(input);
        StringBuffer sb = new StringBuffer((int) (1.2*input.length()));

        int c = 'X', d = 'X', e = 'X';
        boolean ending, in_pre = false;
        try {
            c = reader.read();

            while ( c!=-1 ) {
                if ( c=='\n' && !in_pre ) {
                    sb.append((char) c);
                    d = reader.read();
                    if (d=='\n') {
                        sb.append("\n<p>\n");
                    } else if (d=='\r') {
                        sb.append((char) d);
                        e = reader.read();
                        if ( e=='\n' ) {
                            sb.append("\n<p>\n");
                        } else {
                            sb.append((char) e);
                        }
                    } else {
                        c = d;
                        continue;
                    }
                } else if ( c==':' || c==';' ) {
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
                            case '\n':
                                sb.append((char) c);
                                sb.append((char) d);
                                c = e;
                                continue;
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
                        if ( d=='\n' ) {
                            c = d;
                            continue;
                        } else if ( d!=-1 )
                                sb.append((char) d);
                    }
                } else if ( c=='<' ) {
                    sb.append((char) c);
                    c = reader.read();
                    if ( c=='\n' )
                        continue;
                    else if ( c=='/' ) {
                          ending = true;
                          sb.append((char) c);
                          c = reader.read();
                          if ( c=='\n' )
                              continue;
                    }
                    else
                        ending = false;

                    if ( c=='p' || c=='P' ) {
                        sb.append((char) c);
                        c = reader.read();
                        if ( c=='\n' )
                            continue;
                        else if ( c=='r' || c=='R' ) {
                            sb.append((char) c);
                            c = reader.read();
                            if ( c=='\n' )
                                continue;
                            else if ( c=='e' || c=='E' ) {
                                sb.append((char) c);
                                c = reader.read();
                                if ( c=='\n' )
                                    continue;
                                else if ( c=='>' ) {
                                     /* Do nothing in invalid cases and hope
                                      * it will resolve later */
                                     if ( in_pre && ending )
                                         in_pre = false;
                                     else if ( !in_pre && !ending )
                                         in_pre = true;
                                }
                            }
                        }
                    }
                    sb.append((char) c);
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

    protected String renderWithoutEmoticons(String input) {
        StringReader reader = new StringReader(input);
        StringBuffer sb = new StringBuffer((int) (1.2*input.length()));

        int c = 'X', d = 'X', e = 'X';
        boolean ending, in_pre = false;
        try {
            c = reader.read();

            while ( c!=-1 ) {
                if ( c=='\n' && !in_pre ) {
                    sb.append((char) c);
                    d = reader.read();
                    if (d=='\n') {
                        sb.append("\n<p>\n");
                    } else if (d=='\r') {
                        sb.append((char) d);
                        e = reader.read();
                        if ( e=='\n' ) {
                            sb.append("\n<p>\n");
                        } else {
                            sb.append((char) e);
                        }
                    } else {
                        c = d;
                        continue;
                    }
                } else if ( c=='<' ) {
                    sb.append((char) c);
                    c = reader.read();
                    if ( c=='\n' )
                        continue;
                    else if ( c=='/' ) {
                          ending = true;
                          sb.append((char) c);
                          c = reader.read();
                          if ( c=='\n' )
                              continue;
                    }
                    else
                        ending = false;

                    if ( c=='p' || c=='P' ) {
                        sb.append((char) c);
                        c = reader.read();
                        if ( c=='\n' )
                            continue;
                        else if ( c=='r' || c=='R' ) {
                            sb.append((char) c);
                            c = reader.read();
                            if ( c=='\n' )
                                continue;
                            else if ( c=='e' || c=='E' ) {
                                sb.append((char) c);
                                c = reader.read();
                                if ( c=='\n' )
                                    continue;
                                else if ( c=='>' ) {
                                     /* Do nothing in invalid cases and hope
                                      * it will resolve later */
                                     if ( in_pre && ending )
                                         in_pre = false;
                                     else if ( !in_pre && !ending )
                                         in_pre = true;
                                }
                            }
                        }
                    }
                    sb.append((char) c);
                } else {
                    sb.append((char) c);
                }
                c = reader.read();
            }
        } catch (IOException ioe) {
            log.error("Error while rendering without emoticons!", ioe);
            if (log.isDebugEnabled()) {
                log.debug("Cannot render! ["+(char)c+","+(char)d+","+(char)e+"]\n"+input);
            }
        }
        return sb.toString();
    }
}
