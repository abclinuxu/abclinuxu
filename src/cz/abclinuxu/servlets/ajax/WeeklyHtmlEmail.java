package cz.abclinuxu.servlets.ajax;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Html variant for weekly email, used in iframe.
 * User: literakl
 * Date: 7.2.2010
 */
public class WeeklyHtmlEmail extends HttpServlet {
    static String content;

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.print(content); // setContent must be called earlier
    }

    public static void setContent(String content) {
        WeeklyHtmlEmail.content = content;
    }
}
