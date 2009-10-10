function prepni_sloupec() {
    if (document.getElementById('ls').style.display == 'none') {
        document.getElementById('ls').style.display = 'block'
        document.getElementById('ls_prepinac').innerHTML = '&#215;'
        document.getElementById('ls_prepinac').title = 'Skrýt sloupec'
        document.getElementById('st').style.marginLeft = '270px';
    }
    else {
        document.getElementById('ls').style.display = 'none'
        document.getElementById('ls_prepinac').innerHTML = '&#43;'
        document.getElementById('ls_prepinac').title = 'Ukázat sloupec'
        document.getElementById('st').style.marginLeft = '0px';
        document.getElementById('st').style.borderLeft = 'none';
    }
}

function prepni_plochu(idPlochy) {
    if (document.getElementById(idPlochy).style.display == 'none' || document.getElementById(idPlochy).style.display == '') {
        document.getElementById(idPlochy).style.display = 'block'
    } else {
        document.getElementById(idPlochy).style.display = 'none'
    }
}

function schovej_vlakno(id) {
    if (document.getElementById('comment'+id).style.display == 'none') {
        document.getElementById('comment'+id).style.display = 'block'
        document.getElementById('comment'+id+'_controls').style.display = 'block'
        document.getElementById('comment'+id+'_toggle1').innerHTML = 'Sbalit'
        if(document.getElementById('comment'+id+'_avatar')) {
            document.getElementById('comment'+id+'_avatar').style.display = 'block'
        }
        if (document.getElementById('comment'+id+'_toggle2')) {
            document.getElementById('comment'+id+'_toggle2').style.display = 'none'
        }
    }
    else {
        document.getElementById('comment'+id).style.display = 'none'
        document.getElementById('comment'+id+'_controls').style.display = 'none'
        document.getElementById('comment'+id+'_toggle1').innerHTML = 'Rozbalit'
        if (document.getElementById('comment'+id+'_avatar')) {
            document.getElementById('comment'+id+'_avatar').style.display = 'none'
        }
        if (document.getElementById('comment'+id+'_toggle2')) {
            document.getElementById('comment'+id+'_toggle2').style.display = 'inline'
        }
    }
    prepareCommentNext();
}

function rozbal_vse(id) {
    cur = document.getElementById('comment'+id);
    divs = cur.getElementsByTagName('div');

    if (cur.style.display == 'none')
        schovej_vlakno(id);

    for (var i=0;i<divs.length;i++) {
        m = divs[i].id.match(/comment(\d+)/);

        if (m && divs[i].style.display == 'none')
            schovej_vlakno(m[1]);
    }
}

function addSidebar() {
    if ((typeof window.sidebar == "object") && (typeof window.sidebar.addPanel == "function")) {
        window.sidebar.addPanel("www.abclinuxu.cz",'http://www.abclinuxu.cz/?varianta=sidebar',"");
    } else {
        window.alert("Váš prohlížeč nepodporuje tuto funkci. Zkuste Mozilla Firefox.");
    }
}

function setHomepage() {
	if (document.all) {
    document.body.style.behavior='url(#default#homepage)';
		document.body.setHomePage("http://www.abclinuxu.cz/");
	}
	else {
		window.alert("Použijte prosím nastavení svého prohlížeče.");
	}
}

function addBookmark() {
	if (document.all) {
		window.external.AddFavorite("http://www.abclinuxu.cz","AbcLinuxu.cz");
	}
	else {
		window.alert("Použijte prosím kombinaci Ctrl-D.");
	}
}

// http://www.alexking.org - LGPL
function insertAtCursor(myField, prefix, postfix) {
	var re = new RegExp("^(.*\\S)(\\s*)$");
  myField.focus();
  //IE support
  if (document.selection) {
    sel = document.selection.createRange();
		var selection = sel.text;
		var wasEmpty = (selection == "");
		var space = "";
		if (!wasEmpty) {
			var matches = selection.match(re);
			if (matches) {
				selection = RegExp.$1;
				space = RegExp.$2;
			}
		}
    sel.text = prefix+selection+postfix+space;
		sel.collapse(false);
		if (wasEmpty) {
			sel.moveEnd('character',-(prefix.length+1))
		}
		sel.select();
  }
  //MOZILLA/NETSCAPE support
  else {
		if (myField.selectionStart || myField.selectionStart == '0') {
			var startPos = myField.selectionStart;
			var endPos = myField.selectionEnd;
			var selection = myField.value.substring(startPos, endPos);
			var wasEmpty = (startPos == endPos);
			var space = "";
			if (!wasEmpty) {
				var matches = selection.match(re);
				if (matches) {
					selection = RegExp.$1;
					space = RegExp.$2;
				}
			}
			myField.value = myField.value.substring(0, startPos)
										+ prefix+selection+postfix+space
										+ myField.value.substring(endPos, myField.value.length);

			var newPosition;
			if (wasEmpty) {
				newPosition = startPos+prefix.length;
			} else {
				newPosition = startPos+prefix.length+selection.length+postfix.length+space.length;
			}
			myField.setSelectionRange(newPosition, newPosition);
		} else {
			myField.value += prefix+postfix;
			myField.setSelectionRange(startPos+prefix.length, startPos+prefix.length);
		}
	}
}

function writeRemainingCharsCount(textarea) {
    var regEx = new RegExp('<[^>]*>', 'g');
    var strippedStr = new String(textarea.value);
    strippedStr = strippedStr.replace(regEx, '');
    document.getElementById('signatureTextCounter').innerHTML = '(zbývá '+Math.max(120-strippedStr.length, 0)+' znaků)';
}

function initializeEditor(type) {
    var abcPlugins = "tabfocus,xhtmlxtras,visualchars,table,paste,searchreplace,contextmenu";
    var abcButtons = "bold,italic,link,unlink,bullist,numlist,blockquote,sup,sub,anchor,table,image,charmap,formatselect,|," +
                     "outdent,indent,justifyleft,justifycenter,justifyright,justifyfull,|," +
                     "undo,redo,pastetext,pasteword,cleanup,search,replace,visualchars,help";
    if (type == "blog") {
        abcPlugins = abcPlugins + ",pagebreak";
        abcButtons = abcButtons.replace(",|,undo",",pagebreak,|,undo");
    } else if (type == "news") {
        abcPlugins = "tabfocus";
        abcButtons = "link,unlink,charmap,|,undo,redo,pastetext,search,replace,help";
    }
    if (typeof(quotedText) != "undefined") {
        abcButtons = abcButtons.replace(",|,outdent",",template,|,outdent");
    }
    tinyMCE.init({
        theme : "advanced",
        mode : "none",
        convert_urls : false,
        entity_encoding : "raw",
        plugins : abcPlugins,
        pagebreak_separator : "<!--break-->",
        theme_advanced_buttons1 : abcButtons,
        theme_advanced_buttons2 : "",
        theme_advanced_buttons3 : "",
        theme_advanced_toolbar_location : "top",
        theme_advanced_toolbar_align : "left",
        theme_advanced_statusbar_location : "bottom",
        theme_advanced_resizing : true,
        theme_advanced_blockformats : "div,pre,code,h1,h2,h3,h4,dt,dd",
        valid_elements : ""
        +"a[href|name|rel|title|target],"
        +"abbr[title],"
        +"acronym[title],"
        +"b,"
        +"blockquote[id|style],"
        +"br,"
        +"center,"
        +"cite,"
        +"code,"
        +"dd,"
        +"del,"
        +"div[class|id|style],"
        +"dl,"
        +"dt,"
        +"em,"
        +"h1[id],"
        +"h2[id],"
        +"h3[id],"
        +"h4[id],"
        +"h5[id],"
        +"h6[id],"
        +"hr,"
        +"i,"
        +"img[alt|border|class|src|title|height|width],"
        +"ins,"
        +"kbd,"
        +"li,"
        +"ol[id],"
        +"p[class|id|style],"
        +"pre[class|id|style|width],"
        +"q[id],"
        +"span[id|style],"
        +"sub,"
        +"sup,"
        +"table[border|cellpadding|cellspacing|class|id|width],"
        +"td[align<center?char?justify?left?right|colspan|rowspan|style|valign<baseline?bottom?middle?top],"
        +"tfoot,"
        +"th[align<center?char?justify?left?right|colspan|rowspan|style|valign<baseline?bottom?middle?top],"
        +"thead,"
        +"tr[style],"
        +"tt,"
        +"u,"
        +"ul[id],"
        +"var",

        setup : function(ed) {
            ed.addButton('template', {
                title : 'Vloží komentovaný příspěvek jako citaci',
                onclick : function() {
                    ed.focus();
                    ed.selection.setContent(quotedText);
                }
            });
        }
    });
}

function toggleEditor(id) {
    if (!tinyMCE.get(id)) {
        tinyMCE.execCommand('mceAddControl', true, id);
        document.getElementById('jsEditorButtons').style.display = 'none';
        document.getElementById('rte_'+id).value = "true";
    } else {
        tinyMCE.execCommand('mceRemoveControl', false, id);
        document.getElementById('jsEditorButtons').style.display = 'inline';
        document.getElementById('rte_'+id).value = "false";
    }
}

// start method for checkParent
function startCheckParent(event) {
    if (!event) {
        var event = window.event;
    }
    var target = (event.target) ? event.target : event.srcElement;
    if (target.checked) {
        checkParent(target.parentNode);
    }
}
// recursively traverses parent checkboxes for this check box and turn them on
function checkParent(target) {
    if (target.parentNode.id != "strom") {
        target.parentNode.getElementsByTagName("input")[0].checked = true;
        checkParent(target.parentNode);
    }
}

function prepareCommentNext() {
	window.dsUtils.nextComments = new Object();
	if (window.hiddenNext != null) {
		window.hiddenNext.style.display = "";
		window.hiddenNext.nextSibling.nodeValue = " | ";
	}
	var children = document.getElementById("st").childNodes;
	var stack = new Array();
	for (var i = 0; i < children.length; i++) {
		if (isExpandedCommentNode(children[i])) {
			stack.unshift(children[i]);
		}
	}
	searchNextNewComment(stack);
}

function searchNextNewComment(stack) {
	var last;
	var node;
	while (stack.length > 0) {
		node = stack.pop();
		if (node.className.match(dsUtils.re_comment_novy)) {
//			getFirstElementByName(getFirstElementByName(node,"DIV"), "A").onclick = nextCommentClick;
           var linkDalsi = getFirstElementByName(getFirstElementByName(node,"DIV"), "A");
           if (linkDalsi.innerHTML == "Další") {
             linkDalsi.onclick = nextCommentClick;
           }
			var name = getFirstElementByName(node, "A").name;
			if (last != null) {
				window.dsUtils.nextComments[getFirstElementByName(last, "A").name] = name;
			}
			last = node;
		}
		//ds_hlavicka -> následující ds_text_user*
		node = node.nextSibling;
		while (node != null && (node.nodeType != Node.ELEMENT_NODE || (node.localName != "DIV" && node.nodeName != "DIV"))) {
			node = node.nextSibling;
		}
		//vnořené divy - 1. bez třídy je kontejner vnořené diskuze
		node = node.firstChild;
		while (node != null && (node.nodeType != Node.ELEMENT_NODE || (node.localName != "DIV" && node.nodeName != "DIV") || node.className != "")) {
				node = node.nextSibling;
		}
		//přidej od zadu komentáře do zásobníku
		if (node != null && node.hasChildNodes()) {
			var child = node.lastChild;
			while (child != null) {
				if (isExpandedCommentNode(child)) {
					stack.push(child);
				}
				child = child.previousSibling;
			}
		}
	}
	if (last != null && getFirstElementByName(last,"DIV") != null) {
		var a = getFirstElementByName(getFirstElementByName(last,"DIV"), "A");
		if (a.innerHTML == "Další") {
			a.style.display = "none";
			a.nextSibling.nodeValue = "";
			window.hiddenNext = a;
		}
	}
}

function isExpandedCommentNode(child) {
	return child.nodeType == Node.ELEMENT_NODE && child.className.match(window.dsUtils.re_comment) && (child.localName == "DIV" || child.nodeName == "DIV") && getFirstElementByName(child, "DIV").style.display != "none";
}

function getFirstElementByName(node, name) {
	var children = node.childNodes;
	for (var i = 0; i < children.length; i++) {
		if (children[i].nodeType == Node.ELEMENT_NODE && (children[i].localName == name || children[i].nodeName == name)) {
			return children[i];
		}
	}
	return null;
}

function nextCommentClick(event) {
	if (!event) {
		var event = window.event;
	}
    var target = (event.target) ? event.target : event.srcElement;
	var nextId = window.dsUtils.nextComments[window.dsUtils.re_comment_id.exec(target.parentNode.id)[1]];
	if (nextId != null) {
		window.location = "#"+nextId;
	}
	return false;
}

var Toolkit = {
	relax: function() {
	},
	
	createElement: function(document, tagName, className, id, text) {
		var element = document.createElement(tagName);
		if (className != null) {
			element.className = className;
		}
		if (id != null) {
			element.id = id;
		}
		if (text != null) {
			element.appendChild(element.ownerDocument.createTextNode(text));
		}

		return element;
	},

	appendElement: function(parent, tagName, className, id, text) {
		var element = this.createElement(parent.ownerDocument, tagName, className, id, text);
		parent.appendChild(element);

		return element;
	},

	addEventListener: function(element, type, func, obj) {
		var params = new Array();

		var handler;
		if (typeof(func) == "function") {
			for (var i = 3; i < arguments.length; i++) {
				params.push(arguments[i]);
			}

			handler = function (event) {
				var p = new Array(event).concat(params);
				var result = func.apply(null, p);
				if (result == false) {
					event.returnValue = false;
					if (event.preventDefault) event.preventDefault();
				}
				return result;
			}
		} else {
			for (var i = 4; i < arguments.length; i++) {
				params.push(arguments[i]);
			}

			handler = function (event) {
				var p = new Array(event).concat(params);
				var result = obj[func].apply(obj, p);
				if (result == false) {
					event.returnValue = false;
					if (event.preventDefault) event.preventDefault();
				}
				return result;
			}
		}
		Toolkit.addEventListenerImpl(element, type, handler);
		return handler;
	},

	removeEventListener: function (element, type, handler) {
		Toolkit.removeEventListenerImpl(element, type, handler);
	}
}

if (document.documentElement.addEventListener) {
	Toolkit.addEventListenerImpl = function(element, type, handler) {
		element.addEventListener(type, handler, false);
	};

	Toolkit.removeEventListenerImpl = function(element, type, handler) {
		element.removeEventListener(type, handler, false);
	};

	Toolkit.addEventListenerMSIE = Toolkit.relax;

	Toolkit.removeEventListenerMSIE = Toolkit.relax;
} else {
	Toolkit.addEventListenerImpl = function(element, type, handler) {
		element.attachEvent("on"+type, handler);
	};

	Toolkit.removeEventListenerImpl = function(element, type, handler) {
		element.detachEvent("on"+type, handler);
	};

	Toolkit.addEventListenerMSIE = Toolkit.addEventListenerImpl;

	Toolkit.removeEventListenerMSIE = Toolkit.removeEventListenerImpl;
}

function ModalWindow(className, id, handler) {
	this.window = document.createElement("div");
	this.window.className = "modalWindow";
	this.handler = handler;
	if (className != null) {
		this.window.className += " "+className;
	}
	if (id != null) {
		this.window.id = id;
	}
	this.background = document.createElement("div");
	this.background.className = "modalWindowBackground";
	this.background.style.height = document.body.clientHeight+"px";

	this.closeButton = Toolkit.appendElement(this.window, "span", "krizek", null, "×");
	this.closeButton.title = "Zavřít okno";

	Toolkit.addEventListener(this.closeButton, "click", "close", this);
	Toolkit.addEventListener(this.background, "click", "close", this);
	this.keypressHandler = Toolkit.addEventListener(document.body, "keypress", "keypress", this);

	document.body.appendChild(this.background);
	document.body.appendChild(this.window);
}

ModalWindow.prototype = {
	keypress: function(event) {
		if (event.keyCode == 27) {
			this.close();
		}
	},

	close: function() {
		Toolkit.removeEventListener(document.body, "keypress", this.keypressHandler);
		document.body.removeChild(this.background);
		document.body.removeChild(this.window);
		if (this.handler.onclose) {
			this.handler.onclose();
		}
	}
}

function StitkyLink() {
	var div = document.getElementById("tagfilter");
	if (div) {
		this.clink = Toolkit.appendElement(div, "a", null, null, "Zobrazit štítky");
		this.clink.href = "#";
		Toolkit.addEventListener(this.clink, "click", "showTags", this, this.clink);
		
		this.spanAttribs = Toolkit.appendElement(div, "span", null, null, " Logické operace: ");
		var label = Toolkit.appendElement(this.spanAttribs, "label");
		this.radioAnd = Toolkit.appendElement(label, "input", null, "radioAnd");
		this.radioAnd.type = "radio";
		this.radioAnd.name = "logicalOp";
		this.radioAnd.checked = "checked";
		Toolkit.appendElement(label, "span", null, null, "AND ");
		
		label = Toolkit.appendElement(this.spanAttribs, "label");
		this.radioOr = Toolkit.appendElement(label, "input", null, "radioOr");
		this.radioOr.type = "radio";
		this.radioOr.name = "logicalOp";
		Toolkit.appendElement(label, "span", null, null, " OR ");
		
		label = Toolkit.appendElement(this.spanAttribs, "label");
		this.radioOr = Toolkit.appendElement(label, "input", null, "checkNot");
		this.radioOr.type = "checkbox";
		Toolkit.appendElement(label, "span", null, null, " NOT ");
		
		this.spanAttribs.setAttribute("style", "visibility: hidden");
	}
}

StitkyLink.prototype = {
	visible: false,
	showTags: function(event, button) {
		if (!this.visible) {
			this.clink.innerHTML = "Skrýt štítky";
			this.spanAttribs.setAttribute("style", "");
			
			this.div = document.getElementById("tagfilter");
			this.ndiv = Toolkit.appendElement(this.div, "div", null, null, null);
			new StitkySearch(Page.relationID, this.ndiv);
			this.visible = true;
		} else {
			this.spanAttribs.setAttribute("style", "visibility: hidden");
			this.clink.innerHTML = "Zobrazit štítky";
			this.div.removeChild(this.ndiv);
			this.visible = false;
		}
	}
}

function StitkySearch(nodeID, div) {
	this.nodeID = nodeID;
	this.div = div;
	
	var vyberStitku = Toolkit.appendElement(this.div, "table", "vyberStitku");
	var row = vyberStitku.insertRow(0);
	this.stitkySeznam = Toolkit.appendElement(row.insertCell(0), "select", "stitkySeznam");
	this.stitkySeznam.size = this.seznamSize;
	
	this.stitkyOblibene = Toolkit.appendElement(row.insertCell(1), "div", "stitkyOblibene");
	this.pridatButton = Toolkit.appendElement(this.div, "span", "pridatButton");
	this.pridatButton.setAttribute("style", "visibility: hidden");
	
	Toolkit.appendElement(this.div, "p", "note", null, "Vybraný štítek bude automaticky přidán do filtru včetně nastavení logické operace.");
	
	var sseznam = new SeznamStitkuAJAX(this, this.nodeID);
	var sfavorite = new OblibeneStitkyAJAX(this, this.nodeID);
	
	sseznam.pridatStitekMouse = this.pridatStitekMouseHandler;
	sseznam.pridatStitekKeyboard = this.pridatStitekKeyboardHandler;
	sfavorite.pridatStitek = this.pridatStitekHandler;
	
}

function addSearchTag(stitek) {
	var taglist = document.getElementById("tags");
	var radioAnd = document.getElementById("radioAnd");
	var radioOr = document.getElementById("radioOr");
	var checkNot = document.getElementById("checkNot");
	
	var op = "";
	if (radioAnd.checked)
		op = "AND";
	else if (radioOr.checked)
		op = "OR";
	if (checkNot.checked)
		op = op+" NOT";

	if (taglist.value != "")
		taglist.value = taglist.value+" "+op;
	if (stitek.indexOf(' ') != -1)
		taglist.value = taglist.value+" \""+stitek+"\"";
	else
		taglist.value = taglist.value+" "+stitek;
}

StitkySearch.prototype = {
	seznamSize: 12,
 
	pridatStitekMouseHandler: function(event, seznam) {
		if (seznam.selectedIndex >= 0 && !event.ctrlKey)
			addSearchTag(seznam.options[seznam.selectedIndex].value);
		return false;
	},

	pridatStitekHandler: function(event, stitekID) {
		addSearchTag(stitekID);
		return false;
	},

	pridatStitekKeyboardHandler: function(event, seznam) {
		if (seznam.selectedIndex >= 0 && (event.charCode == 32 || (event.charCode == 0 && event.keyCode == 13)) || (!event.charCode && (event.keyCode == 13 || event.keyCode == 32))) {
			addSearchTag(seznam.options[seznam.selectedIndex].value);
			return false;
		}
	}
}

function Stitky() {
	var seznamStitku = document.getElementById("prirazeneStitky");
	if (!seznamStitku) {
		return;
	}

	var button =  Toolkit.appendElement(seznamStitku.parentNode, "button", "editTags", null, "Upravit");
	Toolkit.addEventListener(button, "click", "showDialog", this, button);
}

Stitky.prototype = {
	showDialog: function(event, button) {
		new StitkyDialog(Page.relationID, button);
	}
}

function StitkyDialog(nodeID, button) {
	this.counter = 0;
	this.lastRefresh = 0;
	this.nodeID = nodeID;
	this.button = button;
	this.stitkyElement = document.getElementById("prirazeneStitky");
	this.dialog = new ModalWindow("modalStitky", null, this);
	this.stitky = new Array();
	
	this.button.disabled = true;

	Toolkit.appendElement(this.dialog.window, "h2", null, null, "Nastavit štítky");
	var prirazeneStitkyParent = Toolkit.appendElement(this.dialog.window, "div", null, null, "Přiřazené štítky: ");
	this.prirazeneStitkyElement = Toolkit.appendElement(prirazeneStitkyParent, "span");
	var filtrElement = Toolkit.appendElement(this.dialog.window, "div", null, null, "Filtr: ");
	this.filtrInput = Toolkit.appendElement(filtrElement, "input");
	this.filtrInput.type = "text";
	this.pridatButton = Toolkit.appendElement(filtrElement, "button", null, null, "Vytvořit štítek");
	this.pridatButton.disabled = true;
//	var vyberStitku = Toolkit.appendElement(this.dialog.window, "div", "vyberStitku");
	var vyberStitku = Toolkit.appendElement(this.dialog.window, "table", "vyberStitku");
	var row = vyberStitku.insertRow(0);
//	this.stitkySeznam = Toolkit.appendElement(vyberStitku, "div", "stitkySeznam");
	this.stitkySeznam = Toolkit.appendElement(row.insertCell(0), "select", "stitkySeznam");
	this.stitkySeznam.size = this.seznamSize;
	this.stitkyOblibene = Toolkit.appendElement(row.insertCell(1), "div", "stitkyOblibene");
	Toolkit.appendElement(this.dialog.window, "p", "note", null, "Úpravy jsou prováděny v reálném čase, proto není nutné je jakkoliv potvrzovat nebo odesílat. Chcete-li okno pro nastavování štítků uzavřít, klikněte buď na křížek v pravém horním rohu, kamkoliv mimo okno, nebo stiskněte Escape.");

	this.filtrInput.focus();

	Toolkit.addEventListener(this.filtrInput, "keypress", "filtrChange", this);
	Toolkit.addEventListener(this.pridatButton, "click", "pridatStitek", this);

	new StitekAJAX(this, this.nodeID, "assigned");
	new SeznamStitkuAJAX(this, this.nodeID);
	new OblibeneStitkyAJAX(this, this.nodeID);
}

StitkyDialog.prototype = {
	seznamSize: 12,

	refreshStitky: function(element) {
		this.prirazeneStitkyElement.parentNode.replaceChild(element, this.prirazeneStitkyElement);
		this.prirazeneStitkyElement = element;
	},

	filtrChange: function(event) {
		this.pridatButton.disabled = true;
		var stitky = this;
		if (this.delayTagListRefresh != null) {
			window.clearTimeout(this.delayTagListRefresh);
		}
		this.delayTagListRefresh = window.setTimeout(function() {
			stitky.refreshTagList();
		}, 300);
	},

	refreshTagList: function() {
		if (this.filtrInput.value != this.oldFiltr) {
			this.oldFiltr = this.filtrInput.value;
			new SeznamStitkuAJAX(this, this.nodeID, this.filtrInput.value);
		}
	},

	pridatStitek: function() {
		new StitekAJAX(this, this.nodeID, "create", null, this.filtrInput.value);
		this.filtrInput.value = "";
		this.filtrChange();
	},

	onclose: function() {
		this.button.disabled = false;
		var element = Toolkit.createElement(this.stitkyElement.ownerDocument, "span", null, "prirazeneStitky");
		if (this.stitky.length > 0) {
			for (var i = 0; i < this.stitky.length; i++) {
				if (i > 0) {
					element.appendChild(element.ownerDocument.createTextNode(", "));
				}
				var a = Toolkit.appendElement(element, "a", null, null, this.stitky[i].title);
				a.href = "/stitky/"+this.stitky[i].id;
				a.title = "Zobrazit objekty, které mají přiřazen štítek „"+this.stitky[i].title+"“.";
			}
		} else {
			Toolkit.appendElement(element, "i", null, null, "není přiřazen žádný štítek");
		}
		this.stitkyElement.parentNode.replaceChild(element, this.stitkyElement);
	}
}

AJAX = {
	init: function() {
		if (window.XMLHttpRequest) {
			this.request = new XMLHttpRequest();
		} else if (window.ActiveXObject) {
    		this.request = new ActiveXObject("Microsoft.XMLHTTP");
		}
		this.request.open("GET", document.location.protocol+"//"+document.location.host+this.getURL(), true);
		this.request.onreadystatechange = this.createEventHandler();
	},

	createEventHandler: function() {
		function func() {
			func.object.readyStateChange();
		}
		func.object = this;
		return func;
	}

}

function Stitek(element) {
	this.title = element.getAttribute("l");
	this.id = element.getAttribute("i");
}

function StitekAJAX(stitky, nodeID, akce, stitekID, stitekName) {
	this.stitky = stitky;
	this.nodeID = nodeID;
	this.counter = this.stitky.counter++;
	this.stitekID = stitekID;
	this.stitekName = stitekName;

	this.akce = akce;
	this.init();
	this.request.send(null);
}

StitekAJAX.prototype = {
	init: AJAX.init,
	createEventHandler: AJAX.createEventHandler,

//	servletPath: "/data/site/stitky.xml",
	servletPath: "/ajax/tags/",


	getURL: function() {
		var url = this.servletPath;
		url += this.akce;
		url += "?rid="+this.nodeID;
		if (this.stitekID) {
			url += "&tagID="+this.stitekID;
		}
		if (this.stitekName) {
			url += "&title="+encodeURI(this.stitekName);
		}
		return url;
	},

	readyStateChange: function() {
		if (this.request.readyState == 4 && this.request.status == 200) {
			this.zobrazitStitky(this.request.responseXML);
		}
	},

	zobrazitStitky: function(doc) {
		if (this.counter < this.stitky.lastRefresh) {
			return;
		}
		if (!doc || !doc.documentElement) {
			return; //odpověď není XML dokument;
		}
		var element = document.createElement("span");
		var stitky = doc.documentElement.getElementsByTagName("s");
		this.stitky.stitky = new Array();
		for (var i = 0; i < stitky.length; i++) {
			var stitek = new Stitek(stitky[i]);
			var a = Toolkit.appendElement(element, "a", "stitek", null, stitek.title);
			a.href = "javascript:";
			a.title = "Kliknutím štítek odstraníte";
			Toolkit.addEventListener(a, "click", "odebratStitek", this, stitek.id);
			if (i < stitky.length-1) {
				element.appendChild(document.createTextNode(", "));
			}
			this.stitky.stitky.push(stitek);
		}
		this.stitky.refreshStitky(element);
		this.stitky.lastRefresh = this.counter;
	},

	odebratStitek: function(event, stitekID) {
		new StitekAJAX(this.stitky, this.nodeID, "unassign", stitekID);
		return false;
	}
}

function SeznamStitkuAJAX(stitky, nodeID, filtr) {
	this.stitky = stitky;
	this.nodeID = nodeID;
	this.filtr = filtr;

	this.init();
	this.request.send(null);
}

SeznamStitkuAJAX.prototype = {
	init: AJAX.init,
	createEventHandler: AJAX.createEventHandler,

//	servletPath: "/data/site/stitky2.xml",
	servletPath: "/ajax/tags/list",


	getURL: function() {
		var url = this.servletPath;
		if (this.filtr) {
			url += "?filter="+encodeURI(this.filtr);
		}
		return url;
	},

	readyStateChange: function() {
		if (this.request.readyState == 4 && this.request.status == 200) {
			this.vypsatStitky(this.request.responseXML);
		}
	},

	vypsatStitky: function(doc) {
		if (!doc || !doc.documentElement) {
			return; //odpověď není XML dokument;
		}
		this.stitky.pridatButton.disabled = !(doc.documentElement.getAttribute("allowCreate") == "true");
//		var stitkySeznam = Toolkit.createElement(document, "div", "stitkySeznam");
		var stitkySeznam = Toolkit.createElement(document, "select", "stitkySeznam");
		stitkySeznam.size = this.stitky.seznamSize;
		var stitky = doc.documentElement.getElementsByTagName("s");
		for (var i = 0; i < stitky.length; i++) {
			var stitek = new Stitek(stitky[i]);
//			var a = Toolkit.appendElement(stitkySeznam, "a", null, null, stitek.title);
//			a.href = "javascript:";
			var a = Toolkit.appendElement(stitkySeznam, "option", null, null, stitek.title);
			a.value = stitek.id;
			a.title = "Kliknutím štítek „nalepíte“";
//			Toolkit.addEventListener(a, "click", "pridatStitekMouse", this, stitek.id);
//			Toolkit.addEventListener(a, "keypress", "pridatStitekKeyboard", this, stitek.id);
		}
		Toolkit.addEventListener(stitkySeznam, "click", "pridatStitekMouse", this, stitkySeznam);
		Toolkit.addEventListener(stitkySeznam, "keypress", "pridatStitekKeyboard", this, stitkySeznam);
		this.stitky.stitkySeznam.parentNode.replaceChild(stitkySeznam, this.stitky.stitkySeznam);
		this.stitky.stitkySeznam = stitkySeznam;
	},

	pridatStitekMouse: function(event, seznam) {
		if (seznam.selectedIndex >= 0 && !event.ctrlKey) {
			new StitekAJAX(this.stitky, this.nodeID, "assign", seznam.options[seznam.selectedIndex].value);
		return false;
		}
	},
	
	pridatStitekKeyboard: function(event, seznam) {
		if (seznam.selectedIndex >= 0 && (event.charCode == 32 || (event.charCode == 0 && event.keyCode == 13)) || (!event.charCode && (event.keyCode == 13 || event.keyCode == 32))) {
			new StitekAJAX(this.stitky, this.nodeID, "assign", seznam.options[seznam.selectedIndex].value);
			return false;
		}
	}
}

function OblibeneStitkyAJAX(stitky, nodeID) {
	this.stitky = stitky;
	this.nodeID = nodeID;

	this.init();
	this.request.send(null);
}

OblibeneStitkyAJAX.prototype = {
	init: AJAX.init,
	createEventHandler: AJAX.createEventHandler,

//	servletPath: "/data/site/stitky.xml",
	servletPath: "/ajax/tags/favourite",

	getURL: function() {
		var url = this.servletPath;
		return url;
	},

	readyStateChange: function() {
		if (this.request.readyState == 4 && this.request.status == 200) {
			this.vypsatStitky(this.request.responseXML);
		}
	},

	vypsatStitky: function(doc) {
		if (!doc || !doc.documentElement) {
			return; //odpověď není XML dokument;
		}
		var stitky = doc.documentElement.getElementsByTagName("s");
		var oblibene = this.stitky.stitkyOblibene;
		for (var i = 0; i < stitky.length; i++) {
			var stitek = new Stitek(stitky[i]);
			var a = Toolkit.appendElement(oblibene, "a", null, null, stitek.title);
			a.href = "javascript:";
			a.title = "Kliknutím štítek „nalepíte“";
			Toolkit.addEventListener(a, "click", "pridatStitek", this, stitek.id);
			if (i < stitky.length-1) {
				oblibene.appendChild(oblibene.ownerDocument.createTextNode(" "));
			}
		}
	},

	pridatStitek: function(event, stitekID) {
		new StitekAJAX(this.stitky, this.nodeID, "assign", stitekID);
		return false;
	}
}

function Forum(rid, currentQuestions, maxQuestions) {
    this.tfoot = document.getElementById("forum_tfoot_"+rid);
    if (!this.tfoot)
        return;
    
    this.maxQuestions = maxQuestions;
    this.rid = rid;
    this.currentQuestions = this.originalQuestions = currentQuestions;
    
    this.createExpandingElem();
}

Forum.prototype = {
    init: AJAX.init,
    createEventHandler: AJAX.createEventHandler,
    paragraph: null,
    servletPath: "/ajax/forum/questions",
    expand: function() {
        var prev = this.currentQuestions;
        var add = parseInt(this.input.value);
        
        if (add == 0)
            return;
        
        this.currentQuestions += add;
        
        if (this.currentQuestions > this.maxQuestions) {
            this.currentQuestions = this.maxQuestions;
            if (prev == this.currentQuestions) {
                alert("Více dotazů nelze zobrazit!");
                return;
            }
        }
        
        this.init();
        this.request.send(null);
        this.createCollapsingElem();
    },
    collapse: function() {
        this.currentQuestions = this.originalQuestions;
        this.init();
        this.request.send(null);
        this.createExpandingElem();
    },
    save: function() {
        if (!Page.userID) {
            window.location = "/Profile?action=login";
            return;
        }
        
        var num = parseInt(this.input.value);
        if (num > this.maxQuestions || num < 0) {
            alert("Zadejte číslo v rozsahu 0 - "+this.maxQuestions+"!");
            return;
        }
        new ForumQuestionsSave(this.rid, num);
        
        if (num != this.currentQuestions) {
            if (num != 0) {
                this.currentQuestions = num;
                this.init();
                this.request.send(null);
            } else {
                var table = document.getElementById("forum_table_"+this.rid);
                table.parentNode.removeChild(table);
            }
        }
    },
    getURL: function() {
          var url = this.servletPath;
          url += "?rid="+this.rid;
          url += "&questions="+this.currentQuestions;
          return url;
    },

    readyStateChange: function() {
          if (this.request.readyState == 4 && this.request.status == 200) {
                  this.loadQuestions(this.request.responseText);
          }
    },
    
    loadQuestions: function(text) {
        var tbody = document.getElementById("forum_tbody_"+this.rid);
        tbody.innerHTML = text;
    },
    
    createExpandingElem: function() {
        var paragraph = Toolkit.createElement(document, "div");

        var btn = Toolkit.appendElement(paragraph, "input");
        btn.type = "button";
        btn.value = "rozbalit";
        
        Toolkit.addEventListener(btn, "click", "expand", this);
        
        Toolkit.appendElement(paragraph, "span", null, null, " dalších ");

        this.input = Toolkit.appendElement(paragraph, "input");
        this.input.type = "text";
        this.input.value = 10;
        this.input.size = 2;
        Toolkit.appendElement(paragraph, "span", null, null, " dotazů");
        
        if (this.paragraph)
            this.tfoot.replaceChild(paragraph, this.paragraph);
        else
            this.tfoot.appendChild(paragraph);
        this.paragraph = paragraph;
    },
    
    createCollapsingElem: function() {
        var paragraph = Toolkit.createElement(document, "div");
        
        var btn = Toolkit.appendElement(paragraph, "input");
        btn.type = "button";
        btn.value = "sbalit";
        Toolkit.addEventListener(btn, "click", "collapse", this);
        
        Toolkit.appendElement(paragraph, "span", null, null, " | ");
        
        var save = Toolkit.appendElement(paragraph, "input");
        save.type = "button";
        save.value = "uložit";
        Toolkit.addEventListener(save, "click", "save", this);
        
        Toolkit.appendElement(paragraph, "span", null, null, " nový stav (zobrazovat ");
        this.input = Toolkit.appendElement(paragraph, "input");
        this.input.type = "text";
        this.input.value = this.currentQuestions;
        this.input.size = 2;
        
        Toolkit.appendElement(paragraph, "span", null, null, " dotazů)");
        
        if (this.paragraph)
            this.tfoot.replaceChild(paragraph, this.paragraph);
        else
            this.tfoot.appendChild(paragraph);
        this.paragraph = paragraph;
    }
}

function ForumQuestionsSave(rid, num) {
    this.rid = rid;
    this.num = num;
    this.init();
    this.request.send(null);
}

ForumQuestionsSave.prototype = {
    init: AJAX.init,
    createEventHandler: AJAX.createEventHandler,
    servletPath: "/ajax/forum/numquestions",
    readyStateChange: function() {
        if (this.request.readyState == 4 && this.request.status == 200) {
                alert(this.request.responseText);
        }
    },
    getURL: function() {
        var url = this.servletPath;
        url += "?rid="+this.rid;
        url += "&questions="+this.num;
        url += "&ticket="+Page.ticket;
        return url;
    }
}

function ShortenedPre(pre) {
    this.pre = pre;
    
    this.pre.setAttribute("style", "height: 150px");
    
    this.div = Toolkit.createElement(document, "div");
    this.div.setAttribute("style", "float: right");

    this.btn = Toolkit.appendElement(this.div, "input");
    this.btn.type = "button";
    this.btn.value = "Rozbalit";

    pre.insertBefore(this.div, pre.firstChild);
    Toolkit.addEventListener(this.btn, "click", "buttonClicked", this);
}
ShortenedPre.prototype = {
    collapsed: true,
    buttonClicked: function() {
        if (this.collapsed) {
            this.pre.setAttribute("style", "");
            this.btn.value = "Sbalit";
        } else {
            this.pre.setAttribute("style", "height: 150px");
            this.btn.value = "Rozbalit";
        }
        this.collapsed = !this.collapsed;
    }
}

function countOccurences(str, character) {
    var pos = 0, count = 0;
    while ( (pos = str.indexOf(character, pos)) != -1 ) {
        pos++;
        count++;
    }
    return count;
}

function shortenLongOutputs() {
    var pres = document.getElementsByTagName("pre");
    for (var i = 0; i < pres.length; i++) {
        if (countOccurences(pres[i].innerHTML, "\n") > 15)
          new ShortenedPre(pres[i]);
    }
}

function init(event, gecko) {
	if (gecko) {
		document.getElementById('menu').style.display='block';
		window.setTimeout(function () {
			document.getElementById('menu').style.display = 'table'
		}, 10);
	}

	if (document.getElementById) {
		window.dsUtils = new Object();
		dsUtils.re_comment = /\bds_hlavicka(?:_novy)?\b/;
		dsUtils.re_comment_novy = /\bds_hlavicka_novy\b/;
		dsUtils.re_comment_id = /(\d+)/;
		if (window.Node == null) {
			window.Node = new Object();
			window.Node.ELEMENT_NODE = 1;
		}
		prepareCommentNext();
	}
        if (document.getElementsByTagName) {
            shortenLongOutputs();
        }

	new Stitky();
	new StitkyLink();
}


if (window.opera == null) {
	if (window.attachEvent) {
		Toolkit.addEventListener(window, "load", init, false);
	} else {
		Toolkit.addEventListener(document, "load", init, false);
	}
}
Toolkit.addEventListener(window, "DOMContentLoaded", init, window.opera == null);
//window.addEventListener("load", init, true);
//window.onload = init;
