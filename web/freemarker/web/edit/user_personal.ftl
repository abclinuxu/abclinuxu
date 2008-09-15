<#include "../header.ftl">

<@lib.showMessages/>

<p>
    Na této stránce si můžete upravit své osobní údaje. Všechny údaje jsou volitelné a nemusíte je vyplňovat.
    Pro vaši ochranu nejdříve zadejte současné heslo.
</p>

<form action="${URL.noPrefix("/EditUser")}" method="POST">
    <table width="100%" border=0 cellpadding=5>
        <tr>
            <td class="required" width="60">Heslo</td>
            <td>
                <input type="password" name="PASSWORD" size="16" tabindex="1">
                <div class="error">${ERRORS.PASSWORD?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required" width="60">Vaše pohlaví</td>
            <td>
                <select name="sex" tabindex="2">
                    <option value="man" <#if PARAMS.sex?default("UNDEF")=="man">SELECTED</#if>>muž</option>
                    <option value="woman"<#if PARAMS.sex?default("UNDEF")=="woman">SELECTED</#if>>žena</option>
                </select>
                <div class="error">${ERRORS.sex?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="60">Rok narození</td>
            <td>
                <input type="text" name="birth" value="${PARAMS.birth?if_exists}" size="24" tabindex="3">
                <div class="error">${ERRORS.birth?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="60">Bydliště</td>
            <td>
                <input type="text" name="city" value="${PARAMS.city?if_exists}" size="24" tabindex="4">
            </td>
        </tr>
        <tr>
            <td width="60">Kraj</td>
            <td>
                <input type="text" name="area" value="${PARAMS.area?if_exists}" size="24" tabindex="5">
            </td>
        </tr>
        <tr>
            <td width="60">Země</td>
            <td>
                <input type="text" name="country" value="${PARAMS.country?if_exists}" size="24" tabindex="6">
            </td>
        </tr>
        <tr>
            <td width="60">&nbsp;</td>
            <td><input type="submit" value="Dokonči" tabindex="7"></td>
        </tr>
    </table>
    <input type="hidden" name="action" value="editPersonal2">
    <input type="hidden" name="uid" value="${MANAGED.id}">
</form>


<#include "../footer.ftl">
