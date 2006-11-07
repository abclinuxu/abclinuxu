<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-2">
    <title>${PARAMS.TITLE?default(TITLE?default('www.abclinuxu.cz'))}</title>
    <meta name="design" content="Petr Soběslavský, Robert Krátký">
    <#if USER?exists><#assign css=TOOL.xpath(USER.data, "/data/settings/css")?default("UNDEF")></#if>
    <#if ! css?exists || css=="UNDEF"><#assign css="/styles.css"></#if>
    <link rel="stylesheet" type="text/css" href="${css}">
    <#if REQUEST_URI?starts_with("/hosting")>
       <link rel="stylesheet" type="text/css" href="/images/hosting/hosting.css">
    </#if>
    <link rel="icon" href="/images/site2/favicon.png" type="image/png">
    <link rel="alternate" title="abclinuxu.cz: články" href="http://www.abclinuxu.cz/auto/abc.rss" type="application/rss+xml">
    <link rel="alternate" title="abclinuxu.cz: blogy" href="http://www.abclinuxu.cz/auto/blog.rss" type="application/rss+xml">
    <link rel="alternate" title="abclinuxu.cz: linuxové blogy" href="http://www.abclinuxu.cz/auto/blogDigest.rss" type="application/rss+xml">
    <link rel="alternate" title="abclinuxu.cz: zprávičky" href="http://www.abclinuxu.cz/auto/zpravicky.rss" type="application/rss+xml">
    <link rel="alternate" title="abclinuxu.cz: diskuse" href="http://www.abclinuxu.cz/auto/diskuse.rss" type="application/rss+xml">
    <link rel="alternate" title="abclinuxu.cz: bazar" href="http://www.abclinuxu.cz/auto/bazar.rss" type="application/rss+xml">
    <link rel="alternate" title="abclinuxu.cz: hardware" href="http://www.abclinuxu.cz/auto/hardware.rss" type="application/rss+xml">
    <link rel="alternate" title="abclinuxu.cz: software" href="http://www.abclinuxu.cz/auto/software.rss" type="application/rss+xml">
    <link rel="alternate" title="abclinuxu.cz: ankety" href="http://www.abclinuxu.cz/auto/ankety.rss" type="application/rss+xml">
    <link rel="bookmark" href="#obsah" title="Obsah stránky" type="text/html">
    <meta name="keywords" content="linux,abclinuxu,hardware,diskuse,nápověda,rada,pomoc">
    <script type="text/javascript" src="/data/site/impact.js"></script>
    <script type="text/javascript" src="/data/site/scripts.js"></script>
    <#if html_header?exists>
        ${html_header}
    </#if>
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

<div id="zh-kont">
  <div id="zh-text" class="zh-box">
    <div id="zh-tema">
      <img class="zh-iko" src="/images/site2/sflista/ab.gif">
        Víte, že portál abclinuxu.cz obnovil sekci <a href="/software">Software</a>?
    </div>
    <div id="zh-ad">
      <img class="zh-iko" src="/images/site2/sflista/64.gif">
        <b>Reklama:</b> <a href="http://www.64bit.cz/sun-fire-x2100-s-opteronem-146/15/product.html">Sun Fire X2100 s Opteronem 146</a>, 19&nbsp;210,-&nbsp;Kč
    </div>
  </div>
  <div id="zh-logo" class="zh-box"><a href="/"></a></div>
  <div id="zh-hledani" class="zh-box">
    <form action="/Search" method="get">
      <input type="text" class="text" name="query">&nbsp;<input class="button" type="submit" value="Hledej">
      <a href="/Search?advancedMode=true">Rozšířené hledání</a>
    </form>
  </div>
</div>
<div class="cistic"></div>

<#include "/include/menu.txt">


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

	<div id="ls_prepinac" title="Skrýt sloupec" onclick="prepni_sloupec()">&#215;</div>

	<div class="obal_ls" id="ls">
	<div class="s">
            <div class="ls_reklama">
                <#if IS_INDEX?exists>
                    <#include "/include/impact-hp-vip.txt">
                <#elseif URL.prefix=='/clanky'>
                    <#include "/include/impact-cl-vip.txt">
                <#else>
                    <#include "/include/impact-oth-vip.txt">
                </#if>
            </div>

	    <!-- Skoleni OKsystem -->

            <#include "/include/oksystem.txt">

            <!-- ANKETA -->
            <#if VARS.currentPoll?exists>
                <#assign relAnketa = VARS.currentPoll, anketa = relAnketa.child, total = anketa.totalVoters,
                         url=relAnketa.url?default("/ankety/show/"+relAnketa.id)>
                <#if anketa.multiChoice><#assign type = "checkbox"><#else><#assign type = "radio"></#if>
                <div class="s_nadpis">
			<a class="s_nadpis-pravy-odkaz" href="/clanky/dir/3500?categoryPosition=0">navrhněte&nbsp;&raquo;</a>
			<a href="/ankety">Anketa</a>
		</div>
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
                <div>&nbsp;<a href="${url}">Komentářů:</a>
		        ${diz.responseCount}<#if diz.responseCount gt 0><@lib.markNewComments diz/>, poslední
		        ${DATE.show(diz.updated,"CZ_SHORT")}</#if>
	        </div>
            </#if>

            <!-- ZPRÁVIČKY -->
            <#assign news=VARS.getFreshNews(USER?if_exists)>
            <div class="s_nadpis">
                <a class="s_nadpis-pravy-odkaz" href="${URL.make("/zpravicky/edit?action=add")}">napište &raquo;</a>
                <#if USER?exists && USER.hasRole("news admin")>
                    <a class="s_nadpis-pravy-odkaz" href="${URL.make("/zpravicky/dir/37672")}" title="Počet čekajících zpráviček">(${VARS.counter.WAITING_NEWS})&nbsp;</a>
                </#if>
                <a href="/zpravicky">Zprávičky</a>
            </div>
            <div class="s_sekce">
                <div class="ls_zpr">
                <#list news as relation>
                    <#if relation_index==4>
                        <p align="center">
                           <a href="/hosting"><img src="/images/bannery/abchosting.gif" alt="AbcHosting je tu pro vás"
                           border="0" width="251" height="60"></a>
                        </p>
                    </#if>
                    <@lib.showTemplateNews relation/>
                    <hr>
                </#list>
                </div>
                <div class="s_odkaz">
                    <a href="/zpravicky">Centrum</a> |
                    <a href="${URL.make("/zpravicky/edit?action=add")}">Napsat</a> |
                    <a href="/History?type=news&amp;from=${news?size}&amp;count=15">Starší</a>
                </div>
            </div>

            <!-- abcprace.cz -->
            <div class="s_nadpis">
                <a href="http://www.abcprace.cz" title="Spojujeme lidi s prací v IT.">Pracovní nabídky</a>
            </div>

            <div class="s_sekce">
                <#include "/include/prace.txt">
            </div>

            <#if ! IS_INDEX?exists>
                <#assign FEEDS = VARS.getFeeds(USER?if_exists,false)>
                <#if (FEEDS.size() > 0)>
                    <!-- ROZCESTNÍK -->
                    <div class="s_nadpis">Rozcestník</div>
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
            <div class="s_nadpis">Portál AbcLinuxu</div>
            <div class="s_sekce">
                <ul>
                    <li><a href="/doc/portal/rss-a-jine-pristupy">RSS a PDA</a></li>
                    <li><a href="/clanky/show/44049">Tým kolem Abíčka</a></li>
                    <li><a href="/clanky/novinky/pojdte-psat-pro-abclinuxu.cz">Jak se stát autorem</a></li>
                    <li><a href="/projekty/zdrojaky">Jak pomoci vylepšit Abíčko</a></li>
                    <li><a href="/doc/propagace">Jak pomoci s propagací Abíčka</a></li>
                    <li><a href="http://bugzilla.abclinuxu.cz" rel="nofollow">Hlášení chyb a námětů</a></li>
                    <li><a href="/hardware/dir/3500">Vzkazy správcům</a> (${VARS.counter.REQUESTS})</li>
                    <li><a href="mailto:info@stickfish.cz">Inzerce</a></li>
                    <#if USER?exists && USER.isMemberOf(11246)>
                        <li><a href="/Admin">Administrace portálu</a></li>
                        <li><a href="/system">Sekce systém</a></li>
                        <li><a href="/system/todo">TODO</a></li>
                    </#if>
                </ul>
            </div>

            <div class="s_nadpis">Doporučujeme</div>
            <div class="s_sekce">
                <ul>
                    <li><a href="javascript:addSidebar();">Přidej sidebar</a></li>
                    <li><a href="javascript:addBookmark();">Přidej mezi oblíbené</a></li>
                    <li><a href="javascript:setHomepage();">Nastav jako domácí stránku</a></li>
                    <li><a href="http://www.linux.cz" rel="nofollow">linux.cz</a></li>
                    <li><a href="http://www.64bit.cz">64bit.cz</a></li>
                    <li><a href="http://www.pravednes.cz" rel="nofollow">pravednes.cz</a></li>
                </ul>
            </div>

	    <div class="s_nadpis">Placené odkazy</div>
            <div class="s_sekce">
                <ul>
                    <li><a href="http://www.e-pocasi.cz/">e-pocasi.cz</a></li>
		            <li><a href="http://shop.tricko-tricka.com">tricko-tricka.com</a></li>
                    <li><a href="http://www.krasnyusmev.cz">Bělení zubů White Pearl</a></li>
                    <li><a href="http://www.kovart.cz/">Umělecké kovářství</a></li>
                </ul>
            </div>

        </div> <!-- s -->
	</div> <!-- obal_ls -->

    <#if plovouci_sloupec?exists>
        <#if URL.prefix=='/hardware'>
             <div class="hw-sloupec">
        <#elseif URL.prefix=='/software'>
             <div class="sw-sloupec">
        </#if>
        <div class="obal_ps">
            <div class="ps"><div class="s">
               ${plovouci_sloupec}
            </div></div> <!-- ps, s -->
        </div> <!-- obal_ps -->
        <#if URL.prefix=='/hardware' || URL.prefix=='/software'>
             </div>
        </#if>
    </#if>

	<div class="st" id="st"><a name="obsah"></a>

    <#if SYSTEM_CONFIG.isMaintainanceMode()>
        <div style="color: red; border: medium solid red; margin: 30px; padding: 3ex">
            <p style="font-size: xx-large; text-align: center">Režim údržby</p>
            <p>
                Právě provádíme údržbu portálu. Prohlížení obsahu by mělo nadále fungovat,
                úpravy obsahu bohužel nejsou prozatím k dispozici. Děkujeme za pochopení.
            </p>
        </div>
    </#if>
