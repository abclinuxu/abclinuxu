<#include "../header.ftl">

<@lib.showMessages/>

<p>Tento formulář slouží pro nahrání či odstranění vašeho avataru.
Pro vaši ochranu nejdříve zadejte současné heslo.
Pak vyberte soubor s vaším avatarem. Soubor musí být
typu PNG, GIF nebo JPEG. Velikost obrázku je omezena
na 50&times;50 pixelů. Maximální velikost souboru je
50&nbsp;kB.
</p>

<form action="${URL.noPrefix("/EditUser")}" method="POST" enctype="multipart/form-data">
    <table class="siroka" border="0" cellpadding="5">
        <tr>
            <td class="required" width="120">Heslo</td>
            <td>
                <input type="password" name="PASSWORD" size="20" tabindex="1">
                <div class="error">${ERRORS.PASSWORD!}</div>
            </td>
        </tr>
        <#assign avatar = TOOL.getUserAvatar(MANAGED!, USER!)?default("UNDEFINED")>
        <#if avatar != "UNDEFINED">
            <tr>
                <td>Současný avatar</td>
                <td>
                    <img src="${avatar}" alt="avatar">
                    <input type="submit" name="remove_avatar" value="Odstraň současný avatar" tabindex="2">
                </td>
            </tr>
        </#if>
        <tr>
            <td width="120">Avatar</td>
            <td>
                <input type="file" name="avatar" size="20" tabindex="2">
                <div class="error">${ERRORS.avatar!}</div>
            </td>
        </tr>
        <tr>
            <td width="120">&nbsp;</td>
            <td>
                <input type="submit" value="Nastav avatar" tabindex="3">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="uploadAvatar2">
    <input type="hidden" name="uid" value="${MANAGED.id}">
</form>


<#include "../footer.ftl">
