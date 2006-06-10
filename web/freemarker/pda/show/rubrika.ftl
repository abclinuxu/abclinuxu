<#include "../header.ftl">

<@lib.showMessages/>

<#if TOOL.xpath(CATEGORY,"data/note")?exists>
 <p class="note">${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER?if_exists)}</p>
</#if>

 <#list ARTICLES.data as relation>
  <@lib.showArticle relation />
  <hr>
 </#list>

 <p>
  <#if (ARTICLES.currentPage.row > 0) >
   <#assign start=ARTICLES.currentPage.row-ARTICLES.pageSize><#if (start<0)><#assign start=0></#if>
   <a href="/clanky/dir/${RELATION.id}?from=${start}">Novìj¹í èlánky</a>
  </#if>
  <#assign start=ARTICLES.currentPage.row + ARTICLES.pageSize>
  <#if (start < ARTICLES.total) >
   <a href="/clanky/dir/${RELATION.id}?from=${start}">Star¹í èlánky</a>
  </#if>
 </p>

<#include "../footer.ftl">
