<#include "../header.ftl">

<@lib.showMessages/>

<h1>Pøístupová práva k relaci</h1>

<p>Zde mù¾ete sledovat stav pøístupových práv k zvolené relaci
a zároveò je rùznì modifikovat.</p>

<p>
<a href="${URL.make("/EditRelation?action=addACL&rid="+CURRENT.id)}">Vytvoø nové ACL</a>
</p>

<#if ACL?exists>
 <form action="${URL.make("/EditRelation")}">

 <#list ACL as acl>
    <input type="checkbox" name="id" value="${acl.id}">
    <#if acl.user?exists><a href="/Profile/${acl.user.id}">${acl.user.name}</a></#if>
    <#if acl.group?exists><a href="/Group?action=members&gid=${acl.group.id}">${TOOL.xpath(acl.group,"/data/name")}</a></#if>
    ${acl.right}: ${acl.value?string}<br>
 </#list>

 <br>
 <input type="submit" value="Odstraò zvolené ACL">
 <input type="hidden" name="rid" value="${CURRENT.id}">
 <input type="hidden" name="action" value="removeACL">
 </form>

</#if>

<#include "../footer.ftl">
