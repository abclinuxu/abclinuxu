<#include "../header.ftl">

<@lib.showMessages/>

<h2>Vytvoření skupiny</h2>

<p>
    Při vytváření skupiny dojde k vytvoření podportálu, který má vlastní sekci pro články, vlastní wiki, poradnu
    a sekci pro akce. Má vlastní administrátorskou skupinu, jejíž členové mohou definovat ostatní adminy.
</p>

<@lib.addForm URL.make("/skupiny/edit"), "name='portalForm'", true>
    <@lib.addInput true, "title", "Název", 40 />
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
        <@lib.addFileBare "icon">
            Rozměry maximálně 100&times;100.
        </@lib.addFileBare>
    </@lib.addFormField>

    <#if USER.hasRole("root")>
        <@lib.addCheckbox "hideForum", "Skrýt fórum" />
    </#if>

    <@lib.addFormField true, "UID prvního admina", "Počáteční člen administrátorské skupiny.">
        <@lib.addInputBare "admin", 10 />
    </@lib.addFormField>

    <@lib.addFormField true, "URL", "Například /gentoo.">
        <@lib.addInputBare "url", 40 />
    </@lib.addFormField>

    <@lib.addSubmit "Vytvoř" />
    <@lib.addHidden "action", "add2" />
</@lib.addForm>

<#include "../footer.ftl">
