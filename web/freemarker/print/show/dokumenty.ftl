<#include "../header.ftl">

<h1 class="st_nadpis">Sekce ${TOOL.xpath(CATEGORY,"/data/name")}</h1>

<@lib.showMessages/>

<p><a href="${URL.noPrefix("/editContent?action=add")}">Vytvoø dokument</a></p>

<#if TOOL.xpath(CATEGORY,"data/note")?exists>
 ${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER?if_exists)}
</#if>

<#assign map=TOOL.groupByType(CHILDREN)>

<div class="hw">
<#if map.documents?exists>
 <ul>
 <#list map.documents as relation>
  <li><a href="${relation.url}">${TOOL.childName(relation)}</a></li>
 </#list>
 </ul>
</#if>
</div>
<#include "../footer.ftl">
