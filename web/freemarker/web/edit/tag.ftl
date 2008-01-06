<#include "../header.ftl">

<@lib.showMessages/>

<h1>Upravit štítek</h1>

<p>
    Každý štítek musí mít unikátní jméno, které smí obsahovat pouze znaky
    české abecedy, číslice, podtržítko, plus a pomlčku. Editací se nezmění
    url štítku.
</p>

<form action="${URL.make("/stitky/edit")}" method="POST" name="form">
    <table cellpadding="5" class="siroka">
        <tr>
            <td class="required">
                Titulek
                <input tabindex="1" type="text" name="title" size="30" value="${PARAMS.title?if_exists?html}">
                <input tabindex="2" type="submit" name="submit" value="Dokonči">
                <div class="error">${ERRORS.title?if_exists}</div>
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="edit2">
    <input type="hidden" name="id" value="${PARAMS.id}">
</form>

<#include "../footer.ftl">
