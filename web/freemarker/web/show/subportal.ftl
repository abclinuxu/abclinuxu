<#import "../macros.ftl" as lib>

<#assign desc=TOOL.xpath(ITEM,"/data/description")?default(TOOL.xpath(ITEM,"/data/descriptionShort")),
        articles=TOOL.createRelation(TOOL.xpath(ITEM,"/data/articles")),
        events=TOOL.createRelation(TOOL.xpath(ITEM,"/data/events"))>
<#if USER?exists && TOOL.xpath(articles.child,"//monitor/id[text()='"+USER.id+"']")?exists>
    <#assign monitorState="Přestaň sledovat články"><#else><#assign monitorState="Sleduj články">
</#if>

<#assign plovouci_sloupec>
    <@lib.showSubportal RELATION, false />
    <#if USER?exists && (USER.hasRole("root") || TOOL.permissionsFor(USER, RELATION).canModify())>
    <div class="s_nadpis">
        Správa skupiny
    </div>
    <div class="s_sekce">
    <ul>
        <#assign articles_rid=TOOL.xpath(ITEM,"/data/article_pool"),
            counter=VARS.getSubportalCounter(RELATION)>
        <li><a href="/skupiny/edit?action=edit&amp;rid=${RELATION.id}">Nastavení</a></li>
        <li><a href="/Group?action=members&amp;gid=${ITEM.group}">Správa administrátorů</a></li>
        <li><a href="/clanky/edit/${articles_rid}?action=add">Napsat článek</a></li>
        <li><a href="/clanky/dir/${articles_rid}">Čekající články</a> <span>(${counter.WAITING_ARTICLES?default("?")})</span></li>
        <li><a href="${events.url}?mode=unpublished">Čekající akce</a> <span>(${counter.WAITING_EVENTS?default("?")})</span></li>
        <li><a href="${URL.make("/EditServers/"+RELATION.id)}">Servery rozcestníku</a></li>
    </ul>
    </div>
    </#if>

    <div class="s_nadpis">Nástroje</div>
    <div class="s_sekce">
        <ul>
            <li><a href="/akce/edit/${events.id}?action=add">Přidat akci</a></li>
            <li>
                <a href="${URL.make("/EditMonitor/"+articles.id+"?action=toggle&amp;redirect="+RELATION.url+TOOL.ticket(USER?if_exists, false))}">${monitorState}</a>
                <span title="Počet lidí, kteří sledují tuto sekci">(${TOOL.getMonitorCount(articles.child.data)})</span>
                <a class="info" href="#">?<span class="tooltip">Zašle upozornění na váš email při vydání nového článku.</span></a>
            </li>
         </ul>
    </div>
</#assign>


<#include "../header.ftl">

<div>
<h1>${ITEM.title}</h1>
<div>${TOOL.render(desc,USER?if_exists)}</div>
</div>

<hr />

<#assign ARTICLES=VARS.getFreshSubportalArticles(USER?if_exists, articles.id)>
<#global CITACE = TOOL.getRelationCountersValue(ARTICLES,"read")/>
<#if (ARTICLES?size>0) >
    <#list ARTICLES as rel>
        <@lib.showArticle rel, "CZ_DM", "CZ_SHORT"/>
        <hr />
    </#list>

    <div class="st_vpravo">
        <a href="/clanky/dir/${articles.id}?from=${ARTICLES?size}">Starší články</a>
    </div>
</#if>


<#assign wiki_rel=TOOL.createRelation(TOOL.xpath(ITEM,"/data/wiki"))>
<#assign exec=TOOL.xpath(wiki_rel.child,"/data/content/@execute")?default("no"), content=TOOL.xpath(wiki_rel.child,"/data/content")>
<h1 class="st_nadpis"><a href="${wiki_rel.url}">Wiki</a></h1>

<#if exec!="yes">
${content}
<#else>
<@content?interpret />
</#if>

<hr />

<#assign event=VARS.getFreshSubportalEvent(events.id)?default("UNDEF")>
<#if event!="UNDEF">
    <h1 class="st_nadpis">Nadcházející akce</h1>
    <@lib.showEvent event, false, false/>

    <div class="st_vpravo">
        <a href="${events.url}">Přehled akcí</a>
    </div>
    <hr />
</#if>

<#if TOOL.xpath(RELATION.child, "/data/forumHidden")?default("no")!="yes">
    <#assign forum_rid=TOOL.xpath(RELATION.child,"/data/forum")>
    <@lib.showForum forum_rid?eval, VARS.defaultSizes.question, false, true />
</#if>

<#assign FEEDS = VARS.getSubportalFeeds(USER?if_exists,RELATION.id)>
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
