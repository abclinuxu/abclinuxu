/*
 * User: literakl
 * Date: Jun 30, 2002
 * Time: 9:03:42 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.filters;

import javax.servlet.*;
import java.io.IOException;

/**
 * This filter is responsible for setting
 * correct encoding of request parameters.
 */
public class EncodingFilter implements Filter {
    String encoding;

    public void init(FilterConfig config) throws ServletException {
        encoding = config.getInitParameter("encoding");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if ( encoding!=null )
            request.setCharacterEncoding(encoding);
        if ( chain!=null )
            chain.doFilter(request,response);
    }

    public void destroy() {
    }
}
