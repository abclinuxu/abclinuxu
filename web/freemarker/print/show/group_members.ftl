<#include "/include/macros.ftl">
<#include "../header.ftl">

<#call showMessages>

<h1> ${TOOL.xpath(GROUP,"data/name")}</h1>

<p>${TOOL.xpath(GROUP.data,"/data/desc")}</p>

<#if (MEMBERS.currentPage.size>0)>

 <h2>Seznam �len� (${MEMBERS.total})</h2>

 <form action="${URL.noPrefix("/Group")}" method="POST">
 <ol start="${MEMBERS.currentPage.row+1}">
  <#list MEMBERS.data as user>
   <li>
    <input type="checkbox" name="uid" value="${user.id}">
    <a href="/Profile?uid=${user.id}">${user.name}</a>
   </li>
  </#list>
 </ol>
 <input type="submit" value="Odstra� zvolen� �leny ze skupiny">
 <input type="hidden" name="gid" value="${GROUP.id}">
 <input type="hidden" name="action" value="removeMembers">
 </form>

 <#if MEMBERS.prevPage?exists>
  <a href="${URL.make("/Group?action=members&gid="+GROUP.id+"&from="+MEMBERS.prevPage.row)}">
  P�edchoz�ch ${MEMBERS.pageSize}</a>.
 </#if>
 <#if MEMBERS.nextPage?exists>
  <a href="${URL.make("/Group?action=members&gid="+GROUP.id+"&from="+MEMBERS.nextPage.row)}">
  N�sleduj�c�ch ${MEMBERS.pageSize}</a>.
 </#if>

</#if>

<p>
<a href="${URL.noPrefix("/SelectUser?sAction=form&url=/EditUser&action=addToGroup&gid="+GROUP.id)}">P�idej
dal��ho �lena</a>
</p>

<#include "../footer.ftl">
