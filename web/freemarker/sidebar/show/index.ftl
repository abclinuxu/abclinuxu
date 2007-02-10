<#include "../header.ftl">

<#macro showArticle(relation)>
 <#local clanek=relation.child>
 <div class="clanek">
  ${DATE.show(clanek.created, "CZ_DM")}&nbsp;
  <a href="/clanky/show/${relation.id}" target="_content">
   ${TOOL.xpath(clanek,"data/name")}</a>
 </div>
</#macro>

<#macro showNews(relation)>
 <#local ITEM=TOOL.sync(relation.child), diz=TOOL.findComments(ITEM)>
 <div class="zpravicka">
  ${DATE.show(ITEM.created,"CZ_SHORT")} | ${NEWS_CATEGORIES[ITEM.subType].name}<br />
  <div>${TOOL.xpath(ITEM,"data/content")}</div>
  <a href="${relation.url?default("/zpravicky/show/"+relation.id)}" target="_content"
   >Komentářů: ${diz.responseCount}</a>
 </div>
<hr />
</#macro>


<div>
<a href="/" title="AbcLinuxu.cz" target="_content">
<img src="/images/site2/abc-logo.gif" width="148" height="46" alt="AbcLinuxu.cz" border="0"></a>
</div>

<br>

<div>
<form action="/Search" method="POST">
<input type="text" name="query" size="12">&nbsp;<input type="submit" value="Hledej">
</form>
</div>

<h3><a href="/clanky" target="_content">Články</a></h3>

<#assign ARTICLES=VARS.getFreshArticles("NONE")>
<#list ARTICLES as rel>
 <@showArticle rel />
</#list>

<h3><a href="/zpravicky">Zprávičky</a></h3>

<#assign NEWS=VARS.getFreshNews("NONE")>
<#list NEWS as rel>
 <@showNews rel />
</#list>

<#include "../footer.ftl">
