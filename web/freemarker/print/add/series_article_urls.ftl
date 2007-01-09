<#include "../header.ftl">

<@lib.showMessages/>

<h1>P�i�azen� �l�nku k seri�lu</h1>

<p>
    V tomto formul��i m��ete p�i�adit v�ce �l�nk� najednou k vybran�mu seri�lu.
    Sta�� vlo�it na samostatn� ��dky jednotliv� URL adresy �l�nk�. URL mus� b�t
    bu� absolutn� v�etn� jm�na serveru nebo relativn� za��naj�c� lom�tkem.
</p>

<form action="${URL.noPrefix("/serialy/edit/"+RELATION.id)}" method="POST">
    <table width="100%" border=0 cellpadding=5>
        <tr>
            <td class="required" width="60">Jm�no</td>
            <td>
                <textarea name="url" class="siroka" rows="20" tabindex="1">${PARAMS.url?if_exists?html}</textarea>
                <div class="error">${ERRORS.url?if_exists}<div>
            </td>
        </tr>

        <tr>
            <td width="60">&nbsp;</td>
            <td><input type="submit" value="Dokon�i" tabindex="2"></td>
        </tr>
    </table>
    <input type="hidden" name="action" value="addArticlesUrls2">
</form>

<#include "../footer.ftl">
