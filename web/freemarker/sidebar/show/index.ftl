<#include "../header.ftl">

<#macro showArticle(relation)>
 <#local clanek=relation.child>
 <p>
  <a href="/clanky/show/${relation.id}" target="_content">
   ${TOOL.xpath(clanek,"data/name")}
  </a><br>
  ${DATE.show(clanek.created, "CZ_SHORT")}
 </p>
</#macro>

<#macro showNews(relation)>
 <#local ITEM=TOOL.sync(relation.child), diz=TOOL.findComments(ITEM)>
 <p>${DATE.show(ITEM.created,"CZ_SHORT")}
 ${TOOL.xpath(ITEM,"data/content")}<br>
 <span style="font-size: 7pt">
  <a href="/news/show/${relation.id}" target="_content" style="font-size: 7pt">Zobrazit</a>
  <#if diz.responseCount gt 0>
   Koment��e: ${diz.responseCount}, posledn� ${DATE.show(diz.updated, "CZ_FULL")}
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

<a href="/clanky/show/5&src=sidebar" class="nadpis" target="_content">�l�nky</a>

<#list ARTICLES as rel>
 <@showArticle rel />
</#list>

<h3 class="nadpis">Zpr�vi�ky</h3>

<#assign NEWS=VARS.getFreshNews(user?if_exists)>
<#list NEWS as rel>
 <@showNews rel />
</#list>

<#include "../footer.ftl">
