<#include "../header.ftl">

<h1 align="center">Fórum ${TOOL.xpath(CATEGORY,"/data/name")}</h1>

<p>Toto diskusní fórum obsahuje celkem ${DIZS.total} diskusí.</p>

<#if TOOL.xpath(CATEGORY,"data/note")?exists>
 ${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER?if_exists)}
</#if>

<@lib.showMessages/>

<#if (DIZS.total > 0) >

<table class="ds">
     <thead>
           <tr>
                <td class="td-dotaz">Dotaz</td>
                <td class="td-stav">Stav</td>
                <td class="td-reakci">Reakcí</td>
                <td class="td-posl">Poslední</td>
            </tr>
        </thead>
        <tbody>
	<#list TOOL.analyzeDiscussions(DIZS.data) as diz>
   <tr>
    <td class="td-dotaz">
     <a href="/forum/show/${diz.relationId}">${TOOL.limit(diz.title,60," ..")}</a>
    </td>
    <td class="td-stav">
        <@lib.showDiscussionState diz />
    </td>
    <td class="td-reakci">${diz.responseCount}</td>
    <td class="td-posl">${DATE.show(diz.updated,"SMART")}</td>
   </tr>
        </#list>
        </tbody>
</table>

</#if>

 <ul>
  <li>
   <form action="/Search" method="POST">
    <input type="text" name="query" size="30" tabindex="1">
    <input type="submit" value="Prohledej toto fórum" tabindex="2">
    <input type="hidden" name="parent" value="${RELATION.id}">
    <input type="hidden" name="type" value="diskuse">
   </form>
  <li><a href="${URL.make("/forum/EditDiscussion?action=addQuez&amp;rid="+RELATION.id)}">Položit nový dotaz</a>
  <#if (DIZS.currentPage.row > 0) >
   <#assign start=DIZS.currentPage.row-DIZS.pageSize><#if (start<0)><#assign start=0></#if>
   <li><a href="/forum/dir/${RELATION.id}?from=${start}&amp;count=${DIZS.pageSize}">Novější dotazy</a>
  </#if>
  <#assign start=DIZS.currentPage.row + DIZS.pageSize>
  <#if (start < DIZS.total) >
   <li><a href="/forum/dir/${RELATION.id}?from=${start}&amp;count=${DIZS.pageSize}">Starší dotazy</a>
  </#if>
 </ul>

<#include "../footer.ftl">
