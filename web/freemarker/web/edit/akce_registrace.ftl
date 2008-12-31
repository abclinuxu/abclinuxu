<#include "../header.ftl">

<@lib.showMessages/>

<h1>Registrace na akci</h1>

<p>
Registrací dáváte najevo svou účast na této akci.
Pokud jste přihlášen(a), formulář byl vyplněn údaji z vašeho profilu.
Na zadanou e-mailovou adresu vám v předstihu přijde upozornění, že se
blíží datum konání akce.
</p>

<form action="/akce/edit" method="post">

    <table class="siroka" border="0" cellpadding="5">
    <tr>
        <td>Akce</td>
        <td><a href="${RELATION.url?default("/akce/"+RELATION.id)}">${TOOL.childName(RELATION)}</a></td>
    </tr>
    <tr>
        <td>Vaše jméno</td>
        <td>
            <input type="text" name="name" value="${PARAMS.name!}">
            <div class="error">${ERRORS.name!}</div>
        </td>
    </tr>
    <tr>
        <td>E-mail</td>
        <td>
            <input type="text" name="email" value="${PARAMS.email!}">
            <div class="error">${ERRORS.email!}</div>
        </td>
    </tr>
    <tr>
        <td>&nbsp;</td>
        <td>
            <input type="submit" value="Dokonči">
        </td>
    </tr>
    </table>

<input type="hidden" name="action" value="register2">
<input type="hidden" name="rid" value="${RELATION.id}">

</form>

<#include "../footer.ftl">
