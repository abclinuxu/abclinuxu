<#include "../header.ftl">

<@lib.showMessages/>

<p>
    Zde můžete nastavit svůj GPG podpis, takže lidé mohou
    ověřovat zprávy, které jim zašlete, a mohou vám posílat
    šifrované e-maily.
</p>

<form action="${URL.noPrefix("/EditUser")}" method="POST" enctype="multipart/form-data">
    <table class="siroka" border="0" cellpadding="5">
        <tr>
            <td class="required" width="120">Heslo</td>
            <td>
                <input type="password" name="PASSWORD" size="20" tabindex="1">
                <div class="error">${ERRORS.PASSWORD?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required" width="120">Veřejný klíč</td>
            <td>
                <textarea name="key" cols="50" rows="20"></textarea>
            </td>
        </tr>
        <tr>
            <td width="120">&nbsp;</td>
            <td>
                <input type="submit" value="Nastav GPG klíč" tabindex="3">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="editGPG2">
    <input type="hidden" name="uid" value="${MANAGED.id}">
</form>

<#include "../footer.ftl">
