//
// adVANTAGE AS Interface Toolkit
// 2004 - 2010 (c) Via Aurea, s.r.o.
// 
// http://www.viaaurea.cz/
// http://www.advantage.as/
//
var AV_VER = '0.80'; //October 2009
var AV_STOP = false; //umo��uje vypnut� reklamy
var AV_DEBUG = false;
var AV_CONTEXT_AD = true; //umo��uje vypnut�/zapnut� kontextov� reklamy

var AV_ERR_CONCAT = "\nV p��pad� probl�m� pros�m kontaktujte admin@advantage.as.";
var AV_ERR_NOID = 'AV_Chyba: funkce AV_print() vol� neexistuj�c� ID pozice.' + AV_ERR_CONCAT;
var AV_ERR_IDPAGE = 'AV_Chyba: funkce AV_init() mus� m�t v parametru ID str�nky. ' + AV_ERR_CONCAT;
var AV_ERR_QUERY = 'AV_Chyba: snaha o znovu na�ten� reklamy AV_reload() bez p�edchoz� inicializace AV_init(). ' + AV_ERR_CONCAT;
var AV_ERR_HTTP = 'AV_Chyba: nelze otev��t HTTP spojen� pomoc� funkce AV_reload(). ' + AV_ERR_CONCAT;
	
var AV_PROT = "http://";
var AV_URL = "stf.advantage.as"
var AV_SCRIPT = "/if/imshow.php"; 

var AV_JSLoaded = false;
var AV_preLoaded = false; //ur�uje, jestli se kreativa natahuje p�ed zobrazen�m
var AV_adverLoaded = false; //ur�uje, jestli se ze serveru na�etla reklama
AV_konfigurace = new Array();
AV_poziceArr = new Array();
AV_poziceCalledArr = new Array(); //seznam volan�ch pozic ve funkci AV_pozice();
AV_reklamyArr = new Array();
AV_cileni = new Array();
AV_stranka = null;
AV_query = null;

AV_posHTMLArr = new Array();
AV_advHTMLArr = new Array();

//
// Inline detekce flashe.
//
if (typeof(IMFV) == 'undefined'){
	IMFV=0;
	FLASH_MAX=15; // do kter� verze se maj� prov�d�t testy
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
// Funkce, kter� na�te k�dy reklamy ze serveru.
//
function AV_init(page){
	
	// pokud se jedn� o vol�n� funkce po na�ten� dat ze serveru (IF1 Float)
	if(AV_adverLoaded){
		// Pokud server poslal n�jakou hl�ku, vyp�e ji
		if(typeof(AV_checkMsg) != 'undefined') alert(AV_checkMsg);
		if(AV_DEBUG) AV_poziceMissAlert(AV_poziceCheck());
	}
	
	if(AV_STOP || AV_adverLoaded) return;
	
	//
	// Ov��en� spr�vnosti vstup�.
	//
	if (typeof(page) != "number"){
		if (AV_stranka) page = AV_stranka;
	}
	if (AV_DEBUG){
		window.setTimeout('window.status = "Debug on! - reklamn� syst�m adVANTAGE"',2000); //zobraz� upozorn�n� na debug mod
		if (typeof(page) != "number")	alert(AV_ERR_IDPAGE);
	}

	//
	// Na�ten� informac� z prohl�e�e u�ivatele.
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
		
	AV_query = "";		
	if(page != null) AV_query += (AV_query ? "&" : "?") + AV_toGET(page, 'pg');
	AV_query += (AV_query ? "&" : "?") + AV_toGET(AV_poziceArr, 'ps');
	AV_query += (AV_query ? "&" : "?") + AV_toGET(AV_reklamyArr, 'ad');
	AV_query += (AV_query ? "&" : "?") + AV_toGET(AV_cileni, 'tg');
	AV_query += (AV_query ? "&" : "?") + AV_toGET(AV_konfigurace, 'cf');
	
	AV_advantageArr = AV_urlParams('advantage');
	if (AV_DEBUG){
		if (AV_advantageArr === false) AV_advantageArr = new Array('debug')
		else AV_advantageArr.push('debug');
	}
	if (AV_advantageArr){
		AV_query += (AV_query ? "&" : "?") + AV_toGET(AV_advantageArr, 'advantage');
	}
	AV_query =  AV_PROT + AV_URL + AV_SCRIPT + AV_query;
	document.write('<scr'+'ipt src="' + AV_query + AV_toGET(Math.random(), 'rnd') + '" type="text/javascript" charset="windows-1250"></scr'+'ipt>');
}

//
// Funkce kter� na�te k�dy reklamy podle ID reklamy (nen� t�eba zn�t str�nku).
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
// Funkce p�ev�d� prom�nou variable do parametru GETu. Parametr variableName ozna�uje n�zev.
//
function AV_toGET(variable, variableName){
	if (typeof(variable) == "object"){			
		var buff = "";
		for(var i in variable){
			if (typeof(variable[i]) == 'function' || typeof(variable[i]) == 'object') continue; //n�kter� server p�edefinov�va b�zovou t��du Array a p��d�v� j� vlastnost / metody
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
// Funkce zoboraz� reklamy, je vol�na ve skritu vr�cen�m ze serveru.
//
function AV_print(){
	if(AV_preLoaded || AV_STOP) return;
	var pos;	
	for (var i in AV_posArr){
		if (typeof(AV_posArr[i]) == 'function' || typeof(AV_posArr[i]) == 'object' && isNaN(i)) continue; 
		if (AV_posArr[i]['html'] != "" && (pos = document.getElementById('AV_pos' + i))){
		  pos.innerHTML = AV_posHTMLArr[i]['head'] + AV_recoding(AV_posArr[i]['html']) + AV_posHTMLArr[i]['foot'];
		}else if(AV_DEBUG){
			alert(AV_ERR_NOID);
		}
	}
	for (var i in AV_advArr){
		if (typeof(AV_advArr[i]) == 'function' || typeof(AV_advArr[i]) == 'object' && isNaN(i)) continue; 
		if (AV_advArr[i]['html'] != "" && (pos = document.getElementById('AV_adv' + i))){
		  pos.innerHTML = AV_advHTMLArr[i]['head'] +  AV_recoding(AV_advArr[i]['html']) + AV_advHTMLArr[i]['foot'];
		}else if(AV_DEBUG){
			alert(AV_ERR_NOID);
		}
	}
}

//
// Funkce um�st� reklamu do str�nky podle ID reklamy.
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
// Funkce um�st� reklamu do str�nky podle ID pozice.
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
// Funkce ov���, jestli byly vol�ny pro v�echny pozice funkce AV_pozice(). Ty kter� nebyly, vr�ti v poli.
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
		alert("Ov��en� korektn�ho nasazen� reklman�ho syst�mu adVANTAGE.\r\n\r\nPro n�sleduj�c� pozice nebyla ve str�nce vol�na fuknce AV_pozice():" + tmpStr);
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
// Funkce zjist� k�dov�n� str�nky.
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
// Funkce pro p�evod znakov� sady win1250 a iso-8859-2.
//
function AV_win2iso(str, fromCharset){
	var conv1250 = "���ݎ����";
	var convISO = "���ݮ����";
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
// Funkce zak�duje �et�zec do form�tu, kter� nen� konfliktn� pro p�enos v URL (GETu)
//
function AV_urlEncode(str){
	str = escape(str);
	// nahradi znaky *@+/ p��slu�n�mi entitami
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
// Funkce se pod�v� do URL str�nky a vyhled� v�echny parametry, nap�. 'advantage', kter� vr�t� jako pole
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
// Funkce pro obejit� aktivace flash v IE
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
// Funkce ur�en� pro integraci kontextov� reklamy
//
function AV_context(server, url, css){
	if (!AV_CONTEXT_AD) return false;
	if (css){
		document.write('<style>@import url(' + css + ');</style>');
	}
	AV_adFox(server, url);
}

//
// Funkce slou��c� jako interface pro eTarget.
// Parametr server nese ID serveru v adVANTAGE, prom�nn� serverArr je pole
// prom�nn�ch syst�mu eTarget o struktu�e array(ID_serveru, ID_country).
//
function AV_eTarget(server, url){
	serverArr = AV_advantage2eTarget(server);
	document.write('<SCRIPT src="http://search.etargetnet.com/cz/impressionmedia/context_ad.php?c=' +
		serverArr[1] +	'&ref=' + serverArr[0] + '&q=' + AV_urlEncode(url) + '"></SCRIPT>');
}

//
// Funkce slou��c� jako interface pro adFox.
// Parametr server nese ID serveru v adVANTAGE, prom�nn� servernt je ID v adFox.
//
function AV_adFox(server, url){
	serverInt = AV_advantage2adFox(server);
	document.write('<SCRIPT src="http://ad.adfox.cz/ppcbe?js=1&format=666699ffffff3366ff00000033996632&partner=' +
		serverInt + '&stranka=' + url + '"></SCRIPT>');
}

//
// Funkce p�ev�d� ID serveru na adVANTAGE na ID serveru a ID zem� eTargetu.
// Funkce je speci�ln� pro instanci IM.
//
function AV_advantage2eTarget(id){
	idConverArr = new Array();
	idConverArr[2]  = new Array(344,2); //server.cz
	return idConverArr[id];
}

//
// Funkce p�ev�d� ID serveru na adVANTAGE na ID serveru syst�mu adFox.
// Funkce je speci�ln� pro instanci IM.
//
function AV_advantage2adFox(id){
	idConverArr = new Array();
	idConverArr[2]  = 990; //server.cz
	return idConverArr[id];
}

AV_JSLoaded = true;


