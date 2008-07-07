<#include "../header.ftl">

<@lib.showMessages/>

<h1>Zapomenuté heslo</h1>

<p>
Zadejte uživatelské jméno, které jste na portálu používal.
Pokud existuje a má přiřazenou e-mailovou adresu, bude vám
zaslán odkaz, přes který si můžete nastavit heslo nové.
</p>

<form action="${URL.noPrefix("/EditUser")}" method="POST">

<table border="0" cellpadding="5">
    <tr>
        <td>Login:</td>
        <td>
            <input type="text" name="login" size="8" value="${PARAMS.login?if_exists}">
            <div class="error">${ERRORS.login?if_exists}</div>
        </td>
    </tr>
    <tr>
        <td>&nbsp;</td>
        <td><input type="submit" value="Odeslat"></td>
    </tr>
</table>

<input type="hidden" name="action" value="forgottenPassword2">
</form>

<#include "../footer.ftl">

