<#include "../header.ftl">

<@lib.showMessages/>

<h1>�prava reklamn�ho k�du</h1>

<p>
    Regul�rn� v�raz slou�� pro ur�en�, zda aktu�ln� URL adresa m�
    b�t obslou�ena t�mto k�dem �i nikoliv. Obvykle posta�� napsat
    za��tek URL adresy (/clanky), pro slo�it�j�� konstrukce kontaktujte
    program�tory. Pokud se ��dn� k�d nebude hodit k aktu�ln� URL adrese,
    pou�ije se hlavn� reklamn� k�d z pozice.
</p>

<form action="${URL.noPrefix("/EditAdvertisement")}" method="POST" name="form">
    <table width=100 border=0 cellpadding=5>
        <tr>
            <td width="90" class="required">Regul�rn� v�raz</td>
            <td>
                <input type="text" name="regexp" value="${PARAMS.regexp?if_exists?html}" size=60 tabindex=1>
                <div class="error">${ERRORS.regexp?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="90">Popis</td>
            <td>
                <textarea name="desc" rows="3" class="siroka" tabindex=2>${PARAMS.desc?if_exists?html}</textarea>
                <div class="error">${ERRORS.desc?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="90">Reklamn� k�d</td>
            <td>
                <textarea name="code" rows="5" class="siroka" tabindex=3>${PARAMS.code?if_exists?html}</textarea>
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
    <input type="hidden" name="editCodeTwo${PARAMS.index}" value="nothing">
    <input type="hidden" name="identifier" value="${PARAMS.identifier}">
</form>

<#include "../footer.ftl">
