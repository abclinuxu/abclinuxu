<#include "../header.ftl">

<@lib.showMessages/>

<h1>Pøiøazení èlánku k seriálu</h1>

<p>
    V tomto formuláøi mù¾ete pøiøadit více èlánkù najednou k vybranému seriálu.
    Staèí vlo¾it na samostatné øádky jednotlivé URL adresy èlánkù. URL musí být
    buï absolutní vèetnì jména serveru nebo relativní zaèínající lomítkem.
</p>

<form action="${URL.noPrefix("/serialy/edit/"+RELATION.id)}" method="POST">
    <table width="100%" border=0 cellpadding=5>
        <tr>
            <td class="required" width="60">Jméno</td>
            <td>
                <textarea name="url" class="siroka" rows="20" tabindex="1">${PARAMS.url?if_exists?html}</textarea>
                <div class="error">${ERRORS.url?if_exists}<div>
            </td>
        </tr>

        <tr>
            <td width="60">&nbsp;</td>
            <td><input type="submit" value="Dokonèi" tabindex="2"></td>
        </tr>
    </table>
    <input type="hidden" name="action" value="addArticlesUrls2">
</form>

<#include "../footer.ftl">
