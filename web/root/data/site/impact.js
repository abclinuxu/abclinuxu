//
// Impact AS Interface Toolkit
// 2004 - 2005 (c) Via Aurea, s.r.o.
// 
// http://www.viaaurea.cz/
// http://www.impact.as/
//
var IM_VER = '0.75'; //January 2006
var IM_STOP = false; //umožòuje vypnutí reklamy
var IM_DEBUG = false;
var IM_CONTEXT_AD = true; //umožòuje vypnutí/zapnutí kontextové reklamy

var IM_ERR_CONCAT = "\nV pøípadì problémù prosím kontaktujte admin@impact.as.";
var IM_ERR_NOID = 'IM_Chyba: funkce IM_print() volá neexistující ID pozice.' + IM_ERR_CONCAT;
var IM_ERR_IDPAGE = 'IM_Chyba: funkce IM_init() musí mít v parametru ID stránky. ' + IM_ERR_CONCAT;
	
var IM_PROT = "http://";
var IM_URL = "sf.impact.as"
var IM_SCRIPT = "/if/imshow.php"; 

var IM_JSLoaded = false;
var IM_preLoaded = false; //urèuje, jestli se kreativa natahuje pøed zobrazením
var IM_adverLoaded = false; //urèuje, jestli se ze serveru naèetla reklama
IM_konfigurace = new Array();
IM_poziceArr = new Array();
IM_poziceCalledArr = new Array(); //seznam volaných pozic ve funkci IM_pozice();
IM_reklamyArr = new Array();
IM_cileni = new Array();
IM_stranka = null;

IM_posHTMLArr = new Array();
IM_advHTMLArr = new Array();

//
// Inline detekce flashe.
//
if (typeof(IMFV) == 'undefined'){
	IMFV=0;
	FLASH_MAX=15; // do které verze se mají provádìt testy
	plugin = (navigator.mimeTypes && navigator.mimeTypes["application/x-shockwave-flash"]) ? navigator.mimeTypes["application/x-shockwave-flash"].enabledPlugin : 0;
	if ( plugin ) {
		var words = navigator.plugins["Shockwave Flash"].description.split(" ");
		for (var i = 0; i < words.length; ++i){
	 		if (isNaN(parseInt(words[i]))) continue;
	 		var IMFV=words[i]; 
		}
	}
	else if (navigator.userAgent && navigator.userAgent.indexOf("MSIE")>=0 
		&& (navigator.appVersion.indexOf("Win") != -1)) {	
		document.write('<SCR' + 'IPT LANGUAGE=VBScript\> \n');
		document.write('i=3\n');
		document.write('on error resume next\n');
		document.write('Do While  IMFV=0 AND i<=FLASH_MAX\n');
		document.write('if (isNull(CreateObject("ShockwaveFlash.ShockwaveFlash." & i)) AND false) then IMFV=i\n');
		document.write('i=i+1\n');
		document.write('Loop\n');
		document.write('if IMFV<>0 then IMFV=IMFV-1\n');
		document.write('</SCR' + 'IPT\>\n');
	}
}

//
// Funkce, která naète kódy reklamy ze serveru.
//
function IM_init(page){
	
	// pokud se jedná o volání funkce po naètení dat ze serveru (IF1 Float)
	if(IM_adverLoaded){
		// Pokud server poslal nìjakou hlášku, vypíše ji
		if(typeof(IM_checkMsg) != 'undefined') alert(IM_checkMsg);
		if(IM_DEBUG) IM_poziceMissAlert(IM_poziceCheck());
	}
	
	if(IM_STOP || IM_adverLoaded) return;
	
	//
	// Ovìøení správnosti vstupù.
	//
	if (typeof(page) != "number"){
		if (IM_stranka) page = IM_stranka;
	}
	if (IM_DEBUG){
		window.setTimeout('window.status = "Debug on! - reklamní systém Impact"',2000); //zobrazí upozornìní na debug mod
		if (typeof(page) != "number")	alert(IM_ERR_IDPAGE);
	}

	//
	// Naètení informací z prohlížeèe uživatele.
	//
	IM_konfigurace['ver'] = IM_VER;
	IM_konfigurace['ref'] = this.location.href;
	IM_konfigurace['fla'] = IMFV;
	if (!IM_konfigurace['typ']) IM_konfigurace['typ'] = 'js';
	IM_konfigurace['chr'] = IM_getCharset();
	IM_konfigurace['coo'] = IM_cookieTest();
	if (!IM_konfigurace['subtyp']){
		if (IM_preLoaded){
			IM_konfigurace['subtyp'] = 'pre';
		}else{
			IM_konfigurace['subtyp'] = 'post';
		}
	}
		
	var query = "";		
	if(page != null) query += (query ? "&" : "?") + IM_toGET(page, 'pg');
	query += (query ? "&" : "?") + IM_toGET(IM_poziceArr, 'ps');
	query += (query ? "&" : "?") + IM_toGET(IM_reklamyArr, 'ad');
	query += (query ? "&" : "?") + IM_toGET(IM_cileni, 'tg');
	query += (query ? "&" : "?") + IM_toGET(IM_konfigurace, 'cf');
	query += (query ? "&" : "?") + IM_toGET(Math.random(), 'rnd');
	
	IM_impactArr = IM_urlParams('impact');
	if (IM_DEBUG){
		if (IM_impactArr === false) IM_impactArr = new Array('debug')
		else IM_impactArr.push('debug');
	}
	if (IM_impactArr){
		query += (query ? "&" : "?") + IM_toGET(IM_impactArr, 'impact');
	}
	
	document.write('<scr'+'ipt src="' + IM_PROT + IM_URL + IM_SCRIPT + query + '" type="text/javascript" charset="windows-1250"></scr'+'ipt>');
}

//
// Funkce která naète kódy reklamy podle ID reklamy (není tøeba znát stránku).
//
function IM_initRekl(){
	IM_init(null);
}

function IM_preload(page){
	IM_preLoaded = true;
	IM_init(page);
}

function IM_preloadRekl(){
	IM_preload(null);
}

//
// Funkce pøevádí promìnou variable do parametru GETu. Parametr variableName oznaèuje název.
//
function IM_toGET(variable, variableName){
	if (typeof(variable) == "object"){			
		var buff = "";
		for(var i in variable){
			if (i == 'copy') continue; //pravdìpodobnì BUG v JS, obèas i nabýva tuto hodnotu,  ikdyž není definováno 
			buff += ((buff == "")?"":"&") + variableName + IM_urlEncode("[" + i + "]") + 
				"=" + IM_urlEncode(variable[i]);
		}
		return buff;
	}else if(typeof(variable) == "boolean"){
		return variableName + "=" + (variable ? 1 : 0);
	}else{
		return variableName + "=" + IM_urlEncode(variable);
	}
}

//
// Funkce zoborazí reklamy, je volána ve skritu vráceném ze serveru.
//
function IM_print(){
	if(IM_preLoaded || IM_STOP) return;
	var pos;
	for (var i in IM_posArr){
		if (IM_posArr[i]['html'] != "" && (pos = document.getElementById('IM_pos' + i))){
		  pos.innerHTML = IM_posHTMLArr[i]['head'] + IM_recoding(IM_posArr[i]['html']) + IM_posHTMLArr[i]['foot'];
		}else if(IM_DEBUG){
			alert(IM_ERR_NOID);
		}
	}
	for (var i in IM_advArr){
		if (IM_advArr[i]['html'] != "" && (pos = document.getElementById('IM_adv' + i))){
		  pos.innerHTML = IM_advHTMLArr[i]['head'] +  IM_recoding(IM_advArr[i]['html']) + IM_advHTMLArr[i]['foot'];
		}else if(IM_DEBUG){
			alert(IM_ERR_NOID);
		}
	}
}

//
// Funkce umístí reklamu do stránky podle ID reklamy.
//
function IM_reklama(id, advHead, advFoot){
	if(IM_STOP) return;
	advHead = advHead ? advHead : '';
	advFoot = advFoot ? advFoot : '';
	if(IM_preLoaded){
		if(IM_adverLoaded && IM_advArr[id] && IM_advArr[id]['html']){
			document.write(advHead);
			document.write(IM_recoding(IM_advArr[id]['html']));
			document.write(advFoot);
		}
	}else{
		IM_reklamyArr[IM_reklamyArr.length] = id;
		IM_advHTMLArr[id] = new Array();
		IM_advHTMLArr[id]['head'] = advHead;
		IM_advHTMLArr[id]['foot'] = advFoot;
		document.write('<span id="IM_adv' + id + '"> </span>');
	}
}

//
// Funkce umístí reklamu do stránky podle ID pozice.
//
function IM_pozice(id, posHead, posFoot){
	IM_poziceCalledArr.push(id);
	if(IM_STOP) return;
	posHead = posHead ? posHead : '';
	posFoot = posFoot ? posFoot : '';
	if(IM_preLoaded){
		if(IM_adverLoaded && IM_posArr[id] && IM_posArr[id]['html']){
			document.write(posHead);
			document.write(IM_recoding(IM_posArr[id]['html']));
			document.write(posFoot);
		}
	}else{
		IM_poziceArr[IM_poziceArr.length] = id;
		IM_posHTMLArr[id] = new Array();
		IM_posHTMLArr[id]['head'] = posHead;
		IM_posHTMLArr[id]['foot'] = posFoot;
		document.write('<span id="IM_pos' + id + '"> </span>');
	}
}


//
// Funkce ovìøí, jestli byly volány pro všechny pozice funkce IM_pozice(). Ty které nebyly, vráti v poli.
//
function IM_poziceCheck(){
	var missing = new Array();
	for(var i=0; i<IM_poziceDbArr.length; i++){
		found = false;
		for(var j=0; j<IM_poziceCalledArr.length; j++){
			if (IM_poziceDbArr[i] == IM_poziceCalledArr[j]){
				found = true;
				continue;
			}
		}
		if (!found) missing.push(IM_poziceDbArr[i]);
	}
	return missing;
}


function IM_poziceMissAlert(missing){
	tmpStr = '';
	for (var i=0; i < missing.length; i++){
		tmpStr +=  "\r\n" + missing[i];
	}
	if (tmpStr){
		alert("Ovìøení korektního nasazení reklmaního systému Impact.\r\n\r\nPro následující pozice nebyla ve stránce volána fuknce IM_pozice():" + tmpStr);
	}
}

//
// Funkce pro test COOKIES
//
function IM_cookieTest(){	
	document.cookie = 'IM_cookie=true';
	return (document.cookie.indexOf('IM_cookie') >= 0 ? 1 : 0);
}

//
// Funkce zjistí kódování stránky.
//
function IM_getCharset(){
	var charsetRe = /charset=([a-zA-Z0-9\-]+)/i;
	var elmArr = document.getElementsByTagName("meta");
	var content, resArr;
	for (var i=0; i < elmArr.length; i++){
		if (content = elmArr[i].getAttribute("content")){
			if (resArr = charsetRe.exec(content)){
				return resArr[1].toLowerCase();
			}
		}
	}
	return "";
}

//
// Funkce pro pøevod znakové sady win1250 a iso-8859-2.
//
function IM_win2iso(str, fromCharset){
	var conv1250 = "¼ŠÝŽ¾šž";
	var convISO = "¥©«Ý®µ¹»¾";
	var buffer = "";
	
	if (fromCharset == "iso"){
		fromSet = convISO;
		toSet = conv1250;
	}else{
		fromSet = conv1250;
		toSet = convISO;
	}

	for (var i = 0; i < str.length; i++){
		if ((index = fromSet.indexOf(str.charAt(i))) >= 0){
			buffer += toSet.charAt(index);
		}else{
			buffer += str.charAt(i);
		}
	}
	return buffer;
}


function IM_recoding(str){
	if (navigator.userAgent.indexOf("MSIE 5") >= 0 && IM_getCharset() == "iso-8859-2"){
		return IM_win2iso(str, "win");
	}else{
		return str;
	}
}

//
// Funkce zakóduje øetìzec do formátu, který není konfliktní pro pøenos v URL (GETu)
//
function IM_urlEncode(str){
	str = escape(str);
	// nahradi znaky *@+/ pøíslušnými entitami
	str = str.replace(/([*@+\/])/g, 
		function (str, foundChar) {
			switch (foundChar){
				case '*': return '%2A'
				case '@': return '%40';
				case '+': return '%2B';
				case '/': return '%2F';
			}
		}
	)
	return str;
}

//
// Funkce se podívá do URL stránky a vyhledá všechny parametry, napø. 'impact', které vrátí jako pole
//
function IM_urlParams(param){
	var url = top.location.href;
	if (url.indexOf('?') > -1){
		urlGet = url.substr(url.indexOf('?'));
		re = new RegExp("[?&]" + param + "=([^&]+)", "g");
		tmpArr = urlGet.match(re);
		if (!tmpArr) return false;
		for (var i = 0; i < tmpArr.length; i++){
			tmpArr[i] = tmpArr[i].substr(tmpArr[i].indexOf('=')+1);
			if (tmpArr[i] == 'debug' || tmpArr[i] == 'check') IM_DEBUG = true; // z URL vyvolany debug
		}
		return tmpArr;
	}
	return false;
}

//
// Funkce urèené pro integraci kontextové reklamy
//
function IM_context(server, url, css){
	if (!IM_CONTEXT_AD) return false;
	if (css){
		document.write('<style>@import url(' + css + ');</style>');
	}
	IM_adFox(server, url);
}

//
// Funkce sloužící jako interface pro eTarget.
// Parametr server nese ID serveru v Impactu, promìnná serverArr je pole
// promìnných systému eTarget o struktuøe array(ID_serveru, ID_country).
//
function IM_eTarget(server, url){
	serverArr = IM_impact2eTarget(server);
	document.write('<SCRIPT src="http://search.etargetnet.com/cz/impressionmedia/context_ad.php?c=' +
		serverArr[1] +	'&ref=' + serverArr[0] + '&q=' + IM_urlEncode(url) + '"></SCRIPT>');
}

//
// Funkce sloužící jako interface pro adFox.
// Parametr server nese ID serveru v Impactu, promìnná servernt je ID v adFox.
//
function IM_adFox(server, url){
	serverInt = IM_impact2adFox(server);
	document.write('<SCRIPT src="http://ad.adfox.cz/ppcbe?js=1&format=666699ffffff3366ff00000033996632&partner=' +
		serverInt + '&stranka=' + url + '"></SCRIPT>');
}

//
// Funkce pøevádí ID serveru na Impactu na ID serveru a ID zemì eTargetu.
// Funkce je speciálnì pro instanci IM.
//
function IM_impact2eTarget(id){
	idConverArr = new Array();
	idConverArr[2]  = new Array(344,2); //chocolate.cz
	idConverArr[3]  = new Array(346,2); //4men.cz
	idConverArr[8] = new Array(146,2); //stratosfera.cz
	idConverArr[15] = new Array(343,2); //dama.cz
	idConverArr[16] = new Array(342,2); //victorie.cz
	idConverArr[24] = new Array(345,2); //mojenoviny.cz
	idConverArr[137] = new Array(468,2); //autoweb.cz
	idConverArr[140] = new Array(348,2); //annonce.cz
	idConverArr[10000]  = new Array(341,2); //auto.cz

	return idConverArr[id];
}

//
// Funkce pøevádí ID serveru na Impactu na ID serveru systému adFox.
// Funkce je speciálnì pro instanci IM.
//
function IM_impact2adFox(id){
	idConverArr = new Array();
	idConverArr[2]  = 990; //chocolate.cz
	idConverArr[3]  = 991; //4men.cz
	idConverArr[8] = 992; //stratosfera.cz
	idConverArr[15] = 993; //dama.cz
	idConverArr[16] = 994; //victorie.cz
	idConverArr[24] = 995; //mojenoviny.cz
	idConverArr[137] = 847; //autoweb.cz
	idConverArr[140] = 998; //annonce.cz
	idConverArr[10000]  = 996; //auto.cz
	return idConverArr[id];
}

IM_JSLoaded = true;


