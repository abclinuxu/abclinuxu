//
// adVANTAGE AS Interface Toolkit
// 2004 - 2008 (c) Via Aurea, s.r.o.
// 
// http://www.viaaurea.cz/
// http://www.advantage.as/
//
var AV_VER = '0.79'; //November 2007
var AV_STOP = false; //umožňuje vypnutí reklamy
var AV_DEBUG = false;
var AV_CONTEXT_AD = true; //umožňuje vypnutí/zapnutí kontextové reklamy

var AV_ERR_CONCAT = "\nV případě problémů prosím kontaktujte admin@advantage.as.";
var AV_ERR_NOID = 'AV_Chyba: funkce AV_print() volá neexistující ID pozice.' + AV_ERR_CONCAT;
var AV_ERR_IDPAGE = 'AV_Chyba: funkce AV_init() musí mít v parametru ID stránky. ' + AV_ERR_CONCAT;
	
var AV_PROT = "http://";
var AV_URL = "stf.advantage.as"
var AV_SCRIPT = "/if/imshow.php"; 

var AV_JSLoaded = false;
var AV_preLoaded = false; //určuje, jestli se kreativa natahuje před zobrazením
var AV_adverLoaded = false; //určuje, jestli se ze serveru načetla reklama
AV_konfigurace = new Array();
AV_poziceArr = new Array();
AV_poziceCalledArr = new Array(); //seznam volaných pozic ve funkci AV_pozice();
AV_reklamyArr = new Array();
AV_cileni = new Array();
AV_stranka = null;

AV_posHTMLArr = new Array();
AV_advHTMLArr = new Array();

//
// Inline detekce flashe.
//
if (typeof(IMFV) == 'undefined'){
	IMFV=0;
	FLASH_MAX=15; // do které verze se mají provádět testy
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
// Funkce, která načte kódy reklamy ze serveru.
//
function AV_init(page){
	
	// pokud se jedná o volání funkce po načtení dat ze serveru (IF1 Float)
	if(AV_adverLoaded){
		// Pokud server poslal nějakou hlášku, vypíše ji
		if(typeof(AV_checkMsg) != 'undefined') alert(AV_checkMsg);
		if(AV_DEBUG) AV_poziceMissAlert(AV_poziceCheck());
	}
	
	if(AV_STOP || AV_adverLoaded) return;
	
	//
	// Ověření správnosti vstupů.
	//
	if (typeof(page) != "number"){
		if (AV_stranka) page = AV_stranka;
	}
	if (AV_DEBUG){
		window.setTimeout('window.status = "Debug on! - reklamní systém adVANTAGE"',2000); //zobrazí upozornění na debug mod
		if (typeof(page) != "number")	alert(AV_ERR_IDPAGE);
	}

	//
	// Načtení informací z prohlížeče uživatele.
	//
	AV_konfigurace['ver'] = AV_VER;
	AV_konfigurace['ref'] = this.location.href;
	AV_konfigurace['fla'] = IMFV;
	if (!AV_konfigurace['typ']) AV_konfigurace['typ'] = 'js';
	AV_konfigurace['chr'] = AV_getCharset();
	AV_konfigurace['coo'] = AV_cookieTest();
	if (!AV_konfigurace['subtyp']){
		if (AV_preLoaded){
			AV_konfigurace['subtyp'] = 'pre';
		}else{
			AV_konfigurace['subtyp'] = 'post';
		}
	}
		
	var query = "";		
	if(page != null) query += (query ? "&" : "?") + AV_toGET(page, 'pg');
	query += (query ? "&" : "?") + AV_toGET(AV_poziceArr, 'ps');
	query += (query ? "&" : "?") + AV_toGET(AV_reklamyArr, 'ad');
	query += (query ? "&" : "?") + AV_toGET(AV_cileni, 'tg');
	query += (query ? "&" : "?") + AV_toGET(AV_konfigurace, 'cf');
	query += (query ? "&" : "?") + AV_toGET(Math.random(), 'rnd');
	
	AV_advantageArr = AV_urlParams('advantage');
	if (AV_DEBUG){
		if (AV_advantageArr === false) AV_advantageArr = new Array('debug')
		else AV_advantageArr.push('debug');
	}
	if (AV_advantageArr){
		query += (query ? "&" : "?") + AV_toGET(AV_advantageArr, 'advantage');
	}
	
	document.write('<scr'+'ipt src="' + AV_PROT + AV_URL + AV_SCRIPT + query + '" type="text/javascript" charset="windows-1250"></scr'+'ipt>');
}

//
// Funkce která načte kódy reklamy podle ID reklamy (není třeba znát stránku).
//
function AV_initRekl(){
	AV_init(null);
}

function AV_preload(page){
	AV_preLoaded = true;
	AV_init(page);
}

function AV_preloadRekl(){
	AV_preload(null);
}

//
// Funkce převádí proměnou variable do parametru GETu. Parametr variableName označuje název.
//
function AV_toGET(variable, variableName){
	if (typeof(variable) == "object"){			
		var buff = "";
		for(var i in variable){
			if (typeof(variable[i]) == 'function' || typeof(variable[i]) == 'object') continue; //některý server předefinováva bázovou třídu Array a přídává jí vlastnost / metody
			buff += ((buff == "")?"":"&") + variableName + AV_urlEncode("[" + i + "]") + 
				"=" + AV_urlEncode(variable[i]);
		}
		return buff;
	}else if(typeof(variable) == "boolean"){
		return variableName + "=" + (variable ? 1 : 0);
	}else{
		return variableName + "=" + AV_urlEncode(variable);
	}
}

//
// Funkce zoborazí reklamy, je volána ve skritu vráceném ze serveru.
//
function AV_print(){
	if(AV_preLoaded || AV_STOP) return;
	var pos;
	for (var i in AV_posArr){
		if (typeof(AV_posArr[i]) == 'function' || typeof(AV_posArr[i]) == 'object') continue; 
		if (AV_posArr[i]['html'] != "" && (pos = document.getElementById('AV_pos' + i))){
		  pos.innerHTML = AV_posHTMLArr[i]['head'] + AV_recoding(AV_posArr[i]['html']) + AV_posHTMLArr[i]['foot'];
		}else if(AV_DEBUG){
			alert(AV_ERR_NOID);
		}
	}
	for (var i in AV_advArr){
		if (typeof(AV_advArr[i]) == 'function' || typeof(AV_advArr[i]) == 'object') continue; 
		if (AV_advArr[i]['html'] != "" && (pos = document.getElementById('AV_adv' + i))){
		  pos.innerHTML = AV_advHTMLArr[i]['head'] +  AV_recoding(AV_advArr[i]['html']) + AV_advHTMLArr[i]['foot'];
		}else if(AV_DEBUG){
			alert(AV_ERR_NOID);
		}
	}
}

//
// Funkce umístí reklamu do stránky podle ID reklamy.
//
function AV_reklama(id, advHead, advFoot){
	if(AV_STOP) return;
	advHead = advHead ? advHead : '';
	advFoot = advFoot ? advFoot : '';
	if(AV_preLoaded){
		if(AV_adverLoaded && AV_advArr[id] && AV_advArr[id]['html']){
			document.write(advHead);
			document.write(AV_recoding(AV_advArr[id]['html']));
			document.write(advFoot);
		}
	}else{
		AV_reklamyArr[AV_reklamyArr.length] = id;
		AV_advHTMLArr[id] = new Array();
		AV_advHTMLArr[id]['head'] = advHead;
		AV_advHTMLArr[id]['foot'] = advFoot;
		document.write('<span id="AV_adv' + id + '"> </span>');
	}
}

//
// Funkce umístí reklamu do stránky podle ID pozice.
//
function AV_pozice(id, posHead, posFoot){
	AV_poziceCalledArr.push(id);
	if(AV_STOP) return;
	posHead = posHead ? posHead : '';
	posFoot = posFoot ? posFoot : '';
	if(AV_preLoaded){
		if(AV_adverLoaded && AV_posArr[id] && AV_posArr[id]['html']){
			document.write(posHead);
			document.write(AV_recoding(AV_posArr[id]['html']));
			document.write(posFoot);
		}
	}else{
		AV_poziceArr[AV_poziceArr.length] = id;
		AV_posHTMLArr[id] = new Array();
		AV_posHTMLArr[id]['head'] = posHead;
		AV_posHTMLArr[id]['foot'] = posFoot;
		document.write('<span id="AV_pos' + id + '"> </span>');
	}
}


//
// Funkce ověří, jestli byly volány pro všechny pozice funkce AV_pozice(). Ty které nebyly, vráti v poli.
//
function AV_poziceCheck(){
	var missing = new Array();
	for(var i=0; i<AV_poziceDbArr.length; i++){
		found = false;
		for(var j=0; j<AV_poziceCalledArr.length; j++){
			if (AV_poziceDbArr[i] == AV_poziceCalledArr[j]){
				found = true;
				continue;
			}
		}
		if (!found) missing.push(AV_poziceDbArr[i]);
	}
	return missing;
}


function AV_poziceMissAlert(missing){
	tmpStr = '';
	for (var i=0; i < missing.length; i++){
		tmpStr +=  "\r\n" + (typeof(AV_poziceNameArr) != "undefined" && AV_poziceNameArr[missing[i]] ? AV_poziceNameArr[missing[i]] + " (" + missing[i] + ")" : missing[i]);
	}
	if (tmpStr){
		alert("Ověření korektního nasazení reklmaního systému adVANTAGE.\r\n\r\nPro následující pozice nebyla ve stránce volána fuknce AV_pozice():" + tmpStr);
	}
}

//
// Funkce pro test COOKIES
//
function AV_cookieTest(){	
	document.cookie = 'AV_cookie=true';
	return (document.cookie.indexOf('AV_cookie') >= 0 ? 1 : 0);
}

//
// Funkce zjistí kódování stránky.
//
function AV_getCharset(){
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
// Funkce pro převod znakové sady win1250 a iso-8859-2.
//
function AV_win2iso(str, fromCharset){
	var conv1250 = "ĽŠŤÝŽľšťž";
	var convISO = "Ą©«Ý®µą»ľ";
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


function AV_recoding(str){
	if (navigator.userAgent.indexOf("MSIE 5") >= 0 && AV_getCharset() == "iso-8859-2"){
		return AV_win2iso(str, "win");
	}else{
		return str;
	}
}

//
// Funkce zakóduje řetězec do formátu, který není konfliktní pro přenos v URL (GETu)
//
function AV_urlEncode(str){
	str = escape(str);
	// nahradi znaky *@+/ příslušnými entitami
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
// Funkce se podívá do URL stránky a vyhledá všechny parametry, např. 'advantage', které vrátí jako pole
//
function AV_urlParams(param){
	try {
		var url = top.location.href;
	}
	catch (e) {
		var url = this.location.href;
	}
	if (url.indexOf('?') > -1){
		urlGet = url.substr(url.indexOf('?'));
		re = new RegExp("[?&]" + param + "=([^&]+)", "g");
		tmpArr = urlGet.match(re);
		if (!tmpArr) return false;
		for (var i = 0; i < tmpArr.length; i++){
			tmpArr[i] = tmpArr[i].substr(tmpArr[i].indexOf('=')+1);
			if (tmpArr[i] == 'debug' || tmpArr[i] == 'check') AV_DEBUG = true; // z URL vyvolany debug
		}
		return tmpArr;
	}
	return false;
}

//
// Funkce pro obejití aktivace flash v IE
//
function AV_write(string){
	document.write(string);
}

function AV_objectRewrite(name){	
	if (typeof(name) == "undefined") name = "object";
	
	var objects = document.getElementsByTagName(name);
	for (var i=0; i<objects.length; i++){
		objects[i].outerHTML = objects[i].outerHTML;
	}
}

function AV_objectRewriteById(id){
	var obj = document.getElementById(id);
	obj.outerHTML = obj.outerHTML;
}

//
// Funkce určené pro integraci kontextové reklamy
//
function AV_context(server, url, css){
	if (!AV_CONTEXT_AD) return false;
	if (css){
		document.write('<style>@import url(' + css + ');</style>');
	}
	AV_adFox(server, url);
}

//
// Funkce sloužící jako interface pro eTarget.
// Parametr server nese ID serveru v adVANTAGE, proměnná serverArr je pole
// proměnných systému eTarget o struktuře array(ID_serveru, ID_country).
//
function AV_eTarget(server, url){
	serverArr = AV_advantage2eTarget(server);
	document.write('<SCRIPT src="http://search.etargetnet.com/cz/impressionmedia/context_ad.php?c=' +
		serverArr[1] +	'&ref=' + serverArr[0] + '&q=' + AV_urlEncode(url) + '"></SCRIPT>');
}

//
// Funkce sloužící jako interface pro adFox.
// Parametr server nese ID serveru v adVANTAGE, proměnná servernt je ID v adFox.
//
function AV_adFox(server, url){
	serverInt = AV_advantage2adFox(server);
	document.write('<SCRIPT src="http://ad.adfox.cz/ppcbe?js=1&format=666699ffffff3366ff00000033996632&partner=' +
		serverInt + '&stranka=' + url + '"></SCRIPT>');
}

//
// Funkce převádí ID serveru na adVANTAGE na ID serveru a ID země eTargetu.
// Funkce je speciálně pro instanci IM.
//
function AV_advantage2eTarget(id){
	idConverArr = new Array();
	idConverArr[2]  = new Array(344,2); //server.cz
	return idConverArr[id];
}

//
// Funkce převádí ID serveru na adVANTAGE na ID serveru systému adFox.
// Funkce je speciálně pro instanci IM.
//
function AV_advantage2adFox(id){
	idConverArr = new Array();
	idConverArr[2]  = 990; //server.cz
	return idConverArr[id];
}

AV_JSLoaded = true;


