<#include "../header.ftl">

<@lib.showMessages/>

<h2>Přidání skupiny</h2>

<@lib.addForm URL.noPrefix("/Group")>
    <@lib.addInput true, "name", "Jméno skupiny", 20 />
    <@lib.addInput true, "desc", "Popis skupiny", 7, "cols='60'" />
    <@lib.addSubmit "Pokračuj" />

    <#if PARAMS.action=="add">
        <@lib.addHidden "action", "add2" />
    <#else>
        <@lib.addHidden "action", "edit2" />
    </#if>
    <@lib.addHidden "gid", PARAMS.gid />
</@lib.addForm>

<#include "../footer.ftl">
