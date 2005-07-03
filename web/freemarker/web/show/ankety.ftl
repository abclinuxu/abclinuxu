<#include "../header.ftl">

<@lib.showMessages/>

<h1 class="st_nadpis">Archiv anket</h1>
<#if POLLS.currentPage.row==0 && USER?exists && USER.hasRole("poll admin")>
 <p>
  <a href="${URL.noPrefix("/EditPoll?action=add&amp;rid=250")}">Vytvoø anketu</a>
 </p>
</#if>

<table>
    <#list POLLS.data as relation>
        <tr>
            <td align="right" width="120px">
                <a href="${relation.url?default("/ankety/show/"+relation.id)}">
                ${DATE.show(relation.child.created, "CZ_DMY")}</a>
            </td>
            <td>${relation.child.text}</td>
        </tr>
    </#list>
</table>

<p>
  <#if (POLLS.currentPage.row > 0) >
   <#assign start=POLLS.currentPage.row-POLLS.pageSize><#if (start<0)><#assign start=0></#if>
   <a href="/ankety?from=${start}&amp;count=${POLLS.pageSize}">Novìj¹í ankety</a>
  </#if>
  <#assign start=POLLS.currentPage.row + POLLS.pageSize>
  <#if (start < POLLS.total) >
   <a href="/ankety?from=${start}&amp;count=${POLLS.pageSize}">Star¹í ankety</a>
  </#if>
</p>

<#include "../footer.ftl">


