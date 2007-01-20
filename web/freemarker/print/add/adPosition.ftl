<#include "../header.ftl">

<@lib.showMessages/>

<h1>Vlo�en� reklamn� pozice</h1>

<p>
    Tato str�nka slou�� pro definov�n� nov� reklamn� pozice. Ka�d� pozice
    mus� m�t sv� jm�no a unik�tn� identifik�tor. D�le je mo�n� zadat popis
    vysv�tluj�c� ��el a um�st�n� t�to pozice a hlavn� reklamn� k�d.
    Ten bude zobrazen v�dy, nebude-li adresa aktu�ln� str�nky obslou�ena jin�m
    reklamn�m k�dem. Reklamn� k�d je obvykle HMTL k�d reklamn� agentury, ale
    m��e to b�t libovoln� HTML k�d v�etn� odkaz� a obr�zk�.
</p>
<p>
    Po vytvo�en� nov� pozice je nutn�, aby program�tor p�idal do pat�i�n�
    �ablony zobrazen� t�to reklamn� pozice. To vy�aduje identifik�tor
    t�to pozice. Identifik�tor se mus� skl�dat jen z p�smen anglick� abecedy,
    ��slic, poml�ky �i podtr��tka. P��znak dynamick�ho k�du nastavte jen tehdy,
    obsahuje-li reklamn� k�d programovac� instrukce jazyku Freemarker a mus� se
    nejd��ve zpracovat.
</p>


<form action="${URL.noPrefix("/EditAdvertisement")}" method="POST" name="form">
    <table class="siroka" border="0" cellpadding="5">
        <tr>
            <td width="90" class="required">Jm�no</td>
            <td>
                <input type="text" name="name" value="${PARAMS.name?if_exists?html}" size="60" tabindex="1">
                <div class="error">${ERRORS.name?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="90" class="required">Identifik�tor</td>
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
            <td width="90">Dynamick� k�d</td>
            <td>
                <input type="checkbox" name="dynamic"<#if PARAMS.dynamic?if_exists>checked</#if> tabindex="4">
            </td>
        </tr>
        <tr>
            <td width="90">Reklamn� k�d</td>
            <td>
                <textarea name="main_code" rows="15" class="siroka" tabindex="5">${PARAMS.main_code?if_exists?html}</textarea>
                <div class="error">${ERRORS.main_code?if_exists}</div>
            </td>
        </tr>
        <tr>
            <td width="90">&nbsp;</td>
            <td>
                <input tabindex="6" type="submit" name="finish" value="Dokon�i">
            </td>
        </tr>
    </table>
    <input type="hidden" name="action" value="addPosition2">
</form>

<#include "../footer.ftl">
