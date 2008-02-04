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
} else {
	Toolkit.addEventListenerImpl = function(element, type, handler) {
		element.attachEvent("on"+type, handler);
	};

	Toolkit.removeEventListenerImpl = function(element, type, handler) {
		element.detachEvent("on"+type, handler);
	};
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


function Stitky() {
	var seznamStitku = document.getElementById("prirazeneStitky");
	if (!seznamStitku) {
		return;
	}

	var button =  Toolkit.appendElement(seznamStitku.parentNode, "button", "editTags", null, "Upravit");
	Toolkit.addEventListener(button, "click", "showDialog", this);
}

Stitky.prototype = {
	showDialog: function(event) {
		new StitkyDialog(Page.relationID);
	}
}

function StitkyDialog(nodeID) {
	this.counter = 0;
	this.lastRefresh = 0;
	this.nodeID = nodeID;
	this.stitkyElement = document.getElementById("prirazeneStitky");
	this.dialog = new ModalWindow("modalStitky", null, this);
	this.stitky = new Array();

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

	Toolkit.addEventListener(this.filtrInput, "keypress", "filtrChange", this);
	Toolkit.addEventListener(this.pridatButton, "click", "pridatStitek", this);

	new StitekAJAX(this, this.nodeID, "assigned");
	new SeznamStitkuAJAX(this, this.nodeID);
	new OblibeneStitkyAJAX(this, this.nodeID);
}

StitkyDialog.prototype = {
	seznamSize: 8,

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
		var element = Toolkit.createElement(this.stitkyElement.ownerDocument, "span", null, "prirazeneStitky");
		if (this.stitky.length > 0) {
			for (var i = 0; i < this.stitky.length; i++) {
				if (i > 0) {
					element.appendChild(element.ownerDocument.createTextNode(", "));
				}
				var a = Toolkit.appendElement(element, "a", null, null, this.stitky[i].title);
				a.href = "/stitky/"+this.stitky[i].id;
				a.title = "Zobrazit objekty, které mají přiřazen tento štítek";
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
	this.request.send();
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
			url += "&title="+this.stitekName;
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
	this.request.send();
}

SeznamStitkuAJAX.prototype = {
	init: AJAX.init,
	createEventHandler: AJAX.createEventHandler,

//	servletPath: "/data/site/stitky2.xml",
	servletPath: "/ajax/tags/list",


	getURL: function() {
		var url = this.servletPath;
		if (this.filtr) {
			url += "?filter="+this.filtr;
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
//			Toolkit.addEventListener(a, "click", "pridatStitek", this, stitek.id);
		}
		Toolkit.addEventListener(stitkySeznam, "change", "pridatStitek", this, stitkySeznam);
		this.stitky.stitkySeznam.parentNode.replaceChild(stitkySeznam, this.stitky.stitkySeznam);
		this.stitky.stitkySeznam = stitkySeznam;
	},

//	pridatStitek: function(event, stitekID) {
//		new StitekAJAX(this.stitky, this.nodeID, "assign", stitekID);
//		return false;
//	},

	pridatStitek: function(event, seznam) {
		new StitekAJAX(this.stitky, this.nodeID, "assign", seznam.options[seznam.selectedIndex].value);
		return false;
	}
}

function OblibeneStitkyAJAX(stitky, nodeID) {
	this.stitky = stitky;
	this.nodeID = nodeID;

	this.init();
	this.request.send();
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

	new Stitky();
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
