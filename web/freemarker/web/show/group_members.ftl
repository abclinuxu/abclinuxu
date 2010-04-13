<#include "../header.ftl">

<@lib.showMessages/>

<h1>${GROUP.title}</h1>

<p>${TOOL.xpath(GROUP.data,"/data/desc")}</p>

<#if (MEMBERS.currentPage.size>0)>

 <h2>Seznam členů (${MEMBERS.total})</h2>

 <form action="${URL.noPrefix("/Group")}" method="POST">
 <ol start="${MEMBERS.currentPage.row+1}">
  <#list MEMBERS.data as user>
   <li>
    <input type="checkbox" name="uid" value="${user.id}">
    <a href="/Profile/${user.id}">${user.name}</a>
   </li>
  </#list>
 </ol>
 <input type="submit" value="Odstraň zvolené členy ze skupiny">
 <input type="hidden" name="gid" value="${GROUP.id}">
 <input type="hidden" name="action" value="removeMembers">
 </form>

 <#if MEMBERS.prevPage??>
  <a href="${URL.make("/Group?action=members&gid="+GROUP.id+"&from="+MEMBERS.prevPage.row)}">
  Předchozích ${MEMBERS.pageSize}</a>.
 </#if>
 <#if MEMBERS.nextPage??>
  <a href="${URL.make("/Group?action=members&gid="+GROUP.id+"&from="+MEMBERS.nextPage.row)}">
  Následujících ${MEMBERS.pageSize}</a>.
 </#if>

</#if>

<p><a href="${URL.noPrefix("/SelectUser?sAction=form&url=/EditUser&action=addToGroup&gid="+GROUP.id)}">Přidej
dalšího člena</a></p>

<#include "../footer.ftl">
