if ((typeof window.sidebar == "object") && (typeof window.sidebar.addPanel == "function")) {
	moz_src = "http://www.abclinuxu.cz/data/site/abclinuxu.src";
	moz_ico = "http://www.abclinuxu.cz/images/site/abclinuxu.png";
	moz_tit = "Portal ABC Linuxu";
	moz_cat = "Web";
	document.write("<br><img src=\"/images/site/mozilla.gif\" width=\"16\" height=\"16\"> <a href=\"javascript:window.sidebar.addSearchEngine(moz_src, moz_ico, moz_tit, moz_cat);\" title=\"Pøidat vyhledávací modul pro Mozillu\">Mozilla/Firefox</a>&nbsp;(<a href=\"http://www.czilla.cz/sidebars/search.html\" title=\"Více o vyhledávacích modulech pro Mozillu\">?</a>)");
}

function toggle(sender, stav) {
    stav = !stav;
    if (sender.form.elements.length) {
        for (var i = 0; i < sender.form.elements.length; i++) {
            if (sender.form.elements[i].type == 'checkbox') {
                sender.form.elements[i].checked = stav;
            }
        }
    }
}
