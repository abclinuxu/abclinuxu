<#import "../macros.ftl" as lib>

<#include "../header.ftl">

<h1>Administrátoři skupiny ${TOOL.childName(RELATION)}</h1>

<ul>
    <#list ADMINS as admin>
        <li>
            <@lib.showUser admin/>
        </li>
    </#list>
    <#if ADMINS?size == 0>
    Žádní administrátoři!
    </#if>
</ul>

<#include "../footer.ftl">
