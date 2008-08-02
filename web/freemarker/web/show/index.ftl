<#import "../ads-macro.ftl" as lib>

 <#assign plovouci_sloupec>

    <@lib.advertisement id="ps-upoutavka" />

    <#assign SOFTWARE = VARS.getFreshSoftware(USER?if_exists)>
    <#if (SOFTWARE?size>0) >
        <div class="s_nadpis">
            <a class="info" href="#">?<span class="tooltip">Katalog softwaru pro GNU/Linux.</span></a>
            <a href="/software">Software</a>
        </div>
        <div class="s_sekce">
            <ul>
            <#list SOFTWARE as rel>
                 <li><a href="${rel.url}">${rel.child.title}</a></li>
            </#list>
            </ul>
            <span class="s_sekce_dalsi"><a href="/History?type=software">další&nbsp;&raquo;</a></span>
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
                 <li><a href="${rel.url?default("/hardware/show/"+rel.id)}">${rel.child.title}</a></li>
            </#list>
            </ul>
            <span class="s_sekce_dalsi"><a href="/History?type=hardware">další&nbsp;&raquo;</a></span>
        </div>
    </#if>

    <@lib.advertisement id="arbo-sq" />
    <@lib.advertisement id="oksystem" />
    <@lib.advertisement id="ps-boxik1" />
    <@lib.advertisement id="ps-boxik2" />
    <@lib.advertisement id="ps-boxik3" />
    <@lib.advertisement id="ps-boxik4" />
    <@lib.advertisement id="ps-boxik5" />
    <@lib.advertisement id="cvonline" />

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
        <#if rel_index==1>
          <@lib.advertisement id="itbiz-box" />
        </#if>
    </#list>

    <div class="st_vpravo">
        <a href="/History?type=articles&amp;from=${ARTICLES?size}&amp;count=10">Starší články</a>
    </div>
</#if>

<#flush>

<#assign forums=TOOL.getUserForums(USER)>
<#list forums.entrySet() as forum>
    <#if forum.value gt 0>
        <div style="clear: right"></div>
        <@lib.showForum forum.key, forum.value, true, (forum_index==0)/>
    </#if>
</#list>

<#assign STORIES=VARS.getFreshStories(USER?if_exists)>
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
    <#assign story=relation.child, blog=relation.parent, title=blog.title?default("UNDEF"),
             url=TOOL.getUrlForBlogStory(relation), CHILDREN=TOOL.groupByType(story.children),
             author=TOOL.createUser(blog.owner)>
    <#if CHILDREN.discussion?exists>
        <#assign diz=TOOL.analyzeDiscussion(CHILDREN.discussion[0])>
    <#else>
        <#assign diz=TOOL.analyzeDiscussion("UNDEF")>
    </#if>
    <a href="${url}" title="${author.nick?default(author.name)?html}<#if title!="UNDEF">, ${title}</#if>">${story.title}</a>
    <span title="Počet&nbsp;komentářů<#if diz.responseCount gt 0>, poslední&nbsp;${DATE.show(diz.updated, "CZ_SHORT")}</#if>">
        (${diz.responseCount}<@lib.markNewComments diz/>)
    </span>
</#macro>


<h2>Služby</h2>

<table class="boxy">
  <tr>
   <td>
    <#assign FAQ = VARS.getFreshFaqs(USER?if_exists)>
    <div class="s_nadpis">
        <a class="info" href="#">?<span class="tooltip">Odpovědi na často kladené otázky.</span></a>
        <a href="/faq">FAQ</a>
    </div>
    <div class="s_sekce">
        <ul>
        <#list FAQ as rel>
             <li>
                <a href="${rel.url}">${rel.child.title}</a>
             </li>
        </#list>
        </ul>
        <span class="s_sekce_dalsi"><a href="/History?type=faq">další&nbsp;&raquo;</a></span>
    </div>
   </td>
   <td>
    <#assign DICTIONARY=VARS.getFreshDictionary(USER?if_exists)>
      <div class="s_nadpis">
        <a class="info" href="#">?<span class="tooltip">Výkladový slovník linuxových pojmů.</span></a>
        <a href="/slovnik">Slovník</a>
      </div>
      <div class="s_sekce">
        <ul>
          <#list DICTIONARY as rel>
            <li>
                <a href="${rel.url}">${rel.child.title}</a>
            </li>
          </#list>
        </ul>
        <span class="s_sekce_dalsi"><a href="/History?type=dictionary">další&nbsp;&raquo;</a></span>
      </div>
   </td>

  <td>
    <#assign PERSONALITY=VARS.getFreshPersonalities(USER?if_exists)>
    <div class="s_nadpis">
        <a class="info" href="#">?<span class="tooltip">Databáze významných osobností z komunity.</span></a>
        <a href="/kdo-je">Kdo je</a>
    </div>
    <div class="s_sekce">
        <ul>
            <#list PERSONALITY as rel>
                <li>
                    <a href="${rel.url}">${rel.child.title}</a>
                </li>
            </#list>
        </ul>
        <span class="s_sekce_dalsi"><a href="/History?type=personalities">další&nbsp;&raquo;</a></span>
    </div>
   </td>
  </tr>
  <tr>
   <td>
    <#assign BAZAAR = VARS.getFreshBazaarAds(USER?if_exists)>
      <div class="s_nadpis">
        <a class="info" href="#">?<span class="tooltip">Inzeráty z AbcBazaru.</span></a>
        <a href="/bazar">Bazar</a>
      </div>
      <div class="s_sekce">
        <ul>
        <#list BAZAAR as rel>
             <li>
                <a href="/bazar/show/${rel.id}">${rel.child.title}</a>
                <#if rel.child.subType=='sell'>
                    <span class="prodej">(P)</span>
                <#else>
                    <span class="koupe">(K)</span>
                </#if>
             </li>
        </#list>
        </ul>
        <span class="s_sekce_dalsi"><a href="/bazar">další&nbsp;&raquo;</a></span>
      </div>
   </td>

   <td>
    <#assign DRIVERS = VARS.getFreshDrivers(USER?if_exists)>
      <div class="s_nadpis">
        <a class="info" href="#">?<span class="tooltip">Nejčerstvější ovladače</span></a>
        <a href="/ovladace">Ovladače</a>
      </div>
      <div class="s_sekce">
        <ul>
        <#list DRIVERS as rel>
             <li>
                <a href="${rel.url}">${rel.child.title}</a>
             </li>
        </#list>
        </ul>
        <span class="s_sekce_dalsi"><a href="/ovladace">další&nbsp;&raquo;</a></span>
      </div>
   </td>

   <td>
    <#assign TRIVIAS = VARS.getFreshTrivia(USER?if_exists)>
      <div class="s_nadpis">
        <a class="info" href="#">?<span class="tooltip">Nejčerstvější kvízy</span></a>
        <a href="/hry">Kvízy</a>
      </div>
      <div class="s_sekce">
        <ul>
        <#list TRIVIAS as rel>
             <li>
                <a href="${rel.url}">${rel.child.title}</a>
             </li>
        </#list>
        </ul>
        <span class="s_sekce_dalsi"><a href="/hry">další&nbsp;&raquo;</a></span>
      </div>
   </td>

  </tr>
</table>

<#assign DESKTOPS = VARS.getFreshScreenshots(USER?if_exists)>
<#if (DESKTOPS?size > 0)>
    <div class="ramec">
      <div class="s_nadpis">
        <a class="info" href="#">?<span class="tooltip">Sbírka uživatelských desktopů. Pochlubte se, jak vypadá vaše pracovní prostředí.</span></a>
        <a href="/desktopy">Vaše desktopy</a>
      </div>
      <div class="s_sekce" style="text-align:center;">
        <#list DESKTOPS as desktop>
          <a href="${desktop.url}" title="${desktop.title}" class="thumb">
            <img width="200" src="${desktop.thumbnailListingUrl}" alt="${desktop.title}" border="0" style="margin: 0.4em 0.3em 0.3em 0.3em">
          </a>
        </#list>
        <br>
        <span class="s_sekce_dalsi"><a href="/desktopy">další&nbsp;&raquo;</a></span>
      </div>
    </div>
</#if>


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
