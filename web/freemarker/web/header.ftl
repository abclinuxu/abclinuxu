<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-2">
    <title>${PARAMS.TITLE?default(TITLE?default('www.abclinuxu.cz'))}</title>
    <meta name="design" content="Petr Sob�slavsk�">
    <#if USER?exists><#assign css=TOOL.xpath(USER.data, "/data/settings/css")?default("UNDEF")></#if>
    <#if ! css?exists || css=="UNDEF"><#assign css="/styles.css"></#if>
    <link rel="stylesheet" type="text/css" href="${css}">
    <link rel="icon" href="/images/site2/favicon.png" type="image/png">
    <link rel="alternate" title="AbcLinuxu: �l�nky" href="http://www.abclinuxu.cz/auto/abc.rss" type="application/rss+xml">
    <link rel="alternate" title="AbcLinuxu: blogy" href="http://www.abclinuxu.cz/auto/blog.rss" type="application/rss+xml">
    <link rel="alternate" title="AbcLinuxu: zpr�vi�ky" href="http://www.abclinuxu.cz/auto/zpravicky.rss" type="application/rss+xml">
    <link rel="alternate" title="AbcLinuxu: diskuse" href="http://www.abclinuxu.cz/auto/diskuse.rss" type="application/rss+xml">
    <link rel="bookmark" href="#obsah" title="Obsah str�nky" type="text/html">
    <meta name="keywords" content="linux,abclinuxu,hardware,diskuse,n�pov�da,rada,pomoc">
    <script type="text/javascript" src="/data/site/scripts.js"></script>
</head>

<body>

<div style="height:0;">
<#include "/include/pocitani2.txt">
<#include "/include/pocitani1.txt"></div>

<#if IS_INDEX?exists>
    <#include "/include/impact-hp.txt">
<#elseif URL.prefix=='/clanky'>
    <#include "/include/impact-cl.txt">
<#else>
    <#include "/include/impact-oth.txt">
</#if>

<#import "macros.ftl" as lib>
<#include "/include/lista.txt">

<#if IS_INDEX?exists>
    <#include "/include/impact-hp-lb.txt">
<#elseif URL.prefix=='/clanky'>
    <#include "/include/impact-cl-lb.txt">
<#else>
    <#include "/include/impact-oth-lb.txt">
</#if>

	<div class="za">
		<a href="/" class="bh">
		    <div class="za_logo">
		    </div>
		</a>

		<div class="za_hledat">
			<form action="/Search" method="get">
				<a href="/Search?advancedMode=true">Roz���en� hled�n�</a> &nbsp;<input type="text"
				class="text" name="query">&nbsp;<input class="button" type="submit" value="Hledej">
			</form>
		</div>

		<div class="cistic">&nbsp;</div>

		<div class="za_mn">
			<a href="/diskuse.jsp" class="za_mn_odkaz">Diskuse</a><!--
			--><a href="/hardware" class="za_mn_odkaz">Hardware</a><!--
			--><a href="/clanky" class="za_mn_odkaz">�l�nky</a><!--
			--><a href="/blog" class="za_mn_odkaz">Blogy</a><!--
			--><a href="http://www.praceabc.cz" class="za_mn_odkaz">Pr�ce</a><!--
			--><a href="/download/abicko.jsp" class="za_mn_odkaz">PDF</a><!--
			--><a href="/slovnik" class="za_mn_odkaz">Slovn�k</a><!--
			--><a href="/ankety" class="za_mn_odkaz">Ankety</a><!--
			--><a href="/drivers" class="za_mn_odkaz">Ovlada�e</a>
		</div>

	</div>

	<div class="obal">

	<div class="hl">
    	<div class="hl_vpravo">
            <#if USER?exists>
                <a href="${URL.noPrefix("/Profile/"+USER.id)}">${USER.name}</a> |
                <#assign blogName = TOOL.xpath(USER,"/data/settings/blog/@name")?default("UNDEF")>
                <#if blogName!="UNDEF"><a href="/blog/${blogName}">Blog</a> |</#if>
                <a href="${URL.noPrefix("/Profile/"+USER.id+"?action=myPage")}">Nastaven�</a> |
                <a href="${URL.noPrefix("/Index?logout=true")}">Odhl�en�</a> |
            <#else>
                <a href="${URL.noPrefix("/Profile?action=login")}">P�ihl�en�</a> |
                <a href="${URL.noPrefix("/EditUser?action=register")}">Registrace</a> |
            </#if>
            <a href="/SelectUser?sAction=form&amp;url=/Profile">Hledat u�ivatele</a>
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
	        title="Skr�t sloupec" alt="Skr�t sloupec" onclick="prepni_sloupec()" align="bottom">
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

            <!-- ANKETA -->
            <#if VARS.currentPoll?exists>
                <#assign relAnketa = VARS.currentPoll, anketa = relAnketa.child, total = anketa.totalVotes>
                <#if anketa.multiChoice><#assign type = "checkbox"><#else><#assign type = "radio"></#if>
                <div class="s_nad_h1"><div class="s_nad_pod_h1"><h1><a href="/ankety">Anketa</a></h1></div></div>
                <div class="s_sekce">
                    <form action="${URL.noPrefix("/EditPoll/"+relAnketa.id)}" method="POST">
                    <div class="ank-otazka">${anketa.text}</div>
                    <#list anketa.choices as choice>
                        <div class="ank-odpov">
                        <#assign procento = TOOL.percent(choice.count,total)>
                        <label><input type=${type} name="voteId" value="${choice.id}">${choice.text}</label>&nbsp;(${procento}%)<br>
                        <img src="/images/site2/anketa.gif" width="${procento}" height="10" alt="${TOOL.percentBar(procento)}"></div>
                    </#list>
                    <input name="submit" type="submit" class="button" value="Hlasuj" src="/images/site2/vote_btn.gif" alt="Hlasuj"> &nbsp;Celkem ${total} hlas�<br>
                    <input type="hidden" name="url" value="/clanky/show/${relAnketa.id}">
                    <input type="hidden" name="action" value="vote">
                    </form>
                </div>
                <#assign diz=TOOL.findComments(anketa), url=relAnketa.url?default("/ankety/show/"+relAnketa.id)>
                <div class="ls_zpr">&nbsp;<a href="${url}">Koment���:</a>
		        ${diz.responseCount}<#if diz.responseCount gt 0>, posledn�
		        ${DATE.show(diz.updated,"CZ_SHORT")}</#if>
		        <br>&nbsp;<a href="/clanky/dir/3500">Navrhn�te novou anketu</a>
		        </div>
            </#if>

            <!-- ZPR�VI�KY -->
            <#assign news=VARS.getFreshNews(USER?if_exists)>
            <div class="s_nad_h1"><div class="s_nad_pod_h1"><h1><a href="/zpravicky">Zpr�vi�ky</a></h1></div></div>
            <div class="s_sekce">

                <#if USER?exists && USER.hasRole("news admin")>
                   <ul>
                        <li><a href="${URL.make("/zpravicky/dir/37672")}">�ekaj�c� zpr�vi�ky</a> (${VARS.counter.WAITING_NEWS})</li>
                   </ul>
                </#if>

	            <div class="s_odkaz">
                    <a href="/zpravicky.jsp">Centrum</a> |
                    <a href="${URL.make("/zpravicky/edit?action=add")}">Napsat zpr�vi�ku</a>
                </div>
        		<hr>
                <div class="ls_zpr">
                <#list news as relation>
                    <#assign item=TOOL.sync(relation.child), autor=TOOL.createUser(item.owner),
                      diz=TOOL.findComments(item), url=relation.url?default("/zpravicky/show/"+relation.id)>
                    ${DATE.show(item.created,"CZ_SHORT")} |
                    ${NEWS_CATEGORIES[item.subType].name}
                    <p>${TOOL.xpath(item,"data/content")}</p>
                    <a href="/Profile/${autor.id}">${TOOL.nonBreakingSpaces(autor.name)}</a>
                    | <a href="${url}" title="<#if diz.responseCount gt 0>posledn� ${DATE.show(diz.updated, "CZ_FULL")}</#if>"
                    >(Koment���: ${diz.responseCount})</a>
                    <hr>
                </#list>
                </div>
                <div class="s_odkaz">
                    <a href="/zpravicky">Centrum</a> |
                    <a href="${URL.make("/zpravicky/edit?action=add")}">Napsat</a> |
                    <a href="/History?type=news&amp;from=${news?size}&amp;count=15">Star��</a>
                </div>
            </div>

            <#if ! IS_INDEX?exists>
                <!-- prace.abclinuxu.cz -->
                <div class="s_nad_h1"><div class="s_nad_pod_h1">
                    <h1><a href="http://www.praceabc.cz"
		           title="Spojujeme lidi s prac� v IT.">Pracovn� nab�dky</a></h1>
                </div></div>

                <div class="s_sekce">
                    <#include "/include/prace.txt">
                </div>
                <#if TOOL.isGuidePostEnabled(USER?if_exists)>
                    <!-- ROZCESTN�K -->
                    <div class="s_nad_h1"><div class="s_nad_pod_h1"><h1>Rozcestn�k</h1></div></div>
                    <div class="s_sekce">
                        <div class="rozc">
                            <#list TOOL.createServers([7,16,1,13,14,12,17,15,3,2,5]) as server>
                                <a class="server" href="${server.url}">${server.name}</a><br>
                                <ul>
                                <#assign linky = TOOL.sublist(SORT.byDate(LINKS[server.name],"DESCENDING"),0,2)>
                                <#list linky as link>
                                    <li><a href="${link.url}">${link.text}</a></li>
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
                  <li><a href="/clanky/show/44049">T�m AbcLinuxu</a></li>
                  <li><a href="/doc/portal/jine_pristupy">Titulky, PDA a RSS</a></li>
                  <li><a href="/clanky/show/64410">Sta�te se autorem</a></li>
                  <li><a href="/clanky/show/44043">P�ehled zm�n na port�lu</a></li>
                  <li><a href="/hardware/dir/3500">Vzkazy spr�vc�m</a> (${VARS.counter.REQUESTS})</li>
                  <li><a href="mailto:filip.korbel@stickfish.cz">Inzerce</a></li>
                  <#if USER?exists && USER.isMemberOf(11246)>
                   <li><a href="/system/todo">TODO (${VARS.counter.TODO?if_exists})</a></li>
                   <li><a href="/system">Sekce syst�m</a></li>
                   <li><a href="/Admin">Administrace port�lu</a></li>
                  </#if>
                 </ul>


            </div>
            <div class="s_nad_h1"><div class="s_nad_pod_h1"><h1>Doporu�ujeme</h1></div></div>
            <div class="s_sekce">
                <ul>
                    <li><a href="javascript:addSidebar();">P�idej sidebar</a></li>
                    <li><a href="javascript:addBookmark();">P�idej mezi obl�ben�</a></li>
                    <li><a href="javascript:setHomepage();">Nastav jako dom�c� str�nku</a></li>
                    <li><a href="http://www.linux.cz">linux.cz</a></li>
                    <li><a href="http://www.broadnet.cz">broadnet.cz</a></li>
                    <li><a href="http://www.pravednes.cz">pravednes.cz</a></li>
                </ul>
            </div>

        </div></div> <!-- ls, s -->
	</div> <!-- obal_ls -->

    <#if plovouci_sloupec?exists>
        <div class="obal_ps" style="width: ${ps_sirka?default(300)}px">
            <div class="ps_prepinac" id="ps_prepinac">
            <!-- i kdyz to bude prazdne, tak to tu musi byt -->
            </div>

            <div class="ps" id="ps"><div class="s">
            ${plovouci_sloupec}
            </div></div> <!-- ps, s -->
        </div> <!-- obal_ps -->
    </#if>

	<div class="st" id="st"><a name="obsah"></a>
