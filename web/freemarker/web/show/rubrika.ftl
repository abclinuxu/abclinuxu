<#include "../header.ftl">

<@lib.showParents PARENTS />

<@lib.showMessages/>

<#if USER?exists && USER.hasRole("article admin")>
 <a href="/SelectUser?sAction=form&amp;url=/clanky/edit/${RELATION.id}&amp;action=add">Pøidej èlánek</a>
</#if>
<#if USER?exists && USER.hasRole("category admin")>
 <a href="${URL.make("/EditCategory?action=edit&rid="+RELATION.id+"&categoryId="+CATEGORY.id)}">Uprav sekci</a>
 <a href="${URL.noPrefix("/EditRelation?action=remove&rid="+RELATION.id+"&prefix="+URL.prefix)}">Sma¾ sekci</a>
</#if>

<#if TOOL.xpath(CATEGORY,"data/note")?exists>
 <p class="note">${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER?if_exists)}</p>
</#if>

<#assign map=TOOL.groupByType(CHILDREN)>

<#if map.article?exists>
 <#assign clanky=SORT.byDate(map.article, "DESCENDING"), from=TOOL.parseInt(PARAMS.from?default("0")), count=15, until=from+count>
 <#if (until>=clanky?size)><#assign until=clanky?size></#if>

 <#list clanky[from..(until-1)] as clanek>
  <@lib.showArticle clanek, "CZ_FULL" />
  <@lib.separator double=!clanek_has_next />
 </#list>

 <p>
 <#if (from>0)>
  <#assign from2=from-count><#if (from2<0)><#assign from2=0></#if>
  <a href="${URL.make("/dir/"+RELATION.id+"?from="+from2)}">Pøedchozí stránka</a>
 </#if>
 <#if (until < clanky?size)>
  <a href="${URL.make("/dir/"+RELATION.id+"?from="+until)}">Dal¹í stránka</a>
 </#if>
 </p>
</#if>

<#include "../footer.ftl">
