<#include "/offline/macros.ftl">

<#global who=TOOL.createUser(ITEM.owner)>
<p>
 Tuto polo¾ku vytvoøil ${who.name} dne ${DATE.show(ITEM.created,"CZ_FULL")}.
</p>

<table cellspacing=0 border=1 cellpadding=5 align="center">
  <tr>
    <td>Jméno</td><td>${TOOL.xpath(ITEM,"data/name")}</td>
  </tr>
  <tr>
    <td>Verze</td><td>${TOOL.xpath(ITEM,"data/version")}</td>
  </tr>
  <tr>
    <td>URL</td>
    <td>
      <a href="${TOOL.xpath(ITEM,"data/url")}">${TOOL.limit(TOOL.xpath(ITEM,"data/url"),50," ..")}</a>
    </td>
  </tr>
<#global note=TOOL.xpath(ITEM,"data/note")>
<#if note?exists>
  <tr>
    <td valign="top">Poznámka</td><td>${TOOL.render(note)}</td>
  </tr>
</#if>
</table>
