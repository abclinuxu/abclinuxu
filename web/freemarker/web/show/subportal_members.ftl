<#import "../macros.ftl" as lib>

<#include "../header.ftl">

<#assign cat=RELATION.child>
<h1>Členové skupiny „${cat.title}“</h1>

<#assign members = cat.getProperty("member")>
<form action="${"/skupiny/edit/"+RELATION.id}" method="post" style="float: right">
    <#if USER?? && members.contains(""+USER.id)>
     <input type="submit" value="Odebrat se">
    <#else>
     <input type="submit" value="Přidat se">
    </#if>
    <input type="hidden" name="action" value="toggleMember">
    <input type="hidden" name="ticket" value="${TOOL.ticketValue(USER!)}">
</form>

<ul>
    <#list members as mem>
        <#assign who = TOOL.createUser(mem)>
        <li>
            <@lib.showUser who/>
        </li>
    </#list>
    <#if members?size == 0>
    Žádní členové!
    </#if>
</ul>

<#include "../footer.ftl">
