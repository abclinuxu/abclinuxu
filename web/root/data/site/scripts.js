function prepni_sloupec() {
    if (document.getElementById('ls').style.display == 'none') {
        document.getElementById('ls').style.display = 'block'
        /* document.getElementById('ls_prepinas_help').style.display = 'inline' */
        document.getElementById('ls_prepinac').style.width = '268px'
        document.getElementById('ls_prepinac_img').src = '/images/site2/sipkaon-text.gif'
        document.getElementById('st').style.marginLeft = '270px';
    }
    else {
        document.getElementById('ls').style.display = 'none'
        document.getElementById('ls_prepinac').style.width = '45px'
        /* document.getElementById('ls_prepinas_help').style.display = 'none' */
        document.getElementById('ls_prepinac_img').src = '/images/site2/sipkaoff-text.gif'
        document.getElementById('st').style.marginLeft = '0px';
    }
}

function addSidebar() {
    if ((typeof window.sidebar == "object") && (typeof window.sidebar.addPanel == "function")) {
        window.sidebar.addPanel("www.abclinuxu.cz",'http://www.abclinuxu.cz/?varianta=sidebar',"");
    } else {
        window.alert("Vá¹ prohlí¾eè nepodporuje tuto funkci. Zkuste Mozillu.");
    }
}

function setHomepage() {
	if (document.all) {
    document.body.style.behavior='url(#default#homepage)';
		document.body.setHomePage("http://www.abclinuxu.cz/");
	}
	else {
		window.alert("Pou¾ijte prosím nastavení svého prohlí¾eèe.");
	}
}

function addBookmark() {
	if (document.all) {
		window.external.AddFavorite("http://www.abclinuxu.cz","AbcLinuxu.cz");
	}
	else {
		window.alert("Pou¾ijte prosím kombinaci Ctrl-D.");
	}
}
