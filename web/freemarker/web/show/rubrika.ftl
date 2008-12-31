<#include "../header.ftl">

<@lib.showMessages/>

<#if USER??>
    <#if TOOL.permissionsFor(USER, RELATION).canCreate()>
        <a href="/clanky/edit/${RELATION.id}?action=add">Přidej článek</a>
    </#if>
    <#if TOOL.permissionsFor(USER, RELATION).canModify()>
     <a href="${URL.make("/EditCategory?action=edit&rid="+RELATION.id+"&categoryId="+CATEGORY.id)}">Uprav sekci</a>
    </#if>
    <#if TOOL.permissionsFor(USER, RELATION.upper).canModify()>
     <a href="${URL.noPrefix("/EditRelation?action=remove&rid="+RELATION.id+"&prefix="+URL.prefix)}">Smaž sekci</a>
    </#if>
</#if>

<@lib.advertisement id="gg-sky" />

<#if TOOL.xpath(CATEGORY,"data/note")??>
 <p>${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER!)}</p>
</#if>

<#global CITACE = TOOL.getRelationCountersValue(ARTICLES.data,"read")/>
<#list ARTICLES.data as relation>
    <@lib.showArticle relation, "SMART_DMY" />
    <hr>
</#list>

 <p>
  <#if (ARTICLES.currentPage.row > 0) >
   <#assign start=ARTICLES.currentPage.row-ARTICLES.pageSize><#if (start<0)><#assign start=0></#if>
   <a href="/clanky/dir/${RELATION.id}?from=${start}">Novější články</a> &#8226;
  </#if>
  <#assign start=ARTICLES.currentPage.row + ARTICLES.pageSize>
  <#if (start < ARTICLES.total) >
   <a href="/clanky/dir/${RELATION.id}?from=${start}">Starší články</a>
  </#if>
 </p>

<#include "../footer.ftl">
