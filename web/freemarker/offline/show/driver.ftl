<#include "../macros.ftl">
<#include "../header.ftl">

<#assign who=TOOL.createUser(ITEM.owner)>
<p>
 Tuto polo�ku vytvo�il <a href="http://www.abclinuxu.cz/Profile/${who.id}">${who.name}</a>
 dne ${DATE.show(ITEM.created,"CZ_FULL")}.
</p>

<div align="right"><a href="${ONLINE}"><img src="../../images/tl-online.gif" width="59" height="23" border="0" alt="online"></a></div>

<table cellspacing=0 border=1 cellpadding=5 align="center">
  <tr>
    <td>Jm�no</td><td>${TOOL.xpath(ITEM,"data/name")}</td>
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
<#assign note=TOOL.xpath(ITEM,"data/note")>
<#if note?exists>
  <tr>
    <td valign="top">Pozn�mka</td><td>${TOOL.render(note,USER?if_exists)}</td>
  </tr>
</#if>
</table>

<#include "../footer.ftl">
