<#include "../header.ftl">

<@lib.showMessages/>

<#if PARAMS.action=="add" || PARAMS.action="add2" >
<h1>Přidání serveru rozcestníku</h1>
<#else>
<h1>Úprava serveru rozcestníku</h1>
</#if>

<@lib.addForm URL.make("/EditServers")>
    <@lib.addInput true, "name", "Název serveru", 40 />
    <@lib.addInput true, "rssUrl", "URL RSS kanálu", 40 />
    <@lib.addInput true, "url", "URL webu", 40 />
    <@lib.addInput false, "contact", "Kontakt na správce RSS kanálu", 40 />

    <#if PARAMS.action=="add" || PARAMS.action="add2" >
        <@lib.addHidden "action", "add2" />
    <#else>
        <@lib.addHidden "action", "edit2" />
    </#if>

    <@lib.addHidden "rid", RELATION.id />
    <@lib.addSubmit "Dokončit" />
</@lib.addForm>

<#include "../footer.ftl">
