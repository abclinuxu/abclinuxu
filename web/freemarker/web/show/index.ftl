<#import "../ads-macro.ftl" as lib>

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
            <a class="info" href="#">?<span class="tooltip">Obrovská databáze znalostí o hardwaru, postupy zprovoznění v GNU/Linuxu.</span></a>
            <a href="/hardware">Hardware</a>
        </div>
        <div class="s_sekce">
            <ul>
            <#list HARDWARE as rel>
                 <li><a href="${rel.url?default("/hardware/show/"+rel.id)}">${TOOL.xpath(rel.child,"data/name")}</a></li>
            </#list>
            </ul>
        </div>
    </#if>

    <@lib.advertisement id="arbo-sq" />
    <@lib.advertisement id="oksystem" />
    <@lib.advertisement id="ps-boxik1" />
    <@lib.advertisement id="ps-boxik2" />
    <@lib.advertisement id="ps-boxik3" />
    <@lib.advertisement id="ps-boxik4" />
    <@lib.advertisement id="ps-boxik5" />

    <#--
    <#assign DRIVERS = VARS.getFreshDrivers(USER?if_exists)>
    <#if (DRIVERS?size>0) >
        <div class="s_nadpis">
            <a class="info" href="#">?<span class="tooltip">Databáze ovladačů pro váš hardware.</span></a>
            <a href="/ovladace">Ovladače</a>
        </div>
        <div class="s_sekce">
            <ul>
            <#list DRIVERS as rel>
                <li><a href="${rel.url?default("/ovladace/show/"+rel.id)}">${TOOL.xpath(rel.child,"data/name")}</a></li>
            </#list>
            </ul>
        </div>
    </#if>
    -->

    <#--<center><@lib.advertisement id="arbo-sq" /></center>-->

</#assign>

<#include "../header.ftl">

<@lib.advertisement id="zprava-hp" />

<@lib.showMessages/>

<#assign ARTICLES=VARS.getFreshArticles(USER?if_exists)>
<#global CITACE = TOOL.getRelationCountersValue(ARTICLES,"read")/>
<#if (ARTICLES?size>0) >
    <#list ARTICLES as rel>
        <@lib.showArticle rel, "CZ_DM", "CZ_SHORT"/>
        <hr />
    </#list>

    <div class="st_vpravo">
        <a href="/History?type=articles&amp;from=${ARTICLES?size}&amp;count=10">Starší články</a>
    </div>
</#if>

<#flush>

<#assign FORUM = VARS.getFreshQuestions(USER?if_exists)>
<#if (FORUM?size > 0)>
  <#assign FORUM=TOOL.analyzeDiscussions(FORUM)>
  <h1 class="st_nadpis"><a href="/poradna" title="Celá Poradna, seznam diskuzních fór">Poradna</a></h1>

  <table class="ds">
    <thead>
      <tr>
        <td class="td-nazev">Dotaz</td>
        <td class="td-meta">Stav</td>
        <td class="td-meta">Reakcí</td>
        <td class="td-datum">Poslední</td>
      </tr>
    </thead>
    <tbody>
     <#list FORUM as diz>
      <tr>
        <td><a href="/forum/show/${diz.relationId}">${TOOL.limit(TOOL.xpath(diz.discussion,"data/title"),60,"...")}</a></td>
        <td class="td-meta"><@lib.showDiscussionState diz /></td>
        <td class="td-meta">${diz.responseCount}</td>
        <td class="td-datum">${DATE.show(diz.updated,"CZ_SHORT")}</td>
      </tr>
     </#list>
    </tbody>
  </table>

  <div style="margin:0.5em 0 0 0; float:right">
     <#--<@lib.advertisement id="arbo-full" />-->
     <@lib.advertisement id="gg-hp-blogy" />
  </div>

  <ul>
    <li><a href="/poradna">Položit dotaz</a></li>
    <li><a href="/History?type=discussions&amp;from=${FORUM?size}&amp;count=20">Starší dotazy</a></li>
    <li><a href="/faq">Časté dotazy (FAQ)</a></li>
  </ul>
</#if>

<#assign STORIES=VARS.getFreshStories(USER?if_exists)>
<#assign STORIES=TOOL.filterRelationsOfBlockedUsers(STORIES,USER?if_exists)>
<#if (STORIES?size>0) >
  <#assign half = STORIES?size/2 >
  <#if STORIES?size%2==1><#assign half=half+1></#if>
    <div class="ramec">
      <div class="s_nadpis">
        <a class="info" href="#">?<span class="tooltip">Vlastní blog si po přihlášení
            můžete založit v nastavení svého profilu</span></a>
        <a href="/blog">Blogy na abclinuxu.cz</a>,
        <a href="/blog/souhrn">stručnější souhrn</a>,
        <a href="/blog/vyber">výběr</a>
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
    <span title="Počet&nbsp;komentářů<#if diz.responseCount gt 0>, poslední&nbsp;${DATE.show(diz.updated, "CZ_SHORT")}</#if>">
        (${diz.responseCount}<@lib.markNewComments diz/>)
    </span>
</#macro>


<h2>Služby</h2>

<table class="boxy">
  <tr>
   <td>
    <#assign BAZAAR = VARS.getFreshBazaarAds(USER?if_exists)>
    <#if (BAZAAR?size>0) >
      <div class="s_nadpis">
        <a class="info" href="#">?<span class="tooltip">Inzeráty z AbcBazaru.</span></a>
        <a href="/bazar">Bazar</a>
      </div>
      <div class="s_sekce">
        <ul>
        <#list BAZAAR as rel>
             <li>
                <a href="/bazar/show/${rel.id}">${TOOL.xpath(rel.child,"data/title")}</a>
                <#if rel.child.subType=='sell'>
                    <span class="prodej">(P)</span>
                <#else>
                    <span class="koupe">(K)</span>
                </#if>
             </li>
        </#list>
        </ul>
      </div>
    </#if>
   </td>

   <td>
    <#assign DICTIONARY=VARS.getFreshDictionary(USER?if_exists)>
    <#if (DICTIONARY?size>0) >
      <div class="s_nadpis">
        <a class="info" href="#">?<span class="tooltip">Výkladový slovník linuxových pojmů.</span></a>
        <a href="/slovnik">Slovník</a>
      </div>
      <div class="s_sekce">
        <ul>
          <#list DICTIONARY as rel>
            <li><a href="${rel.url}">${TOOL.xpath(rel.child,"data/name")}</a></li>
          </#list>
        </ul>
      </div>
    </#if>
   </td>

   <td>
    <#assign FAQ = VARS.getFreshFaqs(USER?if_exists)>
    <#if (FAQ?size>0) >
        <div class="s_nadpis">
            <a class="info" href="#">?<span class="tooltip">Odpovědi na často kladené otázky.</span></a>
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
   </td>
  </tr>
</table>


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

<#include "../footer.ftl">
