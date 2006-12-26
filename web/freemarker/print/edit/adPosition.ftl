<#include "../header.ftl">

<@lib.showMessages/>

<h1>Úprava reklamní pozice</h1>

<p>
    Tato stránka slou¾í pro úpravu existující reklamní pozice. Ka¾dá pozice
    musí mít své jméno a unikátní identifikátor. Dále je mo¾né zadat popis
    vysvìtlující úèel a umístìní této pozice a hlavní reklamní kód.
    Ten bude zobrazen v¾dy, nebude-li adresa aktuální stránky obslou¾ena jiným
    reklamním kódem. Reklamní kód je obvykle HMTL kód reklamní agentury, ale
    mù¾e to být libovolný HTML kód vèetnì odkazù a obrázkù.
</p>


<form action="${URL.noPrefix("/EditAdvertisement")}" method="POST" name="form">
    <table width=100 border=0 cellpadding=5>
        <tr>
            <td width="90" class="required">Jméno</td>
            <td>
                <input type="text" name="name" value="${PARAMS.name?if_exists?html}" size=60 tabindex=1>
                <div class="error">${ERRORS.name?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="90" class="required">Identifikátor</td>
            <td>
                <input type="text" name="newIdentifier" value="${PARAMS.newIdentifier?if_exists?html}" size=60 tabindex=2>
                <div class="error">${ERRORS.newIdentifier?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="90">Popis</td>
            <td>
                <textarea name="desc" rows="3" class="siroka" tabindex=3>${PARAMS.desc?if_exists?html}</textarea>
                <div class="error">${ERRORS.desc?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="90">Reklamní kód</td>
            <td>
                <textarea name="main_code" rows="5" class="siroka" tabindex=4>${PARAMS.main_code?if_exists?html}</textarea>
                <div class="error">${ERRORS.main_code?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="90">&nbsp;</td>
            <td>
                <input tabindex="5" type="submit" name="finish" value="Dokonèi">
            </td>
        </tr>
    </table>
    <input type="hidden" name="identifier" value="${PARAMS.identifier}">
    <input type="hidden" name="action" value="editPosition2">
</form>

<#include "../footer.ftl">
