<#include "../header.ftl">

<#if PARAMS.TITLE?exists><h1>${PARAMS.TITLE}</h1></#if>

<@lib.showMessages/>

<p>Zadan�m podm�nk�m vyhovuje celkem ${USERS.total} u�ivatel�.</p>

<form action="${URL.noPrefix("/SelectUser")}" method="POST">
 <ol start="${USERS.currentPage.row+1}">
  <#list USERS.data as user>
   <li>
    <input type="radio" name="uid" value="${user.id}" <#if user_index==0>checked</#if>>
    <a href="/Profile/${user.id}">${user.name}</a>
   </li>
  </#list>
 </ol>
 <input type="submit" value="Pokra�uj">
 <#if USERS.prevPage?exists>
  <input type="submit" name="previous" value="P�edchoz�ch ${USERS.pageSize} u�ivatel�">
 </#if>
 <#if USERS.nextPage?exists>
  <input type="submit" name="next" value="N�sleduj�c�ch ${USERS.pageSize} u�ivatel�">
 </#if>
 ${SAVED_PARAMS?if_exists}
 <input type="hidden" name="from" value="${USERS.currentPage.row}">
 <input type="hidden" name="sAction" value="redirect">
</form>

<#include "../footer.ftl">
