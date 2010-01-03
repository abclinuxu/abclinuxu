<#include "../header.ftl">

<h1>Přidání záložky</h1>

<#assign directory=PARAMS.path?default("/")>
<#if directory==""><#assign directory="/"></#if>
<p>
    Záložka bude přidána do adresáře <b>${directory}</b>.
</p>

<@lib.addForm URL.noPrefix("/EditBookmarks/"+MANAGED.id)>
    <@lib.addInput false, "title", "Název">
        Zadejte, pokud vkládáte externí odkaz
    </@lib.addInput>

    <@lib.addInput true, "url", "URL", 40 />
    <@lib.addSubmit "Přidat" />

    <@lib.addHidden "ticket", USER.getSingleProperty('ticket') />
    <@lib.addHidden "path", PARAMS.path! />
    <@lib.addHidden "action", "addLink2" />
</@lib.addForm>

<#include "../footer.ftl">
