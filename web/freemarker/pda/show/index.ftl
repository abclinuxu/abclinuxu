<#include "../header.ftl">
<#include "/include/zprava.txt">

<#macro showArticle(relation)>
 <#local clanek=relation.child>
 <p>
  ${DATE.show(clanek.created, "CZ_SHORT")}
  <a href="/clanky/show/${relation.id}">${TOOL.xpath(clanek,"data/name")}</a><br>
  ${TOOL.xpath(clanek,"/data/perex")}
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

<h1>Aktu�ln� �l�nky</h1>
<#list ARTICLES as rel>
 <@showArticle rel />
 <@lib.separator double=!rel_has_next />
</#list>

<#assign NEWS=VARS.getFreshNews(user?if_exists)>
<h1>Zpr�vi�ky</h1>
<#list NEWS as rel>
 <@showNews rel/>
 <@lib.separator double=!rel_has_next />
</#list>

<h1>Aktu�ln� j�dra</h1>
<p>
 <#include "/include/kernel.txt">
</p>

<#include "../footer.ftl">
