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
				if (document.getElementById('comment'+id+'_toggle2')) {
					document.getElementById('comment'+id+'_toggle2').style.display = 'none'
				}
    }
    else {
        document.getElementById('comment'+id).style.display = 'none'
        document.getElementById('comment'+id+'_controls').style.display = 'none'
        document.getElementById('comment'+id+'_toggle1').innerHTML = 'Rozbalit'
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

function init() {
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
}

window.onload = init;
