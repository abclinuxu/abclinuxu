function prepni_sloupec() {
    if (document.getElementById('ls').style.display == 'none') {
        document.getElementById('ls').style.display = 'block'
        document.getElementById('ls_prepinac').style.width = '268px'
        document.getElementById('ls_prepinac_img').src = '/images/site2/sipkaon-text.gif'
        document.getElementById('st').style.marginLeft = '270px';
    }
    else {
        document.getElementById('ls').style.display = 'none'
        document.getElementById('ls_prepinac').style.width = '45px'
        document.getElementById('ls_prepinac_img').src = '/images/site2/sipkaoff-text.gif'
        document.getElementById('st').style.marginLeft = '0px';
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
}

function addSidebar() {
    if ((typeof window.sidebar == "object") && (typeof window.sidebar.addPanel == "function")) {
        window.sidebar.addPanel("www.abclinuxu.cz",'http://www.abclinuxu.cz/?varianta=sidebar',"");
    } else {
        window.alert("V� prohl��e� nepodporuje tuto funkci. Zkuste Mozillu.");
    }
}

function setHomepage() {
	if (document.all) {
    document.body.style.behavior='url(#default#homepage)';
		document.body.setHomePage("http://www.abclinuxu.cz/");
	}
	else {
		window.alert("Pou�ijte pros�m nastaven� sv�ho prohl��e�e.");
	}
}

function addBookmark() {
	if (document.all) {
		window.external.AddFavorite("http://www.abclinuxu.cz","AbcLinuxu.cz");
	}
	else {
		window.alert("Pou�ijte pros�m kombinaci Ctrl-D.");
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

