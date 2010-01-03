<#include "../header.ftl">

<@lib.showMessages/>

<p>Tento formulář slouží pro nahrání či odstranění vašeho avataru.
Pro vaši ochranu nejdříve zadejte současné heslo.
Pak vyberte soubor s vaším avatarem. Soubor musí být
typu PNG, GIF nebo JPEG. Velikost obrázku je omezena
na 50&times;50 pixelů. Maximální velikost souboru je
50&nbsp;kB.
</p>

<@lib.addForm URL.noPrefix("/EditUser"), "", true>
    <@lib.addPassword true, "PASSWORD", "Heslo" />
    <#assign avatar = TOOL.getUserAvatar(MANAGED!, USER!)?default("UNDEFINED")>
    <#if avatar != "UNDEFINED">
        <@lib.addFormField false, "Současný avatar">
            <img src="${avatar}" alt="avatar" />
            <@lib.addSubmitBare "Odstraň současný avatar", "remove_avatar" />
        </@lib.addFormField>
    </#if>

    <@lib.addFile true, "avatar", "Avatar" />
    <@lib.addSubmit "Nastav avatar" />

    <@lib.addHidden "action", "uploadAvatar2" />
    <@lib.addHidden "uid", MANAGED.id />
</@lib.addForm>


<#include "../footer.ftl">
