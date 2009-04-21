<#import "../ads-macro.ftl" as adLib>

 <#assign plovouci_sloupec>

    <@adLib.advertisement id="ps-boxik1" />
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

<#if (ARTICLES?size>0) >
    <#list ARTICLES as rel>
        <#if (rel_index < COMPLETE_ARTICLES)>
            <@lib.showArticle rel, "CZ_DM", "CZ_SHORT", true/>
        <#else>
            <@lib.showArticle rel, "CZ_DM", "CZ_SHORT", false/>
        </#if>
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
    <#if (TOOL.xpath(USER, "/data/profile/forum_mode")!"") == "single">
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

<#if (STORIES?size > 0) >
    <div class="ramec">
        <div class="s_nadpis">
            <@lib.showHelp>Vlastní blog si po přihlášení můžete založit v nastavení svého profilu</@lib.showHelp>
            <a href="/blog">Blogy na abclinuxu.cz</a>,
            <a href="/blog/souhrn">stručnější souhrn</a>,
            <a href="/blog/vyber">výběr</a>
        </div>
        <table width="100%">
            <#if (STORIES?size > 0) >
                <#assign half = STORIES?size/2 >
                <#if STORIES?size%2==1><#assign half=half+1></#if>
                <tr>
                    <td>
                        <ul>
                            <#list STORIES[0..half-1] as story>
                                <li><@lib.showStoryInTable story, false /></li>
                            </#list>
                        </ul>
                    </td>
                    <td>
                        <ul>
                            <#list STORIES[half..] as story>
                                <li><@lib.showStoryInTable story, false /></li>
                            </#list>
                        </ul>
                    </td>
                </tr>
            </#if>
        </table>
    </div>
</#if>

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

<div style="float:right"><@lib.advertisement id="arbo-full" /></div>

<h2 style="clear:right">Služby</h2>

<table class="boxy">

  <#-- Prvni radek boxu -->
  <tr>
   <td>
    <#assign HARDWARE = VARS.getFreshHardware(USER!)>
    <#if (HARDWARE?size>0) >
        <div class="s_nadpis">
            <@lib.showHelp>Obrovská databáze znalostí o hardwaru, postupy zprovoznění v GNU/Linuxu</@lib.showHelp>
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
            <@lib.showHelp>Katalog softwaru pro GNU/Linux</@lib.showHelp>
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
          <@lib.showHelp>Nejčerstvější ovladače</@lib.showHelp>
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
        <@lib.showHelp>Odpovědi na často kladené otázky</@lib.showHelp>
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
          <@lib.showHelp>Výkladový slovník linuxových pojmů</@lib.showHelp>
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
        <@lib.showHelp>Databáze významných osobností z komunity</@lib.showHelp>
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
          <@lib.showHelp>Inzeráty z AbcBazaru</@lib.showHelp>
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
          <@lib.showHelp>Nejčerstvější kvízy</@lib.showHelp>
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

    <td>
        <#assign VIDEOS = VARS.getFreshVideos(USER!)>
        <div class="s_nadpis">
            <@lib.showHelp>Zajímavá linuxová videa</@lib.showHelp>
            <a href="/videa">Videa</a>
        </div>
        <div class="s_sekce">
            <ul>
                <#list VIDEOS as rel>
                    <li><a href="${rel.url}">${rel.child.title}</a></li>
                </#list>
            </ul>
            <span class="s_sekce_dalsi"><a href="/videa">další&nbsp;&raquo;</a></span>
        </div>
    </td>
  </tr>
</table>

<#assign DESKTOPS = VARS.getFreshDesktops(USER!)>
<#if (DESKTOPS?size > 0)>
    <div class="ramec">
      <div class="s_nadpis">
        <@lib.showHelp>Sbírka uživatelských desktopů. Pochlubte se, jak vypadá vaše pracovní prostředí.</@lib.showHelp>
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

<#assign FEEDS = VARS.getFeeds(USER!,true)>
<#if (FEEDS.size() > 0)>
  <h2>Rozcestník</h2>
  <div class="rozc">
    <table>
      <#list FEEDS.keySet() as server>
      <#if server_index % 3 = 0><tr><#assign open=true></#if>
        <td>
            <a class="server" href="${"/presmeruj?class=S&amp;id="+server.id+"&amp;url="+server.url?url}">${server.name}</a>
            <ul>
            <#list FEEDS(server) as link>
                <li>
                    <a href="${"/presmeruj?class=S&amp;id="+server.id+"&amp;url="+link.url?url}">${link.text}</a>
                </li>
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
