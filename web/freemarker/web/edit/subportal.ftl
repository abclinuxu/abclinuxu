<#include "../header.ftl">

<@lib.showMessages/>

<h2>Úprava skupiny</h2>

<p>
    Skupina má vlastní sekci pro články, vlastní wiki, poradnu a sekci pro akce. Má vlastní administrátorskou skupinu,
    jejíž členové mohou definovat ostatní adminy. Zde je možné upravit několik základních údajů.
</p>

<@lib.addForm URL.make("/skupiny/edit"), "", true>
    <@lib.addInput true, "title", "Název" />
    <@lib.addFormField true, "Stručný popis", "Text, který bude zobrazen na v postraním sloupci na jednotlivých stránkách skupiny.">
        <@lib.addTextAreaBare "descShort", 10>
            <@lib.addTextAreaEditor "descShort" />
        </@lib.addTextAreaBare>
    </@lib.addFormField>

    <@lib.addFormField true, "Popis", "Text, který bude zobrazen na úvodní stránce skupiny a ve výpisu skupin.">
        <@lib.addTextAreaBare "desc", 15>
            <@lib.addTextAreaEditor "desc" />
        </@lib.addTextAreaBare>
    </@lib.addFormField>

    <@lib.addFormField false, "Ikonka", "Ikonka zobrazovaná ve výpisu skupin a v pravém sloupci ve skupině.">
        <#assign icon=TOOL.xpath(RELATION.child,"/data/icon")!"UNDEF">
        <#if icon != "UNDEF">
            <img src="${icon}" alt="logo">
            <@lib.addCheckboxBare "remove_icon", "Pouze odstraň současnou ikonku" />
            <br>
        </#if>
        <@lib.addFileBare "icon">
            Rozměry maximálně 100&times;100.
        </@lib.addFileBare>
    </@lib.addFormField>

    <#if USER.hasRole("root")>
        <@lib.addCheckbox "hideForum", "Skrýt fórum" />
    </#if>

    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "action", "edit2" />
    <@lib.addHidden "rid", RELATION.id />
</@lib.addForm>

<#include "../footer.ftl">
