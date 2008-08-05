<#include "../header.ftl">

<@lib.showMessages/>

<p>
    Na této stránce si můžete změnit heslo. Pro vaši ochranu zadejte 
    dvakrát nové heslo. Heslo musí mít nejméně čtyři znaky.
</p>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
    <table width="100%" border=0 cellpadding=5>
        <tr>
            <td class="required" width="160">Nové heslo</td>
            <td>
                <input type="password" name="password" size="16" tabindex="2">
                <div class="error">${ERRORS.password?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required" width="160">Zopakujte nové heslo</td>
            <td>
                <input type="password" name="password2" size="16" tabindex="3">
            </td>
        </tr>
        <tr>
            <td width="160">&nbsp;</td>
            <td>
                <input type="submit" value="Dokonči" tabindex="4">
            </td>
        </tr>
    </table>
    <input type="hidden" name="token" value="${PARAMS.token}">
    <input type="hidden" name="action" value="changeForgottenPassword2">
    <input type="hidden" name="uid" value="${MANAGED.id}">
</form>


<#include "../footer.ftl">
