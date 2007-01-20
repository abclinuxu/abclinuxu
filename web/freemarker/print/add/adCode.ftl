<#include "../header.ftl">

<@lib.showMessages/>

<h1>Vlo�en� dal��ho reklamn�ho k�du</h1>

<p>
    Zde m��ete p�idat dal�� reklamn� k�d k reklamn� pozici.
    Tato funkce je u�ite�n� tehdy, kdy� chcete pro ur�itou pozici
    zobrazovat r�zn� reklamy v z�vislosti na aktu�ln� URL adrese.
    Nap��klad aby �l�nky m�ly sv�j vlastn� reklamn� k�d.
</p>

<p>
    Regul�rn� v�raz slou�� pro ur�en�, zda aktu�ln� URL adresa m�
    b�t obslou�ena t�mto k�dem �i nikoliv. Obvykle posta�� napsat
    za��tek URL adresy (/clanky), pro slo�it�j�� konstrukce kontaktujte
    program�tory. Pokud se ��dn� k�d nebude hodit k aktu�ln� URL adrese,
    pou�ije se hlavn� reklamn� k�d z pozice.
</p>

<p>
    P��znak dynamick�ho k�du nastavte jen tehdy, obsahuje-li reklamn� k�d
    programovac� instrukce jazyku Freemarker a mus� se nejd��ve zpracovat.
</p>

<form action="${URL.noPrefix("/EditAdvertisement")}" method="POST" name="form">
    <table class="siroka" border="0" cellpadding="5">
        <tr>
            <td width="90" class="required">Regul�rn� v�raz</td>
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
            <td width="90">Reklamn� k�d</td>
            <td>
                <textarea name="code" rows="5" class="siroka" tabindex="3">${PARAMS.code?if_exists?html}</textarea>
                <div class="error">${ERRORS.code?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="90">&nbsp;</td>
            <td>
                <input tabindex="4" type="submit" name="finish" value="Dokon�i">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="addCode2">
    <input type="hidden" name="identifier" value="${PARAMS.identifier}">
</form>

<#include "../footer.ftl">
