<#include "../header.ftl">

<#macro showArticle(relation)>
 <#local clanek=relation.child>
 <p>
  <a href="/clanky/ViewRelation?rid=${relation.id}" target="_content">
   ${TOOL.xpath(clanek,"data/name")}
  </a><br>
  ${DATE.show(clanek.created, "CZ_SHORT")}
 </p>
</#macro>

<#macro showNews(relation)>
 <#local
   ITEM=TOOL.sync(relation.child),
   diz=TOOL.findComments(ITEM)
 >
 <p>${DATE.show(ITEM.created,"CZ_SHORT")}
 ${TOOL.xpath(ITEM,"data/content")}<br>
 <span style="font-size: 7pt">
  <a href="/news/ViewRelation?rid=${relation.id}" target="_content" style="font-size: 7pt">Zobrazit</a>
  <#if diz.responseCount gt 0>
   Komentáøe: ${diz.responseCount}, poslední ${DATE.show(diz.lastUpdate, "CZ_FULL")}
  </#if>
 </span>
 </p>
</#macro>


<div align="center">
<a href="/Index?src=sidebar" title="AbcLinuxu.cz" target="_content">
<img src="/images/site/logo_small.gif" width=77 height=97 border=0 alt="AbcLinuxu.cz"></a>
</div>

<br>

<div align="center">
<form action="/Search" method="POST">
<input type="text" name="query" size="12"><br>
<input type="submit" value="Hledej">
</form>
</div>

<a href="/clanky/ViewRelation?rid=5&src=sidebar" class="nadpis">Èlánky</a>

<#list ARTICLES as rel>
 <#call showArticle(rel)>
</#list>

<h3 class="nadpis">Zprávièky</h3>

<#global NEWS=VARS.getFreshNews(user?if_exists)>
<#list NEWS as rel>
 <#call showNews(rel)>
</#list>

<#include "../footer.ftl">
