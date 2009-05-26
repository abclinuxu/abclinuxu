<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN" "http://www.w3.org/TR/REC-html40/strict.dtd" >
<#import "macros.ftl" as lib>
<html lang="cs">
<head>
    <#if USER?? && USER.hasRole("root")><!-- Sablona: ${TEMPLATE!"neznama"} --></#if>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>${PARAMS.TITLE!TITLE!'www.abclinuxu.cz'}</title>
    <link rel="stylesheet" type="text/css" href="${CSS_URI!}">
    <#if INLINE_CSS??>
        <style type="text/css">
            ${INLINE_CSS}
        </style>
    </#if>
    <!--[if IE]>
       <link href="/msie.css" type="text/css" rel="stylesheet">
    <![endif]-->
    <!--[if IE 7]>
       <link href="/msie7.css" type="text/css" rel="stylesheet">
    <![endif]-->
    <!--[if lt IE 7]>
       <link href="/msie6.css" type="text/css" rel="stylesheet">
    <![endif]-->
    <link rel="icon" href="/images/site2/favicon.png" type="image/png">
    <#if IS_INDEX??>
        <meta name="description" content="Komunitní portál: Linux, Open Source, BSD a jiné unixy. Nejvíce funkcí, nejaktivnější poradna, nejživější blogy. Pozor - návykové.">
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
    </#if>
    <#if RSS??>
        <link rel="alternate" title="RSS zdroj aktuální sekce" href="http://www.abclinuxu.cz${RSS}" type="application/rss+xml">
    </#if>

    <#if ASSIGNED_TAGS?? && ASSIGNED_TAGS?size &gt; 0>
        <#assign keywords = "">
        <#list ASSIGNED_TAGS as tag>
           <#assign keywords = keywords + tag.title + ", ">
        </#list>
        <meta name="keywords" lang="cs" content="${keywords}">
    <#elseif IS_INDEX??>
        <meta name="keywords" lang="cs" content="linux,open source,free software,linux hardware,software,ovladače,pomoc">
    </#if>

    <script type="text/javascript">
    	Page = new Object();
        <#if RELATION??>
        	Page.relationID = ${RELATION.id};
	    </#if>
        <#if USER??>
        	Page.userID = ${USER.id};
            Page.ticket = "${TOOL.ticketValue(USER)}";
	    </#if>
    </script>
    <script type="text/javascript" src="/data/site/impact.js"></script>
    <script type="text/javascript" src="/data/site/scripts.js"></script>
    <#--<script src="/data/site/prototype.js" type="text/javascript"></script>-->
    <#if html_header??>
        ${html_header}
    </#if>
    <@lib.initRTE />
</head>

<body id="www-abclinuxu-cz">

<@lib.advertisement id="netmonitor" />

<@lib.advertisement id="ad-init" />

<@lib.advertisement id="sf-lista" />

<@lib.advertisement id="arbo-lb" />

<div id="zh-kont">
  <div id="zh-text" class="zh-box">
    <@lib.advertisement id="zahl-vlevo" />
    <@lib.advertisement id="zahl-vpravo" />
    <@lib.advertisement id="zahl-komplet" />
  </div>
  <div id="zh-logo" class="zh-box"><a href="/"></a></div>
  <div id="zh-hledani" class="zh-box">
    <form action="/hledani" method="get">
     <div>
      <input type="text" class="text" name="dotaz">
      <input class="button" type="submit" value="Hledej">
     </div>
    </form>
    <a href="/hledani?action=toAdvanced">Rozšířené hledání</a>
  </div>
</div>
<div class="cistic"></div>

<#include "/include/menu.txt">

    <div class="obal">

        <div class="hl">
            <div class="hl_vpravo">
              <#if USER??>
                <@lib.showUser USER/> |
                <#assign blogName = TOOL.xpath(USER,"/data/settings/blog/@name")!"UNDEF">
                <#if blogName!="UNDEF"><a href="/blog/${blogName}">Blog</a> |</#if>
                <a href="/History?type=comments&amp;uid=${USER.id}">Mé komentáře</a> |
                <a href="/History?type=discussions&amp;uid=${USER.id}&amp;filter=last">Navštívené diskuse</a> |
                <a href="/History?type=questions&amp;uid=${USER.id}">Mé dotazy</a> |
                <a href="/lide/${USER.login}/zalozky">Záložky</a> |
                <a href="/Profile/${USER.id}?action=myPage">Nastavení</a> |
                <a href="${URL.noPrefix("/Index?logout=true")}">Odhlásit</a>
              <#else>
                <a href="${URL.noPrefix("/Profile?action=login")}">Přihlášení</a> |
                <a href="${URL.noPrefix("/EditUser?action=register")}">Registrace</a>
              </#if>
            </div>
            <div class="hl_vlevo">
              <ul class="menu-top">
                <li><a href="/doc/napoveda/alternativni-design">Styl</a>
                  <ul class="menu-drop">
                  <#list TOOL.getOfferedCssStyles().entrySet() as style>
                    <li><a href="/EditUser/<#if USER??>${USER.id}</#if>?action=changeStyle${TOOL.ticket(USER,false)}&amp;css=${style.key}">${style.value}</a></li>
                  </#list>
                  </ul>
                </li>
              </ul>
            </div>
        </div>

        <div id="ls_prepinac" title="Skrýt sloupec" onclick="prepni_sloupec()">&#215;</div>

        <div class="obal_ls" id="ls">
        <div class="s">
            <@lib.advertisement id="vip" />
            <@lib.advertisement id="vip-text" />

            <!-- ANKETA -->
            <#if VARS.currentPoll??>
                <#assign relAnketa = VARS.currentPoll, anketa = relAnketa.child, total = anketa.totalVoters,
                         url=relAnketa.url?default("/ankety/show/"+relAnketa.id)>
                <#if anketa.multiChoice><#assign type = "checkbox"><#else><#assign type = "radio"></#if>

                <div class="s_nadpis">
                   <a class="s_nadpis-pravy-odkaz" href="/pozadavky?categoryPosition=0">navrhněte&nbsp;&raquo;</a>
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
                  <@lib.advertisement id="anketa" />
                </div>
            </#if>

            <!-- ZPRÁVIČKY -->
            <#assign news=VARS.getFreshNews(USER!)>
            <div class="s_nadpis">
                <a class="s_nadpis-pravy-odkaz" href="${URL.make("/zpravicky/edit?action=add")}">napište &raquo;</a>
                <#if USER?? && USER.hasRole("news admin")>
                    <a class="s_nadpis-pravy-odkaz" href="${URL.make("/zpravicky/dir/37672")}" title="Počet neschválených a čekajících zpráviček">(${VARS.counter.WAITING_NEWS},${VARS.counter.SLEEPING_NEWS})&nbsp;</a>
                </#if>
                <a href="/zpravicky" title="zprávičky">Zprávičky</a>
            </div>

            <@lib.advertisement id="arbo-hyper" />

            <div class="s_sekce">
                <div class="ls_zpr">
                <#list news as relation>
                    <#if relation_index==2>
                         <@lib.advertisement id="sl-box" />
                    </#if>
                    <#if relation_index==4>
                         <@lib.advertisement id="arbo-sky" />
                         <@lib.advertisement id="arbo-double-sky" />
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

            <@lib.advertisement id="sl-jobscz" />
            <@lib.advertisement id="sl-abcprace" />

            <#if ! IS_INDEX??>
                <#assign FEEDS = VARS.getFeeds(USER!,false)>
                <#if (FEEDS.size() > 0)>
                    <!-- ROZCESTNÍK -->
                    <div class="s_nadpis">Rozcestník</div>
                    <div class="s_sekce">
                        <div class="rozc">
                            <#list FEEDS.keySet() as server>
                                <a class="server" href="${"/presmeruj?class=S&amp;id="+server.id+"&amp;url="+server.url?url}">${server.name}</a><br>
                                <ul>
                                <#list FEEDS(server) as link>
                                    <li>
                                        <a href="${"/presmeruj?class=S&amp;id="+server.id+"&amp;url="+link.url?url}">${link.text}</a>
                                    </li>
                                </#list>
                                </ul>
                            </#list>
                        </div>
                    </div>
                </#if>
            </#if>

            <@lib.advertisement id="sl-doporucujeme" />

            <@lib.advertisement id="sl-placene-odkazy" />

        </div> <!-- s -->
        </div> <!-- obal_ls -->

    <#if plovouci_sloupec??>
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
             </div> <!-- hw-sloupec, sw-sloupec -->
        </#if>
    </#if>

        <div class="st" id="st"><a name="obsah"></a>

        <@lib.advertisement id="obsah-box" />

        <#if PARENTS??>
          <div class="pwd-box">
            <div class="do-zalozek">
              <#if RSS??>
                <a href="${RSS}"><img src="/images/site2/feed16.png" width="16" height="16" border="0"></a>
              </#if>
              <#if RELATION?? && USER??>
                <form action="/EditBookmarks/${USER.id}" style="display: inline">
                    <input type="submit" class="button" value="do záložek">
                    <input type="hidden" name="action" value="add">
                    <input type="hidden" name="rid" value="${RELATION.id}">
                    <input type="hidden" name="prefix" value="${URL.prefix}">
                    <input type="hidden" name="ticket" value="${TOOL.ticketValue(USER)}">
                </form>
              </#if>
            </div>
            <div class="pwd">
              <a href="/">AbcLinuxu</a>:/
              <#list TOOL.getParents(PARENTS,USER!,URL) as link>
                <a href="${link.url}">${link.title}</a>
                <#if link_has_next> / </#if>
              </#list>
            </div>
          </div>

          <#if ASSIGNED_TAGS??>
            <@lib.advertisement id="stitky" />

            <div class="tag-box">
              <a href="/stitky">Štítky</a>:
              <span id="prirazeneStitky">
                <#if ASSIGNED_TAGS?size &gt; 0>
                  <#list ASSIGNED_TAGS as tag>
                    <a href="/stitky/${tag.id}" title="Zobrazit objekty, které mají přiřazen štítek „${tag.title}“.">${tag.title}</a><#if tag_has_next>, </#if>
                  </#list>
                <#else>
                  <i>není přiřazen žádný štítek</i>
                </#if>
              </span>
            </div>
          </#if>
        </#if>

        <#if SYSTEM_CONFIG.isMaintainanceMode()>
            <div style="color: red; border: medium solid red; margin: 10px; padding: 3ex">
                <p style="font-size: xx-large; text-align: center">Režim údržby</p>
                <p>Právě provádíme údržbu portálu. Prohlížení obsahu by mělo nadále fungovat,
                   úpravy obsahu bohužel nejsou prozatím k dispozici. Děkujeme za pochopení.</p>
            </div>
        </#if>

