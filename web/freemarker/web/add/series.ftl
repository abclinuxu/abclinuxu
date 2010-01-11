<#include "../header.ftl">

<@lib.showMessages/>

<h1><#if EDIT_MODE!>Úprava seriálu<#else>Vytvoření seriálu</#if></h1>

<p>
    Seriál má své jméno a adresu. Adresa musí začínat prefixem <tt>/serialy</tt> a být unikátní.
    Dále je možné vložit popis, který se zobrazí jak na stránce seriálu, tak ve výpise
    všech seriálů, a zadat URL obrázku, který se zobrazí na stránce seriálu.
</p>

<@lib.addForm URL.noPrefix("/serialy/edit")>
    <@lib.addInput true, "name", "Jméno", 40 />
    <#if ! EDIT_MODE!false>
        <@lib.addInput true, "url", "URL seriálu", 40, "", "/serialy/" />
    </#if>

    <@lib.addTextArea false, "desc", "Popis" />
    <@lib.addInput  false, "icon", "URL obrázku", 40 />
    <@lib.addSubmit "Dokonči" />

    <#if EDIT_MODE!false>
        <@lib.addHidden "action", "edit2" />
        <@lib.addHidden "rid", RELATION.id />
    <#else>
        <@lib.addHidden "action", "add2" />
    </#if>
</@lib.addForm>

<#include "../footer.ftl">
