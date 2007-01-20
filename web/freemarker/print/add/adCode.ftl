<#include "../header.ftl">

<@lib.showMessages/>

<h1>Vlo¾ení dal¹ího reklamního kódu</h1>

<p>
    Zde mù¾ete pøidat dal¹í reklamní kód k reklamní pozici.
    Tato funkce je u¾iteèná tehdy, kdy¾ chcete pro urèitou pozici
    zobrazovat rùzné reklamy v závislosti na aktuální URL adrese.
    Napøíklad aby èlánky mìly svùj vlastní reklamní kód.
</p>

<p>
    Regulární výraz slou¾í pro urèení, zda aktuální URL adresa má
    být obslou¾ena tímto kódem èi nikoliv. Obvykle postaèí napsat
    zaèátek URL adresy (/clanky), pro slo¾itìj¹í konstrukce kontaktujte
    programátory. Pokud se ¾ádný kód nebude hodit k aktuální URL adrese,
    pou¾ije se hlavní reklamní kód z pozice.
</p>

<p>
    Pøíznak dynamického kódu nastavte jen tehdy, obsahuje-li reklamní kód
    programovací instrukce jazyku Freemarker a musí se nejdøíve zpracovat.
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
                <textarea name="code" rows="5" class="siroka" tabindex="3">${PARAMS.code?if_exists?html}</textarea>
                <div class="error">${ERRORS.code?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="90">&nbsp;</td>
            <td>
                <input tabindex="4" type="submit" name="finish" value="Dokonèi">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="addCode2">
    <input type="hidden" name="identifier" value="${PARAMS.identifier}">
</form>

<#include "../footer.ftl">
