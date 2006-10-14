 <#assign plovouci_sloupec>

    <#assign SOFTWARE = VARS.getFreshSoftware(USER?if_exists)>
    <#if (SOFTWARE?size>0) >
        <div class="s_nadpis">
            <a class="info" href="#">?<span class="tooltip">Katalog softwaru pro GNU/Linux.</span></a>
            <a href="/software">Software</a>
        </div>
        <div class="s_sekce">
            <ul>
            <#list SOFTWARE as rel>
                 <li><a href="${rel.url}">${TOOL.xpath(rel.child,"data/name")}</a></li>
            </#list>
            </ul>
        </div>
    </#if>

    <#assign HARDWARE = VARS.getFreshHardware(USER?if_exists)>
    <#if (HARDWARE?size>0) >
        <div class="s_nadpis">
            <a class="info" href="#">?<span class="tooltip">Obrovská databáze znalostí o hardwaru, postupy zprovoznìní v GNU/Linuxu.</span></a>
            <a href="/hardware">Hardware</a>
        </div>
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
        <div class="s_nadpis">
            <a class="info" href="#">?<span class="tooltip">Odpovìdi na èasto kladené otázky.</span></a>
            <a href="/faq">FAQ</a>
        </div>
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
    <#include "/include/datascript.txt">
    <#include "/include/jobpilot.txt">

    <#assign DRIVERS = VARS.getFreshDrivers(USER?if_exists)>
    <#if (DRIVERS?size>0) >
        <div class="s_nadpis">
            <a class="info" href="#">?<span class="tooltip">Databáze ovladaèù pro vá¹ hardware.</span></a>
            <a href="/ovladace">Ovladaèe</a>
        </div>
        <div class="s_sekce">
            <ul>
            <#list DRIVERS as rel>
                <li><a href="${rel.url?default("/ovladace/show/"+rel.id)}">${TOOL.xpath(rel.child,"data/name")}</a></li>
            </#list>
            </ul>
        </div>
    </#if>
</#assign>

<#include "../header.ftl">

<#include "/include/zprava.txt">

<@lib.showMessages/>

<#assign ARTICLES=VARS.getFreshArticles(USER?if_exists)>
<#global CITACE = TOOL.getRelationCountersValue(ARTICLES,"read")/>
<#if (ARTICLES?size>0) >
    <#list ARTICLES as rel>
        <@lib.showArticle rel, "CZ_DM", "CZ_SHORT"/>
        <hr />
    </#list>

    <div class="st_vpravo">
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
                    <a href="/forum/show/${diz.relationId}">${TOOL.limit(TOOL.xpath(diz.discussion,"data/title"),60,"...")}</a>
                </td>
                <td class="td02">
                    <@lib.markNewCommentsQuestion diz/>
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

<#assign STORIES=VARS.getFreshStories(USER?if_exists)>
<#assign STORIES=TOOL.filterRelationsOfBlockedUsers(STORIES,USER?if_exists)>
<#if (STORIES?size>0) >
  <#assign half = STORIES?size/2 >
  <#if STORIES?size%2==1><#assign half=half+1></#if>
    <div class="ramec">
      <div class="s_nadpis">
        <a class="info" href="#">?<span class="tooltip">Vlastní blog si po pøihlá¹ení
            mù¾ete zalo¾it v nastavení svého profilu</span></a>
        <a href="/blog">Blogy na abclinuxu.cz</a>,
        <a href="/blog/souhrn">struènìj¹í souhrn</a>,
        <a href="/blog/vyber">výbìr</a>
      </div>
      <table class="siroka">
        <tr>
          <td>
            <ul>
              <#list STORIES[0..half-1] as relation>
                <li><@printStory relation /></li>
              </#list>
            </ul>
          </td>
          <td>
            <ul>
              <#list STORIES[half..] as relation>
                <li><@printStory relation /></li>
              </#list>
            </ul>
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
    <span title="Poèet&nbsp;komentáøù<#if diz.responseCount gt 0>, poslední&nbsp;${DATE.show(diz.updated, "CZ_SHORT")}</#if>">
        (${diz.responseCount}<@lib.markNewComments diz/>)
    </span>
</#macro>

<#assign FEEDS = VARS.getFeeds(USER?if_exists,true)>
<#if (FEEDS.size() > 0)>
  <h2>Rozcestník</h2>
  <div class="rozc">
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
  </div>
</#if>

<#--
<#assign BAZAAR = VARS.getFreshBazaarAds(USER?if_exists)>
<#if (BAZAAR?size>0) >
    <div class="s_nadpis">
        <a class="info" href="#">?<span class="tooltip">Inzeráty z AbcBazaru.</span></a>
        <a href="/bazar">Bazar</a>
    </div>
    <div class="s_sekce">
        <ul>
        <#list BAZAAR as rel>
             <li><a href="/bazar/show/${rel.id}">${TOOL.xpath(rel.child,"data/title")}</a></li>
        </#list>
        </ul>
    </div>
</#if>
-->

<#include "../footer.ftl">
