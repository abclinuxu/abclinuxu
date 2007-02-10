<#include "../header.ftl">

<@lib.showMessages/>

<form action="${URL.make("/EditRelated/"+RELATION.id)}" method="POST" name="form">
    <p>
        Zde můžete upravit související dokument. Povinným políčkem je adresa dokumentu.
        Políčko jméno musíte vyplnit pouze tehdy, vkládáte-li dokument, který se nenachází
        na tomto portále, nebo když chcete změnit jeho jméno. Popis nemusíte vůbec zadávat,
        používejte jej jen tehdy, urychlí-li to navigaci uživatele.
    </p>
    <table cellpadding="5" style="margin-top:1em">
        <tr>
            <td class="required">Adresa</td>
            <td>
                <input tabindex="1" type="text" name="url" size="60" value="${PARAMS.url?if_exists?html}">
                <div class="error">${ERRORS.url?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td>Jméno</td>
            <td>
                <input tabindex="2" type="text" name="title" size="40" value="${PARAMS.title?if_exists?html}">
                <div class="error">${ERRORS.title?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td>Popis</td>
            <td>
                <textarea tabindex="3" name="description" cols="40" rows="4">${PARAMS.description?if_exists?html}</textarea><br>
                <div class="error">${ERRORS.description?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <input tabindex="4" type="submit" name="submit" value="Dokonči">
            </td>
        </tr>
    </table>
    <input type="hidden" name="document" value="${PARAMS.document}">
    <input type="hidden" name="action" value="edit2">
</form>

<#include "../footer.ftl">
