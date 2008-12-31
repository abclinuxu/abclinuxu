<#include "../header.ftl">

<@lib.showMessages/>

<h1>Vložení reklamní pozice</h1>

<p>
    Tato stránka slouží pro definování nové reklamní pozice. Každá pozice
    musí mít své jméno a unikátní identifikátor. Dále je možné zadat popis
    vysvětlující účel a umístění této pozice a hlavní reklamní kód.
    Ten bude zobrazen vždy, nebude-li adresa aktuální stránky obsloužena jiným
    reklamním kódem. Reklamní kód je obvykle HMTL kód reklamní agentury, ale
    může to být libovolný HTML kód včetně odkazů a obrázků.
</p>
<p>
    Po vytvoření nové pozice je nutné, aby programátor přidal do patřičné
    šablony zobrazení této reklamní pozice. To vyžaduje identifikátor
    této pozice. Identifikátor se musí skládat jen z písmen anglické abecedy,
    číslic, pomlčky či podtržítka. Příznak dynamického kódu nastavte jen tehdy,
    obsahuje-li reklamní kód programovací instrukce jazyku Freemarker a musí se
    nejdříve zpracovat.
</p>


<form action="${URL.noPrefix("/EditAdvertisement")}" method="POST" name="form">
    <table class="siroka" border="0" cellpadding="5">
        <tr>
            <td width="90" class="required">Jméno</td>
            <td>
                <input type="text" name="name" value="${PARAMS.name!?html}" size="60" tabindex="1">
                <div class="error">${ERRORS.name!}</div>
            </td>
        </tr>
        <tr>
            <td width="90" class="required">Identifikátor</td>
            <td>
                <input type="text" name="newIdentifier" value="${PARAMS.newIdentifier!?html}" size="60" tabindex="2">
                <div class="error">${ERRORS.newIdentifier!}</div>
            </td>
        </tr>
        <tr>
            <td width="90">Popis</td>
            <td>
                <textarea name="desc" rows="3" class="siroka" tabindex="3">${PARAMS.desc!?html}</textarea>
                <div class="error">${ERRORS.desc!}</div>
            </td>
        </tr>
        <tr>
            <td width="90">&nbsp;</td>
            <td>
                <input tabindex="6" type="submit" name="finish" value="Dokonči">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="addPosition2">
</form>

<#include "../footer.ftl">
