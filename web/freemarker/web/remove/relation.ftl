<#include "../header.ftl">

<@lib.showMessages/>

<h1>Odstranění objektu</h1>

<p>Chystáte se smazat objekt ${TOOL.childName(CURRENT)}.</p>

<p>Všechny relace pro tento objekt v systému:</p>

<ul>
    <#list PARENTS as relation>
        <li>
            ${TOOL.getType(relation.child)} <a href="${URL.make("/show/"+relation.id,PARAMS.prefix)}">${TOOL.childName(relation)}</a>
        </li>
    </#list>
</ul>

<#if SYSTEM_RELATION>
    <p>
        Tento objekt je systémový, jeho smazání by ohrozilo běh portálu.
    </p>
<#elseif PARENTS?size gt 1>
    <p>
        Jelikož je tento objekt uložen v systému vícekrát, pokud budete pokračovat, zruší se jen tato jeho vazba.
    </p>
<#else>
    <p>
        Pro tento objekt neexistuje žádná další  vazba v systému. Pokud budete pokračovat, objekt a jeho potomci
        budou nenávratně ztraceni!
    </p>
</#if>

<#if ! SYSTEM_RELATION>
    <form action="${URL.noPrefix("/EditRelation")}" method="POST">
        <input type="submit" VALUE="Dokonči" tabindex="1">
        <input type="hidden" name="action" value="remove2">
        <input type="hidden" name="prefix" value="${PARAMS.prefix}">
        <input type="hidden" name="rid" value="${PARAMS.rid}">
    </form>
</#if>
