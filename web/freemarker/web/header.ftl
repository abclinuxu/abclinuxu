<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-2">
    <title>${PARAMS.TITLE?default(TITLE?default('www.abclinuxu.cz'))}</title>
    <meta name="design" content="Petr Soběslavský">
    <link rel="stylesheet" type="text/css" href="/styles.css">
    <link rel="icon" href="/images/site2/favicon.png" type="image/png">
    <link rel="alternate" title="ABC Linuxu RSS" href="http://www.abclinuxu.cz/auto/abc.rss" type="application/rss+xml">
    <link rel="bookmark" href="#obsah" title="Obsah stránky" type="text/html">
    <meta name="keywords" content="linux,abclinuxu,hardware,diskuse,nápověda,rada,pomoc">
    <script type="text/javascript" src="/data/site/scripts.js"></script>
</head>

<body>
<#import "macros.ftl" as lib>
<#include "/include/lista.txt">
<#include "/include/pocitani.txt">
	<div class="za">
		<div class="za_logo">
			<a href="/" class="bh"><img src="/images/site2/abc-logo.png" width="200" height="80" alt="logo"></a>
		</div>

		<div class="za_hledat">
			<form action="/Search" method="get">
				<input type="text" class="text" name="query">&nbsp;<input alt="Hledej" src="/images/site2/lupa.gif" type="image"><br>
				<a href="/Search">Rozšířené hledání</a>
			</form>
		</div>

		<div class="za_banner">
			<#if IS_INDEX?exists><#include "/include/banner_index.txt"><#else><#include "/include/banner.txt"></#if>
		</div>

		<div class="cistic">&nbsp;</div>

		<div class="za_mn">
		  	<a href="http://prace.abclinuxu.cz" class="za_mn_odkaz">Práce</a><!--
			--><a href="/clanky/dir/315" class="za_mn_odkaz">Články</a><!--
			--><a href="/hardware" class="za_mn_odkaz">Hardware</a><!--
			--><a href="/diskuse.jsp" class="za_mn_odkaz">Diskuse</a><!--
			--><a href="/slovnik" class="za_mn_odkaz">Slovník</a><!--
			--><a href="/drivers" class="za_mn_odkaz">Ovladače</a><!--
			--><a href="/download/abicko.jsp" class="za_mn_odkaz">Abíčko</a><!--
			--><a href="/clanky/dir/250" class="za_mn_odkaz">Ankety</a>
		</div>

	</div>

	<div class="obal">

	<div class="hl">
    	<div class="hl_vpravo">
            <#if USER?exists>
                Uživatel: <a href="${URL.noPrefix("/Profile/"+USER.id)}">${USER.name}</a> |
                <a href="${URL.noPrefix("/Profile/"+USER.id+"?action=myPage")}">Nastavení</a> |
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
                <#include "/include/box_index.txt">
            </div></div>

            <!-- ANKETA -->
            <#if VARS.currentPoll?exists>
                <#assign relAnketa = VARS.currentPoll, anketa = relAnketa.child, total = anketa.totalVotes>
                <#if anketa.multiChoice><#assign type = "checkbox"><#else><#assign type = "radio"></#if>
                <div class="s_nad_h1"><div class="s_nad_pod_h1"><h1>Anketa</h1></div></div>
                <div class="s_sekce">
                    <form action="${URL.noPrefix("/EditPoll/"+relAnketa.id)}" method="POST">
                    <div class="ank-otazka">${anketa.text}</div>
                    <#list anketa.choices as choice>
                        <div class="ank-odpov">
                        <#assign procento = TOOL.percent(choice.count,total)>
                        <label><input type=${type} name="voteId" value="${choice.id}">${choice.text}</label>&nbsp;(${procento}%)<br>
                        <img src="/images/site2/anketa.gif" width="${procento}" height="10" alt="${TOOL.percentBar(procento)}"></div>
                    </#list>
                    <input name="submit" type="submit" id="submit" value="Hlasuj" src="/images/site2/vote_btn.gif" alt="Hlasuj"> &nbsp;Celkem ${total} hlasů<br>
                    <input type="hidden" name="url" value="/clanky/show/${relAnketa.id}">
                    <input type="hidden" name="action" value="vote">
                    </form>
                </div>
                <#assign diz=TOOL.findComments(anketa)>
                <div class="ls_zpr">&nbsp;<a href="/news/show/${relAnketa.id}">Komentářů:</a>
		        ${diz.responseCount}<#if diz.responseCount gt 0>, poslední
		        ${DATE.show(diz.updated,"CZ_SHORT")}</#if>
		        <br>&nbsp;<a href="/clanky/dir/3500">Navrhněte novou anketu</a>
		        </div>
            </#if>

            <!-- ZPRÁVIČKY -->
            <#assign news=VARS.getFreshNews(user?if_exists)>
            <div class="s_nad_h1"><div class="s_nad_pod_h1"><h1>Zprávičky</h1></div></div>
            <div class="s_sekce">
                <#if USER?exists && USER.hasRole("news admin")>
                   <ul>
                        <li><a href="${URL.make("/news/dir/37672")}">Čekající zprávičky</a> (${VARS.counter.WAITING_NEWS})</li>
                   </ul>
                </#if>

	            <div class="s_odkaz">
                    <a href="/zpravicky.jsp">Centrum</a> |
                    <a href="${URL.make("/news/edit?action=add")}">Napsat zprávičku</a>
                </div>
        		<hr>
                <div class="ls_zpr">
                <#list news as relation>
                    <#assign item=TOOL.sync(relation.child), autor=TOOL.createUser(item.owner), diz=TOOL.findComments(item)>
                    ${DATE.show(item.created,"CZ_SHORT")} |
                    ${NEWS_CATEGORIES[item.subType].name}
                    <p>${TOOL.xpath(item,"data/content")}</p>
                    <a href="/Profile/${autor.id}">${TOOL.nonBreakingSpaces(autor.name)}</a>
                    | (<a href="/news/show/${relation.id}">Komentářů:</a> ${diz.responseCount})
                    <hr>
                </#list>
                </div>
                <div class="s_odkaz">
                    <a href="/zpravicky.jsp">Centrum</a> |
                    <a href="${URL.make("/news/edit?action=add")}">Napsat</a> |
                    <a href="/History?type=news&amp;from=${news?size}&amp;count=15">Starší</a>
                </div>
            </div>

            <#if ! IS_INDEX?exists>
                <!-- prace.abclinuxu.cz -->
                <div class="s_nad_h1"><div class="s_nad_pod_h1">
                    <h1><a href="http://prace.abclinuxu.cz">Prace.abclinuxu.cz</a></h1>
                </div></div>

                <div class="s_sekce">
                    <#include "/include/prace.txt">
                </div>

                <!-- ROZCESTNÍK -->
                <div class="s_nad_h1"><div class="s_nad_pod_h1"><h1>Rozcestník</h1></div></div>
                <div class="s_sekce">
                    <div class="rozc">
                        <#list TOOL.createServers([7,1,13,12,14,15,3,2,5,4]) as server>
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

            <!-- REDAKCE -->
            <div class="s_nad_h1"><div class="s_nad_pod_h1"><h1>Redakce</h1></div></div>
            <div class="s_sekce">
                <ul>
                  <li><a href="/clanky/show/44049">Tým AbcLinuxu</a></li>
                  <li><a href="/doc/portal/jine_pristupy">Titulky, PDA a RSS</a></li>
                  <li><a href="/clanky/show/64410">Staňte se autorem</a></li>
                  <li><a href="/clanky/show/44043">Přehled změn na portálu</a></li>
                  <li><a href="${URL.make("/clanky/dir/3500")}">Vzkazy správcům</a> (${VARS.counter.REQUESTS})</li>
                  <li><a href="mailto:filip.korbel@stickfish.cz">Inzerce</a></li>
                  <#if USER?exists && USER.isMemberOf(11246)>
                   <li><a href="${URL.make("/hardware/dir/50795")}">TODO (${VARS.counter.TODO?if_exists})</a></li>
                   <li><a href="${URL.make("/hardware/dir/8000")}">Sekce systém</a></li>
                   <li><a href="/Admin">Administrace portálu</a></li>
                  </#if>
                 </ul>


            </div>
            <div class="s_nad_h1"><div class="s_nad_pod_h1"><h1>Doporučujeme</h1></div></div>
            <div class="s_sekce">
                <ul>
                    <li><a href="javascript:addSidebar();">Přidej sidebar</a></li>
                    <li><a href="javascript:addBookmark();">Přidej mezi oblíbené</a></li>
                    <li><a href="javascript:setHomepage();">Nastav jako domácí stránku</a></li>
                    <li><a href="http://www.linux.cz">linux.cz</a></li>
                    <li><a href="http://www.broadnet.cz">broadnet.cz</a></li>
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
