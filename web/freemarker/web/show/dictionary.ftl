<#include "../header.ftl">

<h1 class="st_nadpis">${TOOL.xpath(ITEM,"/data/name")}</h1>

<#assign RECORDS = CHILDREN.record>
<#list RECORDS as REL_RECORD>
<div class="dict-item">
 <#assign RECORD = REL_RECORD.child, who=TOOL.createUser(RECORD.owner)>

  ${TOOL.render(TOOL.element(RECORD.data,"/data/description"),USER?if_exists)}

 <p align="right">
  <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>, ${DATE.show(RECORD.updated,"CZ_FULL")}<br>
  <a href="${URL.make("/edit?action=edit&amp;rid="+RELATION.id+"&amp;recordId="+RECORD.id)}">Upravit</a>
  <#if USER?exists && USER.hasRole("remove relation")>
   <#if RECORDS?size gt 1><#assign cislo=REL_RECORD.id><#else><#assign cislo=RELATION.id></#if>
   <a href="${URL.noPrefix("/EditRelation?action=remove&amp;prefix=/slovnik&amp;rid="+cislo)}" title="Sma¾">Smazat</a>
  </#if>
 </p>
</div>
</#list>

<p>
 Pokud chcete doplnit èi zpøesnit popis tohoto pojmu, mù¾ete
 <a href="${URL.make("/edit?action=addRecord&amp;rid="+RELATION.id)}">pøidat</a>
 dal¹í záznam. Jste-li autorem popisu, pou¾ijte akci Upravit.
</p>

<#list PREV?reverse as dict><a href="${dict.subType}">${TOOL.xpath(dict,"/data/name")}</a> - </#list>
${TOOL.xpath(ITEM,"/data/name")} <#if (NEXT?size>0)>-</#if>
<#list NEXT?if_exists as dict>
<a href="${dict.subType}">${TOOL.xpath(dict,"/data/name")}</a> <#if dict_has_next> - </#if>
</#list>

<p class="monitor"><b>AbcMonitor</b> vám emailem za¹le upozornìní pøi zmìnì.
 <#if USER?exists && TOOL.xpath(ITEM,"//monitor/id[text()='"+USER.id+"']")?exists>
  <#assign monitorState="Vypni">
 <#else>
  <#assign monitorState="Zapni">
 </#if>
 <a href="${URL.make("/edit?action=monitor&amp;rid="+RELATION.id)}">${monitorState}</a>
 (${TOOL.getMonitorCount(ITEM.data)})
</p>

<p><b>Nástroje</b>: <a href="/slovnik/${ITEM.subType}?varianta=print">Tisk</a></p>

<#include "../footer.ftl">
