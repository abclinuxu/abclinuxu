<#include "../header.ftl">

<@lib.showMessages/>

<h2>Údaje o autorovi</h2>

<form action="${URL.noPrefix("/autori/edit")}" method="POST">
    <table width="100%" border=0 cellpadding=5>
        <tr>
            <td class="required" width="60">Jméno</td>
            <td>
                <input type="text" name="name" value="${PARAMS.name?if_exists?html}" size="24" tabindex="1">
                <div class="error">${ERRORS.name?if_exists}<div>
            </td>
        </tr>
        <tr>
            <td class="required" width="60">Příjmení</td>
            <td>
            <input type="text" name="surname" value="${PARAMS.surname?if_exists?html}" size="24" tabindex="2">
            <div class="error">${ERRORS.surname?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="60">Přezdívka</td>
            <td>
            <input type="text" name="nickname" value="${PARAMS.nickname?if_exists?html}" size="24" tabindex="3">
            <div class="error">${ERRORS.nickname?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td width="60">Rodné číslo</td>
            <td>
            <input type="text" name="birthNumber" value="${PARAMS.birthNumber?if_exists}" size="24" tabindex="4">
            <div class="error">${ERRORS.birthNumber?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td width="60">Číslo účtu</td>
            <td>
            <input type="text" name="accountNumber" value="${PARAMS.accountNumber?if_exists}" size="24" tabindex="5">
            <div class="error">${ERRORS.accountNumber?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td width="60">Email</td>
            <td>
            <input type="text" name="email" value="${PARAMS.email?if_exists?html}" size="24" tabindex="6">
            <div class="error">${ERRORS.email?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td width="60">Telefon</td>
            <td>
            <input type="text" name="phone" value="${PARAMS.phone?if_exists?html}" size="24" tabindex="7">
            <div class="error">${ERRORS.phone?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td width="60">Adresa</td>
            <td>
            <input type="text" name="address" value="${PARAMS.address?if_exists?html}" size="24" tabindex="8">
            <div class="error">${ERRORS.address?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td width="60">Login uživatele</td>
            <td>
            <input type="text" name="login" value="${PARAMS.login?if_exists}" size="24" tabindex="9">
            <div class="error">${ERRORS.login?if_exists}</div>
            </td>
        </tr>

        <tr>
            <td width="60">&nbsp;</td>
            <td><input type="submit" value="Dokonči" tabindex="10"></td>
        </tr>
    </table>
    <#if EDIT_MODE?if_exists>
        <input type="hidden" name="action" value="edit2">
        <input type="hidden" name="rid" value="${RELATION.id}">
    <#else>
        <input type="hidden" name="action" value="add2">
    </#if>
</form>

<#include "../footer.ftl">
