<#include "../header.ftl">

<@lib.showMessages/>

<p>Tento formulář slouží pro nahrání vaší fotografie.
Pro vaši ochranu nejdříve zadejte současné heslo.
Pak vyberte soubor s vaší fotografií. Soubor musí být
typu PNG, GIF nebo JPEG. Pro fotografie je optimální
JPEG, velikost souboru snižte vhodnou volbou komprese.
Doporučené rozměry obrázku jsou 175 pixelů na šířku
a 200 pixelů na výšku. Maximální velikost souboru je
50&nbsp;kB.
</p>

<@lib.addForm URL.noPrefix("/EditUser"), "", true>
    <@lib.addPassword true, "PASSWORD", "Heslo" />

    <#assign photo = TOOL.xpath(MANAGED,"/data/profile/photo")!"UNDEFINED">
    <#if photo != "UNDEFINED">
        <@lib.addFormField false, "Současná fotografie">
            <img src="${photo}" alt="fotka">
            <@lib.addSubmitBare "Odstraň foto", "remove_photo" />
        </@lib.addFormField>
    </#if>

    <@lib.addFile true, "photo", "Fotografie" />
    <@lib.addSubmit "Dokonči" />
    <@lib.addHidden "action", "uploadPhoto2" />
    <@lib.addHidden "uid", MANAGED.id />
</@lib.addForm>


<#include "../footer.ftl">
