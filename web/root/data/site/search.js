function offerAddon() {
    if ((typeof window.sidebar == "object") && (typeof window.sidebar.addPanel == "function")) {
	moz_src = "http://www.abclinuxu.cz/data/site/abclinuxu.src";
	moz_ico = "http://www.abclinuxu.cz/images/site/abclinuxu.png";
	moz_tit = "Portal ABC Linuxu";
	moz_cat = "Web";
	document.write("<br><img src=\"/images/site/mozilla.gif\" width=\"16\" height=\"16\"> <a href=\"javascript:window.sidebar.addSearchEngine(moz_src, moz_ico, moz_tit, moz_cat);\" title=\"Přidat vyhledávací modul pro Mozillu\">Mozilla/Firefox</a>&nbsp;(<a href=\"http://www.czilla.cz/sidebars/search.html\" title=\"Více o vyhledávacích modulech pro Mozillu\">?</a>)");
         }
}

/**
 * Handles state of checkboxes,
 * used by toggleCheckBoxes function
 */
function MultipleChoiceState(initialState) {
    this.state = initialState;
}

MultipleChoiceState.prototype.toggle = function() {
    if(this.state==false) {
        this.state = true;
    }
    else {
        this.state = false;
    }
}

MultipleChoiceState.prototype.value = function() {
    return this.state;
}


/**
 Toggles all checkboxes in given form using MultipleChoiceState to
 remember current state
*/
function toggleCheckBoxes(form, state) {
    state.toggle();
    if (form.elements.length) {
        for (var i = 0; i < form.elements.length; i++) {
            if (form.elements[i].type == 'checkbox') {
                form.elements[i].checked = state.value();
            }
        }
    }
}
