<#import "../ads-macro.ftl" as adLib>

 <#assign plovouci_sloupec>

    <@adLib.advertisement id="ps-upoutavka" />

    <#assign EVENTS=VARS.getFreshEvents(USER!)>
    <div class="s_nadpis">
        <a class="s_nadpis-pravy-odkaz" href="${URL.make("/akce/edit/233274?action=add")}">zadejte &raquo;</a>
        <#if USER?? && TOOL.permissionsFor(USER,TOOL.createRelation(233274)).canModify()>
            <a class="s_nadpis-pravy-odkaz" href="${URL.make("/akce?mode=unpublished")}" title="Počet neschválených akcí">(${VARS.counter.WAITING_EVENTS})&nbsp;</a>
        </#if>
        <a href="/akce">Kalendář akcí</a>
    </div>
    <div class="s_sekce">
        <ul>
        <#list EVENTS as rel>
             <li>
                <#assign date=DATE.show(rel.child.created, "CZ_DM")>
                 <#if rel.child.date1??>
                    <#assign toDate=DATE.show(rel.child.date1, "CZ_DM")>
                    <#if toDate!=date><#assign date=date+"-"+toDate></#if>
                 </#if>
                <a href="${rel.url!("/akce/show/"+rel.id)}">${rel.child.title}</a> <span>(${date})</span>
             </li>
        </#list>
        </ul>
    </div>

    <@adLib.advertisement id="arbo-sq" />
    <@adLib.advertisement id="oksystem" />
    <@adLib.advertisement id="ps-boxik1" />
    <@adLib.advertisement id="ps-boxik2" />
    <@adLib.advertisement id="ps-boxik3" />
    <@adLib.advertisement id="ps-boxik4" />
    <@adLib.advertisement id="ps-boxik5" />
    <@adLib.advertisement id="cvonline" />

    <#--<center><@adLib.advertisement id="arbo-sq" /></center>-->

</#assign>

<#include "../header.ftl">

<@adLib.advertisement id="zprava-hp" />

<@lib.showMessages/>

<#assign ARTICLES=VARS.getFreshArticles(USER!)>
<#global CITACE = TOOL.getRelationCountersValue(ARTICLES,"read")/>
<#if (ARTICLES?size>0) >
    <#list ARTICLES as rel>
        <@lib.showArticle rel, "CZ_DM", "CZ_SHORT"/>
        <hr />
        <#if rel_index==1>
          <@adLib.advertisement id="itbiz-box" />
        </#if>
    </#list>

    <div class="st_vpravo">
        <a href="/History?type=articles&amp;from=${ARTICLES?size}&amp;count=${SYSTEM_CONFIG.getSectionArticleCount()}">Starší články</a>
    </div>
</#if>

<#flush>

<#assign single_mode=false>
<#if USER??>
    <#if TOOL.xpath(USER, "/data/profile/forum_mode")!""=="single">
        <#assign single_mode=true>
    </#if>
    <small>
    <#if !single_mode>
        samostatné poradny
        |
        <a href="${URL.noPrefix("/EditUser/"+USER.id+"?action=changeForumMode&amp;forumMode=single"+TOOL.ticket(USER,false))}">všechny diskuze v jednom výpisu</a>
    <#else>
        <a href="${URL.noPrefix("/EditUser/"+USER.id+"?action=changeForumMode&amp;forumMode=split"+TOOL.ticket(USER,false))}">samostatné poradny</a>
        |
        všechny diskuze v jednom výpisu
    </#if>
    </small>
</#if>

<#if !single_mode>
    <#assign forums=TOOL.getUserForums(USER)>
    <#list forums.entrySet() as forum>
        <#if forum.value gt 0>
            <div style="clear: right"></div>
            <@lib.showForum forum.key, forum.value, true, (forum_index==0), true, single_mode/>
        </#if>
    </#list>
<#else>
    <@lib.showForum 0, 0, true, true, true, single_mode/>
</#if>

<#assign STORIES=VARS.getFreshStories(USER!)>
<#if (STORIES?size>0) >
  <#assign half = STORIES?size/2 >
  <#if STORIES?size%2==1><#assign half=half+1></#if>
    <div class="ramec">
      <div class="s_nadpis">
          <@lib.showHelp>Vlastní blog si po přihlášení můžete založit v nastavení svého profilu</@lib.showHelp>
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
    <!-- UID: ${relation.parent.owner} -->
    <#assign story=relation.child, blog=relation.parent, title=blog.title?default("UNDEF"),
             url=TOOL.getUrlForBlogStory(relation), CHILDREN=TOOL.groupByType(story.children),
             author=TOOL.createUser(blog.owner)>
    <#if CHILDREN.discussion??>
        <#assign diz=TOOL.analyzeDiscussion(CHILDREN.discussion[0])>
    <#else>
        <#assign diz=TOOL.analyzeDiscussion("UNDEF")>
    </#if>
    <#local signs="", tooltip="">
    <#if CHILDREN.poll??><#local signs=signs+", A", tooltip=tooltip+"anketa"></#if>
    <#if TOOL.screenshotsFor(story)?size gt 0><#if tooltip!=""><#local tooltip=tooltip+", "></#if><#local signs=signs+", O", tooltip=tooltip+"obrázek"></#if>
    <#if CHILDREN.video??><#if tooltip!=""><#local tooltip=tooltip+", "></#if><#local signs=signs+", V", tooltip=tooltip+"video"></#if>

    <a href="${url}" title="${author.nick!author.name?html}<#if title!="UNDEF">, ${title}</#if>">${story.title}</a>
    <span title="Počet&nbsp;komentářů<#if diz.responseCount gt 0>, poslední&nbsp;${DATE.show(diz.updated, "CZ_SHORT")}</#if><#if tooltip!=""> [${tooltip}]</#if>">
        (${diz.responseCount}<@lib.markNewComments diz/>${signs})
    </span>
    <#if (story.getProperty("digest")?size > 0)>
        <img src="/images/site2/digest.png" class="blog_digest" alt="Digest blog" title="Kvalitní zápisek vybraný do digestu">
    </#if>
</#macro>

<#assign SUBPORTALS = VARS.getLatestSubportalChanges(USER!)>
<#if (SUBPORTALS?size>0) >
<#assign half = SUBPORTALS?size/2 >
<#if SUBPORTALS?size%2==1><#assign half=half+1></#if>

<div class="ramec">
    <div class="s_nadpis">
        <a class="info" href="#">?<span class="tooltip">Poslední aktivita v podportálech různých zájmových skupin.</span></a>
        <a href="/skupiny">Skupiny</a>
    </div>
    <div class="s_sekce">
        <table class="siroka">
        <tr>
          <td>
            <ul>
              <#list SUBPORTALS[0..half-1] as map>
                <li><@printSubportal map /></li>
              </#list>
            </ul>
          </td>
          <td>
            <ul>
              <#list SUBPORTALS[half..] as map>
                <li><@printSubportal map /></li>
              </#list>
            </ul>
          </td>
        </tr>
      </table>
    </div>
</div>
</#if>

<#macro printSubportal map>
    <a href="${map.subportal.url}">${TOOL.childName(map.subportal)}</a>
    <span>|</span>
    <a href="${map.relation.url}">${map.relation.child.title}</a>
</#macro>

<h2>Služby</h2>

<table class="boxy">

  <#-- Prvni radek boxu -->
  <tr>
   <td>
    <#assign HARDWARE = VARS.getFreshHardware(USER!)>
    <#if (HARDWARE?size>0) >
        <div class="s_nadpis">
            <a class="info" href="#">?<span class="tooltip">Obrovská databáze znalostí o hardwaru, postupy zprovoznění v GNU/Linuxu.</span></a>
            <a href="/hardware">Hardware</a>
        </div>
        <div class="s_sekce">
            <ul>
            <#list HARDWARE as rel>
                 <li><a href="${rel.url!"/hardware/show/"+rel.id}">${rel.child.title}</a></li>
            </#list>
            </ul>
            <span class="s_sekce_dalsi"><a href="/History?type=hardware">další&nbsp;&raquo;</a></span>
        </div>
    </#if>
   </td>
   <td>
    <#assign SOFTWARE = VARS.getFreshSoftware(USER!)>
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
    </td>

   <td>
    <#assign DRIVERS = VARS.getFreshDrivers(USER!)>
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
  </tr>

  <#-- druhy radek boxu -->
  <tr>
   <td>
    <#assign FAQ = VARS.getFreshFaqs(USER!)>
    <div class="s_nadpis">
        <a class="info" href="#">?<span class="tooltip">Odpovědi na často kladené otázky.</span></a>
        <a href="/faq">FAQ</a>
    </div>
    <div class="s_sekce">
        <ul>
        <#list FAQ as rel>
             <li><a href="${rel.url}">${rel.child.title}</a></li>
        </#list>
        </ul>
        <span class="s_sekce_dalsi"><a href="/History?type=faq">další&nbsp;&raquo;</a></span>
    </div>
   </td>
   <td>
    <#assign DICTIONARY=VARS.getFreshDictionary(USER!)>
      <div class="s_nadpis">
        <a class="info" href="#">?<span class="tooltip">Výkladový slovník linuxových pojmů.</span></a>
        <a href="/slovnik">Slovník</a>
      </div>
      <div class="s_sekce">
        <ul>
          <#list DICTIONARY as rel>
            <li><a href="${rel.url}">${rel.child.title}</a></li>
          </#list>
        </ul>
        <span class="s_sekce_dalsi"><a href="/History?type=dictionary">další&nbsp;&raquo;</a></span>
      </div>
   </td>

  <td>
    <#assign PERSONALITY=VARS.getFreshPersonalities(USER!)>
    <div class="s_nadpis">
        <a class="info" href="#">?<span class="tooltip">Databáze významných osobností z komunity.</span></a>
        <a href="/kdo-je">Kdo je</a>
    </div>
    <div class="s_sekce">
        <ul>
            <#list PERSONALITY as rel>
                <li><a href="${rel.url}">${rel.child.title}</a></li>
            </#list>
        </ul>
        <span class="s_sekce_dalsi"><a href="/History?type=personalities">další&nbsp;&raquo;</a></span>
    </div>
   </td>
  </tr>

  <#-- Treti radek boxu -->
  <tr>
    <td>
    <#assign BAZAAR = VARS.getFreshBazaarAds(USER!)>
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
    <#assign TRIVIAS = VARS.getFreshTrivia(USER!)>
      <div class="s_nadpis">
        <a class="info" href="#">?<span class="tooltip">Nejčerstvější kvízy</span></a>
        <a href="/hry">Kvízy</a>
      </div>
      <div class="s_sekce">
        <ul>
        <#list TRIVIAS as rel>
             <li><a href="${rel.url}">${rel.child.title}</a></li>
        </#list>
        </ul>
        <span class="s_sekce_dalsi"><a href="/hry">další&nbsp;&raquo;</a></span>
      </div>
   </td>
  </tr>
</table>

<#assign DESKTOPS = VARS.getFreshDesktops(USER!)>
<#if (DESKTOPS?size > 0)>
    <div class="ramec">
      <div class="s_nadpis">
        <a class="info" href="#">?<span class="tooltip">Sbírka uživatelských desktopů. Pochlubte se, jak vypadá vaše pracovní prostředí.</span></a>
        <a href="/desktopy">Vaše desktopy</a>
      </div>
      <div class="s_sekce" style="text-align:center;">
        <table align="center">
          <tr>
            <#list DESKTOPS as desktop>
              <td>
                <@lib.showDesktop desktop />
              </td>
            </#list>
          </tr>
        </table>
        <span class="s_sekce_dalsi"><a href="/desktopy">další&nbsp;&raquo;</a></span>
      </div>
    </div>
</#if>

<#assign VIDEOS = VARS.getFreshVideos(USER!)>
<#if (VIDEOS?size > 0)>
    <div class="ramec">
      <div class="s_nadpis">
        <a class="info" href="#">?<span class="tooltip">Zajímavá linuxová videa.</span></a>
        <a href="/videa">Videa</a>
      </div>
      <div class="s_sekce" style="text-align:center;">
        <table align="center">
          <tr>
            <#list VIDEOS as video>
              <td>
                <@lib.showVideo video />
              </td>
            </#list>
          </tr>
        </table>
        <span class="s_sekce_dalsi"><a href="/videa">další&nbsp;&raquo;</a></span>
      </div>
    </div>
</#if>

<#assign FEEDS = VARS.getFeeds(USER!,true)>
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
