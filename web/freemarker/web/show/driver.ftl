<#include "../header.ftl">

<#assign who=TOOL.createUser(ITEM.owner)>
<p>
    <#if PARAMS.revize?exists>
        Právì si prohlí¾íte revizi èíslo ${PARAMS.revize}, kterou vytvoøil
        <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
        (${DATE.show(ITEM.updated,"CZ_FULL")}).
        <a href="${RELATION.url?default("/ovladace/show/"+RELATION.id)}">Návrat na aktuální verzi</a>.
    <#else>
        Tuto polo¾ku naposledy upravil <a href="/Profile/${who.id}">${who.nick?default(who.name)}</a>
        (${DATE.show(ITEM.updated,"CZ_FULL")}).
        Pokud chcete doplnit, opravit nebo aktualizovat ovladaè,
        <a href="${URL.make("/edit?action=edit&amp;rid="+RELATION.id)}">vlo¾te novou verzi</a>.
        K dispozici je i <a href="/revize?rid=${RELATION.id}&amp;prefix=/ovladace">archiv zmìn</a>
        tohoto ovladaèe, tak¾e si mù¾ete prohlédnout, jakými zmìnami ovladaè pro¹el postupem èasu.
    </#if>
</p>

<p><b>AbcMonitor</b> vám emailem za¹le upozornìní pøi zmìnì.
 <#if USER?exists && TOOL.xpath(ITEM,"//monitor/id[text()='"+USER.id+"']")?exists>
  <#assign monitorState="Vypni">
 <#else>
  <#assign monitorState="Zapni">
 </#if>
 <a href="${URL.make("/edit?action=monitor&amp;rid="+RELATION.id+"&amp;driverId="+ITEM.id)}">${monitorState}</a>
 (${TOOL.getMonitorCount(ITEM.data)})
</p>

<table cellspacing=0 border=1 cellpadding=5 align="center" style="margin-top: 1em;">
  <tr>
    <td>Jméno</td><td>${TOOL.xpath(ITEM,"data/name")}</td>
  </tr>
  <tr>
    <td>Verze</td><td>${TOOL.xpath(ITEM,"data/version")}</td>
  </tr>
  <tr>
    <td>Adresa</td>
    <td>
      <a href="${TOOL.xpath(ITEM,"data/url")}">${TOOL.limit(TOOL.xpath(ITEM,"data/url"),50," ..")}</a>
    </td>
  </tr>
  <tr>
    <td valign="top">Poznámka</td><td>${TOOL.render(TOOL.element(ITEM.data,"data/note"),USER?if_exists)}</td>
  </tr>
</table>

<p><b>Nástroje</b>: <a href="${RELATION.url?default("/ovladace/show/"+RELATION.id)}?varianta=print<#if PARAMS.revize?exists>&amp;revize=${PARAMS.revize}</#if>">Tisk</a></p>

<#include "../footer.ftl">
