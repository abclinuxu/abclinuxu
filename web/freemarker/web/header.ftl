<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-2">
    <title>${PARAMS.TITLE?default(TITLE?default('www.abclinuxu.cz'))}</title>
    <meta name="design" content="Petr Soběslavský">
    <#if USER?exists><#assign css=TOOL.xpath(USER.data, "/data/settings/css")?default("UNDEF")></#if>
    <#if ! css?exists || css=="UNDEF"><#assign css="/styles.css"></#if>
    <link rel="stylesheet" type="text/css" href="${css}">
    <#if REQUEST_URI?starts_with("/hosting")>
       <link rel="stylesheet" type="text/css" href="/images/hosting/hosting.css">
    </#if>
    <link rel="icon" href="/images/site2/favicon.png" type="image/png">
    <link rel="alternate" title="abclinuxu.cz: články" href="http://www.abclinuxu.cz/auto/abc.rss" type="application/rss+xml">
    <link rel="alternate" title="abclinuxu.cz: blogy" href="http://www.abclinuxu.cz/auto/blog.rss" type="application/rss+xml">
    <link rel="alternate" title="abclinuxu.cz: zprávičky" href="http://www.abclinuxu.cz/auto/zpravicky.rss" type="application/rss+xml">
    <link rel="alternate" title="abclinuxu.cz: diskuse" href="http://www.abclinuxu.cz/auto/diskuse.rss" type="application/rss+xml">
    <link rel="alternate" title="abclinuxu.cz: ankety" href="http://www.abclinuxu.cz/auto/ankety.rss" type="application/rss+xml">
    <link rel="bookmark" href="#obsah" title="Obsah stránky" type="text/html">
    <meta name="keywords" content="linux,abclinuxu,hardware,diskuse,nápověda,rada,pomoc">
    <script type="text/javascript" src="/data/site/scripts.js"></script>
</head>

<body id="www-abclinuxu-cz">

<#if IS_INDEX?exists>
<#include "/include/netmonitor_hp.txt">
<#include "/include/impact-hp.txt">
<#elseif URL.prefix=='/clanky'>
<#include "/include/netmonitor_ostatni.txt">
<#include "/include/impact-cl.txt">
<#else>
<#include "/include/netmonitor_ostatni.txt">
<#include "/include/impact-oth.txt">
</#if>

<#import "macros.ftl" as lib>
<#include "/include/lista.txt">

<center>
<#if IS_INDEX?exists>
    <#include "/include/impact-hp-lb.txt">
<#elseif URL.prefix=='/clanky'>
    <#include "/include/impact-cl-lb.txt">
<#else>
    <#include "/include/impact-oth-lb.txt">
</#if>
</center>

	<div class="za">
		<a href="/" class="za_logo">
		</a>

		<div class="za_hledat">
			<form action="/Search" method="get">
				<a href="/Search?advancedMode=true">Rozšířené hledání</a> &nbsp;<input type="text"
				class="text" name="query">&nbsp;<input class="button" type="submit" value="Hledej">
			</form>
		</div>

		<div class="cistic">&nbsp;</div>

		<div class="za_mn">
			<a href="/diskuse.jsp" class="za_mn_odkaz">Diskuse</a><!--
			--><a href="/faq" class="za_mn_odkaz">FAQ</a><!--
			--><a href="/hardware" class="za_mn_odkaz">Hardware</a><!--
			--><a href="/clanky" class="za_mn_odkaz">Články</a><!--
			--><a href="/ucebnice" class="za_mn_odkaz">Učebnice</a><!--
			--><a href="/blog" class="za_mn_odkaz">Blogy</a><!--
			--><a href="/download/abicko.jsp" class="za_mn_odkaz">PDF</a><!--
			--><a href="/slovnik" class="za_mn_odkaz">Slovník</a><!--
			--><a href="/ankety" class="za_mn_odkaz">Ankety</a><!--
			--><a href="/ovladace" class="za_mn_odkaz">Ovladače</a><!--
			--><a href="/hosting" class="za_mn_odkaz">Hosting</a><!--
			--><a href="http://www.praceabc.cz" class="za_mn_odkaz">Práce</a>
		</div>

	</div>

	<div class="obal">

	<div class="hl">
    	<div class="hl_vpravo">
            <#if USER?exists>
                <a href="${URL.noPrefix("/Profile/"+USER.id)}">${USER.name}</a> |
                <#assign blogName = TOOL.xpath(USER,"/data/settings/blog/@name")?default("UNDEF")>
                <#if blogName!="UNDEF"><a href="/blog/${blogName}">Blog</a> |</#if>
                <a href="/History?type=comments&amp;uid=${USER.id}">Diskuse</a> |
                <a href="${URL.noPrefix("/Index?logout=true")}">Odhlášení</a> |
            <#else>
                <a href="${URL.noPrefix("/Profile?action=login")}">Přihlášení</a> |
                <a href="${URL.noPrefix("/EditUser?action=register")}">Registrace</a> |
            </#if>
            <a href="/SelectUser?sAction=form&amp;url=/Profile">Hledat uživatele</a>
        </div>
        <div class="hl_vlevo">
            <#if PARENTS?exists>
                <#list TOOL.getParents(PARENTS,USER?if_exists,URL) as link>
                    <a href="${link.url}">${link.text}</a>
            <#if link_has_next> - </#if>
                </#list>
            </#if>&nbsp;
        </div>
	</div>

	<div class="obal_ls">
        <div class="ls_prepinac" id="ls_prepinac">
            <img id="ls_prepinac_img" src="/images/site2/sipkaon-text.gif" width="42" height="12"
	        title="Skrýt sloupec" alt="Skrýt sloupec" onclick="prepni_sloupec()" align="bottom">
	    </div>

        <div class="ls" id="ls"><div class="s">
            <div class="ls_reklama"><div class="ad">
                <#if IS_INDEX?exists>
                    <#include "/include/impact-hp-vip.txt">
                <#elseif URL.prefix=='/clanky'>
                    <#include "/include/impact-cl-vip.txt">
                <#else>
                    <#include "/include/impact-oth-vip.txt">
                </#if>
            </div></div>

	    <!-- Skoleni OKsystem -->

            <#include "/include/oksystem.txt">

            <!-- ANKETA -->
            <#if VARS.currentPoll?exists>
                <#assign relAnketa = VARS.currentPoll, anketa = relAnketa.child, total = anketa.totalVoters,
                         url=relAnketa.url?default("/ankety/show/"+relAnketa.id)>
                <#if anketa.multiChoice><#assign type = "checkbox"><#else><#assign type = "radio"></#if>
                <div class="s_nad_h1"><div class="s_nad_pod_h1"><h1><a href="/ankety">Anketa</a></h1></div></div>
                <div class="s_sekce">
                    <form action="${URL.noPrefix("/EditPoll/"+relAnketa.id)}" method="POST">
                    <div class="ank-otazka">${anketa.text}</div>
                    <#list anketa.choices as choice>
                        <div class="ank-odpov">
                          <#assign procento = TOOL.percent(choice.count,total)>
                          <label><input type="${type}" name="voteId" value="${choice.id}">${choice.text}</label>&nbsp;(<span title="${choice.count}">${procento}%</span>)<br>
                          <div class="ank-sloup-okraj" style="width: ${procento}px">
                            <div class="ank-sloup"></div>
                          </div>
                        </div>
                    </#list>
                    <input name="submit" type="submit" class="button" value="Hlasuj" src="/images/site2/vote_btn.gif" alt="Hlasuj">
                    &nbsp;Celkem ${total} hlasů<br>
                    <input type="hidden" name="url" value="${url}">
                    <input type="hidden" name="action" value="vote">
                    </form>
                </div>
                <#assign diz=TOOL.findComments(anketa)>
                <div class="ls_zpr">&nbsp;<a href="${url}">Komentářů:</a>
		        ${diz.responseCount}<#if diz.responseCount gt 0><@lib.markNewComments diz/>, poslední
		        ${DATE.show(diz.updated,"CZ_SHORT")}</#if>
		        <br>&nbsp;<a href="/clanky/dir/3500?categoryPosition=0">Navrhněte novou anketu</a>
		        </div>
            </#if>

            <div class="s_nad_h1"><div class="s_nad_pod_h1"><h1>Projekty</h1></div></div>
            <div class="s_sekce">
                <ul>
                    <li><a href="/doc/projekty/ucebnice">Učebnice Linuxu</a></li>
                    <li><a href="/projekty/abclinux/verze-2005">ABC Linux 2005</a></li>
                    <li><a href="/projekty/zdrojaky">Zdrojáky Abíčka</a></li>
		    <!--li><a href="/clanky/ruzne/nakrmte-tucnaka">Nakrmte tučňáka</a></li-->
		    <li><a href="/clanky/novinky/tricka-abclinuxu.cz">Trička</a></li>
                </ul>
            </div>

            <!-- ZPRÁVIČKY -->
            <#assign news=VARS.getFreshNews(USER?if_exists)>
            <div class="s_nad_h1"><div class="s_nad_pod_h1"><h1><a href="/zpravicky">Zprávičky</a></h1></div></div>
            <div class="s_sekce">

	        <div class="s_odkaz">
                    <a href="/zpravicky">Centrum</a> |
                    <a href="${URL.make("/zpravicky/edit?action=add")}">Napsat zprávičku</a>
            	    <#if USER?exists && USER.hasRole("news admin")>
                        | <a href="${URL.make("/zpravicky/dir/37672")}" title="Počet čekajících zpráviček">(${VARS.counter.WAITING_NEWS})</a>
                    </#if>
                </div>
        		<hr>
                <div class="ls_zpr">
                <#list news as relation>
                    <#if relation_index==8>
                        <p align="center">
                           <a href="/hosting"><img src="/images/bannery/abchosting.gif" alt="AbcHosting je tu pro vás"
                           border="0" width="251" height="60"></a>
                        </p>
                        <#if ! IS_INDEX?exists>
                            <#include "/include/anketa-distro-06-small.txt">
                        </#if>
                        <hr>
                        <#include "/include/redhat_zpravicka.txt">
                    </#if>
                    <#assign item=TOOL.sync(relation.child), autor=TOOL.createUser(item.owner),
                      diz=TOOL.findComments(item), url=relation.url?default("/zpravicky/show/"+relation.id)>
                    ${DATE.show(item.created,"CZ_SHORT")} |
                    ${NEWS_CATEGORIES[item.subType].name}
                    <p>${TOOL.xpath(item,"data/content")}</p>
                    <a href="/Profile/${autor.id}">${TOOL.nonBreakingSpaces(autor.name)}</a>
                    | <a href="${url}" title="<#if diz.responseCount gt 0>poslední ${DATE.show(diz.updated, "CZ_FULL")}</#if>"
                    >(Komentářů: ${diz.responseCount}<@lib.markNewComments diz/>)</a>
                    <hr>
                </#list>
                </div>
                <div class="s_odkaz">
                    <a href="/zpravicky">Centrum</a> |
                    <a href="${URL.make("/zpravicky/edit?action=add")}">Napsat</a> |
                    <a href="/History?type=news&amp;from=${news?size}&amp;count=15">Starší</a>
                </div>
            </div>

            <#if ! IS_INDEX?exists>
                <!-- prace.abclinuxu.cz -->
                <div class="s_nad_h1"><div class="s_nad_pod_h1">
                    <h1><a href="http://www.praceabc.cz"
		           title="Spojujeme lidi s prací v IT.">Pracovní nabídky</a></h1>
                </div></div>

                <div class="s_sekce">
                    <#include "/include/prace.txt">
                </div>
                <#assign FEEDS = VARS.getFeeds(USER?if_exists,false)>
                <#if (FEEDS.size() > 0)>
                    <!-- ROZCESTNÍK -->
                    <div class="s_nad_h1"><div class="s_nad_pod_h1"><h1>Rozcestník</h1></div></div>
                    <div class="s_sekce">
                        <div class="rozc">
                            <#list FEEDS.keySet() as server>
                                <a class="server" href="${server.url}" rel="nofollow">${server.name}</a><br>
                                <ul>
                                <#list FEEDS(server) as link>
                                    <li><a href="${link.url}" rel="nofollow">${link.text}</a></li>
                                </#list>
                                </ul>
                            </#list>
                        </div>
                    </div>
                </#if>
            </#if>

            <!-- REDAKCE -->
            <div class="s_nad_h1"><div class="s_nad_pod_h1"><h1>Redakce</h1></div></div>
            <div class="s_sekce">
                <ul>
                  <li><a href="/doc/portal/rss-a-jine-pristupy">Titulky, PDA a RSS</a></li>
                  <li><a href="/doc/propagace">Propagace</a></li>
                  <li><a href="/clanky/show/44049">Tým AbcLinuxu</a></li>
                  <li><a href="/clanky/novinky/pojdte-psat-pro-abclinuxu.cz">Pište pro abclinuxu</a></li>
                  <li><a href="http://bugzilla.abclinuxu.cz" rel="nofollow">Bugzilla</a></li>
                  <li><a href="/hardware/dir/3500">Vzkazy správcům</a> (${VARS.counter.REQUESTS})</li>
                  <li><a href="mailto:info@stickfish.cz">Inzerce</a></li>
                  <#if USER?exists && USER.isMemberOf(11246)>
                   <li><a href="/Admin">Administrace portálu</a></li>
                   <li><a href="/system">Sekce systém</a></li>
                   <li><a href="/system/todo">TODO</a></li>
                  </#if>
                 </ul>


            </div>
            <div class="s_nad_h1"><div class="s_nad_pod_h1"><h1>Doporučujeme</h1></div></div>
            <div class="s_sekce">
                <ul>
                    <li><a href="javascript:addSidebar();">Přidej sidebar</a></li>
                    <li><a href="javascript:addBookmark();">Přidej mezi oblíbené</a></li>
                    <li><a href="javascript:setHomepage();">Nastav jako domácí stránku</a></li>
                    <li><a href="http://www.linux.cz" rel="nofollow">linux.cz</a></li>
                    <li><a href="http://www.unixshop.cz">unixshop.cz</a></li>
                    <li><a href="http://www.pravednes.cz" rel="nofollow">pravednes.cz</a></li>
		    <li><a href="http://www.autoweb.cz" rel="nofollow">autoweb.cz</a></li>
		    <li><a href="http://shop.tricko-tricka.com">tricko-tricka.com</a></li>
                </ul>
            </div>

        </div></div> <!-- ls, s -->
	</div> <!-- obal_ls -->

    <#if plovouci_sloupec?exists>
        <div class="obal_ps">
            <div class="ps_prepinac">
            <!-- i kdyz to bude prazdne, tak to tu musi byt -->
            </div>

            <div class="ps"><div class="s">
            ${plovouci_sloupec}
            </div></div> <!-- ps, s -->
        </div> <!-- obal_ps -->
    </#if>

	<div class="st" id="st"><a name="obsah"></a>
