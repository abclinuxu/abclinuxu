<#include "../header.ftl">

<h1 align="center">Fórum ${TOOL.xpath(ITEM,"/data/name")}</h1>

<p>Toto diskusní fórum obsahuje celkem ${DIZS.total} diskusí.</p>

<#if TOOL.xpath(ITEM,"data/note")?exists>
 ${TOOL.render(TOOL.element(ITEM.data,"data/note"),USER?if_exists)}
</#if>

<@lib.showMessages/>

<#if (DIZS.total > 0) >

<div class="ds">
   <table>
     <thead>
           <tr>
                <td class="td01">Dotaz</td>
                <td class="td02">Reakcí</td>
                <td class="td03">Poslední</td>
            </tr>
        </thead>
        <tbody>
	<#list TOOL.analyzeDiscussions(DIZS.data) as diz>
   <tr onmouseover="javascript:style.backgroundColor='#F7F7F7'" onmouseout="javascript:style.backgroundColor='#FFFFFF'">
    <td class="td01">
     <a href="/forum/show/${diz.relationId}">${TOOL.limit(diz.title,60," ..")}</a>
    </td>
    <td class="td02"><span class="pidi">${diz.responseCount}</span></td>
    <td class="td03"><span class="pidi">${DATE.show(diz.updated,"CZ_FULL")}</span></td>
   </tr>
        </#list>
        </tbody>
  </table>
</div>

</#if>

 <ul>
  <li>
   <form action="/Search" method="POST">
    <input type="text" name="query" size="30" tabindex="1">
    <input type="submit" value="Prohledej toto fórum" tabindex="2">
    <input type="hidden" name="parent" value="${RELATION.id}">
    <input type="hidden" name="type" value="diskuse">
   </form>
  <li><a href="${URL.make("/forum/EditDiscussion?action=addQuez&amp;rid="+RELATION.id)}">Polo¾it nový dotaz</a>
  <#if (DIZS.currentPage.row > 0) >
   <#assign start=DIZS.currentPage.row-DIZS.pageSize><#if (start<0)><#assign start=0></#if>
   <li><a href="/forum/dir/${RELATION.id}?from=${start}&amp;count=${DIZS.pageSize}">Novìj¹í dotazy</a>
  </#if>
  <#assign start=DIZS.currentPage.row + DIZS.pageSize>
  <#if (start < DIZS.total) >
   <li><a href="/forum/dir/${RELATION.id}?from=${start}&amp;count=${DIZS.pageSize}">Star¹í dotazy</a>
  </#if>
 </ul>

<#include "../footer.ftl">
