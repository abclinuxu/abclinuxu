<#include "../header.ftl">

<h1 align="center">${TOOL.xpath(ITEM,"/data/name")}</h1>

<#assign RECORDS = CHILDREN.record>
<#list RECORDS as REL_RECORD>
 <#assign RECORD = REL_RECORD.child, who=TOOL.createUser(RECORD.owner)>

 <div class="slovnik">
 ${TOOL.render(TOOL.element(RECORD.data,"/data/description"),USER?if_exists)}
 </div>

 <p align="right">
  <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>, ${DATE.show(RECORD.updated,"CZ_FULL")}<br>
  <a href="${URL.make("/edit?action=edit&rid="+RELATION.id+"&recordId="+RECORD.id)}">Upravit</a>
  <#if USER?exists && USER.hasRole("remove relation")>
   <#if RECORDS?size gt 1><#assign cislo=REL_RECORD.id><#else><#assign cislo=RELATION.id></#if>
   <a href="${URL.noPrefix("/EditRelation?action=remove&prefix=/slovnik&rid="+cislo)}" title="Sma�">Smazat</a>
  </#if>
 </p>
</#list>

<p>
 Pokud chcete doplnit �i zp�esnit popis tohoto pojmu, m��ete
 <a href="${URL.make("/edit?action=addRecord&rid="+RELATION.id)}">p�idat</a>
 dal�� z�znam. Jste-li autorem popisu, pou�ijte akci Upravit.
</p>

<#list PREV?reverse as dict><a href="${dict.subType}">${TOOL.xpath(dict,"/data/name")}</a> - </#list>
${TOOL.xpath(ITEM,"/data/name")} <#if (NEXT?size>0)>-</#if>
<#list NEXT?if_exists as dict>
<a href="${dict.subType}">${TOOL.xpath(dict,"/data/name")}</a> <#if dict_has_next> - </#if>
</#list>

<p class="monitor"><b>AbcMonitor</b> v�m emailem za�le upozorn�n� p�i zm�n�.
 <#if USER?exists && TOOL.xpath(ITEM,"//monitor/id[text()='"+USER.id+"'")?exists>
  <#assign monitorState="Vypni">
 <#else>
  <#assign monitorState="Zapni">
 </#if>
 <a href="${URL.make("/edit?action=monitor&rid="+RELATION.id)}">${monitorState}</a>
 (${TOOL.getMonitorCount(ITEM.data)})
</p>

<p><b>N�stroje</b>: <a href="/forum/show/${RELATION.id}?varianta=print">Tisk</a></p>

<#include "../footer.ftl">
