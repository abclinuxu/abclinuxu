<#include "../header.ftl">

<@lib.showMessages/>

<h1>Přiřazení článků k seriálu</h1>

<p>
    V tomto formuláři můžete přiřadit více článků najednou k vybranému seriálu.
    Stačí vložit na samostatné řádky jednotlivé URL adresy článků. URL musí být
    buď absolutní (včetně jména serveru), nebo relativní začínající lomítkem.
</p>

<form action="${URL.noPrefix("/serialy/edit/"+RELATION.id)}" method="POST">
    <table width="100%" border=0 cellpadding=5>
        <tr>
            <td class="required" width="60">Jméno</td>
            <td>
                <textarea name="url" class="siroka" rows="20" tabindex="1">${PARAMS.url!?html}</textarea>
                <div class="error">${ERRORS.url!}<div>
            </td>
        </tr>

        <tr>
            <td width="60">&nbsp;</td>
            <td><input type="submit" value="Dokonči" tabindex="2"></td>
        </tr>
    </table>
    <input type="hidden" name="action" value="addArticlesUrls2">
</form>

<#include "../footer.ftl">
