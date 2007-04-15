<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN" "http://www.w3.org/TR/REC-html40/strict.dtd" >
<html lang="cs">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>${PARAMS.TITLE?default(TITLE?default('www.abclinuxu.cz'))}</title>
    <link rel="stylesheet" type="text/css" href="${CSS_URI?if_exists}">
    <!--[if IE]>
       <link href="/msie.css" type="text/css" rel="stylesheet">
    <![endif]-->
    <!--[if IE 7]>
       <link href="/bugie.css" type="text/css" rel="stylesheet">
    <![endif]-->
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
    <meta name="keywords" lang="cs" content="linux,abclinuxu,hardware,software,ovladače,diskuse,nápověda,rada,pomoc">
    <meta name="keywords" lang="en" content="linux,hardware,software,drivers,forum,help,faq,advice">
    <script type="text/javascript" src="/data/site/impact.js"></script>
    <script type="text/javascript" src="/data/site/scripts.js"></script>
    <#if html_header?exists>
        ${html_header}
    </#if>
</head>

<body onload="document.getElementById('menu').style.display='block';window.setTimeout(new Function('document.getElementById(\'menu\').style.display=\'table\''), 10)" id="www-abclinuxu-cz">

<#import "macros.ftl" as lib>
<@lib.advertisement id="netmonitor" />
<@lib.advertisement id="ad-init" />

<@lib.advertisement id="sf-lista" />

<@lib.advertisement id="arbo-lb" />

<div id="zh-kont">
  <div id="zh-text" class="zh-box">
    <div id="zh-tema">
       <@lib.advertisement id="zahl-vlevo" />
    </div>
    <div id="zh-ad">
       <@lib.advertisement id="zahl-vpravo" />
    </div>
  </div>
  <div id="zh-logo" class="zh-box"><a href="/"></a></div>
  <div id="zh-hledani" class="zh-box">
    <form action="/hledani" method="get">
     <div>
      <input type="text" class="text" name="dotaz">
      <input class="button" type="submit" value="Hledej">
     </div>
    </form>
    <a href="/hledani?advancedMode=true">Rozšířené hledání</a>
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
            <@lib.advertisement id="vip" />
            <@lib.advertisement id="vip-text" />

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
                     <div>
                      <input name="submit" type="submit" class="button" value="Hlasuj" alt="Hlasuj">
                        Celkem ${total} hlasů
                      <input type="hidden" name="url" value="${url}">
                      <input type="hidden" name="action" value="vote">
                     </div>
                    </form>
                </div>
                <#assign diz=TOOL.findComments(anketa)>
                <div>&nbsp;<a href="${url}" title="${anketa.text}">Komentářů:</a>
		        ${diz.responseCount}<#if diz.responseCount gt 0><@lib.markNewComments diz/>, poslední
		        ${DATE.show(diz.updated,"CZ_SHORT")}</#if>
	        </div>
            </#if>

            <!-- ZPRÁVIČKY -->
            <#assign news=VARS.getFreshNews(USER?if_exists)>
            <div class="s_nadpis">
                <a class="s_nadpis-pravy-odkaz" href="${URL.make("/zpravicky/edit?action=add")}">napište &raquo;</a>
                <#if USER?exists && USER.hasRole("news admin")>
                    <a class="s_nadpis-pravy-odkaz" href="${URL.make("/zpravicky/dir/37672")}" title="Počet neschválených a čekajících zpráviček">(${VARS.counter.WAITING_NEWS},${VARS.counter.SLEEPING_NEWS})&nbsp;</a>
                </#if>
                <a href="/zpravicky" title="zprávičky">Zprávičky</a>
            </div>

            <@lib.advertisement id="arbo-hyper" />

            <div class="s_sekce">
                <div class="ls_zpr">
                <#list news as relation>
                    <#if relation_index==4>
			        <@lib.advertisement id="sl-mini" />
                    </#if>
                    <@lib.showTemplateNews relation/>
                    <hr>
                </#list>
                </div>
                <div class="s_odkaz">
                    <a href="/zpravicky">Centrum</a> |
                    <a href="${URL.make("/zpravicky/edit?action=add")}" rel="nofollow">Napsat</a> |
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

            <div class="s_nadpis">Doporučujeme</div>
            <div class="s_sekce">
                <ul>
                    <li><a href="http://www.linux.cz" rel="nofollow">linux.cz</a></li>
                    <li><a href="http://www.64bit.cz">64bit.cz</a></li>
                    <li><a href="http://www.pravednes.cz" rel="nofollow">pravednes.cz</a></li>
                </ul>
            </div>

            <@lib.advertisement id="sl-placene-odkazy" />

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
    <div style="color: red; border: medium solid red; margin: 10px; padding: 3ex">
        <p style="font-size: xx-large; text-align: center">Režim údržby</p>
        <p>
            Právě provádíme údržbu portálu. Prohlížení obsahu by mělo nadále fungovat,
            úpravy obsahu bohužel nejsou prozatím k dispozici. Děkujeme za pochopení.
        </p>
    </div>
</#if>

