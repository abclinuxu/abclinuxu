<#include "../header.ftl">

<#if PARAMS.action == "add" || PARAMS.action == "add2">
    <h1>Vložení videa</h1>
<#else>
    <h1>Úprava videa</h1>
</#if>

<@lib.showMessages/>

<p>
    Zde máte možnost upravit video. Systém podporuje Stream.cz, Youtube a Google Video.
</p>

<@lib.addForm URL.make("/videa/edit"), "", true>
    <@lib.addInput true, "title", "Titulek" />
    <@lib.addInput true, "url", "Link na video" />
    <@lib.addTextArea true, "description", Popis" />
    <@lib.addSubmit "Dokonči" />

    <#if PARAMS.action == "add" || PARAMS.action == "add2">
        <@lib.addHidden "action", "add2" />
        <@lib.addHidden "redirect", PARAMS.redirect! />
    <#else>
        <@lib.addHidden "action", "edit2" />
        <@lib.addHidden "rid", PARAMS.rid />
    </#if>

</@lib.addForm>

<#include "../footer.ftl">
