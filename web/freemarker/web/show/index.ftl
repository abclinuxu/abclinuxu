<#assign plovouci_sloupec>
    <div class="ps_nad_reklama">
    reklama
        <div class="ps_reklama">
        <#include "/include/hucek.txt">
        </div>
    </div>

    <!-- ZAKLADY LINUXU -->
    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <a class="info" href="#">?<span class="tooltip">Co je to Linux a jak ho pou¾ít.</span></a>
        <h1>Základy Linuxu</h1>
    </div></div>
    <div class="s_sekce">
        <ul>
            <li><a href="/clanky/show/26394">Co je to Linux?</a></li>
            <li><a href="/clanky/show/12707">Je opravdu zdarma?</a></li>
            <li><a href="/clanky/show/9503">Jakou zvolit distribuci?</a></li>
            <li><a href="/clanky/show/14665">Náhrady Windows aplikací</a></li>
            <li><a href="/clanky/show/20310">Rozcestník na¹ich seriálù</a>
        </ul>
    </div>

    <#assign HARDWARE = VARS.getFreshHardware(USER?if_exists)>
    <#if (HARDWARE?size>0) >
        <div class="s_nad_h1"><div class="s_nad_pod_h1">
            <a class="info" href="#">?<span class="tooltip">Obrovská databáze znalostí o hardwaru</span></a>
            <h1><a href="/hardware">Hardware</a></h1>
        </div></div>
        <div class="s_sekce">
            <ul>
            <#list HARDWARE as rel>
                 <li><a href="/hardware/show/${rel.id}">${TOOL.xpath(rel.child,"data/name")}</a></li>
            </#list>
            </ul>
        </div>
    </#if>

    <#assign FAQ = VARS.getFreshFaqs(USER?if_exists)>
    <#if (FAQ?size>0) >
        <div class="s_nad_h1"><div class="s_nad_pod_h1">
            <a class="info" href="#">?<span class="tooltip">Odpovìdi na èasto kladené otázky</span></a>
            <h1><a href="/faq">FAQ</a></h1>
        </div></div>
        <div class="s_sekce">
            <ul>
            <#list FAQ as rel>
                 <li><a href="${rel.url}">${TOOL.xpath(rel.child,"data/title")}</a></li>
            </#list>
            </ul>
        </div>
    </#if>

    <#assign DICTIONARY=VARS.getFreshDictionary(USER?if_exists)>
    <#if (DICTIONARY?size>0) >
        <div class="s_nad_h1"><div class="s_nad_pod_h1">
            <a class="info" href="#">?<span class="tooltip">Jestli nevíte, co znamená nìkteré slovo, podívejte se do na¹eho slovníku</span></a>
            <h1><a href="/slovnik">Slovník</a></h1>
        </div></div>
        <div class="s_sekce">
            <ul>
            <#list DICTIONARY as rel>
                <li><a href="/slovnik/${rel.child.subType}">${TOOL.xpath(rel.child,"data/name")}</a></li>
            </#list>
            </ul>
        </div>
    </#if>

    <!-- prace.abclinuxu.cz -->
    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <a class="info" href="#">?<span class="tooltip">První server s nabídkami práce (nejen) pro tuèòáky. Spojujeme lidi s prací v IT.</span></a>
        <h1><a href="http://www.praceabc.cz"
	       title="Spojujeme lidi s prací v IT.">Pracovní nabídky</a></h1>
    </div></div>
    <div class="s_sekce">
        <#include "/include/prace_main.txt">
    </div>

    <#assign DRIVERS = VARS.getFreshDrivers(USER?if_exists)>
    <#if (DRIVERS?size>0) >
        <div class="s_nad_h1"><div class="s_nad_pod_h1">
            <a class="info" href="#">?<span class="tooltip">Databáze ovladaèù pro vá¹ hardware</span></a>
            <h1><a href="/ovladace">Ovladaèe</a></h1>
        </div></div>
        <div class="s_sekce">
            <ul>
            <#list DRIVERS as rel>
                <li><a href="${rel.url?default("/ovladace/show/"+rel.id)}">${TOOL.xpath(rel.child,"data/name")}</a></li>
            </#list>
            </ul>
        </div>
    </#if>

    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <h1>Aktuální jádra</h1>
    </div></div>
    <div class="s_sekce">
        <#include "/include/kernel.txt">
    </div>

    <!-- unixshop -->
    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <a class="info" href="#">?<span class="tooltip">Kvalitní ¾elezo pro va¹e serverovny za dostupné ceny</span></a>
        <h1><a href="http://www.unixshop.cz">unixshop.cz</a></h1>
    </div></div>
    <div class="s_sekce">
        <#include "/include/unixshop.txt">
    </div>
</#assign>

<#include "../header.ftl">

<#include "/include/zprava.txt">
<@lib.showMessages/>

<#assign ARTICLES=VARS.getFreshArticles(USER?if_exists)>
<#if (ARTICLES?size>0) >
    <#list ARTICLES as rel>
        <@lib.showArticle rel, "CZ_DM", "CZ_SHORT"/>
        <hr>
        <#if rel_index==2>
            <#assign STORIES=VARS.getFreshStories(USER?if_exists)>
            <#if (STORIES?size>0) >
                <div class="ramec-st">
                    <div class="s_nad_h1"><div class="s_nad_pod_h1">
                        <a class="info" href="#">?<span class="tooltip">Vlastní blog si po pøihlá¹ení
                        mù¾ete zalo¾it v nastavení svého profilu</span></a>
                        <h1><a href="/blog">Blogy na AbcLinuxu</a></h1>
                    </div></div>
                    <div class="s_sekce">
                        <ul>
                            <#list STORIES as relation>
                                <li>
                                    <#assign story=relation.child, blog=relation.parent>
                                    <#assign url=TOOL.getUrlForBlogStory(blog.subType, story.created, relation.id)>
                                    <#assign title=TOOL.xpath(blog,"//custom/title")?default("UNDEF")>
                                    <#assign CHILDREN=TOOL.groupByType(story.children)>
                                    <#if CHILDREN.discussion?exists>
                                        <#assign diz=TOOL.analyzeDiscussion(CHILDREN.discussion[0])>
                                    <#else>
                                        <#assign diz=TOOL.analyzeDiscussion("UNDEF")>
                                    </#if>
                                    <a href="${url}" title="Komentáøù:&nbsp;${diz.responseCount}<#if diz.responseCount gt 0>, poslední&nbsp;${DATE.show(diz.updated, "CZ_SHORT")}</#if>">${TOOL.xpath(story, "/data/name")}</a>
                                    <span>| ${DATE.show(story.created, "CZ_DM")}
                                    <#if title!="UNDEF"> | <a href="/blog/${blog.subType}">${title}</a></#if></span>
                                </li>
                            </#list>
                        </ul>
                    </div>
                </div>
            </#if>
        </#if>
    </#list>

    <div class="st_uprostred">
        <a href="/History?type=articles&amp;from=${ARTICLES?size}&amp;count=10">Star¹í èlánky</a>
    </div>
</#if>

<#flush>

<#assign FORUM = VARS.getFreshQuestions(USER?if_exists)>
<#if (FORUM?size > 0)>
    <#assign FORUM=TOOL.analyzeDiscussions(FORUM)>
    <div class="ds">
        <h1 class="st_nadpis"><a href="/diskuse.jsp" title="Celé diskusní fórum">Diskusní fórum</a></h1>

        <table>
        <thead>
            <tr>
                <td class="td01">Dotaz</td>
                <td class="td02">Stav</td>
                <td class="td03">Reakcí</td>
                <td class="td04">Poslední</td>
            </tr>
        </thead>
        <tbody>
        <#list FORUM as diz>
            <tr>
                <td class="td01">
                    <a href="/forum/show/${diz.relationId}">${TOOL.limit(TOOL.xpath(diz.discussion,"data/title"),60," ..")}</a>
                </td>
                <td class="td02">
                    <#if TOOL.xpath(diz.discussion,"/data/frozen")?exists>
                        <img src="/images/site2/zamceno.gif" alt="Z" title="Diskuse byla administrátory uzamèena">
                    </#if>
                    <#if TOOL.isQuestionSolved(diz.discussion.data)>
                        <img src="/images/site2/vyreseno.gif" alt="V" title="Diskuse byla podle ètenáøù vyøe¹ena">
                    </#if>
                    <#if USER?exists && TOOL.xpath(diz.discussion,"//monitor/id[text()='"+USER.id+"']")?exists>
                        <img src="/images/site2/sledovano.gif" alt="S" title="Tuto diskusi sledujete monitorem">
                    </#if>
                </td>
                <td class="td03">${diz.responseCount}</td>
                <td class="td04">${DATE.show(diz.updated,"CZ_SHORT")}</td>
            </tr>
        </#list>
        </tbody>
        </table>
    </div>
    <ul>
        <li><a href="/diskuse.jsp">Polo¾it dotaz</a>
        <li><a href="/History?type=discussions&amp;from=${FORUM?size}&amp;count=20">Star¹í dotazy</a>
    </ul>
</#if>

<#if TOOL.isGuidePostEnabled(USER?if_exists)>
    <div class="st_nad_rozc"><div class="st_rozc">
        <h1 class="st_nadpis">Rozcestník</h1>
        <div class="s"><div class="s_sekce"><div class="rozc">
        <table>
        <#list TOOL.createServers([16,1,13,12,17,14]) as server>
            <#if server_index % 3 = 0><tr><#assign open=true></#if>
            <td>
            <a class="server" href="${server.url}">${server.name}</a>
                <ul>
                <#assign linky = TOOL.sublist(SORT.byDate(LINKS[server.name],"DESCENDING"),0,3)>
                <#list linky as link>
                    <li><a href="${link.url}">${link.text}</a></li>
                </#list>
                </ul>
            </td>
            <#if server_index % 3 = 2></tr><#assign open=false></#if>
        </#list>
        <#if open></tr></#if>
        </table>
        </div></div></div>
    </div></div>
</#if>

<#include "../footer.ftl">
