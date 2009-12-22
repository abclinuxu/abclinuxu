<#include "../../header.ftl">

<@lib.showMessages/>

<h2>Osobní údaje</h2>

<p>Zadejte prosím své osobní kontaktní a bankovní údaje</p>

<form action="${URL.noPrefix("/sprava/redakce/autori/edit")}" method="POST">
    <table class="siroka">
        <tr>
            <td class="required">Číslo účtu:</td>
            <td>
                <input type="text" name="accountNumber" value="${(AUTHOR.accountNumber)!}" size="60" class="siroka"/>
                <div class="error">${ERRORS.accountNumber!}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Adresa:</td>
            <td>
                <textarea name="address" class="siroka" rows="4">${(AUTHOR.address)!}</textarea>
                <div class="error">${ERRORS.address!}</div>
            </td>
        </tr>
        <tr>
            <td class="required">Email:</td>
            <td>
                <input type="text" name="email" value="${(AUTHOR.email)!?html}" size="60" class="siroka"/>
                <div class="error">${ERRORS.email!}</div>
            </td>
        </tr>
        <tr>
            <td>Telefon:</td>
            <td>
                <input type="text" name="phone" value="${(AUTHOR.phone)!?html}" size="60" class="siroka"/>
                <div class="error">${ERRORS.phone!}</div>
            </td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td><input type="submit" value="Dokonči"/></td>
        </tr>
    </table>
    <input type="hidden" name="action" value="editSelf2"/>
    <input type="hidden" name="rid" value="${(RELATION.id)!}"/>
</form>

<#include "../../footer.ftl">