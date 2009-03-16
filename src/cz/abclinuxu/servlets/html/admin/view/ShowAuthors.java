package cz.abclinuxu.servlets.html.admin.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.servlets.AbcAction;

public class ShowAuthors implements AbcAction {

	public String process(HttpServletRequest request,
			HttpServletResponse response, Map env) throws Exception {

		throw new MissingArgumentException("Chybí parametr action!");
	}
}
