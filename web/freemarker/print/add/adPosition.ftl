<#include "../header.ftl">

<@lib.showMessages/>

<h1>Vlo¾ení reklamní pozice</h1>

<p>
    Tato stránka slou¾í pro definování nové reklamní pozice. Ka¾dá pozice
    musí mít své jméno a unikátní identifikátor. Dále je mo¾né zadat popis
    vysvìtlující úèel a umístìní této pozice a hlavní reklamní kód.
    Ten bude zobrazen v¾dy, nebude-li adresa aktuální stránky obslou¾ena jiným
    reklamním kódem. Reklamní kód je obvykle HMTL kód reklamní agentury, ale
    mù¾e to být libovolný HTML kód vèetnì odkazù a obrázkù.
</p>
<p>
    Po vytvoøení nové pozice je nutné, aby programátor pøidal do patøièné
    ¹ablony zobrazení této reklamní pozice. To vy¾aduje identifikátor
    této pozice. Identifikátor se musí skládat jen z písmen anglické abecedy,
    èíslic, pomlèky èi podtr¾ítka. Pøíznak dynamického kódu nastavte jen tehdy,
    obsahuje-li reklamní kód programovací instrukce jazyku Freemarker a musí se
    nejdøíve zpracovat.
</p>


<form action="${URL.noPrefix("/EditAdvertisement")}" method="POST" name="form">
    <table class="siroka" border="0" cellpadding="5">
        <tr>
            <td width="90" class="required">Jméno</td>
            <td>
                <input type="text" name="name" value="${PARAMS.name?if_exists?html}" size="60" tabindex="1">
                <div class="error">${ERRORS.name?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="90" class="required">Identifikátor</td>
            <td>
                <input type="text" name="identifier" value="${PARAMS.identifier?if_exists?html}" size="60" tabindex="2">
                <div class="error">${ERRORS.identifier?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="90">Popis</td>
            <td>
                <textarea name="desc" rows="3" class="siroka" tabindex="3">${PARAMS.desc?if_exists?html}</textarea>
                <div class="error">${ERRORS.desc?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="90">Dynamický kód</td>
            <td>
                <input type="checkbox" name="dynamic"<#if PARAMS.dynamic?if_exists>checked</#if> tabindex="4">
            </td>
        </tr>
        <tr>
            <td width="90">Reklamní kód</td>
            <td>
                <textarea name="main_code" rows="15" class="siroka" tabindex="5">${PARAMS.main_code?if_exists?html}</textarea>
                <div class="error">${ERRORS.main_code?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="90">&nbsp;</td>
            <td>
                <input tabindex="6" type="submit" name="finish" value="Dokonèi">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="addPosition2">
</form>

<#include "../footer.ftl">
