<#include "../header.ftl">

<#if PARAMS.TITLE?exists><h1 class="st_nadpis">${PARAMS.TITLE}</h1></#if>

<@lib.showMessages/>

<p>Zadaným podmínkám vyhovuje celkem ${USERS.total} uživatelů.</p>

<form action="${URL.noPrefix("/SelectUser")}" method="POST">
 <ol start="${USERS.currentPage.row+1}">
  <#list USERS.data as user>
   <li>
    <input type="radio" name="uid" value="${user.id}" <#if user_index==0>checked</#if>>
    <a href="/Profile/${user.id}">${user.name}</a>
   </li>
  </#list>
 </ol>
 <input type="submit" value="Pokračuj">
 <#if USERS.prevPage?exists>
  <input type="submit" name="previous" value="Předchozích ${USERS.pageSize} uživatelů">
 </#if>
 <#if USERS.nextPage?exists>
  <input type="submit" name="next" value="Následujících ${USERS.pageSize} uživatelů">
 </#if>
 ${SAVED_PARAMS?if_exists}
 <input type="hidden" NAME="ticket" VALUE="${USER.getSingleProperty('ticket')}">
 <input type="hidden" name="from" value="${USERS.currentPage.row}">
 <input type="hidden" name="sAction" value="redirect">
</form>

<#include "../footer.ftl">
