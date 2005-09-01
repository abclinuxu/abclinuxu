<#include "../header.ftl">

<h1 align="center">F�rum ${TOOL.xpath(CATEGORY,"/data/name")}</h1>

<p>Toto diskusn� f�rum obsahuje celkem ${DIZS.total} diskus�.</p>

<#if TOOL.xpath(CATEGORY,"data/note")?exists>
 ${TOOL.render(TOOL.element(CATEGORY.data,"data/note"),USER?if_exists)}
</#if>

<@lib.showMessages/>

<#if (DIZS.total > 0) >

<div class="ds">
   <table>
     <thead>
           <tr>
                <td class="td01">Dotaz</td>
                <td class="td02">Stav</td>
                <td class="td03">Reakc�</td>
                <td class="td04">Posledn�</td>
            </tr>
        </thead>
        <tbody>
	<#list TOOL.analyzeDiscussions(DIZS.data) as diz>
   <tr>
    <td class="td01">
     <a href="/forum/show/${diz.relationId}">${TOOL.limit(diz.title,60," ..")}</a>
    </td>
    <td class="td02">
       <#if TOOL.xpath(diz.discussion,"/data/frozen")?exists>
         <img src="/images/site2/zamceno.gif" alt="Z" title="Diskuse byla administr�tory uzam�ena">
       </#if>
       <#if TOOL.isQuestionSolved(diz.discussion.data)>
         <img src="/images/site2/vyreseno.gif" alt="V" title="Diskuse byla podle �ten��� vy�e�ena">
       </#if>
       <#if USER?exists && TOOL.xpath(diz.discussion,"//monitor/id[text()='"+USER.id+"']")?exists>
         <img src="/images/site2/sledovano.gif" alt="S" title="Tuto diskusi sledujete monitorem">
       </#if>
    </td>
    <td class="td03">${diz.responseCount}</td>
    <td class="td04">${DATE.show(diz.updated,"CZ_FULL")}</td>
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
    <input type="submit" value="Prohledej toto f�rum" tabindex="2">
    <input type="hidden" name="parent" value="${RELATION.id}">
    <input type="hidden" name="type" value="diskuse">
   </form>
  <li><a href="${URL.make("/forum/EditDiscussion?action=addQuez&amp;rid="+RELATION.id)}">Polo�it nov� dotaz</a>
  <#if (DIZS.currentPage.row > 0) >
   <#assign start=DIZS.currentPage.row-DIZS.pageSize><#if (start<0)><#assign start=0></#if>
   <li><a href="/forum/dir/${RELATION.id}?from=${start}&amp;count=${DIZS.pageSize}">Nov�j�� dotazy</a>
  </#if>
  <#assign start=DIZS.currentPage.row + DIZS.pageSize>
  <#if (start < DIZS.total) >
   <li><a href="/forum/dir/${RELATION.id}?from=${start}&amp;count=${DIZS.pageSize}">Star�� dotazy</a>
  </#if>
 </ul>

<#include "../footer.ftl">
