<#include "../header.ftl">

<@lib.showMessages/>

<h1>Vložení dalšího reklamního kódu</h1>

<p>
    Zde můžete přidat další reklamní kód k reklamní pozici.
    Tato funkce je užitečná tehdy, když chcete pro určitou pozici
    zobrazovat různé reklamy v závislosti na aktuální URL adrese.
    Například aby články měly svůj vlastní reklamní kód.
</p>

<p>
    Regulární výraz slouží pro určení, zda aktuální URL adresa má
    být obsloužena tímto kódem či nikoliv. Obvykle postačí napsat
    začátek URL adresy (/clanky), pro složitější konstrukce kontaktujte
    programátory. Pokud se žádný kód nebude hodit k aktuální URL adrese,
    použije se hlavní reklamní kód z pozice.
</p>

<p>
    Příznak dynamického kódu nastavte jen tehdy, obsahuje-li reklamní kód
    programovací instrukce jazyku Freemarker a musí se nejdříve zpracovat.
</p>

<form action="${URL.noPrefix("/EditAdvertisement")}" method="POST" name="form">
    <table class="siroka" border="0" cellpadding="5">
        <tr>
            <td width="90" class="required">Regulární výraz</td>
            <td>
                <input type="text" name="regexp" value="${PARAMS.regexp?if_exists?html}" size="60" tabindex="1">
                <div class="error">${ERRORS.regexp?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="90">Popis</td>
            <td>
                <textarea name="desc" rows="3" class="siroka" tabindex="2">${PARAMS.desc?if_exists?html}</textarea>
                <div class="error">${ERRORS.desc?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="90">Reklamní kód</td>
            <td>
                <textarea name="code" rows="15" class="siroka" tabindex="3">${PARAMS.code?if_exists?html}</textarea>
                <div class="error">${ERRORS.code?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="90">&nbsp;</td>
            <td>
                <input tabindex="4" type="submit" name="finish" value="Dokonči">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="addCode2">
    <input type="hidden" name="identifier" value="${PARAMS.identifier}">
</form>

<#include "../footer.ftl">
