<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
 <title>${PARAMS.TITLE?default(TITLE?default('ABCLinuxu.cz - v� pr�vodce sv�tem Linuxu'))}</title>
 <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-2">
 <meta name="keywords" content="linux,abclinuxu,hardware,diskuse,n�pov�da,rada,pomoc">
 <link rel="stylesheet" type="text/css" href="/abc.css">
 <!--[if IE]>
 <link rel="stylesheet" type="text/css" href="/abc_ie.css">
 <![endif]-->
 <link rel="icon" href="/images/site2/abc_fav.png" type="image/png">
 <link rel="alternate" title="ABCLinuxu.cz RSS" HREF="http://www.abclinuxu.cz/auto/abc.rss" TYPE="application/rss+xml">
</head>

<body>
<#import "macros.ftl" as lib>
<#include "/include/lista.txt">
<#include "/include/pocitani.txt">

<div id="advert">

 <div id="logo"><a href="/"><h1>ABCLinuxu.cz</h1></a></div>
 <div id="banner1">

<#if IS_INDEX?exists>
 <#include "/include/banner_index.txt">
<#else>
 <#include "/include/banner.txt">
</#if>

 </div>

 <div id="navig1">
  <p class="l">${DATE.show("CZ_FULL_TXT")} | ${DATE.show("CZ_DAY")}</p>
  <p class="r">
  <span class="mini">
  <a href="/SelectUser?sAction=form&url=/Profile">Najdi u�ivatele</a> |
  <#if USER?exists>
   <a href="${URL.noPrefix("/Index?logout=true")}">Odhl�en�</a> |
   <a href="${URL.noPrefix("/Profile/"+USER.id+"?action=myPage")}">M�j profil</a>
   <img src="/images/site2/ico_user.gif" alt="U�ivatel" align="absmiddle">
   <strong><a href="${URL.noPrefix("/Profile/"+USER.id)}">${USER.name}</a></strong>
  <#else>
   <a href="${URL.noPrefix("/Profile?action=login")}">P�ihl�en�</a> |
   <a href="${URL.noPrefix("/EditUser?action=register")}">Registrace</a>
  </#if>
  </span>
  </p>
 </div>

 <div id="navig2">
  <p class="l">
   <a href="/clanky/dir/3?parent=yes">Rubriky</a>
   <a href="/hardware/dir/1">Hardware</a>
   <a href="/diskuse.jsp">Diskuse</a>
   <a href="/slovnik/">Slovn�k</a>
   <a href="/drivers/dir/318">Ovlada�e</a>
   <a href="/download/abicko.jsp">Ab��ko</a>
   <a href="/clanky/dir/250">Ankety</a>
  </p>
  <form action="/Search" method="get" name="search">
  <input name="query" type="text" id="query" value="hledan� text" onFocus="this.value=''">
  <input name="submit" type="image" id="submit" value="Hledat" src="/images/site2/submit_btn.gif" alt="Hledat" align="middle">
  </form>
 </div>

</div>

<div id="c1"><div id="c2">

 <div id="left"><div class="c3">

 <#if VARS.currentPoll?exists>
 <#assign anketa = VARS.currentPoll, total = anketa.totalVotes>
 <#if anketa.multiChoice><#assign type = "checkbox"><#else><#assign type = "radio"></#if>
 <div class="colhead"><b>Anketa</b></div>
 <div class="coltextnb">
  <form action="${URL.noPrefix("/EditPoll")}" method="POST">
  ${anketa.text}<br>
  <#list anketa.choices as choice>
   <input type=${type} name="voteId" value="${choice.id}">
   <#assign procento = TOOL.percent(choice.count,total)>
   ${choice.text}&nbsp;(${procento}%)<br>
   <img src="/images/site/graf.gif" width="${procento}" alt="${TOOL.percentBar(procento)}" class="poll-image"><br>
  </#list>
  <br>
  <input name="submit" type="image" id="submit" value="Hlasuj" src="/images/site2/vote_btn.gif" alt="Hlasuj" align="absmiddle"> &nbsp;Celkem ${total} hlas�<br>
  <input type="hidden" name="pollId" value="${anketa.id}">
  <input type="hidden" name="url" value="/clanky/dir/250">
  <input type="hidden" name="action" value="vote">
  </form>
 </div>
 </#if>

 <div class="colhead"><b>Zpr�vi�ky ABC Linuxu</b></div>

 <#assign news=VARS.getFreshNews(user?if_exists)>
 <div class="colcontrol"><b>
  <a href="/zpravicky.jsp">Centrum</a> |
  <a href="${URL.make("/news/edit?action=add")}">P�idat</a> |
  <a href="/History?type=news&from=${news?size}&count=15">Dal��</a>
 </b></div>
 <#list news as relation>
  <#if relation_index=4>
    <br><img src="/images/site/wap.gif" width="168" height="48" alt="wap.abclinuxu.cz"><br><br>
  </#if>
  <#assign item=TOOL.sync(relation.child), autor=TOOL.createUser(item.owner), diz=TOOL.findComments(item)>
  <div class="coltexttop"><b>${DATE.show(item.created,"CZ_SHORT")} | Koment���: ${diz.responseCount}</b><br>
   <b>${NEWS_CATEGORIES[item.subType].name}</b>
   <a href="/Profile/${autor.id}">${TOOL.nonBreakingSpaces(autor.name)}</a> <!-- zpravicka by zabirala mene mista -->
  </div>
  <div class="coltext">
   <p>${TOOL.xpath(item,"data/content")}
   <a href="/news/show/${relation.id}"><img src="/images/site2/vice.gif" alt="V�ce ..." border="0"></a>
   </p>
   <!--<p class="author"><a href="/Profile/${autor.id}">${autor.name}</a></p>-->
  </div>
 </#list>

 <p>
 <script language="JavaScript">
 function addSidebar() {
  if ((typeof window.sidebar == "object") && (typeof window.sidebar.addPanel == "function")) {
   window.sidebar.addPanel("www.abclinuxu.cz",'http://www.abclinuxu.cz/?varianta=sidebar',"");
  } else {
   window.alert("V� prohl�e� nepodporuje tuto funkci. Zkuste Mozillu.");
  }
 }
 </script>
 <form>&nbsp;&nbsp;<input name="submit" type="image" onClick='addSidebar()' value="P�idej sidebar" src="/images/site2/sidebar_btn.gif" alt="P�idej sidebar" align="middle"></form>
 <!--[if IE]>
 &nbsp;&nbsp;<a href="#" onclick="this.style.behavior = 'url(#default#homepage)'; this.setHomePage('http://www.abclinuxu.cz');"><img src="/images/site2/homepage_btn.gif" alt="Nastavit jako domovskou str�nku prohl�e�e"></a>
 <![endif]-->
 </p>
 <br>

</div></div>

<div id="right"><div class="c3">

 <#if IS_INDEX?exists>

 <div class="colhead"><b>Reklama</b></div>

 <div class="pad">
   <div class="ad">
	<#include "/include/hucek.txt">
   </div>
 </div>

 </#if>

 <div class="colhead"><b>Redakce</b></div>

 <div class="pad">
  <a href="/clanky/show/44049">T�m AbcLinuxu</a><br>
  <a href="/clanky/show/44046">Export �l�nk� a RSS</a><br>
  <a href="/clanky/show/42393">Sta�te se autorem</a><br>
  <a href="/clanky/show/44043">P�ehled zm�n na port�lu</a><br>
  <a href="${URL.make("/clanky/dir/3500")}">Vzkazy spr�vc�m</a> (${VARS.counter.REQUESTS})<br>
  <a href="mailto:filip.korbel@stickfish.cz">Inzerujte u n�s</a><br>
  <#if USER?exists && USER.hasRole("news admin")>
   <a href="${URL.make("/news/dir/37672")}">�ekaj�c� zpr�vi�ky</a> (${VARS.counter.WAITING_NEWS})<br>
  </#if>
  <#if USER?exists && USER.isMemberOf(11246)>
   <a href="${URL.make("/hardware/dir/50795")}">TODO (${VARS.counter.TODO?if_exists})</a><br>
   <a href="${URL.make("/hardware/dir/8000")}">Sekce syst�m</a><br>
   <a href="/Admin">Administrace port�lu</a>
  </#if>
 </div>

 <div class="colhead"><b>Rozcestn�k</b></div>

 <#list TOOL.createServers([7,1,13,12,3,2,5,4]) as server>
 <div class="coltexttop"><a href="${server.url}"><b>${server.name}</b></a></div>
 <div class="pad">
  <ul>
   <#assign linky = TOOL.sublist(SORT.byDate(LINKS[server.name],"DESCENDING"),0,4)>
   <#list linky as link>
    <li><a href="${link.url}">${link.text}</a></li>
   </#list>
  </ul>
 </div>
 </#list>

 <div class="colhead"><b>Partne�i</b></div>
 <div>
  <a href="http://www.broadnet.cz"><img src="/images/site/partner_broadnet.gif" alt="Broadnet" width="168" height="50" border="0"></a><br>
  <a href="http://www.stickfish.cz"><img src="/images/site/partner_stickfish.gif" alt="Stickfish" width="168" height="50" border="0"></a>
 </div>

</div></div>

<div id="content"><div class="c3">
