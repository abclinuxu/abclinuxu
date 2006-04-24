<#assign plovouci_sloupec>
    <div class="ps_nad_reklama">
    reklama
        <div class="ps_reklama">
        <#include "/include/hucek.txt">
        </div>
    </div>

    <!-- ZAKLADY LINUXU -->
    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <a class="info" href="#">?<span class="tooltip">Co je to Linux a jak ho pou��t.</span></a>
        <h1>Z�klady Linuxu</h1>
    </div></div>
    <div class="s_sekce">
        <ul>
            <li><a href="/ucebnice">U�ebnice Linuxu</a></li>
            <li><a href="/clanky/ruzne/cim-v-linuxu-nahradit-aplikace-windows">N�hrady Windows aplikac�</a></li>
            <li><a href="/clanky/ruzne/abcserialy">Rozcestn�k na�ich seri�l�</a>
        </ul>
    </div>

    <#assign HARDWARE = VARS.getFreshHardware(USER?if_exists)>
    <#if (HARDWARE?size>0) >
        <div class="s_nad_h1"><div class="s_nad_pod_h1">
            <a class="info" href="#">?<span class="tooltip">Obrovsk� datab�ze znalost� o hardwaru, postupy zprovozn�n� v GNU/Linuxu.</span></a>
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

    <#assign DICTIONARY=VARS.getFreshDictionary(USER?if_exists)>
    <#if (DICTIONARY?size>0) >
        <div class="s_nad_h1"><div class="s_nad_pod_h1">
            <a class="info" href="#">?<span class="tooltip">Jestli nev�te, co znamen� n�kter� slovo, pod�vejte se do na�eho slovn�ku.</span></a>
            <h1><a href="/slovnik">Slovn�k</a></h1>
        </div></div>
        <div class="s_sekce">
            <ul>
            <#list DICTIONARY as rel>
                <li><a href="/slovnik/${rel.child.subType}">${TOOL.xpath(rel.child,"data/name")}</a></li>
            </#list>
            </ul>
        </div>
    </#if>

    <#assign FAQ = VARS.getFreshFaqs(USER?if_exists)>
    <#if (FAQ?size>0) >
        <div class="s_nad_h1"><div class="s_nad_pod_h1">
            <a class="info" href="#">?<span class="tooltip">Odpov�di na �asto kladen� ot�zky.</span></a>
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

    <#include "/include/softronik.txt">

    <#include "/include/redhat.txt">

    <!-- prace.abclinuxu.cz -->
    <div class="s_nad_h1"><div class="s_nad_pod_h1">
        <a class="info" href="#">?<span class="tooltip">Prvn� server s nab�dkami pr�ce (nejen) pro tu���ky. Spojujeme lidi s prac� v IT.</span></a>
        <h1><a href="http://www.praceabc.cz"
	       title="Spojujeme lidi s prac� v IT.">Pracovn� nab�dky</a></h1>
    </div></div>
    <div class="s_sekce">
        <#include "/include/prace_main.txt">
    </div>

    <#assign DRIVERS = VARS.getFreshDrivers(USER?if_exists)>
    <#if (DRIVERS?size>0) >
        <div class="s_nad_h1"><div class="s_nad_pod_h1">
            <a class="info" href="#">?<span class="tooltip">Datab�ze ovlada�� pro v� hardware.</span></a>
            <h1><a href="/ovladace">Ovlada�e</a></h1>
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
	<a class="info" href="#">?<span class="tooltip">Odkazy na nejnov�j�� linuxov� j�dra �ad 2.0, 2.2, 2.4 a 2.6.</span></a>
        <h1>Aktu�ln� j�dra</h1>
    </div></div>
    <div class="s_sekce">
        <#include "/include/kernel.txt">
    </div>
</#assign>

<#include "../header.ftl">

<#include "/include/zprava.txt">
<#include "/include/anketa-distro-06.txt">
<@lib.showMessages/>

<#assign ARTICLES=VARS.getFreshArticles(USER?if_exists)>
<#global CITACE = TOOL.getRelationCountersValue(ARTICLES)/>
<#if (ARTICLES?size>0) >
    <#list ARTICLES as rel>
        <@lib.showArticle rel, "CZ_DM", "CZ_SHORT"/>
        <hr />
    </#list>

    <div class="st_vpravo">
        <a href="/History?type=articles&amp;from=${ARTICLES?size}&amp;count=10">Star�� �l�nky</a>
    </div>
</#if>

<#flush>

<#assign FORUM = VARS.getFreshQuestions(USER?if_exists)>
<#if (FORUM?size > 0)>
    <#assign FORUM=TOOL.analyzeDiscussions(FORUM)>
    <div class="ds">
        <h1 class="st_nadpis"><a href="/diskuse.jsp" title="Cel� diskusn� f�rum">Diskusn� f�rum</a></h1>

        <table>
        <thead>
            <tr>
                <td class="td01">Dotaz</td>
                <td class="td02">Stav</td>
                <td class="td03">Reakc�</td>
                <td class="td04">Posledn�</td>
            </tr>
        </thead>
        <tbody>
        <#list FORUM as diz>
            <tr>
                <td class="td01">
                    <a href="/forum/show/${diz.relationId}">${TOOL.limit(TOOL.xpath(diz.discussion,"data/title"),60," ..")}</a>
                </td>
                <td class="td02">
                    <@lib.markNewCommentsQuestion diz/>
                    <#if TOOL.xpath(diz.discussion,"/data/frozen")?exists>
                        <img src="/images/site2/zamceno.gif" alt="Z" title="Diskuse byla administr�tory uzam�ena">
                    </#if>
                    <#if TOOL.isQuestionSolved(diz.discussion.data)>
                        <img src="/images/site2/vyreseno.gif" alt="V" title="Diskuse byla podle �ten��� vy�e�ena">
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
        <li><a href="/diskuse.jsp">Polo�it dotaz</a>
        <li><a href="/History?type=discussions&amp;from=${FORUM?size}&amp;count=20">Star�� dotazy</a>
    </ul>
</#if>

<#assign STORIES=VARS.getFreshStories(USER?if_exists)>
<#assign STORIES=TOOL.filterRelationsOfBlockedUsers(STORIES,USER?if_exists)>
<#if (STORIES?size>0) >
    <#assign half = STORIES?size/2 >
    <#if STORIES?size%2==1><#assign half=half+1></#if>
    <div class="ramec-st">
        <div class="s_nad_h1"><div class="s_nad_pod_h1">
            <a class="info" href="#">?<span class="tooltip">Vlastn� blog si po p�ihl�en�
            m��ete zalo�it v nastaven� sv�ho profilu</span></a>
            <h1><a href="/blog">Blogy na AbcLinuxu</a>, <a href="/blog/souhrn">stru�n�j�� souhrn</a></h1>
        </div></div>
        <table width="99%">
            <tr>
                <td valign="top">
         <div class="s_sekce">
          <ul>
                    <#list STORIES[0..half-1] as relation>
                        <li><@printStory relation /></li>
                    </#list>
          </ul>
         </div>
                </td>
                <td valign="top">
         <div class="s_sekce">
          <ul>
                <#list STORIES[half..] as relation>
                        <li><@printStory relation /></li>
                    </#list>
          </ul>
         </div>
                </td>
            </tr>
        </table>
    </div>
</#if>

<#macro printStory relation>
    <#assign story=relation.child, blog=relation.parent, title=TOOL.xpath(blog,"//custom/title")?default("UNDEF"),
             url=TOOL.getUrlForBlogStory(blog.subType, story.created, relation.id), CHILDREN=TOOL.groupByType(story.children),
             author=TOOL.createUser(blog.owner)>
    <#if CHILDREN.discussion?exists>
        <#assign diz=TOOL.analyzeDiscussion(CHILDREN.discussion[0])>
    <#else>
        <#assign diz=TOOL.analyzeDiscussion("UNDEF")>
    </#if>
    <a href="${url}" title="${author.nick?default(author.name)?html}<#if title!="UNDEF">, ${title}</#if>">${TOOL.xpath(story, "/data/name")}</a>
    <span title="Po�et&nbsp;koment���<#if diz.responseCount gt 0>, posledn�&nbsp;${DATE.show(diz.updated, "CZ_SHORT")}</#if>">
        (${diz.responseCount}<@lib.markNewComments diz/>)
    </span>
</#macro>

<#assign FEEDS = VARS.getFeeds(USER?if_exists,true)>
<#if (FEEDS.size() > 0)>
    <div class="st_nad_rozc"><div class="st_rozc">
        <h1 class="st_nadpis">Rozcestn�k</h1>
        <div class="s"><div class="s_sekce"><div class="rozc">
        <table>
        <#list FEEDS.keySet() as server>
            <#if server_index % 3 = 0><tr><#assign open=true></#if>
            <td>
            <a class="server" href="${server.url}" rel="nofollow">${server.name}</a>
                <ul>
                <#list FEEDS(server) as link>
                    <li><a href="${link.url}" rel="nofollow">${link.text}</a></li>
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
