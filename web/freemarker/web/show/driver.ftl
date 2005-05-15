<#include "../header.ftl">

<#assign who=TOOL.createUser(ITEM.owner)>
<p>
 Tuto polo¾ku naposledy upravil <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
 dne ${DATE.show(ITEM.updated,"CZ_FULL")}.
</p>
<p>
 <a href="${URL.make("/edit?action=edit&amp;rid="+RELATION.id)}">Vlo¾ novou verzi</a>
</p>

<p class="monitor"><b>AbcMonitor</b> vám emailem za¹le upozornìní pøi zmìnì.
 <#if USER?exists && TOOL.xpath(ITEM,"//monitor/id[text()='"+USER.id+"']")?exists>
  <#assign monitorState="Vypni">
 <#else>
  <#assign monitorState="Zapni">
 </#if>
 <a href="${URL.make("/edit?action=monitor&amp;rid="+RELATION.id+"&amp;driverId="+ITEM.id)}">${monitorState}</a>
 (${TOOL.getMonitorCount(ITEM.data)})
</p>

<table cellspacing=0 border=1 cellpadding=5 align="center">
  <tr>
    <td>Jméno ovladaèe</td><td>${TOOL.xpath(ITEM,"data/name")}</td>
  </tr>
  <tr>
    <td>Verze ovladaèe</td><td>${TOOL.xpath(ITEM,"data/version")}</td>
  </tr>
  <tr>
    <td>URL ovladaèe</td>
    <td>
      <a href="${TOOL.xpath(ITEM,"data/url")}">${TOOL.limit(TOOL.xpath(ITEM,"data/url"),50," ..")}</a>
    </td>
  </tr>
  <tr>
    <td valign="top">Poznámka</td><td>${TOOL.render(TOOL.element(ITEM.data,"data/note"),USER?if_exists)}</td>
  </tr>
</table>

<p><b>Nástroje</b>: <a href="/forum/show/${RELATION.id}?varianta=print">Tisk</a></p>

<#include "../footer.ftl">
