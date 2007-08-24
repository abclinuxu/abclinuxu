<#include "../header.ftl">

<@lib.showMessages/>

<p>
    Tento průvodce slouží k odstranění nebo sloučení uživatelů.
    Pokud chcete uživatele odstranit, zadejte pouze první číslo uživatele.
    Pro slučování uživatelů zadejte i číslo cílového uživatele.
    Odstranění je možné pouze, pokud uživatel ještě nic nepublikoval,
    jinak nejdříve musíte ručně smazat všechen jeho obsah.
</p>

<form action="${URL.make("/EditUser")}" method="POST">
    <table border="0" cellpadding="5">
        <tr>
            <td class="required">Číslo uživatele na smazání</td>
            <td>
                <input type="text" name="uid1" value="${PARAMS.uid1?if_exists}" tabindex="1">
            </td>
            <td class="error">${ERRORS.uid1?if_exists}</td>
        </tr>
        <tr>
            <td>Číslo uživatele pro přesun dat</td>
            <td>
                <input type="text" name="uid2" value="${PARAMS.uid2?if_exists}" tabindex="2">
            </td>
            <td class="error">${ERRORS.uid2?if_exists}</td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td>
                <input type="hidden" name="action" value="removeMerge2">
                <input type="submit" value="Pokračuj" tabindex="3">
            </td>
        </tr>
    </table>
</form>

<#include "../footer.ftl">
