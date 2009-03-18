<#include "../../header.ftl">

<@lib.showMessages/>

<h2>Údaje o autorovi</h2>

<form action="${URL.noPrefix("/sprava/redakce/autori/edit")}" method="POST">
    <table width="100%" border="0" cellpadding="5">
        <tr>
            <td class="required" width="60">Jméno</td>
            <td>
                <input type="text" name="name" value="${PARAMS.name!?html}" size="24" tabindex="1">
                <div class="error">${ERRORS.name!}<div>
            </td>
        </tr>
        <tr>
            <td class="required" width="60">Příjmení</td>
            <td>
            <input type="text" name="surname" value="${PARAMS.surname!?html}" size="24" tabindex="2">
            <div class="error">${ERRORS.surname!}</div>
            </td>
        </tr>
        <tr>
            <td width="60">Přezdívka</td>
            <td>
            <input type="text" name="nickname" value="${PARAMS.nickname!?html}" size="24" tabindex="3">
            <div class="error">${ERRORS.nickname!}</div>
            </td>
        </tr>

        <tr>
            <td width="60">Rodné číslo</td>
            <td>
            <input type="text" name="birthNumber" value="${PARAMS.birthNumber!}" size="24" tabindex="4">
            <div class="error">${ERRORS.birthNumber!}</div>
            </td>
        </tr>

        <tr>
            <td width="60">Číslo účtu</td>
            <td>
            <input type="text" name="accountNumber" value="${PARAMS.accountNumber!}" size="24" tabindex="5">
            <div class="error">${ERRORS.accountNumber!}</div>
            </td>
        </tr>

        <tr>
            <td width="60">Email</td>
            <td>
            <input type="text" name="email" value="${PARAMS.email!?html}" size="24" tabindex="6">
            <div class="error">${ERRORS.email!}</div>
            </td>
        </tr>

        <tr>
            <td width="60">Telefon</td>
            <td>
            <input type="text" name="phone" value="${PARAMS.phone!?html}" size="24" tabindex="7">
            <div class="error">${ERRORS.phone!}</div>
            </td>
        </tr>

        <tr>
            <td width="60">Adresa</td>
            <td>
            <input type="text" name="address" value="${PARAMS.address!?html}" size="24" tabindex="8">
            <div class="error">${ERRORS.address!}</div>
            </td>
        </tr>

        <tr>
            <td width="60">Login uživatele</td>
            <td>
            <input type="text" name="login" value="${PARAMS.login!}" size="24" tabindex="9">
            <div class="error">${ERRORS.login!}</div>
            </td>
        </tr>

        <tr>
            <td width="60">&nbsp;</td>
            <td><input type="submit" value="Dokonči" tabindex="10"></td>
        </tr>
    </table>
    <#if EDIT_MODE!false>
        <input type="hidden" name="action" value="edit2">
        <input type="hidden" name="aId" value="${AUTHOR.id}"/>
    <#else>
        <input type="hidden" name="action" value="add2">
    </#if>
</form>

<#include "../../footer.ftl">
