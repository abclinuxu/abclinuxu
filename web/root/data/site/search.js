if ((typeof window.sidebar == "object") && (typeof window.sidebar.addPanel == "function")) {
	moz_src = "http://www.abclinuxu.cz/data/site/mozilla_search.src";
	moz_ico = "http://www.abclinuxu.cz/images/site/abc_fav.png";
	moz_tit = "Portál ABC Linuxu";
	moz_cat = "Web";
	document.write("<img src=\"/images/site/mozilla.gif\" width=\"16\" height=\"16\"> <a href=\"javascript:window.sidebar.addSearchEngine(moz_src, moz_ico, moz_tit, moz_cat);\">Vyhledávací modul pro Mozillu</a>&nbsp;(<a href=\"http://www.czilla.cz/sidebars/search.html\" title=\"Více o vyhledávacích modulech pro Mozillu\">?</a>)");
}
