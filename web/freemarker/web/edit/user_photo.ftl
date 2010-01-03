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

    <#assign photo = TOOL.xpath(MANAGED,"/data/profile/photo")?default("UNDEFINED")>
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

<form action="${URL.noPrefix("/EditUser")}" method="POST" enctype="multipart/form-data">
    <table width="100%" border=0 cellpadding=5>
        <tr>
            <td class="required" width="120">Heslo</td>
            <td>
                <input type="password" name="PASSWORD" size="20" tabindex="1">
                <div class="error">${ERRORS.PASSWORD!}</div>
            </td>
        </tr>
        <#assign photo = TOOL.xpath(MANAGED,"/data/profile/photo")?default("UNDEFINED")>
        <#if photo != "UNDEFINED">
            <tr>
                <td>Současná fotografie</td>
                <td>
                    <img src="${photo}" alt="fotka">
                    <input type="submit" name="remove_photo" value="Odstraň foto" tabindex="2">
                </td>
            </tr>
        </#if>
        <tr>
            <td width="120">Fotografie</td>
            <td>
                <input type="file" name="photo" size="20" tabindex="2">
                <div class="error">${ERRORS.photo!}</div>
            </td>
        </tr>
        <tr>
            <td width="120">&nbsp;</td>
            <td><input type="submit" value="Dokonči" tabindex="3"></td>
        </tr>
    </table>
    <input type="hidden" name="action" value="uploadPhoto2">
    <input type="hidden" name="uid" value="${MANAGED.id}">
</form>


<#include "../footer.ftl">
