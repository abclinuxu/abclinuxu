<#include "../header.ftl">

<h1 align="center">Fórum  ${TOOL.xpath(ITEM,"/data/name")}</h1>

<@lib.showParents PARENTS />

<p>Toto diskusní fórum obsahuje celkem ${DIZS.total} diskusí.</p>

<#if TOOL.xpath(ITEM,"data/note")?exists>
 ${TOOL.render(TOOL.element(ITEM.data,"data/note"),USER?if_exists)}
</#if>

<@lib.showMessages/>

<#if (DIZS.total > 0) >
 <table width="99%" cellspacing="0" cellpadding="0" border="0" class="hpforum" align="center">
  <tr>
   <td><b>Dotaz</b></td>
   <td align="center" width="70px"><b>Odpovìdí</b></td>
   <td align="right"><b>Poslední</b></td>
  </tr>
  <#list TOOL.analyzeDiscussions(DIZS.data) as diz>
   <tr bgcolor="#FFFFFF" onmouseover="javascript:style.backgroundColor='#EFEFEF'" onmouseout="javascript:style.backgroundColor='#FFFFFF'">
    <td>
     <a href="/forum/show/${diz.relationId}">${TOOL.limit(diz.title,60," ..")}</a>
    </td>
    <td align="center" width="70px">${diz.responseCount}</td>
    <td align="right" nowrap="nowrap">${DATE.show(diz.updated,"CZ_FULL")}</td>
   </tr>
   <tr><td colspan="3"><@lib.separator double=!diz_has_next /></td></tr>
  </#list>
 </table>
</#if>

 <ul>
  <li>
   <form action="/Search" method="POST">
    <input type="text" name="query" size="30" tabindex="1" class="pole">
    <input type="submit" value="Prohledej toto fórum" tabindex="2" class="buton">
    <input type="hidden" name="parent" value="${RELATION.id}">
    <input type="hidden" name="type" value="diskuse">
   </form>
  <li><a href="${URL.make("/forum/EditDiscussion?action=addQuez&rid="+RELATION.id)}">Polo¾it nový dotaz</a>
  <#if (DIZS.currentPage.row > 0) >
   <#assign start=DIZS.currentPage.row-DIZS.pageSize><#if (start<0)><#assign start=0></#if>
   <li><a href="/forum/dir/${RELATION.id}?from=${start}&count=${DIZS.pageSize}">Novìj¹í dotazy</a>
  </#if>
  <#assign start=DIZS.currentPage.row + DIZS.pageSize>
  <#if (start < DIZS.total) >
   <li><a href="/forum/dir/${RELATION.id}?from=${start}&count=${DIZS.pageSize}">Star¹í dotazy</a>
  </#if>
 </ul>

<#include "../footer.ftl">
