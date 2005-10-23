<#include "../header.ftl">

<@lib.showMessages/>

<#if USER?exists && USER.hasRole("article admin")>
 <a href="/SelectUser?sAction=form&amp;url=/clanky/edit/${RELATION.id}&amp;action=add">Pøidej èlánek</a>
</#if>
<#if USER?exists && USER.hasRole("category admin")>
 <a href="${URL.make("/EditCategory?action=edit&rid="+RELATION.id+"&categoryId="+CATEGORY.id)}">Uprav sekci</a>
 <a href="${URL.noPrefix("/EditRelation?action=remove&rid="+RELATION.id+"&prefix="+URL.prefix)}">Sma¾ sekci</a>
</#if>

<#if TOOL.xpath(CATEGORY,"data/note")?exists>
 <p>${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER?if_exists)}</p>
</#if>

<#global CITACE = TOOL.getRelationCountersValue(ARTICLES.data)/>
 <#list ARTICLES.data as relation>
  <@lib.showArticle relation, "CZ_FULL" />
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
