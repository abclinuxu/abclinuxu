<#include "../header.ftl">

<@lib.showMessages/>

<p>
    Na této stránce si můžete změnit své základní údaje.
    Pro vaši ochranu nejdříve zadejte současné heslo.
</p>

<p>
    Vaše jméno musí být nejméně pět znaků dlouhé. Email musí být platný.
    Nebojte se, budeme jej chránit před spammery a my vám budeme
    zasílat jen ty informace, které si sami objednáte.
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
            <td class="required" width="60">Jméno</td>
            <td>
                <input type="text" name="name" value="${PARAMS.name?if_exists}" size="24" tabindex="2">
                <div class="error">${ERRORS.name?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td class="required" width="60">Přezdívka</td>
            <td>
                <input type="text" name="nick" id="nick" value="${PARAMS.nick?if_exists}" size="24" tabindex="3"
                onChange="new Ajax.Updater('nickError', '/ajax/checkNick', {parameters: { value : $F('nick')}})">
                <div class="error" id="nickError">${ERRORS.nick?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="60">Email</td>
            <td>
                <input type="text" name="email" value="${PARAMS.email?if_exists}" size="24" tabindex="4">
                <div class="error">${ERRORS.email?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="60">&nbsp;</td>
            <td><input type="submit" value="Dokonči" tabindex="5"></td>
        </tr>
    </table>
    <input type="hidden" name="action" value="editBasic2">
    <input type="hidden" name="uid" value="${MANAGED.id}">
</form>


<#include "../footer.ftl">
